# Create Sifting — Create Fabric 6.0.8.1

- 适配 **Create Fabric 6.0.8.1** / **Minecraft 1.20.1**（Fabric）— Supports **Create Fabric 6.0.8.1** / **Minecraft 1.20.1** (Fabric)
- 版本 **V1.0.1** · 协议 **LGPL-3.0** — Version **V1.0.1** · License **LGPL-3.0**

> 模组内置 简体中文 / 繁體中文 / 文言（华夏）/ English / Français / 日本語 / 한국어 / Русский / Upside-down English —— 全部完整翻译（含配置界面）。

## Table of Contents / 目录

### [English](#english) · [中文文档](#中文文档)

> 嘿，你是不是在找[兼容性说明](#兼容性)？
> Hey, are you looking for [compatibility notes](#compatibility)?

| English | 中文 |
|---|---|
| [About this port](#about-this-port) | [简介](#简介) |
| [Features](#features) | [关于本移植](#关于本移植) |
| [Blocks (defaults all configurable)](#blocks-defaults-all-configurable) | [功能特性](#功能特性) |
| [Meshes (9)](#meshes-9) | [添加的内容](#添加的内容) |
| [Mechanics & usage](#mechanics--usage) | [机制与使用](#机制与使用) |
| [Recipes](#recipes) | [筛选配方](#筛选配方) |
| [Configuration](#configuration) | [配置](#配置) |
| [Localization](#localization) | [本地化](#本地化) |
| [Requirements](#requirements) | [运行要求](#运行要求) |
| [Compatibility](#compatibility) | [兼容性](#兼容性) |
| [Confirmed compatible (tested)](#compatibility) | [确认兼容（实测）](#确认兼容实测) |
| [Theoretically compatible](#compatibility) | [理论兼容（基于官方兼容边界推导）](#理论兼容基于官方兼容边界推导) |
| [Unknown compatibility](#compatibility) | [兼容性未知](#兼容性未知) |
| [Not compatible](#compatibility) | [不兼容](#不兼容) |
| [License & credits](#license--credits) | [许可与致谢](#许可与致谢) |
| [Download & build](#download--build) | [下载与构建](#下载与构建) |

---

## English

A sifter addon for **Create**, adapted to run on **Create 6.0.8.1** for **Minecraft 1.20.1** (Fabric).

Originally written by **oierbravo** (Forge), ported to Fabric by **Shulej**; this build updates the Fabric port for Create 6.0.8.1, adds a graphical config screen (difficulty presets + per-recipe output overrides), and backports several 1.21 features (sturdy / advanced sturdy meshes, crushed basalt and the netherite sifting chain) to 1.20.1.

Given this mod's long-running balance debate, this build adds **custom balance adjustments** — difficulty presets plus per-recipe overrides — so every player can tune drop chances and processing times to taste.

---

### About this port

> Community adaptation — **not** an official release.

**Official version (Forge) — thanks to [oierbravo](https://www.curseforge.com/minecraft/mc-mods/create-sifting)!**
**Shulej's Fabric port — thanks to [Shulej](https://modrinth.com/mod/create-sifting-fabric)!**

---

### Features

- **Two kinetic sifters** (Sifter & Brass Sifter) with the familiar Create feel;
- **9 meshes** (string, andesite, zinc, brass, custom, sturdy, advanced brass, advanced sturdy, advanced custom);
- **13 built-in sifting recipes**, plus 9 crafting recipes and 2 basalt processing recipes;
- **8 languages fully localized** (EN / CN / TW / FR / JA / KO / RU / UD), config UI included;
- Works on both client and server; recipes shown in **JEI** (15.20+);
- **Netherite sifting on 1.20.1** (sturdy / advanced sturdy meshes + crushed basalt).

---

### Blocks (defaults all configurable)

| Block | Description |
| :--- | :--- |
| Sifter | 1 item per cycle; stress impact **4.0su**, minimum speed **1 RPM**, output capacity **16 slots** |
| Brass Sifter | 8 items per cycle; stress impact **8.0su**, minimum speed **16 RPM**, output capacity **64 slots**; output filter + redstone lock |
| Crushed Basalt | Ground from basalt (crushing wheels / millstone); sift it for nether resources |
| Crushed End Stone | No built-in recipe; for pack devs via datapack |
| Dust | No built-in recipe; for pack devs via datapack |

---

### Meshes (9)

String, Andesite, Zinc, Brass, Custom, Sturdy, Advanced Brass, Advanced Sturdy, Advanced Custom.

- **Custom** and **Advanced Custom** have no built-in recipes (datapack for pack devs).
- A sifter holding an advanced mesh only runs advanced-mesh recipes. The advanced meshes are **not** limited to the brass sifter — the plain sifter accepts them too (unlike the official Forge version).

---

### Mechanics & usage

- **Power**: feed rotation from the side via gears.
- **Install a mesh**: right-click the sifter with a mesh.
- **Take out items**: right-click with an empty hand — output first; if the output is empty, the input stack is returned.
- **Remove the mesh**: sneak + right-click with an empty hand (mesh returns to your inventory).
- **Input**: toss blocks on top, or feed via hoppers, chutes, brass funnels, etc.
- **Output**: right-click to collect, or extract from the side with automation.

**Brass Sifter** — advanced sifter:
- 8 items per cycle (configurable)
- Output filter (only output the selected items)
- Stops processing on a redstone signal

**Waterlogged sifting** — some recipes require the sifter to be waterlogged: place the sifter in water, or hand-sift while standing in a liquid.

---

### Recipes

13 built-in sifting recipes (V1-based); default processing time **500 ticks (25 s)**, adjustable in the config screen. All recipes are viewable in-game via **JEI** (15.20+).

**Gravel**
- Andesite mesh: copper nugget 5%, zinc nugget 1%, iron nugget 1%, gold nugget 5%, coal 10%, flint 10%
- Zinc mesh: copper 5%, zinc 2%, iron 5%, gold 10%, coal 10%, lapis lazuli 5%, flint 10%, experience nugget 10%
- Brass mesh: crushed raw copper 10%, crushed raw zinc 10%, crushed raw gold 5%, crushed raw iron 10%, lapis 10%, coal 15%, flint 10%, experience nugget 10%
- Advanced brass mesh: crushed raw copper 10%, crushed raw zinc 10%, crushed raw gold 5%, crushed raw iron 15%, lapis 10%, diamond 5%, emerald 2%, experience nugget 10%

**Sand**
- String mesh: redstone 5%, bone meal 40%
- Andesite mesh: redstone 10%, bone meal 40%, experience nugget 10%
- Zinc mesh: redstone 15%, bone meal 40%, experience nugget 10%
- Brass mesh: redstone (×2) 25%, glowstone dust 10%, bone meal 40%, blaze powder 5%, experience nugget 20%

**Soul Sand**
- Brass mesh: quartz 10%, nether wart 5%, experience nugget 10%
- Advanced brass mesh: quartz 45%, quartz 15%, nether wart 10%, ghast tear 5%, experience nugget 20%

**Dirt (waterlogged)**
- String mesh: kelp 20%, seagrass 30%, tube coral 5%, brain coral 5%, bubble coral 5%, fire coral 5%, horn coral 5%

**Crushed Basalt** (new in this adaptation — absent from official 1.20.1)
- Sturdy mesh: ancient debris 2%, netherite scrap 1%, experience nugget 10%
- Advanced sturdy mesh: ancient debris 5%, netherite scrap 2%, experience nugget 10%

---

### Configuration

Graphical config screen via **Mod Menu → Config** (no keybind needed). Settings are stored server-side in `createsifter-server.toml`; on dedicated servers the host's settings sync to clients.

**Difficulty presets** (output chance × / processing time ×)
- Ultra: ×1.5 / ×0.5
- High (default): ×1 / ×1
- Medium: ×0.6 / ×1.5
- Low: ×0.35 / ×2.0
- Custom: freely adjustable

**Per-recipe overrides** — open any recipe in the config list to change each output's chance (0–100%) and count; changes apply immediately on drop.

**Other mechanical defaults** (configurable): stress impact 4 / 8su · minimum speed 1 / 16 RPM · output capacity 16 / 64 slots · items per cycle (brass) 8.

---

### Localization

All 8 built-in language files are fully translated — including the config UI (items, blocks, difficulty presets, and the per-recipe override screen):

| Language file | Status |
| :--- | :--- |
| `zh_cn.json` | **简体中文** — Full |
| `zh_tw.json` | **繁體中文** — Full |
| `zh_hk.json` | **繁體中文** — Full |
| `lzh.json` | 文言（华夏）— 備譯 |
| `en_us.json` | English (US) — Complete |
| `en_ud.json` | ǝɹǝɥʇ sı ɥsılƃuƎ uʍopǝpᴉsdn — Complete |
| `fr_fr.json` | Français — Traduction complète |
| `ja_jp.json` | 日本語 — 完全な翻訳 |
| `ko_kr.json` | 한국어 — 완전한 번역 |
| `ru_ru.json` | Русский — Полный перевод |

---

### Requirements

- Minecraft **1.20.1** · Fabric Loader **0.17.2+** · Fabric API **≥ 0.92.11+1.20.1**
- **Create Fabric ≥ 6.0.8.1+build.1744-mc1.20.1** (bundles Registrate / Ponder / Flywheel / Porting Lib)
- Java **17–21**

Server and client must use the same jar; clients also need this mod, Create and Fabric API.

---

### Compatibility

Compiled for **Minecraft 1.20.1 · Create 6.0.8.1+ · Fabric**.

**Confirmed compatible (tested)** — verified on both client and server, and load-tested in an 84-mod local modpack plus a 270+ mod pack:

- Optimization/rendering: Sodium 0.5.13, Sodium Extra, Iris 1.7.6, Indium, Lithium, C2ME, ModernFix, FerriteCore, Krypton, CreateBetterFps, EntityCulling, Dynamic FPS, MemoryLeakFix.
- Utility: JEI 15.20.0.134 (recipes display correctly), Mod Menu 7.2.2, Cloth Config, Controlling, AppleSkin, BetterF3.
- Content: The Twilight Forest, Create: Addition 1.3.4, Create Big Cannons 5.11.4.

**Theoretically compatible** — Create ecosystem addons (1.20.1 Fabric, latest versions meeting the 6.0.8.1 floor): Steam 'n' Rails 1.7.2+, Copycats+ 3.0.8+, Create Deco 2.1.1, Slice & Dice 3.6.0, Enchantment Industry 2.5.2, Jetpack 4.4.2, Extended Cogwheels 2.1.1+, New Age, Ore Excavation, Power Loader 2.0.3, Diesel Generators 2.1.4, Interactive 1.2.1, Crystal Clear, Numismatics, Utilities, Contraption Terminals, Goggles 6.1.1, Trading Floor, Central Kitchen, Bells & Whistles, Interiors, Design n' Decor, Dreams & Desires, Copper & Zinc, Ultimine, Oxidized, Pattern Schematics, etc.

**Recipe viewer**: EMI/REI coexist without conflict, but only a **JEI** plugin is implemented — sifting recipes will **not** display in EMI/REI (JEI 15.20+ recommended).

**Not compatible**:

| Item | Type | Behavior |
| :--- | :--- | :--- |
| Create ≤ 0.5.1 | Dependency refusal | Loader refuses to start (not a crash) |
| Create 6.0.0 – 6.0.8 | Dependency refusal | Requires ≥ 6.0.8.1 |
| OptiFine / OptiFabric | Officially broken by Create | Incompatible on all versions |
| Old addons phased out by Create (railways<1.5.3, copycats≤1.1.1, slice&dice≤3.0.0, jetpack≤4.1.1, enchantment-industry<1.2.16, bigcannons≤0.5.3, diesel≤2.1.3, power-loader≤1.4.2, extendedgears≤2.1.0, interactive<1.1.0, garnished≤1.6.3, …) | Officially phased out | Install the latest version |
| Sodium<0.5.0 / Iris≤1.2.5 / Sound Physics<1.4.5 | Officially broken by Create | Use a newer version |
| Original createsifter 0.1.1 | Modid conflict | Cannot be installed together |

**Upgrade** — on Create 6.0.7–6.0.8, upgrade to **6.0.8.1**:
1. From `mods/`, delete the old `create-fabric-*.jar` and put in the new (6.0.8.1) jar.
2. Delete any standalone Flywheel, Ponder, Porting Lib (`porting_lib_*`), Registrate jars left in `mods/` — leftover legacy libraries are the most common source of errors in the Create 6 era.
3. Check whether other Create add-ons meet the new floor (see the incompatible table above, e.g. Railways ≥1.5.3, Copycats >1.1.1) and upgrade them together if needed.
4. Verify at launch: enter the game and a world, confirm no red errors in the log and add-ons work.
5. Enjoy your wonderful gaming time.

---

### License & credits

**LGPL-3.0**. Upstream © **oierbravo** (Forge original, LGPL-3.0) and **Shulej** (Fabric port); additions and modifications in this adaptation are likewise LGPL-3.0. Not an official release; provided **AS-IS without warranty** — no guarantee of future updates or long-term maintenance.

Thanks to **oierbravo**, **Shulej**, and the **Create** development team.

---

### Download & build

- Download: [GitHub Releases](https://github.com/jiesoon771/createsifter-createfabric6.0.8.1/releases) · [Modrinth](https://modrinth.com/mod/create-sifting-create-6-fabric) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-sifting-create-fabric-6-0-8-1/preview)
- Source (LGPL-3.0): [GitHub](https://github.com/jiesoon771/createsifter-createfabric6.0.8.1)
- Build from source: JDK 17+; place `create-fabric-6.0.8.1+build.1744-mc1.20.1.jar` and its nested jars in `libs/`, then run `gradlew.bat build` → `build/libs/createsifter-0.2.0+1.20.1.jar`. See `Porting-Notes-移植说明.md`.

---

## 中文文档

### 简介

机械动力：筛子为机械动力添加两种**可由动力驱动的筛子方块**与 **9 种筛网**。将沙砾、沙子、灵魂沙、泥土、粉碎玄武岩等方块投入筛子，配合不同等级的筛网，可筛出对应矿物与资源。

本模组是 Create Sifting 的**非官方 Fabric 维护分支**：原版由 **oierbravo** 开发（Forge），**Shulej** 移植到 Fabric，本适配版在 Shulej 移植版基础上更新，使其兼容 **Create 6.0.8.1**（该版本移除了旧移植版依赖的部分 API）。

---

### 关于本移植

> 此为**社区适配版**，**非**官方发布。

**官方原版（Forge）—— 感谢 [oierbravo](https://www.curseforge.com/minecraft/mc-mods/create-sifting)！**
**Shulej 的 Fabric 移植版 —— 感谢 [Shulej](https://modrinth.com/mod/create-sifting-fabric)！**

相较旧移植版的主要差异：

- 新增**图形化配置界面**（Mod Menu），针对该模组**平衡性长期存在的争议**提供**自定义平衡调整**：难度预设 + 逐配方覆盖，可自定义各配方的产出概率与处理时间；
- 从官方新版本**移植部分内容至 1.20.1**：耐固筛网、高级耐固筛网、粉碎玄武岩及下界合金相关筛选配方——官方原版 1.20.1 并无这些内容，下界合金配方直至 1.21 版本才加入。

---

### 功能特性

- **两种动能筛子**（动力筛子 + 黄铜动力筛子），保留熟悉的机械动力操作手感；
- **9 种筛网**（丝线、安山岩、锌、黄铜、自定义、耐固、高级黄铜、高级耐固、高级自定义）；
- **13 条内置筛分配方**、**9 条合成配方**与 **2 条玄武岩处理配方**；
- **9 种语言完整本地化**（简中 / 繁中 / 文言（华夏）/ 英 / 法 / 日 / 韩 / 俄 / 倒装英语），含配置界面；
- 客户端与服务端均可用，支持 **JEI**（15.20+）配方显示；
- 额外支持 **1.20.1 的下界合金筛选**（耐固 / 高级耐固筛网 + 粉碎玄武岩）。

---

### 添加的内容

#### 方块

| 方块 | 说明 |
| :--- | :--- |
| 动力筛子（Sifter） | 基础筛子，每次循环处理 **1** 个物品；应力影响 **4.0su**，最低转速 **1 RPM**，输出容量 **16 槽位** |
| 黄铜动力筛子（Brass Sifter） | 进阶筛子，每次循环处理 **8** 个物品；应力影响 **8.0su**，最低转速 **16 RPM**，输出容量 **64 槽位**；支持输出过滤与红石控制 |
| 粉碎玄武岩（Crushed Basalt） | 由粉碎轮/石磨研磨玄武岩获得，用于筛选下界资源 |
| 粉碎末地石（Crushed End Stone） | 无内置配方，供整合包开发者通过数据包添加自定义筛选配方 |
| 尘土（Dust） | 无内置配方，供整合包开发者通过数据包添加自定义筛选配方 |

#### 筛网（物品）

筛网是筛选配方的前提，均不可堆叠。共 9 种：

| 筛网 | 说明 |
| :--- | :--- |
| 线筛网（String Mesh） | 基础筛网 |
| 安山筛网（Andesite Mesh） | |
| 锌筛网（Zinc Mesh） | |
| 黄铜筛网（Brass Mesh） | |
| 自定义筛网（Custom Mesh） | 无内置配方，供数据包使用 |
| 耐固筛网（Sturdy Mesh） | **本适配版从官方 1.21 移植** |
| 高级黄铜筛网（Advanced Brass Mesh） | 高级筛网 |
| 高级耐固筛网（Advanced Sturdy Mesh） | **本适配版从官方 1.21 移植**，高级筛网 |
| 高级自定义筛网（Advanced Custom Mesh） | 高级筛网，无内置配方，供数据包使用 |

> **关于高级筛网**：决定能否执行高级配方的因素是筛子内安装的网，而非筛子种类。装有高级筛网的筛子只能执行需要高级筛网的配方；**普通动力筛子同样可以安装高级筛网**。黄铜动力筛子的优势在于单循环 8 个物品、输出过滤与红石控制，并非高级筛网专属。

#### 配方总览

内置 **13 条筛选配方**、**9 条合成配方**（2 条筛子 + 7 条筛网）与 **2 条玄武岩处理配方**（粉碎、磨粉）。完整配方可在游戏内通过 **JEI**（15.20+）查看。

---

### 机制与使用

#### 动力输入

筛子是动能方块（同时作为齿轮件参与传动），需从侧面通过齿轮等获得动力，转速低于最低要求时不运转（动力筛子默认最低转速 **1**，黄铜筛子 **16**）。实际加工速度随转速提高：每 tick 的进度推进量为 `clamp(|转速| ÷ 16, 1, 512)`，转速越高单周期耗时越短。

#### 基本操作

- **安装筛网**：手持筛网右键筛子，一次安装一个。
- **取下筛网**：空手按住 Shift 并右键，筛网返还背包。
- **投入物品**：将待筛方块投掷到筛子顶部会自动吸入；也可用漏斗、溜槽、黄铜漏斗等自动化设备输入（仅接受当前配方可处理的物品）。
- **取出产物**：空手右键取出全部产物；若输出区为空，则同时取回输入槽中的物品。破坏方块时，输入、筛网与产物全部掉落。

#### 黄铜动力筛子

- 每次循环处理 **8** 个物品（普通筛子为 1 个）。
- 支持**输出过滤**：可指定只保留特定产物，被过滤的产物会**掉落回世界**而非消失。
- 受到**红石信号**时暂停加工（不影响动能传动）。

#### 手持筛滤

手持筛网可长按手动筛滤：

- 另一只手持有可筛物品时，右键手持筛网即可开始筛滤；
- 或对准地面上的物品实体右键，拾取并筛滤；
- 站在液体（如水中）时视为含水状态；
- 一次筛滤消耗筛网 **1 点耐久**，筛出物直接进入背包（假玩家则掉落）。

#### 含水筛选

部分配方（如泥土）要求「含水」状态。将筛子放置在水源中（自动变为含水方块），或手持筛网时站在液体中，即视为含水。含水状态与配方不符时不会触发加工。

---

### 筛选配方

13 条筛选配方的**基础处理时间均为 500 tick（25 秒）**，实际时长受转速与难度时间倍率影响。表中概率为该配方下的单次掉落判定概率，同一配方可有多条独立判定。

#### 沙砾（Gravel）

| 筛网 | 产物与概率 |
| :--- | :--- |
| 安山筛网 | 铜粒 5% · 锌粒 1% · 铁粒 1% · 金粒 5% · 煤炭 10% · 燧石 10% |
| 锌筛网 | 铜粒 5% · 锌粒 2% · 铁粒 5% · 金粒 10% · 煤炭 10% · 青金石 5% · 燧石 10% · 经验颗粒 10% |
| 黄铜筛网 | 粉碎铜矿石 10% · 粉碎锌矿石 10% · 粉碎金矿石 5% · 粉碎铁矿石 10% · 青金石 10% · 煤炭 15% · 燧石 10% · 经验颗粒 10% |
| 高级黄铜筛网 | 粉碎铜矿石 10% · 粉碎锌矿石 10% · 粉碎金矿石 5% · 粉碎铁矿石 15% · 青金石 10% · 钻石 5% · 绿宝石 2% · 经验颗粒 10% |

#### 沙子（Sand）

| 筛网 | 产物与概率 |
| :--- | :--- |
| 线筛网 | 红石粉 5% · 骨粉 40% |
| 安山筛网 | 红石粉 10% · 骨粉 40% · 经验颗粒 10% |
| 锌筛网 | 红石粉 15% · 骨粉 40% · 经验颗粒 10% |
| 黄铜筛网 | 红石粉（×2）25% · 荧石粉 10% · 骨粉 40% · 烈焰粉 5% · 经验颗粒 20% |

#### 灵魂沙（Soul Sand）

| 筛网 | 产物与概率 |
| :--- | :--- |
| 黄铜筛网 | 下界石英 10% · 下界疣 5% · 经验颗粒 10% |
| 高级黄铜筛网 | 下界石英 45% · 下界石英 15% · 下界疣 10% · 恶魂之泪 5% · 经验颗粒 20% |

> 高级黄铜筛网的灵魂沙配方中，下界石英为 **45% 与 15% 两条独立判定**（非合并为 60% 单次判定）。

#### 泥土（Dirt，需含水）

| 筛网 | 产物与概率 |
| :--- | :--- |
| 线筛网 | 海带 20% · 海草 30% · 管珊瑚 5% · 脑纹珊瑚 5% · 气泡珊瑚 5% · 火珊瑚 5% · 鹿角珊瑚 5% |

#### 粉碎玄武岩（Crushed Basalt，本适配版新增下界合金链）

玄武岩 → 粉碎轮粉碎 / 石磨研磨 → 粉碎玄武岩 → 筛分。耐固筛网与高级耐固筛网为本适配版从官方新版移植至 1.20.1 的内容。

| 筛网 | 产物与概率 |
| :--- | :--- |
| 耐固筛网 | 远古残骸 2% · 下界合金碎片 1% · 经验颗粒 10% |
| 高级耐固筛网 | 远古残骸 5% · 下界合金碎片 2% · 经验颗粒 10% |

---

### 配置

通过 **Mod Menu → Config** 打开图形化配置界面（无需注册键位）。所有设置保存在服务端配置文件 `createsifter-server.toml`，专用服务器以服主设置为准并同步至客户端；单机则为本地存档/主机设置。

#### 难度预设

| 预设 | 产出倍率 | 时间倍率 |
| :--- | :---: | :---: |
| 超高（easiest） | ×1.5 | ×0.5 |
| 高（默认） | ×1.0 | ×1.0 |
| 中 | ×0.6 | ×1.5 |
| 低（hardest） | ×0.35 | ×2.0 |
| 自定义 | 可自由调节（产出 0.05–1.0，时间 0.1–10.0） | |

#### 逐配方覆盖

进入存档后，在配置界面的配方列表中点击任意配方，可单独修改每个产物的掉落概率（0–100%）与单次筛出数量，改动在物品掉落时即时生效。

#### 下界合金开关

`enableNetheriteSift`（默认开启）：本适配版新增的总开关。关闭后，下界合金系掉落（远古残骸、下界合金碎片）将不再产出，粉碎玄武岩配方仅保留经验颗粒产出。

#### 其他机械参数

- 应力影响（stress impact，单位 su）：动力筛子 4su、黄铜筛子 8su；
- 最低转速（minimumSpeed，单位 RPM）：动力筛子 1 RPM、黄铜筛子 16 RPM；
- 输出容量（outputCapacity，单位槽位）：动力筛子 16 槽位、黄铜筛子 64 槽位；
- 每周期处理数（itemsPerCycle，黄铜）：8；
- 渲染开关（客户端）：是否渲染被筛方块、是否渲染移动中的筛网。

---

### 本地化

内置 9 种语言文件**全部完整翻译**（含配置界面：物品、方块、难度预设、逐配方覆盖界面）：

| 语言文件 | 完成度 |
| :--- | :--- |
| `zh_cn.json` | 简体中文 — 完整翻译 |
| `zh_tw.json` | 繁體中文 — 完整翻譯 |
| `zh_hk.json` | 繁體中文 — 完整翻譯 |
| `lzh.json` | 文言（华夏）— 備譯 |
| `en_us.json` | English (US) — Complete |
| `en_ud.json` | ǝɹǝɥʇ sı ɥsılƃuƎ uʍopǝpᴉsdn — Complete |
| `fr_fr.json` | Français — Traduction complète |
| `ja_jp.json` | 日本語 — 完全な翻訳 |
| `ko_kr.json` | 한국어 — 완전한 번역 |
| `ru_ru.json` | Русский — Полный перевод |

---

### 运行要求

- Minecraft **1.20.1** · Fabric Loader **0.17.2+** · Fabric API **≥ 0.92.11+1.20.1**
- **Create Fabric ≥ 6.0.8.1+build.1744-mc1.20.1**（嵌套自带 Registrate / Ponder / Flywheel / Porting Lib）
- Java **17~21**

**服务器要求**：服务器与客户端使用同一份 jar 文件，配置由服务器主导。客户端同样需安装本模组、机械动力与 Fabric API，版本必须与服务器一致。

---

### 兼容性

以下数据基于 **Minecraft 1.20.1 · Create 6.0.8.1+ · Fabric** 环境整理。

#### 确认兼容（实测）

- **核心依赖**：Create 6.0.8.1+、Fabric API 0.92.11+、MC 1.20.1、Java 17/21（客户端与专用服务器均已实测，含 270+ 模组整合包加载共存验证）。
- **优化/渲染**：Sodium 0.5.13、Sodium Extra、Iris 1.7.6、Indium、Lithium、C2ME、ModernFix、FerriteCore、Krypton、CreateBetterFps、EntityCulling、Dynamic FPS、MemoryLeakFix。
- **功能/辅助**：JEI 15.20.0.134（配方正常显示）、Mod Menu 7.2.2、Cloth Config、Controlling、AppleSkin、BetterF3。
- **生态/内容**：暮色森林、Create: Addition 1.3.4、Create Big Cannons 5.11.4。
- **其他功能模组（加载共存验证通过）**：Carry On、Waystones、Traveler's Backpack、FTB Ultimine、Inventory Profiles Next、Mouse Tweaks、Double Doors、Falling Leaves、Hardcore Revival、Simply Swords、Chat Heads、Eating Animation、NotEnoughAnimations、IMBlocker、Resolution Control+、LambDynamicLights、Modern UI、Zoomify、TaCZ Refabricated、I18nUpdateMod、AmbientSounds、Presence Footsteps、Sound Physics Remastered、LAN Server Properties、FastQuit、Voice Chat、JeCharacters、Open Parties and Claims、Do a Barrel Roll、Chunky、Continuity、Supplementaries / Amendments（+ Moonlight）、Cull Less Leaves、Xaero's World Map、Terralith、YUNG's 系列、Lazydfu、GPUMemLeakFix、fast-ip-ping 等。

#### 理论兼容（基于官方兼容边界推导）

**Create 生态附属**（1.20.1 Fabric，最新版本满足 Create 6.0.8.1 下限）：Steam 'n' Rails 1.7.2+、Copycats+ 3.0.8+、Create Deco 2.1.1、Slice & Dice 3.6.0、Enchantment Industry 2.5.2、Jetpack 4.4.2、Extended Cogwheels 2.1.1+、New Age、Ore Excavation、Power Loader (Fabric) 2.0.3、Diesel Generators [Fabric] 2.1.4、Interactive 1.2.1、Crystal Clear、Numismatics、Utilities、Contraption Terminals、Goggles 6.1.1、Trading Floor、Central Kitchen、Bells & Whistles、Interiors、Design n' Decor、Dreams & Desires、Copper & Zinc、Ultimine、Oxidized、Pattern Schematics 等。

**常见优化/辅助/库模组**：Embeddium、Starlight、Cull Less Leaves、GPUMemLeakFix、Xaero 地图、JourneyMap、背包/伤害显示/语音/汉化类、Architectury、Kotlin for Fabric、YUNG's API、GeckoLib、Terralith、YUNG's 系列、农夫乐事系、Supplementaries（已实测）等。

**配方查看器**：EMI / REI 可共存不冲突，但本模组只实现 JEI 插件，筛子配方在 EMI / REI 中**不显示**（建议使用 JEI 15.20+）。

#### 兼容性未知

- 深度修改配方系统的模组（KubeJS 配方脚本、CraftTweaker 系）：无集成，理论共存，但加载行为可能被干预；
- 自定义渲染管线（Canvas 等）：与 Create 的 Flywheel 兼容性由 Create 侧决定；
- 深度集成动力网络 / 方块实体的 Create 附加（Interactive 具体交互、TFMG 等）。

#### 不兼容

| 项 | 类型 | 表现 |
| :--- | :--- | :--- |
| Create 0.5.1 及更早 | 依赖拒绝 | 加载器拒绝启动（非崩溃） |
| Create 6.0.0 ~ 6.0.8 | 依赖拒绝 | 需 ≥ 6.0.8.1 |
| OptiFine / OptiFabric | Create 官方不兼容 | 全版本不兼容 |
| 被 Create 淘汰的旧版附加（railways<1.5.3、copycats≤1.1.1、slice&dice≤3.0.0、jetpack≤4.1.1、enchantment-industry<1.2.16、bigcannons≤0.5.3、diesel≤2.1.3、power-loader≤1.4.2、extendedgears≤2.1.0、interactive<1.1.0、garnished≤1.6.3 等） | Create 官方淘汰 | 安装最新版 |
| Sodium<0.5.0 / Iris≤1.2.5 / Sound Physics<1.4.5 | Create 官方不兼容 | 使用新版 |
| 原版 createsifter 0.1.1 | 同 modid 冲突 | 不可同时安装 |

**升级提示**：若当前 Create 为 6.0.7~6.0.8，需升级到 **6.0.8.1**，建议按以下步骤操作：

1. 从 `mods/` 删除旧的 `create-fabric-*.jar`，放入新版（6.0.8.1）jar；
2. 删除 `mods/` 中残留的独立 Flywheel、Ponder、Porting Lib（`porting_lib_*`）、Registrate 等库文件——残留旧库是 Create 6 时代最常见的报错源头；
3. 检查其他 Create 附加是否满足新下限（见上文不兼容表，如 Railways ≥1.5.3、Copycats >1.1.1 等），如有需要一并升级；
4. 启动验证：进入游戏并进入存档，确认日志无红色报错、各附加功能正常；
5. 尽情享受游戏。

---

### 许可与致谢

> 最上游 **oierbravo** 采用 **LGPL-3.0**（Forge 版）；本适配版所基于的 Fabric 源码仓库（**Shulej**）声称 MIT。为最充分对齐上游授权链，**本适配版已开源，整体按 LGPL-3.0 发布**，并保留全部上游声明（随 jar 附带，见 `LICENSE.txt_createsifter`）。源码见 [GitHub](https://github.com/jiesoon771/createsifter-createfabric6.0.8.1)。

本项目按「原样（AS-IS）」提供，不附带任何形式的担保。已验证基本功能可用，但仍可能存在缺陷；作者不保证未来更新或长期维护，请自行承担使用风险。

感谢 **oierbravo**、**Shulej** 以及 **Create 开发团队**。

---

### 下载与构建

**下载（jar）**：

- [GitHub Releases](https://github.com/jiesoon771/createsifter-createfabric6.0.8.1/releases)
- [Modrinth](https://modrinth.com/mod/create-sifting-create-6-fabric)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create-sifting-create-fabric-6-0-8-1/preview)

**源码（已开源，LGPL-3.0）**：[GitHub](https://github.com/jiesoon771/createsifter-createfabric6.0.8.1)

**从源码构建**：JDK 17+；将 `create-fabric-6.0.8.1+build.1744-mc1.20.1.jar` 及其嵌套 jar 按 `libs/` 布局放置后，执行 `gradlew.bat build`，产物生成于 `build/libs/createsifter-V1.0.3.jar`。详见 `Porting-Notes-移植说明.md`。
