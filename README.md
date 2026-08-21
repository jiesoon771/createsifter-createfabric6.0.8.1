# Create Sifting (Fabric Port) — Create 6 Update

**Create 6 社区适配版 / A community adaptation for Create 6**

> **ENGLISH** · [中文](#中文说明)

An updated, community-maintained build of **Create Sifting** for the **Create 6** port on **Minecraft 1.20.1** (Fabric / Quilt).

---

## English

**Create Sifting** adds a simple sifter mechanic to the [Create](https://modrinth.com/mod/create) mod, inspired by the sieves of *Ex Nihilo*. It was originally written by [**oierbravo**](https://github.com/oierbravo) for Forge, and ported to Fabric by [**Shulej**](https://github.com/Shulej). This fork adapts Shulej's Fabric port to run on **Create 6 (6.0.8.1+)**, which removed several APIs the old build depended on.

> This is **not** an official release. It is a community build, provided in good faith for anyone who still plays Create 6 on 1.20.1.

### Features

- Kinetic **Sifter** and **Brass Sifter** with the familiar behaviors
- **7 meshes** (string, andesite, brass, zinc, custom, advanced …, as in the original port)
- **20 built-in sifting recipes**, server-verified
- Full Chinese & English localization
- Works on a dedicated server and with JEI (15.20+)

### Requirements

- Minecraft **1.20.1** · Fabric Loader **0.16.5+**
- Fabric API **≥ 0.92.11+1.20.1**
- **Create Fabric ≥ 6.0.8.1+build.1744-mc1.20.1**
- **Java 17+** (tested on 17 and 21)
- JEI optional (15.20+ to show sifting recipes)

### Create version compatibility

| Create version | Status | Notes |
|---|---|---|
| **6.0.8.1** (in the instance) | ✅ Tested | Client (minimal & 270-mod packs) and dedicated server verified: recipes, config, rendering all normal |
| **6.0.8.1 and above** (future 6.0.9, 6.1.x …) | ✅ Declared compatible | `fabric.mod.json` declares `"create": ">=6.0.8.1+build.1744-mc1.20.1"`; newer versions pass the loader check, and minor API drift is usually compatible |
| **6.0.0 – 6.0.7** | ⚠️ Not tested, not guaranteed | Compiled against the 6.0.8.1 API; early 6.0.x bundle older nested Ponder/Flywheel/Porting Lib versions and *may* miss classes at runtime — 6.0.8.1 is recommended |
| **0.5.1 or older** | ❌ Not supported | This is exactly why the port exists — the original 0.1.1 targets Create 0.5.1 and the two APIs are incompatible |

In short: **guaranteed on 6.0.8.1, declared compatible on 6.0.8.1+**. Minor Create updates (6.0.x) can be followed directly; if a future major bump (6.1+/7.x) breaks, a fresh recompile is needed.

### Build from source

```bash
gradlew.bat build
```

Output: `build/libs/createsifter-<version>.jar`. See `移植说明.md` for the exact `libs/` layout needed to compile.

### License, attribution & warranty

- Upstream code is by **oierbravo**, released under the **MIT License**; Shulej's Fabric port is likewise **MIT**. See [`LICENSE.txt`](LICENSE.txt).
- The code added or modified by **this port** is provided freely: use, modify, and redistribute it as you like — you do **not** need to credit the porter.
- **One obligation survives**: if a file contains upstream code, keep the MIT notice in `LICENSE.txt` when redistributing.
- Provided **"AS IS", without warranty** of any kind. The porter is **not actively maintaining** this build.

### Acknowledgements

Thanks to **oierbravo** for the original Create Sifting, **Shulej** for the Fabric port, and **the Create team** for their wonderful mod. This Create 6 adaptation was prepared by **jiesoon771**.

---

## 中文说明

**Create Sifting**（机械动力：筛子）为 [Create（机械动力）](https://modrinth.com/mod/create) 增加了一套简单易用的筛滤机制，灵感源自 *Ex Nihilo* 的筛子。原版由 [**oierbravo**](https://github.com/oierbravo) 为 Forge 编写，[**Shulej**](https://github.com/Shulej) 将其移植到 Fabric。本分支把 Shulej 的 Fabric 移植版适配到了 **Create 6（6.0.8.1+）**——旧版依赖的一批 API 在 Create 6 中已被移除，因此需要这次适配才能正常运行。

> 这是一个**社区版本**，并非官方发布。它是为仍在 1.20.1 上使用 Create 6 的朋友们善意提供的一份适配。

### 功能

- 动力筛子 / 黄铜筛子，机制与操作方式保持原样
- **7 种筛网**（线、安山岩、黄铜、锌、定制、进阶……与原移植版一致）
- **20 个内置筛滤配方**，已在服务端实测全部正常加载
- 中英文语言文件齐全
- 支持专用服务器，兼容 JEI（15.20+ 可显示筛子配方分类）

### 环境要求

- Minecraft **1.20.1** · Fabric Loader **0.16.5+**
- Fabric API **≥ 0.92.11+1.20.1**
- **Create Fabric ≥ 6.0.8.1+build.1744-mc1.20.1**
- **Java 17 或更高**（17 与 21 均已实测）
- JEI 可选（安装 15.20+ 即可查看筛子配方）

### Create 版本兼容性

| Create 版本 | 状态 | 说明 |
|---|---|---|
| **6.0.8.1**（实例当前版本） | ✅ 已实测 | 客户端（精简 / 270 模组整合包）+ 专用服务器均验证：配方、配置、渲染全部正常 |
| **6.0.8.1 及以上**（未来 6.0.9、6.1.x…） | ✅ 声明兼容 | `fabric.mod.json` 声明 `"create": ">=6.0.8.1+build.1744-mc1.20.1"`；更高版本可直接通过加载器校验，同系列小版本通常 API 兼容 |
| **6.0.0 ~ 6.0.7** | ⚠️ 未实测，不保证 | 代码按 6.0.8.1 的 API 编译；早期 6.0.x 嵌套的 Ponder/Flywheel/Porting Lib 版本不同，可能出现运行时缺类，建议直接用 6.0.8.1 |
| **0.5.1 及更早** | ❌ 不支持 | 这正是移植的原因——原版 0.1.1 才是面向 0.5.1 的，两套 API 完全不兼容 |

简单说：**保证能用的是 6.0.8.1，声明上兼容 6.0.8.1 及以上**。Create 更新小版本（6.0.x）可直接跟随；若将来升大版本（6.1+/7.x）报错，就需要重新编译适配一次。

### 从源码构建

```bash
gradlew.bat build
```

产物在 `build/libs/createsifter-<版本号>.jar`。编译所需的 `libs/` 布局与依赖细节见 [`移植说明.md`](移植说明.md)。

### 许可、署名与免责

- 上游代码归 **oierbravo** 所有，依 **MIT 许可**发布；Shulej 的 Fabric 移植版同样为 **MIT**。完整文本见 [`LICENSE.txt`](LICENSE.txt)。
- 本移植**新增或修改**的代码可自由使用：你可以随意使用、修改、再分发，**不必向移植者署名**。
- 但有**一条必须遵守**：只要文件里包含上游代码，再分发时就必须保留 `LICENSE.txt` 中的 MIT 版权声明——这是上游的要求，移植者无法替你免除。
- 本版本按**"现状"提供，不作任何担保**；移植作者**不再主动维护**。

### 致谢

感谢 **oierbravo** 创作了 Create Sifting，感谢 **Shulej** 完成了 Fabric 移植，也向贡献了如此优秀模组的 **Create 开发团队**致以敬意。本 Create 6 适配版本由 **jiesoon771** 完成。