# Create Sifter (1.20 Fabric 移植) 代码审计报告

> ## ⚠️ 本报告已过时 · SUPERSEDED
>
> 本报告基于 **2026-08-21** 旧代码基线（分支 `1.20`，HEAD `92d3514`），其后代码已大量重写。
> 经 2026-08-25 复核，**原报告的 4 个"严重"问题（C1 空配料越界、C2 override 解析异常、C3 产物静默销毁、C4 渲染除零）及 H4（`advanced_custom_mesh` 误注册）均已修复**，不再成立：
>
> - **C1** → `SiftingRecipe` 已加 `getItems().length == 0` 空配料保护（当前代码见 `SiftingRecipe.java` 构造函数适量边界）。
> - **C2** → `SifterConfig` 的 `parseInt(parts[1], -1)` 已适配畸形条目，不再抛 `NumberFormatException`。
> - **C3** → `SifterBlockEntity.process()` 已改为事务插槽 + 收集溢出物并落回地面（`tryToInsertOutputItem` 返回未插入部分），产物不再静默丢失。
> - **C4** → 渲染进度已加 `total <= 0` 保护与 clamp，不再产生 Infinity/NaN 矩阵。
> - **H4** → `ModItems.ADVANCED_CUSTOM_MESH` 已改为 `AdvancedCustomMesh::new`。
>
> 本文件仅作历史留存，**请以当前 `src/main` 源码与最新版本（V1.0.1）为准**，勿据此判断当前模组存在崩溃/丢件缺陷。

- **审计日期**: 2026-08-21
- **审计范围**: `src/main/java` 全部源码 + `src/main/resources` 数据/资源（重点是当前工作区未提交的改动：难度系统、Mod Menu 配置界面、 crushed_netherrack 配方链）
- **代码基线**: 分支 `1.20`，HEAD `92d3514` + 工作区未提交修改

---

## 总览

| 等级 | 数量 | 典型问题 |
|------|------|----------|
| 🔴 严重（崩溃/物品损失） | 4 | 空配料数组越界、override 解析未捕获异常、产物静默消失、渲染除零 |
| 🟠 较高（逻辑/架构缺陷） | 7 | 客户端 UI 改服务端配置实际无效、网络同步把难度倍率二次应用、advanced 标志链路断裂 |
| 🟡 中等（健壮性/性能） | 8 | 每帧配方重排、每次 roll 新建 RandomSource、线程安全 |
| 🔵 低（UI/UX/清理） | 9 | 硬编码中文、斑马纹失效、小屏溢出、dev 遗留 log4j 配置 |

---

## 一、严重问题（崩溃 / 物品损失）

### 🔴 C1. 空配料导致数组越界崩溃
**位置**: `SiftingRecipe.java:94, 103, 111, 122`（`getSiftableIngredient` / `getMeshIngredient` / `getMeshItemStack` / `getSiftableItemStack`）

```java
ItemStack itemStack = ingredients.get(i).getItems()[0];  // 无长度检查
```

`Ingredient.getItems()` 对空配料（引用了不存在的 tag、tag 下没有物品、KubeJS 生成空配料等）返回**空数组**，此处直接 `[0]` 越界。

- 构造函数（`SiftingRecipe.java:48`）在配方反序列化时就会调用 `getMeshItemStack()` → **含此类配方的数据包/世界直接加载失败**。
- `getSiftableIngredient()` 在 `matches()`（`SiftingRecipe.java:70`）中被调用，而 `matches` 在服务端 tick 里跑 → 若 `/reload` 后某个 tag 变空，**服务器进入崩溃循环**。

**修复建议**: 使用前检查 `getItems().length == 0`（或 `Ingredient.isEmpty()`），空配料应跳过或使配方无效化并记录日志，而不是抛异常。

---

### 🔴 C2. `getOverride` 对畸形配置条目未捕获解析异常
**位置**: `SifterConfig.java:102`

```java
if (Integer.parseInt(parts[1]) != outputIndex) continue;  // 未防护
```

`parts[2]`、`parts[3]` 都走了安全的 `parseInt(s, fallback)`，唯独 `parts[1]`（输出索引）用裸 `Integer.parseInt`。手工编辑 `config/createsifter-server.toml` 时写入如 `"createsifter:x|abc|50|3"` 这类条目：

- `rollResults()`（服务端 tick）抛 `NumberFormatException` → **服务器崩溃循环**；
- 配置界面 `recipeSummary()`（`SifterConfigScreen.java:92`，渲染线程每帧调用）同样崩溃 → 客户端界面无法打开。

**修复建议**: 改用已有的 `parseInt(parts[1], -1)` 并跳过不匹配项。

---

### 🔴 C3. 输出容器未满时产物被静默销毁（物品丢失）
**位置**: `SifterBlockEntity.java:115-117`（防满检查）与 `203-205`（插入）

```java
// 防满检查：只有当某个槽位 count == slotLimit 时才停机
if (outputInv.getStackInSlot(i).getCount() == outputInv.getSlotLimit(i)) return;
...
// 插入：剩余部分直接丢弃
inv.insert(ItemVariant.of(stack), stack.getCount(), t);
```

两个漏洞点叠加：

1. **检查语义错误**：只要输出区还有"任意一个槽位没精确到达上限"就继续加工，但 roll 出的产物总量可能超过剩余空间 —— `insert` 的返回值（实际插入量）被无视，**多出的部分凭空消失**。
2. **对非 64 上限物品失效**：如果 `getSlotLimit()` 返回固定 64（porting lib `ItemStackHandler` 的常见实现），堆叠上限 16 的产物（如雪球类）永远到不了 64，防满检查**永远不触发**，会持续吞产物。

黄铜筛子 `itemsPerCycle` 默认 8（每周期 roll 8 组产物），丢件风险放大 8 倍。

**修复建议**：改为模拟插入——roll 后先在事务内尝试插入，插入不下的部分 `ItemHelper.dropInWorld` 掉落（Create 系机器的标准做法），或不足时中断本周期；防满检查改用 `getStackInSlot(i).getMaxStackSize()` 或直接依赖事务模拟结果。

---

### 🔴 C4. 渲染矩阵除零 / NaN
**位置**: `SifterRenderer.java:49-53`、`SifterBlockEntity.java:279-285`

```java
float progress = blockEntity.getProcessingRemainingPercent();  // = timer/total，周期结束时为 0
...
.scale((float).9, progress, (float).9)
.translate(new Vec3(-xPos + 0.05, 1.05 / progress, 0.05));   // progress → 0 时为 Infinity
```

- 每个加工周期结束、下个 tick 重置 timer 之间的窗口内 `progress == 0` → `1.05/0 = Infinity` 进入矩阵，Y 轴缩放同时为 0（退化矩阵），轻则方块闪烁/消失，重则驱动层报错。
- 新放置、尚未 tick 的方块 `totalTime == timer == 0` → `0/0 = NaN`，NaN 矩阵会随管线传播。

**修复建议**: `getProcessingRemainingPercent()` 加 `total <= 0` 保护并 clamp 到 `[ε, 1]`；渲染端对 `progress` 做 `Mth.clamp(progress, 0.01f, 1f)`。`BrassSifterRenderer` 若同样写法需一并修。

---

## 二、较高问题（逻辑 / 架构缺陷）

### 🟠 H1. Mod Menu 配置界面在多人游戏下完全无效，但 UI 声称"已保存到服务端"
**位置**: `SifterConfigScreen.java`、`RecipeEditScreen.java`、`ModConfigs.java`

配置界面是纯客户端 Screen，`SifterConfig.DIFFICULTY.set(...)`、`RECIPE_OVERRIDES.set(...)` 只写入**客户端本地**的 `config/createsifter-server.toml`：

- **单人游戏**：客户端与集成服务端共享 JVM 配置对象 → 生效 ✅
- **多人游戏（专用服务器）**：改动永远到不了服务器；服务器重新同步/玩家重登后本地副本还会被覆盖回来 ❌
- `RecipeEditScreen.java:175` 底部固定显示"改动保存到服务端配置 · Saved to server config"——在多人环境下是**误导性文案**；`SifterConfigScreen.java:181` 的"服务端为准 server wins"说明作者有意识，但界面没有据此禁用编辑。

另外两处不一致：
- `SifterConfigScreen.stepChance/stepTime`（154/160 行）显式调用 `ModConfigs.saveServer()`，而 `RecipeEditScreen.adjust/reset`（62/100/117 行）**没有**调用，完全依赖 `ConfigValue.set` 的隐式保存行为（取决于 forgeconfigapiport 版本，可能不落盘）。
- `stepChance` 屏幕端 clamp 下限是 `0.0`，而配置定义下限是 `0.0001`（`SifterConfig.java:42`）——把 0.6 一路减到 0.0 时会尝试 `set(0.0)` 触发校验失败（抛异常或静默忽略，取决于实现），且浮点累减有漂移。

**修复建议**: 
1. 检测 `Minecraft.getInstance().getCurrentServer() != null && !isSingleplayer()`（或用 fabric 的 `ClientPlayNetworking` 判断）时将编辑控件禁用并显示"请在服务器端修改配置"；
2. 统一在所有写路径后调用 `ModConfigs.saveServer()`；
3. stepChance 的 clamp 与配置范围保持一致（≥0.0001），累计时用整数步进（如 5% 一档）避免浮点漂移。

---

### 🟠 H2. 网络序列化把"难度时间倍率"烘焙进 processingTime，客户端二次相乘
**位置**: `SiftingRecipeSerializer.java:35-37, 77`（写）、`96-99`（读）

```java
buffer.writeVarInt(recipe.getProcessingDuration());  // getProcessingDuration() 已经 × 难度倍率！
```

`getProcessingDuration()`（`SiftingRecipe.java:192-197`）每次调用都乘 `Difficulty.effectiveTimeMultiplier()`。序列化时：

1. 服务器发给客户端的 duration = 基础值 × **服务器**倍率，被 `readFromBuffer` 存进 `processingDuration` 字段；
2. 客户端之后每次调用 `getProcessingDuration()` 又乘一次**客户端本地**倍率 → **JEI 显示、客户端 tick 的时长双重相乘**。

当客户端本地 `createsifter-server.toml` 与服务器难度不一致（多人默认就不一致）时，JEI 里的加工时长和方块内进度条都是错的。`writeToJson`（35-37 行）同理：任何把配方序列化回 JSON 的场景（数据包生成、第三方工具）都会把当前倍率固化进文件。

**修复建议**: 序列化时写原始 `recipe.processingDuration` 字段（或提供 `getBaseProcessingDuration()`），倍率只在运行时查询路径上生效。

---

### 🟠 H3. `findAdvanced` 是死代码，advanced 标志链路断裂 → 规则失效 + 潜在死循环
**位置**: `ModRecipeTypes.java:101-115`、`SifterBlockEntity.java:138-153`

- `SifterBlockEntity.tick()`/`process()` 的配方搜索都走 `find(...)`（固定 `advanced=false`），`findAdvanced` **没有任何调用者**。
- 于是"高级网不能跑普通配方"这条规则（`SiftingRecipe.java:67`）只在**缓存命中检查**（`lastRecipe.matches(..., hasAdvancedMesh())`）里生效，搜索路径完全不检查：
  - 数据包用**tag 型网配料**同时匹配普通网与高级网时，`find` 选中的配方在下一 tick 被 `matches(advanced=true)` 否决 → 再次 `find` 又选中 → **timer 每次被重置为时长、永远不递减，筛子死循环空转**（`SifterBlockEntity.java:154`）。
  - 反向规则（普通网跑不了高级配方）目前靠 ingredient 匹配兜底才成立，属于巧合而非设计。

**修复建议**: `find` 增加布尔参数（或在 `matches` 里直接从 `inv.getItem(1)` 判断网类型），删除或使用 `findAdvanced`。

---

### 🟠 H4. `advanced_custom_mesh` 注册成了 `AdvancedBrassMesh`
**位置**: `ModItems.java`（ADVANCED_CUSTOM_MESH 条目）

```java
public static final ItemEntry<AdvancedBrassMesh> ADVANCED_CUSTOM_MESH =
        CreateSifter.REGISTRATE.item("advanced_custom_mesh", AdvancedBrassMesh::new)  // 应为 AdvancedCustomMesh::new
```

`AdvancedCustomMesh` 类存在但从未被实例化；`advanced_custom_mesh` 物品内部的 `mesh` 字段被错误设为 `ADVANCED_BRASS`。当前 `mesh` 字段没有其他读取方，暂无玩法影响，属于埋雷式接线错误。

**修复建议**: 改为 `AdvancedCustomMesh::new`。

---

### 🟠 H5. 配置值被静态/构造期缓存，改动不生效
**位置**: `SifterBlockEntity.java:53, 65`、`BrassSifterBlockEntity.java:20-21`

```java
public static float DEFAULT_MINIMUM_SPEED = SifterConfig.SIFTER_MINIMUM_SPEED.get().floatValue();
protected int itemsProcessedPerCycle = BrassSifterConfig.BRASS_SIFTER_ITEMS_PER_CYCLE.get();
```

- `DEFAULT_MINIMUM_SPEED` 在类加载时定格 → 修改 `minimumSpeed` 配置**必须重启游戏**；
- `hasSpeedRequirement()`（`SiftingRecipe.java:143`）拿配方 `minimumSpeed` 与这个缓存常量比较，进一步固化旧值；
- `itemsProcessedPerCycle` 在方块实体构造时定格，已放置的机器不会跟随配置更新；
- `outputInv` 容量同理（构造时定格）。

**修复建议**: 改为每次 tick 时 `.get()`（nightconfig 读取是轻量 map 查询），或监听配置重载事件刷新。

---

### 🟠 H6. 客户端 timer 用本地难度文件计算 + 占位 100 循环，进度条周期性跳变
**位置**: `SifterBlockEntity.java:139-153`、`ModRecipeTypes.java:102-103`

`find()` 在客户端**恒返回 empty**（`if (world.isClientSide) return Optional.empty();`）。于是客户端 `tick()` 在 timer 归零后走 `timer = 100; totalTime = 100;` 占位分支（141-144 行），等服务器 `sendData` 同步真实值才纠正——每个加工周期结束时客户端进度条都会先跳到 100 再跳回真实时长。同时客户端读的是自己那份 `createsifter-server.toml`，与服务器难度不一致时进度条整体漂移。

**修复建议**: 客户端 timer 归零后直接停住等待同步，不本地重置；难度/倍率类值由服务器在 `write()` 中随 NBT 下发而不是客户端自查。

---

## 三、中等问题（健壮性 / 性能）

### 🟡 M1. `rollResults` 每次调用新建 `RandomSource`
**位置**: `SiftingRecipe.java:204`。每次 roll 都 `RandomSource.create()`（含种子生成开销）。建议复用 `level.random` 或 BE 持有的实例。黄铜筛子每周期 roll 8 次，量大。

### 🟡 M2. 配方匹配路径每次重新解析 Ingredient
**位置**: `SiftingRecipe.java:92-127`。`getSiftableIngredient()`/`getMeshIngredient()` 在每次 `matches()`（即每次 `find()` 扫描的每个配方）中被调用，且 `getItems()` 对 tag 配料每次都重新解析并分配数组。`SifterInventoryHandler.insert`（`SifterBlockEntity.java:312-315`）对**每次漏斗/掉落物插入尝试**都触发 `canProcess` → 全量配方扫描。大型配方包下这是每 tick 的显著开销。建议在构造函数中一次性解析缓存 mesh/非mesh 配料（顺带解决 C1）。

### 🟡 M3. 配置界面渲染循环内反复分配
**位置**: `SifterConfigScreen.java:193`（`render` 每帧调用 `collectRecipes()` = `getAllRecipesFor` + 拷贝 + 排序）、`recipeSummary()`（92 行）每帧对每个可见配方的每个产物做 `getOverride`（O(n) 遍历 + 字符串 split，`SifterConfig.java:97-107`）。建议在 `init`/滚动时缓存结果。

### 🟡 M4. `RecipeEditScreen` 用 `indexOf` 定位行号，重复产物时编辑错行
**位置**: `RecipeEditScreen.java:38`（构造）、`61`（重置全部）。

```java
recipe.getRollableResults().forEach(out -> overrides.add(copyOverride(recipe,
        recipe.getRollableResults().indexOf(out), out)));  // 两个产物内容相同时返回第一个下标
```

同一配方若有两个相同产物（相同物品+概率，JSON 里很常见），`indexOf` 永远返回靠前的下标 → 后面的行编辑/重置写到错误的 override 条目。应改为普通下标 for 循环。

### 🟡 M5. 线程安全：渲染线程改配置，服务端线程同时读
单人环境下 Mod Menu 界面在渲染线程执行 `DIFFICULTY.set` / `RECIPE_OVERRIDES.set`（nightconfig 结构写），而集成服务端线程正在 `getOverride().get()` / `rollResults()` 中遍历同一结构 → 理论上存在 `ConcurrentModificationException`/脏读竞态。建议把配置写入调度到服务端主线程（如 `Minecraft.getInstance().execute(...)` 不够，需要 `server.execute(...)`）。

### 🟡 M6. override 数量上限 512 可超过物品最大堆叠
**位置**: `SifterConfig.java:104,130`（clamp 512）vs `SiftingRecipe.java:225`（`stack.setCount(count)`）。UI 端 clamp 是 64（`RecipeEditScreen.java:114`），但手改配置文件可到 512 → 产生超堆叠上限的 ItemStack，插入时溢出部分又落回 C3 的销毁路径。建议统一 clamp 到 `stack.getMaxStackSize()`。

### 🟡 M7. `isAdvancedMesh` 忽略入参
**位置**: `SiftingRecipe.java:134-136`。方法体读 `this.meshStack` 而非参数 `meshStack`，当前恰好因构造顺序正确，属于易踩坑的坏味道。

### 🟡 M8. 掉落物筛取路径的客户端无反馈
**位置**: `BaseMesh.java:104-110`。"捡地上物品手筛"分支只在服务端写 `Sifting` NBT，客户端标签要等背包同步才出现 → 开始使用的头几 tick 无粒子、无手中物品渲染（`MeshItemRenderer` 依赖该标签）。与"另一只手"分支（客户端也写标签）行为不一致。

---

## 四、UI / UX 问题（界面与呈现）

### 🔵 U1. 配置界面文案硬编码中英混排，完全不走语言文件
`SifterConfigScreen`/`RecipeEditScreen` 全部用 `Component.literal("重置全部 / Reset all")`、`"超高/高/中/低/自定义"`、`describe()` 里的整段中文说明。英文用户会看到中文标签；而项目自带 `ModLang` 工具类和 8 个语言文件却未使用。仓库历史里已有 "Fix: remove Chinese text from English tip label" 这类修复，说明这个问题是被关注过的。**建议**: 所有文案改为 `Component.translatable` + lang key（en_us/zh_cn 各一份）。

### 🔵 U2. 斑马纹条纹逻辑失效
**位置**: `RecipeEditScreen.java:154`、`SifterConfigScreen.java:204`。

```java
if ((y & 1) == 0)  // y = 40 + row*24 恒为偶数 → 每一行都着色，没有交替效果
```

应改为 `(row & 1) == 0`。

### 🔵 U3. `SifterConfigScreen` 固定 9 行不适应小窗口
**位置**: `SifterConfigScreen.java:42`（`VISIBLE_ROWS = 9` 硬编码）+ `109-118`。列表背景高度是 `height - 40` 自适应的，但行数固定：小高度（大 GUI 缩放）下 ✎ 按钮画到列表背景外、与底部 Done 按钮重叠。`RecipeEditScreen` 已按 `(height - top - 30) / ROW_H` 自适应（82 行），两个屏幕行为不一致，应统一。

### 🔵 U4. 重置按钮与滚动条重叠
**位置**: `RecipeEditScreen.java:102`（按钮右缘 `width-12`）vs `172` 行滚动条（`width-14..width-12`），重叠 2px。按钮宽度收窄 4px 即可。

### 🔵 U5. 文本截断按 6px/字符估算，CJK 文本溢出
**位置**: `RecipeEditScreen.java:163`、`SifterConfigScreen.java:208`（`/6` 估宽）。中文字符约 9px 宽，中文产物名/摘要会超出预期裁剪范围。建议用 `this.font.width(...)` 实测后截断。

### 🔵 U6. JEI 速度需求显示原始浮点数
**位置**: `SiftingCategory.java:84`。`recipe.getSpeedRequirement()` 直接拼接 → 显示 "16.0 RPM"。建议 `Math.round` 或 `%d`。

### 🔵 U7. 新物品本地化缺口
`crushed_netherrack`、`basalt_pebble`、`blackstone_pebble` 的 key 只加进了 en_us / zh_cn / zh_tw；`en_ud, fr_fr, ja_jp, ko_kr, ru_ru` 均缺失（回退英文原名）。另外 `en_ud.json` 连既有的 `recipe.sifting.waterlogged` key 也缺。

### 🔵 U8. 配方编辑无"取消/撤销"语义
`RecipeEditScreen` 每次点击立即写配置并落盘，只有逐条/全部"重置"可回滚到配方默认值。误触后无撤销，建议至少在 Done 时批量提交。

### 🔵 U9. `getProcessingRemainingPercent` 命名与语义
**位置**: `SifterBlockEntity.java:279-285`。返回 `timer/total`（从 1 递减到 0），名字叫 "RemainingPercent" 但实现绕了一圈 `1 - (total-timer)/total`，且无 `total==0` 保护（见 C4）。建议重写为 `total <= 0 ? 1 : Mth.clamp(timer/(float)total, 0, 1)` 并改名 `getProgressRemaining`。

---

## 五、清理 / 杂项

1. **`src/main/resources/log4j2.xml` 是 dev 遗留物且会随 jar 发布**（`resources` 根目录的 log4j2.xml 可能被采用为全局日志配置），并把 `io.github.shulej.createsifter` 设为 DEBUG——`getProcessingDuration`/`rollResults` 每次筛物打 2+ 行、每个产物再打 1-2 行（`SiftingRecipe.java:195,202,217,222`），生产环境是日志风暴+性能损耗。**建议移出 resources 或删除**（连同那批 `log.debug`）。
2. `MinecraftMixin.java` 是空壳，却仍占用 mixin 配置条目。
3. `SifterBlock.use` 与 `BrassSifterBlock.use`、两份 `updateEntityAfterFallOn` 大段复制粘贴（`SifterBlock.java:64-124` vs `BrassSifterBlock.java:91-151`），收获/插入逻辑改动需要双改，建议提取到共同基类。
4. `ModBlocks.java` 文件末尾缺换行符；`ModItems.register()`/`ModBlocks.register()` 空方法可删。
5. `BrassSifterBlockEntity.itemsProcessedPerCycle`（20 行）遮蔽了父类同名字段（`SifterBlockEntity.java:66`），靠覆写 `getItemsProcessedPerCycle()` 才正确工作，建议删除父类死字段。
6. **玩法平衡提示（非缺陷）**: 新增 `crushed_netherrack` 链条：地狱岩 → 磨粉 → 筛，黄铜网期望产出含 10% 烈焰粉、10% 金粒、5% 石英、**1% 下界合金碎片**、10% 经验粒；高级网 2% 合金碎片。地狱岩近乎无限（玄武岩磨粉/巨人等），这条链显著产出入下界合金与烈焰粉的再生通道，建议在 README/配置中说明并考虑下调或提供开关。

---

## 六、修复优先级建议

| 优先级 | 项 |
|--------|----|
| P0（发版前必修） | C1 空配料越界、C2 override 解析异常、C3 产物销毁、C4 渲染除零 |
| P1（尽快） | H2 序列化双重倍率、H1 多人下 UI 误导（至少先改文案/禁用）、H3 advanced 链路、M4 indexOf 错行 |
| P2（下个版本） | H4/H5/H6、M1-M3、M5-M8、U1-U5 |
| P3（择机清理） | 第五节全部 |

---

*报告由静态审计生成，未运行游戏验证；涉及 porting lib / forgeconfigapiport 内部行为处（`getSlotLimit` 语义、`ConfigValue.set` 是否自动落盘）已标注不确定性，建议修复时以实际依赖版本源码为准。*
