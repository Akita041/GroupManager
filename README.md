# GroupManager

> 基于 [ElgarL/GroupManager](https://github.com/ElgarL/GroupManager) 官方 **v2.9** 版本 Fork 的二次改进构建。
> 仅供个人服务器使用，遵循上游 GPL-3.0 许可证开源。

## 声明

- 本项目 Fork 自 GroupManager 官方仓库的 **v2.9** 标签（2020-11-18 发布的 2.x 线最终版），在完整保留其代码结构、权限语义与数据格式的前提下继续维护和改进。
- **本项目为非官方构建，与上游 ElgarL 及原开发团队无从属或背书关系**；如需官方版本请访问上游仓库。
- 原始版权归属于 AnjoCaido、Gabriel Couto、ElgarL 及各贡献者；本仓库的全部修改同样以 **GPL-3.0** 发布（见 [LICENSE.md](LICENSE.md)）。
- 仅供个人学习与个人服务器使用，不提供任何担保。

## 构建

```bash
./gradlew clean build
```

- 需要 JDK 21
- 首次构建需联网拉取依赖（SpigotMC / Maven Central / PlaceholderAPI 仓库）

## 使用

- 服务端：Spigot / Paper **1.21.1+**（需 Java 21 运行）
- 将 jar 放入 `plugins/` 目录后重启
- 首次运行生成 `plugins/GroupManager/config.yml`，mirrors 会按当前服务器世界自动生成
- 指令（`/manuadd`、`/manpromote`、`/manload` …）与权限节点（`groupmanager.*`）与经典 GroupManager 完全一致
- 完整使用文档见上游 Wiki：<https://elgarl.github.io/GroupManager/>

## 插件开发者兼容性

API 包名与类结构未变（`org.anjocaido.groupmanager`），以 GMHook 方式依赖 GroupManager 的插件无需修改：

```java
GroupManager gm = (GroupManager) Bukkit.getPluginManager().getPlugin("GroupManager");
AnjoPermissionsHandler handler = gm.getWorldsHolder().getWorldPermissions(player);
String group = handler.getGroup(player.getName());
```

## 许可证

[GPL-3.0](LICENSE.md) —— 沿袭上游许可证。GroupManager 原版权益归 AnjoCaido / Gabriel Couto / ElgarL 所有。
