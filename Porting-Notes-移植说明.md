# Create Sifting（机械动力：筛子）—— Create 6.0.8.1 移植版 Porting Notes

- 版本 / Version：**V1.0.3**（Fabric / Minecraft 1.20.1）
- 移植作者 / Port author：**jiesoon771**
- 基于 / Based on：原版 [Shulej/createsifter](https://github.com/Shulej/createsifter) 0.1.1+1.20.1（最早源头为 oierbravo 的 LGPL-3.0 Forge 原版），移植到 **Create Fabric 6.0.8.1+build.1744-mc1.20.1**
  （The original [Shulej/createsifter](https://github.com/Shulej/createsifter) 0.1.1+1.20.1, whose earliest source is oierbravo's LGPL-3.0 Forge original, ported to **Create Fabric 6.0.8.1+build.1744-mc1.20.1**）

---

## Table of Contents / 目录

### [English](#english) · [中文](#中文)

| English | 中文 |
|---|---|
| [1. Background](#1-background) | [一、背景](#一背景) |
| [2. Major Changes](#2-major-changes) | [二、主要改动](#二主要改动相对原版-011-源码) |
| [3. Create Version Compatibility](#3-create-version-compatibility) | [三、Create 版本兼容性](#三create-版本兼容性) |
| [4. Installation](#4-installation) | [四、安装](#四安装直接使用) |
| [5. Building from Source](#5-building-from-source) | [五、从源码构建](#五从源码构建) |
| [6. Verification Done](#6-verification-done) | [六、已做的验证](#六已做的验证) |
| [7. File Structure](#7-file-structure) | [七、文件结构](#七文件结构) |
| [8. License & Copyright](#8-license--copyright) | [八、许可与版权](#八许可与版权) |

---

## English

### 1. Background

- The original 0.1.1 was compiled against the Create 0.5.1 API. Create 6.0 removed a large batch of classes it depended on (`Lang`→`CreateLang`/catnip, `VecHelper`/`Pair` moved into the catnip library, `BlockStressDefaults`→`BlockStressValues`, the gear-rendering Instance→Visual migration, Ponder extracted into its own library, etc.), so loading 0.1.1 directly would crash.
- The author promised Create 6+ support in GitHub Issue #4 but has never released it, and there is no other Create-6-compatible sifter mod on Modrinth either.
- The port is therefore built on the open-source code (earliest source: the LGPL-3.0 Forge original). Core content and gameplay — kinetic sifter / brass sifter, 6 basic meshes + 3 advanced meshes, handheld sifting, 24 built-in recipes — match the original.

### 2. Major Changes (relative to the original 0.1.1 source)

| Module | Change |
|---|---|
| Utility library | `com.simibubi.create.foundation.utility.Lang/VecHelper/Pair/Iterate/Pointing/AnimationTickHolder` → `net.createmod.catnip.*` |
| Rendering | `CachedBufferer`→`CachedBuffers`, `SuperByteBuffer`→catnip, `TransformStack.cast`→`TransformStack.of`, `PartialModel`→Flywheel 1.0 (`PartialModel.of`), `FluidRenderer.renderFluidBox`→`CatnipServices.FLUID_RENDERER` |
| Stress | `BlockStressDefaults.setImpact` (removed) → `BlockStressValues.IMPACTS.registerProvider(...)` (the new addon API added in Create 6) |
| Gear visuals | `SifterCogInstance/BrassSifterCogInstance` (SingleRotatingInstance, removed) → `SingleAxisRotatingVisual.of(...)` |
| Block-entity registration | `.instance(...)` → `.visual(...)` |
| Ponder | Registration switched to the standalone Ponder library's `PonderPlugin` (`PonderIndex.addPlugin` at client init); scenes use the 1.0.91 API (`world()`/`overlay()`/`util.select()`); removed deleted `setKineticSpeed`/`createItemOnBelt` |
| JEI compat | Kept, upgraded to compile against JEI 15.49 (runtime-compatible with JEI 15.20+, tested with 15.20.0.134) |
| KubeJS / CraftTweaker compat | Removed (dependency resolution failed and it is unused; the original compat code also only targeted Create 0.5.1) |
| Data generation | Removed the `fabric-datagen` entry point and datagen classes; the 24 recipes are baked in as static JSON (verified to all load on a dedicated server) |
| Config | Kept as-is (ForgeConfigSpec + forgeconfigapiport, provided nested by Create 6.0.8.1) |
| Language files | All **9 languages (10 files: `en_us` / `en_ud` / `zh_cn` / `zh_tw` / `zh_hk` / `lzh` / `ja_jp` / `ko_kr` / `fr_fr` / `ru_ru`)** completed with key sets and placeholders aligned to English |
| Bytecode | Compiled with `--release 17` (Java 17 bytecode); `fabric.mod.json` declares `java >=17`; verified to run on both Java 17 and Java 21 |

### 3. Create Version Compatibility

| Create version | Status | Notes |
|---|---|---|
| **6.0.8.1** (current instance version) | ✅ Tested | Verified on client (lite / 270-mod pack) + dedicated server: recipes, config and rendering all work |
| **6.0.8.1 and later** (future 6.0.9, 6.1.x, …) | ✅ Declared | `fabric.mod.json` declares `"create": ">=6.0.8.1+build.1744-mc1.20.1"`; newer versions pass loader validation directly, and minor versions of the same line are usually API-compatible |
| **6.0.0 – 6.0.8** | ❌ Unsupported | Requires `create >=6.0.8.1`; below that the loader refuses to start (not a crash) — upgrade to 6.0.8.1 |
| **0.5.1 and earlier** | ❌ Unsupported | This is exactly why the port exists — the original 0.1.1 is meant for 0.5.1, and the two API sets are incompatible |

### 4. Installation (Direct Use)

1. Built jar: the repo does not ship a jar directly — download a release from the Modrinth page, or build it yourself (see §5; `gradlew.bat build` produces `build/libs/createsifter-V1.0.3.jar`).
2. Copy it into the instance's `mods/` folder, **replacing the old `createsifter-0.1.1+1.20.1.jar` (delete the old file first)**.
3. Requirements:
   - Minecraft 1.20.1 (Fabric Loader **0.17.2+**)
   - Fabric API ≥ 0.92.11+1.20.1
   - **Create Fabric ≥ 6.0.8.1+build.1744-mc1.20.1** (nests Registrate / Ponder / Flywheel / Porting Lib)
   - Java **17 or higher** (both 17 and 21 tested)
   - JEI optional (if installed, 15.20+ shows the sifting recipe category)

### 5. Building from Source

Prerequisites: JDK 17+, working network access.

1. Put `create-fabric-6.0.8.1+build.1744-mc1.20.1.jar` and all of its `META-INF/jars/` nested jars into the `libs/` directory:
   - Simply copy the instance's `[机械动力] create-fabric-6.0.8.1+build.1744-mc1.20.1.jar` from the mods folder to `libs/create-fabric-6.0.8.1.jar`;
   - Unzip that jar and place its `META-INF/jars/*.jar` files (22) together with their own nested `META-INF/jars/*.jar` (second level, ~5 2.3.13 modules) into `libs/nested/`. The build script uses:
     - `libs/create-fabric-6.0.8.1.jar`
     - Under `libs/nested/`: Ponder, Registrate, Flywheel, base, transfer, porting_lib_core/fluids/utility/data/common, forgeconfigapiport, etc. (this repo's `libs/` already ships the complete dependency layout and can be referenced directly).
2. Run from the command line:
   ```
   gradlew.bat build
   ```
   The built jar is output at `build/libs/createsifter-V1.0.3.jar`.

### 6. Verification Done

- Real client launch (title screen): both the CS-lite instance (Java 21) and the 270-mod pack (Java 17 / Java 21, with Sodium, Iris, JEI 15.20.0.134, Create 6.0.8.1) reached the game normally.
- Real dedicated-server launch: world loaded successfully; logs show every sifting recipe parsed.
- Config generated correctly: `sifter.stressImpact=4.0`, `brass_sifter.stressImpact=8.0`, etc.
- Loot tables for `dust` / `crushed_end_stone` / `crushed_basalt` and the `mineable/pickaxe` tag are filled in, and the blocks drop correctly when mined.

### 7. File Structure

```
createsifter/
├── .github/workflows/build.yml      ← CI auto build
├── src/main/java/io/github/shulej/createsifter/  ← full source
├── src/main/resources/              ← resources (lang, recipes, models, textures, etc.)
├── build.gradle / gradle.properties / settings.gradle   ← build scripts
├── gradlew / gradlew.bat / gradle/wrapper/   ← Gradle wrapper
├── README.md                        ← project notes (English-Chinese)
├── LICENSE.txt                      ← LGPL-3.0 (with porter addendum)
├── .editorconfig
├── .gitignore
└── Porting-Notes-移植说明.md         ← this document
```

> Note: the built jar (`createsifter-V1.0.3.jar`) is a build artifact and is never committed; run `gradlew.bat build` to generate it under `build/libs/`. A backup of the original 0.1.1 is not provided in the repo either.

### 8. License & Copyright

- The upstream code belongs to **oierbravo**; its original Forge version is released under **LGPL-3.0**; this repository as a whole is licensed under **LGPL-3.0**, with the full notice in `LICENSE.txt`.
- The parts this port adds/modifies may be freely used, modified and redistributed without crediting the porter; you only need to keep the upstream copyright notice in `LICENSE.txt` when redistributing.
- This version is provided "as is" without warranty, and the port author no longer actively maintains it. See `LICENSE.txt` and `README.md`.

---

## 中文

### 一、背景

- 原版 0.1.1 是按 Create 0.5.1 的 API 编译的。Create 6.0 移除了它依赖的一大批类（`Lang`→`CreateLang`/catnip、`VecHelper`/`Pair` 迁入 catnip 库、`BlockStressDefaults`→`BlockStressValues`、齿轮渲染 Instance→Visual 系统、Ponder 独立成库等），直接加载必崩。
- 作者在 GitHub Issue #4 承诺适配 Create 6+，但至今未发布；Modrinth 上也没有其他支持 Create 6 的筛子模组。
- 因此基于开源源码（最早源头为 LGPL-3.0）自行移植。核心内容与玩法（动力筛子/黄铜筛子、6 种基础筛网 + 3 种高级筛网、手持筛滤、24 个内置配方）与原版一致。

### 二、主要改动（相对原版 0.1.1 源码）

| 模块 | 改动 |
|---|---|
| 工具库 | `com.simibubi.create.foundation.utility.Lang/VecHelper/Pair/Iterate/Pointing/AnimationTickHolder` → `net.createmod.catnip.*` |
| 渲染 | `CachedBufferer`→`CachedBuffers`、`SuperByteBuffer`→catnip、`TransformStack.cast`→`TransformStack.of`、`PartialModel`→Flywheel 1.0（`PartialModel.of`）、`FluidRenderer.renderFluidBox`→`CatnipServices.FLUID_RENDERER` |
| 压力值 | `BlockStressDefaults.setImpact`（已删除）→ `BlockStressValues.IMPACTS.registerProvider(...)`（Create 6 新增的 addon API） |
| 齿轮视觉 | `SifterCogInstance/BrassSifterCogInstance`（SingleRotatingInstance，已删除）→ `SingleAxisRotatingVisual.of(...)` |
| 方块实体注册 | `.instance(...)` → `.visual(...)` |
| Ponder | 注册机制改为独立 Ponder 库的 `PonderPlugin`（客户端初始化时 `PonderIndex.addPlugin`）；场景改用 1.0.91 API（`world()`/`overlay()`/`util.select()` 等方法形式；移除已删除的 `setKineticSpeed`/`createItemOnBelt`） |
| JEI 兼容 | 保留，升级到 JEI 15.49 编译（运行时与 JEI 15.20+ 兼容，已实测 15.20.0.134） |
| KubeJS / CraftTweaker 兼容 | 已移除（依赖解析失败且不被使用；原版兼容代码同样只适配 Create 0.5.1） |
| 数据生成 | 移除 `fabric-datagen` 入口与数据生成类；24 个配方以静态 JSON 内置（已实测服务端全部加载） |
| 配置 | 原样保留（ForgeConfigSpec + forgeconfigapiport，由 Create 6.0.8.1 嵌套提供） |
| 语言文件 | **9 种语言（10 个文件：`en_us` / `en_ud` / `zh_cn` / `zh_tw` / `zh_hk` / `lzh` / `ja_jp` / `ko_kr` / `fr_fr` / `ru_ru`）**全部完整翻译，键集与占位符对齐英文 |
| 字节码 | 以 `--release 17` 编译（Java 17 字节码），`fabric.mod.json` 声明 `java >=17`；Java 17 / 21 均已实测可运行 |

### 三、Create 版本兼容性

| Create 版本 | 状态 | 说明 |
|---|---|---|
| **6.0.8.1**（实例当前版本） | ✅ 已实测 | 客户端（精简 / 270 模组整合包）+ 专用服务器均验证：配方、配置、渲染全部正常 |
| **6.0.8.1 及以上**（未来 6.0.9、6.1.x…） | ✅ 声明兼容 | `fabric.mod.json` 声明 `"create": ">=6.0.8.1+build.1744-mc1.20.1"`；更高版本可直接通过加载器校验，同系列小版本通常 API 兼容 |
| **6.0.0 ~ 6.0.8** | ❌ 不支持 | 本模组要求 `create >=6.0.8.1`，低于 6.0.8.1 加载器会拒绝启动（非崩溃），需升级到 6.0.8.1 |
| **0.5.1 及更早** | ❌ 不支持 | 这正是移植的原因——原版 0.1.1 才是面向 0.5.1 的，两套 API 完全不兼容 |

### 四、安装（直接使用）

1. 成品 jar：仓库不直接提供 jar，可从 Modrinth 页下载发布版，或自行构建（见第五节，`gradlew.bat build` 后产出 `build/libs/createsifter-V1.0.3.jar`）。
2. 复制到实例的 `mods/` 目录，**替换掉旧的 `createsifter-0.1.1+1.20.1.jar`（先删除旧文件）**。
3. 环境要求：
   - Minecraft 1.20.1（Fabric Loader **0.17.2+**）
   - Fabric API ≥ 0.92.11+1.20.1
   - **Create Fabric ≥ 6.0.8.1+build.1744-mc1.20.1**（嵌套自带 Registrate/Ponder/Flywheel/Porting Lib）
   - Java **17 或更高**（17 与 21 均已实测）
   - JEI 可选（装的话 15.20+ 即可显示筛子配方分类）

### 五、从源码构建

前置：JDK 17+、网络可用。

1. 将 `create-fabric-6.0.8.1+build.1744-mc1.20.1.jar` 及其全部 `META-INF/jars/` 嵌套 jar 放入 `libs/` 目录：
   - 直接把实例 mods 里的 `[机械动力] create-fabric-6.0.8.1+build.1744-mc1.20.1.jar` 复制为 `libs/create-fabric-6.0.8.1.jar`；
   - 解压该 jar，把其中 `META-INF/jars/*.jar`（22 个）以及它们各自再嵌套的 `META-INF/jars/*.jar`（二级，约 5 个 2.3.13 模块）一起放进 `libs/nested/`。构建脚本会用到：
     - `libs/create-fabric-6.0.8.1.jar`
     - `libs/nested/` 下的：Ponder、Registrate、Flywheel、base、transfer、porting_lib_core/fluids/utility/data/common、forgeconfigapiport 等（本仓库 `libs/` 已附带完整依赖布局，可直接引用）。
2. 命令行执行：
   ```
   gradlew.bat build
   ```
   成品输出在 `build/libs/createsifter-V1.0.3.jar`。

### 六、已做的验证

- 客户端真实启动（标题界面）：CS 精简实例（Java 21）与 270 模组整合包（Java 17 / Java 21，含 Sodium、Iris、JEI 15.20.0.134、Create 6.0.8.1）均正常进入游戏界面。
- 专用服务器真实启动：世界加载成功，日志加载全部筛子配方解析通过。
- 配置生成正确：`sifter.stressImpact=4.0`、`brass_sifter.stressImpact=8.0` 等。
- `dust` / `crushed_end_stone` / `crushed_basalt` 的 loot table 与 `mineable/pickaxe` 标签已补齐，挖掘可正常掉落。

### 七、文件结构

```
createsifter/
├── .github/workflows/build.yml      ← CI 自动构建
├── src/main/java/io/github/shulej/createsifter/  ← 完整源码
├── src/main/resources/              ← 资源（lang、recipes、models、textures 等）
├── build.gradle / gradle.properties / settings.gradle   ← 构建脚本
├── gradlew / gradlew.bat / gradle/wrapper/   ← Gradle wrapper
├── README.md                        ← 项目说明（中英对照）
├── LICENSE.txt                      ← LGPL-3.0（含移植者附加说明）
├── .editorconfig
├── .gitignore
└── Porting-Notes-移植说明.md      ← 本文档
```

> 注意：成品 jar（`createsifter-V1.0.3.jar`）是**构建产物**，不会入库；执行 `gradlew.bat build` 后生成于 `build/libs/` 下。原版 0.1.1 备份也不随仓库提供。

### 八、许可与版权

- 上游代码归 **oierbravo** 所有，其原始 Forge 版以 **LGPL-3.0** 发布；本仓库整体按 **LGPL-3.0** 授权，完整声明见 `LICENSE.txt`。
- 本移植新增/修改部分可自由使用、修改、再分发，无需向移植者署名；仅需在再分发时保留 `LICENSE.txt` 中的上游版权声明。
- 本版本按"现状"提供、不作担保，移植作者不再主动维护。详见 `LICENSE.txt` 与 `README.md`。