# Athena

基于 Vertx 创建的分布式 Game 服务器.

目标是能够搭建高可用 Game 服务器集群, 支持无缝横向扩展.

#### 包含模块有:
1. Http 模块.
2. WebSocket 网络连接管理模块.
3. 分布式 Player 管理中心模块.
4. 业务处理模块

#### 存储
目前使用了 MongoDB 作为存储, Redis 作为缓存.