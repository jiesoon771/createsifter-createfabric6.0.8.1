# 机械动力：筛子（fabric移植版） —— 适配最新Create 6.0.8.1

# Minecraft 1.20.1

**喂！看这里！有中文，翻译关一下哈，不然很杂的( •̀ ω •́ )**

> **English** · [中文说明](#中文说明)

---

## English

A simple, no-fuss sifter addon for **Create**, adapted to run on **Create 6.0.8.1** for **Minecraft 1.20.1** (Fabric).

Originally written by **oierbravo** and ported to Fabric by **Shulej**, this build updates the Fabric port so it works with Create 6.0.8.1, which removed several APIs the older version relied on.

Considering the balance issues with this mod, I have added custom balance adjustments, allowing players to tailor the mod's features to their own preferences.

> Community adaptation — **not** an official release.

> Official version (Forge) by **oierbravo** — https://www.curseforge.com/minecraft/mc-mods/create-sifting
> Shulej's Fabric port — https://modrinth.com/mod/create-sifting-fabric

### Features

- **Kinetic Sifter** and **Brass Sifter** with the familiar behavior
- **7 meshes** (string, andesite, brass, zinc, custom, advanced, …)
- **20 built-in sifting recipes**
- English & Chinese localization
- Works on client and server; shows recipes in **JEI** (15.20+)

**Localization / 新增语言：**

| Language file | Status |
| :--- | :--- |
| zh_cn.json | **简体中文** — ✅ complete |
| zh_tw.json | **繁體中文** — ✅ complete |
| en_us.json | **English (US)** — ✅ baseline |
| en_ud.json | **upside-down English** (ǝɯǝɯ) — ✅ complete |
| fr_fr.json | **Français** — ⚠️ core terms only, config UI in English |
| ja_jp.json | **日本語** — ⚠️ core terms only, config UI in English |
| ko_kr.json | **한국어** — ⚠️ core terms only, config UI in English |
| ru_ru.json | **Русский** — ⚠️ core terms only, config UI in English |

### Difficulty & Recipe Configuration (config UI)

A config screen is available via **Mod Menu** (https://modrinth.com/mod/modmenu) **→ Config** (no keybinding needed).

- **Global difficulty preset** — ultra (chance x1.5 / time x0.5), high (original, x1.0 / x1.0), medium (x0.6 / x1.5), low (x0.35 / x2.0), or **custom** (editable chance & time steppers).
- **Per-recipe overrides** — after entering a world, scroll the recipe list to open each recipe and set the drop chance (0–100%) and amount per success for every output. Changes are applied when items are rolled.
- **Server-authoritative:** all values are stored in the server-side config (`createsifter-server.toml`), so on a dedicated server the host's settings win and sync to clients. In single-player the local world is the host.

### Requirements

- Minecraft **1.20.1** · Fabric Loader **0.16.5+** · Fabric API **≥ 0.92.11+1.20.1**
- **Create Fabric ≥ 6.0.8.1+build.1744-mc1.20.1**
- Java **17~21**

**Server:** the server and client use the **same** JAR file. Configuration is server-side controlled. The client must also install createsifter, Create, and Fabric API, and all versions **must** match the server's.

### Compatibility

Compiled for **Minecraft 1.20.1 · Create 6.0.8.1+ · Fabric**.

#### Confirmed Compatible ✅ (tested)

Core requirements: Create 6.0.8.1+, Fabric API 0.92.11+, MC 1.20.1, Java 17/21 (verified on both client and server).

Tested alongside a 270+-mod modpack:

- Optimization / rendering: Sodium 0.5.13, Sodium Extra, Iris 1.7.6, Indium, Lithium, C2ME, ModernFix, FerriteCore, Krypton, CreateBetterFps, EntityCulling, Dynamic FPS, MemoryLeakFix
- Utility: JEI 15.20.0.134 (recipes display correctly), Mod Menu 7.2.2, Cloth Config, Controlling, AppleSkin, BetterF3
- Content: The Twilight Forest, Create: Addition 1.3.4, Create Big Cannons 5.11.4
- Plus the 250+ other mods in the modpack (load-time coexistence verified)

#### Theoretically Compatible (not individually tested; derived from official boundaries)

Create ecosystem addons (1.20.1 Fabric, latest versions meeting the Create 6.0.8.1 floor):

Create: Steam 'n' Rails 1.7.2+ (needs ≥1.5.3), Copycats+ 3.0.8+ (needs >1.1.1), Create Deco 2.1.1, Slice & Dice 3.6.0 (needs >3.0.0), Enchantment Industry 2.5.2 (needs ≥1.2.16), Jetpack 4.4.2 (needs >4.1.1), Extended Cogwheels 2.1.1+ (needs >2.1.0), New Age, Ore Excavation, Power Loader (Fabric) 2.0.3, Diesel Generators [Fabric] 2.1.4, Interactive 1.2.1, Crystal Clear, Numismatics, Utilities, Contraption Terminals, Goggles 6.1.1, Trading Floor, Central Kitchen, Bells & Whistles, Interiors, Design n' Decor, Dreams & Desires, Copper & Zinc, Ultimine, Oxidized, Pattern Schematics, etc.

Common optimization / utility / library mods: Embeddium, Starlight, Cull Less Leaves, GPUMemLeakFix, Xaero maps, JourneyMap, backpack/damage-display/voice/translation mods, Architectury, Kotlin for Fabric, YUNG's API, GeckoLib, Terralith, YUNG's series, Farmer's Delight series, Supplementaries (tested), etc.

Recipe viewer note: EMI/REI coexist without conflict, but this mod only implements a **JEI** plugin — sifting recipes **will not display** in EMI/REI (JEI 15.20+ is recommended).

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

Tip: if your Create is 6.0.7–6.0.8, upgrade to 6.0.8.1 to use this mod.

> Upgrade steps:
> 1. From the `mods/` folder, delete the old `create-fabric-*.jar` and place the new (6.0.8.1) jar.
> 2. Delete any standalone flywheel, ponder, porting_lib_*, and registrate-fabric jars in `mods/`, if present.
> 3. Leftover standalone libraries from the Create 6 era are the most common source of errors (old Flywheel/Ponder conflict with new Create). Check whether other Create add-ons meet the minimum version for the new Create (see Category 4 above, e.g. Railways ≥ 1.5.3, Copycats > 1.1.1) and upgrade them at the same time if needed.
> 4. Verify at launch: enter the game, enter a world, check the log for no red errors, and confirm add-ons work.
> 5. Enjoy your wonderful gaming time!

### License, attribution & warranty

> Note: the direct upstream **oierbravo** uses **LGPL-3.0** (Forge version); the Fabric source repo this adaptation is based on (**Shulej**) claims **MIT**. To align most faithfully with the original licensing chain, this adaptation is published as a whole under **LGPL-3.0**. All upstream notices have been retained.

LGPL-3.0 — upstream © **oierbravo** (and **Shulej**'s Fabric port). The code added by this adaptation is licensed under LGPL-3.0 as well. If you redistribute it, please keep the upstream notice (bundled in the jar as `LICENSE.txt_createsifter`).

This project is provided AS-IS, without warranty of any kind. It has been verified for basic functionality, but bugs may still exist. The author does not guarantee updates or active maintenance in the future; use at your own risk.

Thanks to **oierbravo**, **Shulej**, and the **Create team!**

> tip:
> If there's a bug, don't find me 😭😭😭 I just updated the mod to work with the latest Create version. I'm a total newbie – I can't fix mod bugs, sorry!!!
> Source code is open source on GitHub under LGPL-3.0: https://github.com/jiesoon771/createsifter-createfabric6.0.8.1
> Downloads (jar): https://github.com/jiesoon771/createsifter-createfabric6.0.8.1/releases

---

## 中文说明

轻巧、开箱即用的**机械动力**筛选器（Sifter）附属组件，已适配在 **Create 6.0.8.1** 上运行，支持 **Minecraft 1.20.1**（Fabric）。

本项目最初由 **oierbravo** 撰写，由 **Shulej** 移植到 Fabric。本版本在 **Shulej** 的 Fabric 移植版基础上更新，使其兼容 Create 6.0.8.1——新版本移除了一些旧版依赖的 API。

考虑到该模组 **平衡性问题**，我加入了自定义平衡性调整，玩家可以根据自己的需求自定义这个模组的功能。

> 此为**社区适配版**，**非**官方发布。

> 官方原版（Forge，作者 oierbravo）— https://www.curseforge.com/minecraft/mc-mods/create-sifting
> Shulej 的 Fabric 移植版 — https://modrinth.com/mod/create-sifting-fabric

### 功能特性

- **动能筛选器**与**黄铜筛选器**，保留原有的熟悉手感
- **7 种筛网**（丝线、安山岩、黄铜、锌、定制、高级……）
- **20 条内置筛分配方**
- 中英文双语本地化
- 服务端与客户端均可用；支持 **JEI**（15.20+）配方显示

### 难度与配方配置（配置界面）

通过 **Mod Menu**（https://modrinth.com/mod/modmenu）**→ Config** 打开配置界面（无需注册键位）。

- **全局难度预设** —— 超高（产出×1.5 / 时间×0.5）、高（原版 ×1.0 / ×1.0）、中（×0.6 / ×1.5）、低（×0.35 / ×2.0），或**自定义**（可微调产出概率与处理时间的加减按钮）。
- **逐配方覆盖** —— 进入存档后，滚动配方列表，点开每条配方即可修改每个产物的掉落概率（0–100%）与每次筛出数量，掉落时生效。
- **服务端配置为准** —— 所有数值保存在服务端配置（`createsifter-server.toml`），专用服务器上以服主设置同步给客户端；单机则为本地存档/主机设置。

### 运行要求

- Minecraft **1.20.1** · Fabric Loader **0.16.5+** · Fabric API **≥ 0.92.11+1.20.1**
- **Create Fabric ≥ 6.0.8.1+build.1744-mc1.20.1**
- Java **17~21**

**服务器要求：** 服务器与客户端使用**同一份** jar 文件，配置由服务器主导。客户端也要装 createsifter + 机械动力 + Fabric API，版本**必须**与服务器一致。

### 兼容性

以下数据基于 **Minecraft 1.20.1 · Create 6.0.8.1+ · Fabric** 环境整理。

#### 一、确认兼容 ✅（实测）

**核心依赖：** Create 6.0.8.1+、Fabric API 0.92.11+、MC 1.20.1、Java 17/21（客户端与服务器均已实测）。

**实测共存（270+ 模组的整合包）：**

- 优化/渲染：Sodium 0.5.13、Sodium Extra、Iris 1.7.6、Indium、Lithium、C2ME、ModernFix、FerriteCore、Krypton、CreateBetterFps、EntityCulling、Dynamic FPS、MemoryLeakFix
- 功能/辅助：JEI 15.20.0.134（配方正常显示）、Mod Menu 7.2.2、Cloth Config、Controlling、AppleSkin、BetterF3
- 生态/内容：暮色森林、Create: Addition 1.3.4、Create Big Cannons 5.11.4
- 以及整合包其余 250+ 模组（加载共存验证通过）

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
| Create 0.5.1 及更早 | 不支持 | 加载器拒绝启动（非崩溃） |
| Create 6.0.0 ~ 6.0.8 | 不支持 | 需 ≥6.0.8.1 |
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

### 许可、署名与免责

> 注意：最上游 **oierbravo** 采用 **LGPL-3.0**（Forge 版）；本适配版基于的 Fabric 源码仓库（**Shulej**）声称 **MIT**。为最充分对齐上游授权链，本适配版整体按 **LGPL-3.0** 发布。本适配版已保留全部上游声明。

LGPL-3.0 协议——上游版权归 **oierbravo**（及 **Shulej** 的 Fabric 移植版）。本适配版新增/修改的代码同样以 LGPL-3.0 授权。若再分发，请保留上游声明（随 jar 附带，见 `LICENSE.txt_createsifter`）。

本项目按"原样（AS-IS）"提供，不附带任何形式的担保。已验证基本功能可用，但仍可能存在缺陷。作者**不保证**未来会更新或长期维护；请**自行承担使用风险**。

感谢 **oierbravo**、**Shulej** 以及 **Create 开发团队！**

> tip / 提示：
> 有BUG不要找我(´。＿。｀)，有BUG不要找我〒▽〒，有BUG不要找我≧ ﹏ ≦，有BUG不要找我口牙！我只是适配了最新版机械动力，本人小白(还社恐），模组有BUG我修不了啊 X﹏X 抱歉！！！
> 本项目源码已按 LGPL-3.0 协议在 GitHub 开源：https://github.com/jiesoon771/createsifter-createfabric6.0.8.1
> 下载（jar）：https://github.com/jiesoon771/createsifter-createfabric6.0.8.1/releases
