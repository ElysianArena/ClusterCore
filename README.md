# ClusterCore

一个基于Redis的WaterdogPE群组服核心插件，使用AI辅助完成，支持跨服传送和服务器管理功能。

得益于Redis，ClusterCore能够支撑起大型群组服务器，一服一端或者一服多端都不在话下！
目前只支持Nukkit-MOT，其他平台请自行实现。

Ps: 如果是一服一端的设计，那么你需要使用setServerOpen(true)来控制服务器的开关状态，保证房间只在等待状态时允许加入。

## 🌟 项目简介

ClusterCore最初是专为ElysianArena服务器群组设计的核心工具，提供以下主要功能：

- 🔗 **跨服传送** - 玩家可以在不同服务器间自由传送
- 🔄 **服务器发现** - 自动发现和管理群组中的服务器
- 💾 **数据同步** - 通过Redis实现实时数据共享
- 🎮 **命令系统** - 简单易用的玩家命令

## 📦 项目结构

```
ClusterCore/
├── nukkit-mot/     # Nukkit-MOT服务器端插件
└── waterdogpe/     # WaterdogPE代理端插件
```

## ⚙️ 技术栈

- **语言**: Java 21
- **构建工具**: Maven
- **依赖管理**: Redis (Jedis 5.1.0)
- **游戏平台**: 
  - Nukkit-MOT (子服端)
  - WaterdogPE (代理端)

## 🚀 快速开始

### 1. 环境要求

- Java 21+
- Redis服务器
- Nukkit-MOT 或 WaterdogPE 服务器

### 2. 安装步骤

#### Nukkit-MOT端安装
1. 将编译后的jar包放入Nukkit服务器的`plugins`目录
2. 启动服务器生成配置文件
3. 编辑`plugins/ClusterCore/config.yml`配置Redis连接
4. 重启服务器

#### WaterdogPE端安装
1. 将编译后的jar包放入WaterdogPE的`plugins`目录
2. 启动代理服务器生成配置文件
3. 编辑`plugins/ClusterCore/config.yml`配置Redis连接
4. 重启代理服务器

### 3. 配置示例

**Nukkit-MOT配置 (`config.yml`)**:
```yaml
server:
  groupId: "lobby"      # 服务器分组ID
  wdpeId: "lobby-1"     # 服务器唯一标识
  maxPlayers: 100       # 最大玩家数

redis:
  host: localhost       # Redis地址
  port: 6379           # Redis端口
  password: ""         # Redis密码（可选）
```

**WaterdogPE配置 (`config.yml`)**:
```yaml
heartbeat:
  interval: 60         # 心跳间隔（秒）
  timeout: 5           # 心跳超时（秒）

redis:
  host: localhost      # Redis地址
  port: 6379          # Redis端口
  password: ""        # Redis密码（可选）
```

## 🎮 使用指南

### 玩家命令

#### `/hub` - 返回大厅服务器
- **功能**: 将玩家传送回大厅服务器
- **用法**: 直接输入`/hub`

#### `/wdt <分组ID>` - 传送到指定分组
- **权限**: `clustercore.wdt`
- **功能**: 查看并传送到指定分组的可用服务器
- **用法**: `/wdt survival` (传送到生存服务器组)

### API接口

对于开发者，可以通过API实现更多自定义功能：

#### 主要API方法

```java
// 获取API实例
ClusterAPI api = Main.getInstance().getAPI();

// 传送玩家到指定服务器
api.transferToServer(player, "survival-1");

// 传送玩家到指定分组（自动选择）
api.transferToGroup(player, "minigames");

// 传送玩家到大厅
api.transferToLobby(player);

// 获取可用服务器列表
CompletableFuture<JsonObject> servers = api.getAvailableServers("lobby");

// 控制服务器开关状态
api.setServerOpen(false);  // 关闭服务器
boolean isOpen = api.isServerOpen();  // 检查服务器状态
```

## 🔧 开发者指南

### 项目模块说明

- **nukkit-mot**: 运行在Nukkit子服上的插件模块
- **waterdogpe**: 运行在WaterdogPE代理上的插件模块

## 🤝 贡献指南

欢迎提交Issue和Pull Request来帮助改进项目！

## 📄 许可证

本项目采用MIT许可证。

## 🆘 常见问题

**Q: Redis连接失败怎么办？**
A: 检查Redis服务器是否正常运行，配置文件中的地址和端口是否正确。

**Q: 玩家无法传送怎么办？**
A: 确保目标服务器已在Redis中正确注册，检查网络连接是否正常。

**Q: 如何添加新的服务器分组？**
A: 在相应服务器的配置文件中设置不同的`groupId`即可。

---
