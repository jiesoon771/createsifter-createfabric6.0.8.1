# 机械动力：筛子（fabric移植版） —— 适配最新Create 6.0.8.1

# Minecraft 1.20.1

**喂！看这里！有中文，翻译关一下哈，不然很杂的( •̀ ω •́ )**

> **English** · [中文说明](#中文说明)

---

## English

A simple, no-fuss sifter addon for **Create**, adapted to run on **Create 6.0.8.1** for **Minecraft 1.20.1** (Fabric).

Originally written by **oierbravo** and ported to Fabric by **Shulej**, this build updates the Fabric port so it works with Create 6.0.8.1, which removed several APIs the older version relied on.

> Community adaptation — **not** an official release.

### Features

- **Kinetic Sifter** and **Brass Sifter** with the familiar behavior
- **7 meshes** (string, andesite, brass, zinc, custom, advanced, …)
- **20 built-in sifting recipes**
- English & Chinese localization
- Works on client and server; shows recipes in **JEI** (15.20+)

---

## 中文说明

轻巧、开箱即用的**机械动力**筛选器（Sifter）附属组件，已适配在 **Create 6.0.8.1** 上运行，支持 **Minecraft 1.20.1**（Fabric）。

本项目最初由 **oierbravo** 撰写，由 **Shulej** 移植到 Fabric。本版本在 **Shulej** 的 Fabric 移植版基础上更新，使其兼容 Create 6.0.8.1——新版本移除了一些旧版依赖的 API。

> 此为**社区适配版**，**非**官方发布。

### 功能特性

- **动能筛选器**与**黄铜筛选器**，保留原有的熟悉手感
- **7 种筛网**（丝线、安山岩、黄铜、锌、定制、高级……）
- **20 条内置筛分配方**
- 中英文双语本地化
- 服务端与客户端均可用；支持 **JEI**（15.20+）配方显示

---

## [NEW] 对比原版模组的新增内容

本次更新在继承原版（oierbravo/Shulej）功能的基础上，额外增加了以下内容：

### 🗣 多语言支持（新增 / 完善）

| 语言文件 | 完成度 |
| :--- | :--- |
| `zh_cn.json` | ✅ 简体中文 — 完整翻译 |
| `zh_tw.json` | ✅ 繁體中文 — 完整翻譯 |
| `en_us.json` | ✅ English (US) — 完整（基线） |
| `en_ud.json` | ✅ Upside‑Down English — 完整（彩蛋） |
| `fr_fr.json` | ⚠️ 仅核心术语，配置界面为英文 |
| `ja_jp.json` | ⚠️ コア用語のみ、設定画面は英語 |
| `ko_kr.json` | ⚠️ 핵심 용어만, 설정 화면은 영어 |
| `ru_ru.json` | ⚠️ Только основные термины, интерфейс конфигурации на английском |

### ⚙️ 便利的配置 UI（平衡性调整）

考虑到模组平衡性一直有争议，本次改版加入了**图形化配置界面**（通过 Mod Menu → Config 打开），让你可以直接调整：

- **难度预设**：超高 / 高 / 中 / 低 / 自定义 —— 一键切换，产出概率和处理时间联动变化。
- **细调参数**：自定义模式下，可独立调节产出概率倍率和加工时间倍率（步进滑块）。
- **逐配方微调**：每一条筛分配方的每个产出物，均可单独修改掉落概率（0~100%）和单次产出数量，修改即时生效。

所有配置改动**立即生效**，进入存档后也可随时调整。服务器端配置为准（`createsifter-server.toml`），客户端自动同步。

---

## 难度与配方配置（配置界面）

通过 **Mod Menu → Config** 打开配置界面（无需注册键位）。

- **全局难度预设** —— 超高（产出×1.5 / 时间×0.5）、高（原版 ×1.0 / ×1.0）、中（×0.6 / ×1.5）、低（×0.35 / ×2.0），或**自定义**（可微调产出概率与处理时间的加减按钮）。
- **逐配方覆盖** —— 进入存档后，滚动配方列表，点开每条配方即可修改每个产物的掉落概率（0–100%）与每次筛出数量，掉落时生效。
- **服务端配置为准** —— 所有数值保存在服务端配置（`createsifter-server.toml`），专用服务器上以服主设置同步给客户端；单机则为本地存档/主机设置。

---

## 运行要求

- Minecraft **1.20.1** · Fabric Loader **0.16.5+** · Fabric API **≥ 0.92.11+1.20.1**
- **Create Fabric ≥ 6.0.8.1+build.1744-mc1.20.1**
- Java **17~21**

**服务器要求：** 服务器与客户端使用**同一份** jar 文件，配置由服务器主导。客户端也要装 createsifter + 机械动力 + Fabric API，版本**必须**与服务器一致。

---

## 兼容性

以下数据基于 **Minecraft 1.20.1 · Create 6.0.8.1+ · Fabric** 环境整理。

### 一、确认兼容 ✅（实测）

**核心依赖：** Create 6.0.8.1+、Fabric API 0.92.11+、MC 1.20.1、Java 17/21（客户端与服务器均已实测）。

**实测共存（270+ 模组的整合包）：**

- 优化/渲染：Sodium 0.5.13、Sodium Extra、Iris 1.7.6、Indium、Lithium、C2ME、ModernFix、FerriteCore、Krypton、CreateBetterFps、EntityCulling、Dynamic FPS、MemoryLeakFix
- 功能/辅助：JEI 15.20.0.134（配方正常显示）、Mod Menu 7.2.2、Cloth Config、Controlling、AppleSkin、BetterF3
- 生态/内容：暮色森林、Create: Addition 1.3.4、Create Big Cannons 5.11.4
- 以及整合包其余 250+ 模组（加载共存验证通过）

### 二、理论兼容 ✅（未逐一实测，基于官方兼容边界推导）

**Create 生态附属（1.20.1 Fabric，最新版本满足 Create 6.0.8.1 下限）：**

Create: Steam 'n' Rails 1.7.2+（需 ≥1.5.3）、Copycats+ 3.0.8+（需 >1.1.1）、Create Deco 2.1.1、Slice & Dice 3.6.0（需 >3.0.0）、Enchantment Industry 2.5.2（需 ≥1.2.16）、Jetpack 4.4.2（需 >4.1.1）、Extended Cogwheels 2.1.1+（需 >2.1.0）、New Age、Ore Excavation、Power Loader (Fabric) 2.0.3、Diesel Generators [Fabric] 2.1.4、Interactive 1.2.1、Crystal Clear、Numismatics、Utilities、Contraption Terminals、Goggles 6.1.1、Trading Floor、Central Kitchen、Bells & Whistles、Interiors、Design n' Decor、Dreams & Desires、Copper & Zinc、Ultimine、Oxidized、Pattern Schematics 等

**常见优化/辅助/库：** Embeddium、Starlight、Cull Less Leaves、GPUMemLeakFix、Xaero 地图、JourneyMap、背包/伤害显示/语音/汉化类、Architectury、Kotlin for Fabric、YUNG's API、GeckoLib、Terralith、YUNG's 系列、农夫乐事系、Supplementaries（实测）等

**配方查看器：** EMI/REI 可共存不冲突，但本模组只实现 JEI 插件 → EMI/REI 中筛子配方**不显示**（建议使用 JEI 15.20+）。

### 三、兼容性未知 ⚠️（未测试，存在潜在触点）

- **深度修改配方系统的模组**：KubeJS 配方脚本、CraftTweaker 系——本模组无集成，理论共存，但加载行为可能被干预
- **自定义渲染管线**：Canvas 等与 Create 的 Flywheel 兼容性由 Create 侧决定
- **深度集成动力网络/方块实体的 Create 附加**：Interactive 具体交互、TFMG 等
- 未列出的任何小众模组

### 四、不兼容 ❌（理论/实测）

| 项 | 类型 | 表现 |
|---|---|---|
| Create 0.5.1 及更早 | 依赖拒绝 | 加载器拒绝启动（非崩溃） |
| Create 6.0.0 ~ 6.0.8 | 依赖拒绝 | 需 ≥6.0.8.1 |
| OptiFine / OptiFabric | Create 官方不兼容 | 全版本不兼容 |
| 被 Create 淘汰的旧版附加（railways<1.5.3、copycats≤1.1.1、slice&dice≤3.0.0、jetpack≤4.1.1、enchantment-industry<1.2.16、bigcannons≤0.5.3、diesel≤2.1.3、power-loader≤1.4.2、extendedgears≤2.1.0、interactive<1.1.0、garnished≤1.6.3 等） | Create 官方淘汰 | 装最新版即可 |
| Sodium<0.5.0 / Iris≤1.2.5 / Sound Physics<1.4.5 | Create 官方不兼容 | 用新版 |
| 原版 createsifter 0.1.1 | 同 modid 冲突 | 不可同时安装 |

**提示：** 如果你的机械动力是 6.0.7~6.0.8，需要升级到 **6.0.8.1** 才能使用本模组。

> 升级步骤：
> 1、从 mods/ 删除旧版 create-fabric-*.jar，并放入新版（6.0.8.1）jar
> 2、删除 mods 里独立的 flywheel、ponder、porting_lib_*、registrate-fabric jar（如有）
> 3、残留旧版独立库是 Create 6 时代最常见的报错源头（旧 Flywheel/Ponder 与新 Create 冲突），检查其他 Create 附加模组是否满足新 Create 的下限（详见上面第四类，如 railways≥1.5.3、copycats>1.1.1 等）——不满足的同步升级
> 4、启动验证：进游戏 → 进世界 → 看日志无红字、附加模组功能正常
> 5、开启美好游戏时光~

---

## 许可、署名与免责

> 注意：最上游 **oierbravo** 采用 **LGPL-3.0**（Forge 版）；本适配版基于的 Fabric 源码仓库（**Shulej**）为 **MIT**。两方协议差异已知悉，本适配版已保留全部上游声明。

MIT 协议——上游版权归 **oierbravo**（及 **Shulej** 的 Fabric 移植版）。本适配版新增/修改的代码可自由使用；对移植者署名属自愿行为，非强制。若再分发，请保留上游 MIT 声明（随 jar 附带，见 `LICENSE.txt_createsifter`）。

本项目按"原样（AS-IS）"提供，不附带任何形式的担保。已验证基本功能可用，但仍可能存在缺陷。作者**不保证**未来会更新或长期维护；请**自行承担使用风险**。

感谢 **oierbravo**、**Shulej** 以及 **Create 开发团队！**

> tip / 提示：
> 有BUG不要找我(´。＿。｀)，有BUG不要找我〒▽〒，有BUG不要找我≧ ﹏ ≦，有BUG不要找我口牙！我只是适配了最新版机械动力，本人小白(还社恐），模组有BUG我修不了啊 X﹏X 抱歉！！！

> [NEW] 本项目源码已按 MIT 协议在 GitHub 开源：[gitbub-createsifter-createfabric6.0.8.1](https://github.com/jiesoon771/createsifter-createfabric6.0.8.1)
