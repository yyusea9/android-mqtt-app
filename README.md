# MQTT Android App

一个完整的 Android MQTT 消息收发应用，支持通过 MQTT 协议连接到 Broker、发送和接收消息，并在界面上实时显示所有操作日志。

## 功能特性

✅ **MQTT 连接管理**
- 支持自定义 Broker 地址
- 一键连接/断开连接
- 自动重连机制

✅ **消息订阅**
- 订阅指定主题
- 实时接收消息

✅ **消息发送**
- 支持发送自定义消息
- QoS 级别为 1

✅ **实时日志显示**
- 所有操作都带时间戳
- 自动滚动到最新内容
- 支持清空日志

## 项目结构

```
android-mqtt-app/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/mqttapp/
│           │   ├── MainActivity.java           # 主界面 Activity
│           │   └── MqttClientHelper.java       # MQTT 客户端帮助类
│           ├── res/
│           │   ├── layout/
│           │   │   └── activity_main.xml       # 主界面布局
│           │   └── drawable/
│           │       └── edit_text_bg.xml        # EditText 背景样式
│           └── AndroidManifest.xml             # 应用清单
├── build.gradle                                 # 项目配置
└── README.md                                    # 项目说明
```

## 依赖库

- **androidx.appcompat** - Android 应用兼容性库
- **com.google.android.material** - Material Design 组件
- **androidx.constraintlayout** - 约束布局
- **org.eclipse.paho.client.mqttv3** - MQTT 3.1.1 客户端
- **org.eclipse.paho.android.service** - MQTT Android 服务

## 使用步骤

### 1. 打开应用

应用启动后，你会看到以下界面区域：

**连接配置区**
- Broker 地址（默认：tcp://broker.emqx.io:1883）
- 客户端 ID（自动生成）

**消息收发区**
- 主题输入框
- 消息输入框

**日志显示区**
- 实时显示所有操作日志

### 2. 连接到 MQTT Broker

1. 输入 Broker 地址（可使用默认地址）
2. 输入客户端 ID（可使用自动生成的 ID）
3. 点击 **连接** 按钮

预期日志输出：
```
[HH:mm:ss] [连接中...] tcp://broker.emqx.io:1883
[HH:mm:ss] ✓ MQTT 连接成功
```

### 3. 订阅主题

1. 输入要订阅的主题（例：test/topic）
2. 点击 **订阅** 按钮

预期日志输出：
```
[HH:mm:ss] [订阅中...] test/topic
[HH:mm:ss] [订阅成功] test/topic
```

### 4. 发送消息

1. 确保已连接到 Broker 和订阅了主题
2. 在消息输入框中输入消息内容
3. 点击 **发送** 按钮

预期日志输出：
```
[HH:mm:ss] [发送中...] test/topic -> Hello MQTT
[HH:mm:ss] [发送] test/topic ✓
```

### 5. 接收消息

当其他客户端向订阅的主题发送消息时，应用会自动显示在日志中：

```
[HH:mm:ss] [接收] test/topic -> 消息内容
```

## MQTT 测试工具

为了更好地测试应用，你可以使用以下工具：

### MQTT 客户端工具
- **MQTT.fx** - 图形化客户端（Windows/Mac/Linux）
- **MQTTX** - 现代化 MQTT 客户端（跨平台）
- **mosquitto** - 命令行工具

### 示例命令

```bash
# 订阅主题
mosquitto_sub -h broker.emqx.io -t test/topic

# 发布消息
mosquitto_pub -h broker.emqx.io -t test/topic -m "Hello from PC"
```

## 权限配置

应用已配置以下权限：
- `android.permission.INTERNET` - 网络连接
- `android.permission.ACCESS_NETWORK_STATE` - 网络状态检查
- `android.permission.WAKE_LOCK` - 保持后台连接

## 代码说明

### MainActivity.java
- 管理界面元素和用户交互
- 接收 MQTT 事件广播并更新日志

### MqttClientHelper.java
- 封装 MQTT 客户端逻辑
- 使用 BroadcastReceiver 与 MainActivity 通信
- 支持连接、订阅、发布、断开等操作

## 常见问题

**Q: 无法连接到 Broker？**
- 检查网络连接
- 确认 Broker 地址和端口是否正确
- 查看日志中的具体错误信息

**Q: 无法接收消息？**
- 确认已成功订阅主题
- 检查日志中是否有订阅成功的提示
- 尝试使用 MQTT 工具向同一主题发送消息

**Q: 应用闪退？**
- 查看 Android Studio 的 Logcat 输出
- 检查是否满足最低 API 级别（21）

## 许可证

MIT License

## 作者

Created by @yyusea9