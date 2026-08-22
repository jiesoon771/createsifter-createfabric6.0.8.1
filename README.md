# 机械动力：筛子（fabric移植版） —— 适配最新Create 6.0.8.1

# Minecraft 1.20.1

**喂！看这里！有中文，翻译关一下哈，不然很杂的( •̀ ω •́ )**
> **English** · [中文说明](#中文说明)
---

## English

A simple, no-fuss sifter addon for **Create**, adapted to run on **Create 6.0.8.1** for **Minecraft 1.20.1** (Fabric).

Originally written by **oierbravo** and ported to Fabric by **Shulej**, this build updates the Fabric port so it works with Create 6.0.8.1, which removed several APIs the older version relied on.

> Community adaptation — **not** an official release.
> Here is the official version link, thanks to oierbravo!
https://www.curseforge.com/minecraft/mc-mods/create-sifting
### Features

- **Kinetic Sifter** and **Brass Sifter** with the familiar behavior
- **7 meshes** (string, andesite, brass, zinc, custom, advanced, …)
- **20 built-in sifting recipes**
- English & Chinese localization
- Works on client and server; shows recipes in **JEI** (15.20+)

---

### Additions over vanilla

#### Added localization

This update adds or improves the following language files:

| Language file | Status |
| :--- | :--- |
| `zh_cn.json` | **Simplified Chinese** — ✅ Full translation |
| `zh_tw.json` | **Traditional Chinese** — ✅ Full translation |
| `en_us.json` | **English (US)** — ✅ Complete baseline |
| `en_ud.json` |Hey, read it backwards （ǝɯǝɯ）ǝʇǝldɯoƆ ✅ — **ɥsılƃuƎ uʍopǝpᴉsdn** |
| `fr_fr.json` | **Français** — ⚠️ Core terms only, config UI in English |
| `ja_jp.json` | **日本語** — ⚠️ Core terms only, config UI in English |
| `ko_kr.json` | **한국어** — ⚠️ Core terms only, config UI in English |
| `ru_ru.json` | **Русский** — ⚠️ Core terms only, config UI in English |

## Convenient UI configuration for difficulty & balance

Given that the balance of this mod has always been controversial, this update introduces a user‑friendly configuration interface (accessible via **Mod Menu → Config**, no keybinding needed).

- **Difficulty presets** – Very High (output x1.5 / time x0.5), High (original x1.0 / x1.0), Medium (x0.6 / x1.5), Low (x0.35 / x2.0), or **Custom** (adjustable sliders for chance and time multipliers).
- **Per‑recipe overrides** – After entering a world, scroll the recipe list and open each recipe to modify drop chance (0–100%) and amount per success for every output. Changes apply when items are rolled.
- **Server‑authoritative** – All values are stored in the server‑side config (`createsifter-server.toml`). On a dedicated server, the host’s settings win and sync to clients. In single‑player, the local world is the host.

### Requirements

- Minecraft **1.20.1** · Fabric Loader **0.16.5+** · Fabric API **≥ 0.92.11+1.20.1**
- **Create Fabric ≥ 6.0.8.1+build.1744-mc1.20.1**
- Java **17~21**

**Server:** the server and client use the **same** JAR file. Configuration is server-side controlled. The client must also install createsifter, Create, and Fabric API, and all versions **must** match the server's.

### Compatibility

Compiled for **Minecraft 1.20.1 · Create 6.0.8.1+ · Fabric**.

#### Confirmed Compatible ✅ (tested)

**Core requirements:** Create 6.0.8.1+, Fabric API 0.92.11+, MC 1.20.1, Java 17/21 (verified on both client and server).

**Tested alongside an 84‑mod local modpack (and also verified in a larger 270+ modpack, all load‑time coexistence confirmed):**

- Optimization / rendering: Sodium 0.5.13, Sodium Extra, Iris 1.7.6, Indium, Lithium, C2ME, ModernFix, FerriteCore, Krypton, CreateBetterFps, EntityCulling, Dynamic FPS, MemoryLeakFix
- Utility: JEI 15.20.0.134 (recipes display correctly), Mod Menu 7.2.2, Cloth Config, Controlling, AppleSkin, BetterF3
- Content: The Twilight Forest, Create: Addition 1.3.4, Create Big Cannons 5.11.4
- Other functional mods (load-time coexistence verified): Carry On, Waystones, Traveler's Backpack, FTB Ultimine, Inventory Profiles Next, Mouse Tweaks, Double Doors, Falling Leaves, Hardcore Revival, Simply Swords, Chat Heads, Eating Animation, NotEnoughAnimations, IMBlocker, Resolution Control+, LambDynamicLights, Modern UI, Zoomify, TaCZ Refabricated, I18nUpdateMod, AmbientSounds, Presence Footsteps, Sound Physics Remastered, LAN Server Properties, FastQuit, Voice Chat, JeCharacters, Open Parties and Claims, Do a Barrel Roll, Chunky, Continuity, Supplementaries, Amendments (+ Moonlight), Cull Less Leaves, Xaero's World Map, Terralith, YUNG's Better Dungeons / Mineshafts / Ocean Monuments, Lazydfu, GPUMemLeakFix, fast-ip-ping

#### Theoretically Compatible (not individually tested; derived from official boundaries)

**Create ecosystem addons (1.20.1 Fabric, latest versions meeting the Create 6.0.8.1 floor):**

Create: Steam 'n' Rails 1.7.2+ (needs ≥1.5.3), Copycats+ 3.0.8+ (needs >1.1.1), Create Deco 2.1.1, Slice & Dice 3.6.0 (needs >3.0.0), Enchantment Industry 2.5.2 (needs ≥1.2.16), Jetpack 4.4.2 (needs >4.1.1), Extended Cogwheels 2.1.1+ (needs >2.1.0), New Age, Ore Excavation, Power Loader (Fabric) 2.0.3, Diesel Generators [Fabric] 2.1.4, Interactive 1.2.1, Crystal Clear, Numismatics, Utilities, Contraption Terminals, Goggles 6.1.1, Trading Floor, Central Kitchen, Bells & Whistles, Interiors, Design n' Decor, Dreams & Desires, Copper & Zinc, Ultimine, Oxidized, Pattern Schematics, etc.

**Common optimization / utility / library mods:** Embeddium, Starlight, Cull Less Leaves, GPUMemLeakFix, Xaero maps, JourneyMap, backpack/damage-display/voice/translation mods, Architectury, Kotlin for Fabric, YUNG's API, GeckoLib, Terralith, YUNG's series, Farmer's Delight series, Supplementaries (tested), etc.

**Recipe viewer note:** EMI/REI coexist without conflict, but this mod only implements a **JEI** plugin — sifting recipes **will not display** in EMI/REI (JEI 15.20+ is recommended).

#### Unknown Compatibility ⚠️ (untested; potential points of contact)

- Mods that deeply alter the recipe system (KubeJS recipe scripts, the CraftTweaker family): no integration here, theoretically coexist, but load behavior may be affected
- Custom rendering pipelines (Canvas, etc.): compatibility with Create's Flywheel is decided by the Create side
- Create addons with deep stress-network / block-entity integration (Interactive, TFMG, etc.)
- Any other niche mods not listed

#### Incompatible ❌ (theoretical / tested)

| Item | Type | Behavior |
|---|---|---|
| Create 0.5.1 and earlier | Dependency refusal | Loader refuses to start (not a crash) |
| Create 6.0.0 ~ 6.0.8 | Dependency refusal | Requires ≥6.0.8.1 |
| OptiFine / OptiFabric | Officially broken by Create | Incompatible on all versions |
| Old addons phased out by Create (railways<1.5.3, copycats≤1.1.1, slice&dice≤3.0.0, jetpack≤4.1.1, enchantment-industry<1.2.16, bigcannons≤0.5.3, diesel≤2.1.3, power-loader≤1.4.2, extendedgears≤2.1.0, interactive<1.1.0, garnished≤1.6.3, etc.) | Officially phased out by Create | Install the latest version |
| Sodium<0.5.0 / Iris≤1.2.5 / Sound Physics<1.4.5 | Officially broken by Create | Use the new version |
| Original createsifter 0.1.1 | Modid conflict | Cannot be installed together |

**Tip:** if your Create is 6.0.7–6.0.8, upgrade to **6.0.8.1** to use this mod.

> Upgrade steps:
> 1. From the `mods/` folder, delete the old `create-fabric-*.jar` and place the new (6.0.8.1) jar.
> 2. Delete any standalone flywheel, ponder, porting_lib_*, and registrate-fabric jars in `mods/`, if present.
> 3. Leftover standalone libraries from the Create 6 era are the most common source of errors (old Flywheel/Ponder conflict with new Create). Check whether other Create add-ons meet the minimum version for the new Create (see Category 4 above, e.g. Railways ≥ 1.5.3, Copycats > 1.1.1) and upgrade them at the same time if needed.
> 4. Verify at launch: enter the game, enter a world, check the log for no red errors, and confirm add-ons work.
> 5. Enjoy your wonderful gaming time!

### License, attribution & warranty

> Note: the direct upstream **oierbravo** uses **LGPL-3.0** (Forge version); the Fabric source repo this adaptation is based on (**Shulej**) is **MIT**. The difference between the two upstream licenses is acknowledged; all upstream notices have been retained.

MIT License — upstream © **oierbravo** (and **Shulej**'s Fabric port). The code added by this adaptation may be used freely; credit to the porter is appreciated but not required. If you redistribute it, please keep the upstream MIT notice (bundled in the jar as `LICENSE.txt_createsifter`).

This project is provided AS-IS, without warranty of any kind. It has been verified for basic functionality, but bugs may still exist. The author does not guarantee updates or active maintenance in the future; use at your own risk.

Thanks to **oierbravo**, **Shulej**, and the **Create team!**

> tip:
> If there's a bug, don't find me 😭😭😭 I just updated the mod to work with the latest Create version. I'm a total newbie – I can't fix mod bugs, sorry!!!
>
> This project is now open‑source on GitHub under the MIT license:Yes, that's the one.
Download：
> https://modrinth.com/mod/create-sifting-create-6-fabric
> https://www.curseforge.com/minecraft/mc-mods/create-sifting-create-fabric-6-0-8-1/preview
---

## 中文说明

轻巧、开箱即用的**机械动力**筛选器（Sifter）附属组件，已适配在 **Create 6.0.8.1** 上运行，支持 **Minecraft 1.20.1**（Fabric）。

本项目最初由 **oierbravo** 撰写，由 **Shulej** 移植到 Fabric。本版本在 **Shulej** 的 Fabric 移植版基础上更新，使其兼容 Create 6.0.8.1——新版本移除了一些旧版依赖的 API。

> 此为**社区适配版**，**非**官方发布。
> 这是官方版本的链接，感谢oierbravo！
https://www.curseforge.com/minecraft/mc-mods/create-sifting
### 功能特性

- **动能筛选器**与**黄铜筛选器**，保留原有的熟悉手感
- **7 种筛网**（丝线、安山岩、黄铜、锌、定制、高级……）
- **20 条内置筛分配方**
- 中英文双语本地化
- 服务端与客户端均可用；支持 **JEI**（15.20+）配方显示

---

### 对比原版模组的新增内容

#### 新增语言支持

本次更新新增或完善了以下语言文件：

| 语言文件 | 完成度 |
| :--- | :--- |
| `zh_cn.json` | **简体中文** — ✅ 完整翻译 |
| `zh_tw.json` | **繁體中文** — ✅ 完整翻譯 |
| `en_us.json` | **English (US)** — ✅ 完整（基线） |
| `en_ud.json` |反着看 **ɥsılƃuƎ uʍopǝpᴉsdn**  — ✅ 完整（彩蛋） |
| `fr_fr.json` | **Français** — ⚠️ 仅核心术语，配置界面为英文 |
| `ja_jp.json` | **日本語** — ⚠️ コア用語のみ、設定画面は英語 |
| `ko_kr.json` | **한국어** — ⚠️ 핵심 용어만, 설정 화면은 영어 |
| `ru_ru.json** | **Русский** — ⚠️ Только основные термины, интерфейс конфигурации на английском |

#### 便利的配置 UI（平衡性调整）

考虑到模组平衡性一直有争议，本次改版加入了**图形化配置界面**（通过 **Mod Menu → Config** 打开，无需注册键位）。

- **难度预设** – 超高（产出×1.5 / 时间×0.5）、高（原版 ×1.0 / ×1.0）、中（×0.6 / ×1.5）、低（×0.35 / ×2.0），或**自定义**（可微调产出概率与处理时间的加减滑块）。
- **逐配方覆盖** – 进入存档后，滚动配方列表，点开每条配方即可修改每个产物的掉落概率（0–100%）与每次筛出数量，改动在物品掉落时生效。
- **服务端配置为准** – 所有数值保存在服务端配置（`createsifter-server.toml`），专用服务器上以服主设置同步给客户端；单机则为本地存档/主机设置。

### 运行要求

- Minecraft **1.20.1** · Fabric Loader **0.16.5+** · Fabric API **≥ 0.92.11+1.20.1**
- **Create Fabric ≥ 6.0.8.1+build.1744-mc1.20.1**
- Java **17~21**

**服务器要求：** 服务器与客户端使用**同一份** jar 文件，配置由服务器主导。客户端也要装 createsifter + 机械动力 + Fabric API，版本**必须**与服务器一致。

### 兼容性

以下数据基于 **Minecraft 1.20.1 · Create 6.0.8.1+ · Fabric** 环境整理。

#### 一、确认兼容 ✅（实测）

**核心依赖：** Create 6.0.8.1+、Fabric API 0.92.11+、MC 1.20.1、Java 17/21（客户端与服务器均已实测）。

**实测共存（84 模组的本地整合包，并在 270+ 模组的大规模整合包中也验证通过，加载共存全部通过）：**

- 优化/渲染：Sodium 0.5.13、Sodium Extra、Iris 1.7.6、Indium、Lithium、C2ME、ModernFix、FerriteCore、Krypton、CreateBetterFps、EntityCulling、Dynamic FPS、MemoryLeakFix
- 功能/辅助：JEI 15.20.0.134（配方正常显示）、Mod Menu 7.2.2、Cloth Config、Controlling、AppleSkin、BetterF3
- 生态/内容：暮色森林、Create: Addition 1.3.4、Create Big Cannons 5.11.4
- 其他功能区模组（加载共存验证通过）：搬运 Carry On、传送石碑 Waystones、旅行者背包 Traveler's Backpack、连锁破坏 FTB Ultimine、一键整理 Inventory Profiles Next、鼠标手势 Mouse Tweaks、双开门 Double Doors、落叶 Falling Leaves、硬核复活 Hardcore Revival、简易刀剑 Simply Swords、聊天头像 Chat Heads、进食动画 Eating Animation、更多动画 NotEnoughAnimations、输入法修复 IMBlocker、分辨率控制 Resolution Control+、动态光源 LambDynamicLights、现代化 UI Modern UI、放大镜 Zoomify、枪械 TaCZ、自动汉化 I18nUpdateMod、自然音效 AmbientSounds、脚步声 Presence Footsteps、物理声效 Sound Physics Remastered、自定义局域网 LAN Server Properties、快速退出 FastQuit、语音聊天 Voice Chat、拼音搜索 JeCharacters、领地 Open Parties and Claims、翻滚飞行 Do a Barrel Roll、预生成区块 Chunky、连接纹理 Continuity、锦致装饰 Supplementaries/Amendments（+ Moonlight）、更好树叶 Cull Less Leaves、Xaero 世界地图、泰拉瑞斯亚 Terralith、YUNG 地牢/矿井/海底神殿系列、DFU 优化 Lazydfu、GPU 泄漏修复 GPUMemLeakFix、fast-ip-ping

（库依赖如 Architectury / Kotlin for Fabric / Fabric API 等另行归类，不计入上述功能模组）

#### 二、理论兼容 ✅（未逐一实测，基于官方兼容边界推导）

**Create 生态附属（1.20.1 Fabric，最新版本满足 Create 6.0.8.1 下限）：**

Create: Steam 'n' Rails 1.7.2+（需 ≥1.5.3）、Copycats+ 3.0.8+（需 >1.1.1）、Create Deco 2.1.1、Slice & Dice 3.6.0（需 >3.0.0）、Enchantment Industry 2.5.2（需 ≥1.2.16）、Jetpack 4.4.2（需 >4.1.1）、Extended Cogwheels 2.1.1+（需 >2.1.0）、New Age、Ore Excavation、Power Loader (Fabric) 2.0.3、Diesel Generators [Fabric] 2.1.4、Interactive 1.2.1、Crystal Clear、Numismatics、Utilities、Contraption Terminals、Goggles 6.1.1、Trading Floor、Central Kitchen、Bells & Whistles、Interiors、Design n' Decor、Dreams & Desires、Copper & Zinc、Ultimine、Oxidized、Pattern Schematics 等

**常见优化/辅助/库：** Embeddium、Starlight、Cull Less Leaves、GPUMemLeakFix、Xaero 地图、JourneyMap、背包/伤害显示/语音/汉化类、Architectury、Kotlin for Fabric、YUNG's API、GeckoLib、Terralith、YUNG's 系列、农夫乐事系、Supplementaries（实测）等

**配方查看器：** EMI/REI 可共存不冲突，但本模组只实现 JEI 插件 → EMI/REI 中筛子配方**不显示**（建议使用 JEI 15.20+）。

#### 三、兼容性未知 ⚠️（未测试，存在潜在触点）

- **深度修改配方系统的模组**：KubeJS 配方脚本、CraftTweaker 系——本模组无集成，理论共存，但加载行为可能被干预
- **自定义渲染管线**：Canvas 等与 Create 的 Flywheel 兼容性由 Create 侧决定
- **深度集成动力网络/方块实体的 Create 附加**：Interactive 具体交互、TFMG 等
- 未列出的任何小众模组

#### 四、不兼容 ❌（理论/实测）

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
> 1. 从 `mods/` 删除旧版 `create-fabric-*.jar`，并放入新版（6.0.8.1）jar
> 2. 删除 `mods` 里独立的 `flywheel`、`ponder`、`porting_lib_*`、`registrate-fabric` jar（如有）
> 3. 残留旧版独立库是 Create 6 时代最常见的报错源头（旧 Flywheel/Ponder 与新 Create 冲突），检查其他 Create 附加模组是否满足新 Create 的下限（详见上面第四类，如 railways≥1.5.3、copycats>1.1.1 等）——不满足的同步升级
> 4. 启动验证：进游戏 → 进世界 → 看日志无红字、附加模组功能正常
> 5. 开启美好游戏时光~

### 许可、署名与免责

> 注意：最上游 **oierbravo** 采用 **LGPL-3.0**（Forge 版）；本适配版基于的 Fabric 源码仓库（**Shulej**）为 **MIT**。两方协议差异已知悉，本适配版已保留全部上游声明。

MIT 协议——上游版权归 **oierbravo**（及 **Shulej** 的 Fabric 移植版）。本适配版新增/修改的代码可自由使用；对移植者署名属自愿行为，非强制。若再分发，请保留上游 MIT 声明（随 jar 附带，见 `LICENSE.txt_createsifter`）。

本项目按“原样（AS-IS）”提供，不附带任何形式的担保。已验证基本功能可用，但仍可能存在缺陷。作者**不保证**未来会更新或长期维护；请**自行承担使用风险**。

感谢 **oierbravo**、**Shulej** 以及 **Create 开发团队！**

> tip / 提示：
> 有BUG不要找我(´。＿。｀)，有BUG不要找我〒▽〒，有BUG不要找我≧ ﹏ ≦，有BUG不要找我口牙！我只是适配了最新版机械动力，本人小白(还社恐），模组有BUG我修不了啊 X﹏X 抱歉！！！
>
> 本项目源码已按 MIT 协议在 GitHub 开源：对，就是这个项目。
> 下载：
> https://modrinth.com/mod/create-sifting-create-6-fabric
> https://www.curseforge.com/minecraft/mc-mods/create-sifting-create-fabric-6-0-8-1/preview
