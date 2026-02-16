package com.example.mqttapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity {

    private EditText brokerUrlInput;
    private EditText topicInput;
    private EditText messageInput;
    private EditText clientIdInput;
    private TextView logTextView;
    private ScrollView logScrollView;
    
    private Button connectButton;
    private Button disconnectButton;
    private Button subscribeButton;
    private Button publishButton;
    private Button clearLogButton;

    private MqttClientHelper mqttClientHelper;
    private BroadcastReceiver mqttReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initMqtt();
        registerBroadcastReceiver();
    }

    private void initViews() {
        brokerUrlInput = findViewById(R.id.broker_url_input);
        topicInput = findViewById(R.id.topic_input);
        messageInput = findViewById(R.id.message_input);
        clientIdInput = findViewById(R.id.client_id_input);
        logTextView = findViewById(R.id.log_text_view);
        logScrollView = findViewById(R.id.log_scroll_view);

        connectButton = findViewById(R.id.connect_button);
        disconnectButton = findViewById(R.id.disconnect_button);
        subscribeButton = findViewById(R.id.subscribe_button);
        publishButton = findViewById(R.id.publish_button);
        clearLogButton = findViewById(R.id.clear_log_button);

        // 设置默认值
        brokerUrlInput.setText("tcp://broker.emqx.io:1883");
        clientIdInput.setText("android_client_" + System.currentTimeMillis());
        topicInput.setText("test/topic");

        connectButton.setOnClickListener(v -> connectToBroker());
        disconnectButton.setOnClickListener(v -> disconnect());
        subscribeButton.setOnClickListener(v -> subscribeTopic());
        publishButton.setOnClickListener(v -> publishMessage());
        clearLogButton.setOnClickListener(v -> clearLog());
    }

    private void initMqtt() {
        mqttClientHelper = new MqttClientHelper(this);
    }

    private void registerBroadcastReceiver() {
        mqttReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (MqttClientHelper.MQTT_ACTION_CONNECT_SUCCESS.equals(action)) {
                    addLog("✓ MQTT 连接成功");
                } else if (MqttClientHelper.MQTT_ACTION_CONNECT_FAILURE.equals(action)) {
                    String error = intent.getStringExtra("error");
                    addLog("✗ MQTT 连接失败: " + error);
                } else if (MqttClientHelper.MQTT_ACTION_MESSAGE_RECEIVED.equals(action)) {
                    String topic = intent.getStringExtra("topic");
                    String message = intent.getStringExtra("message");
                    addLog("[接收] " + topic + " -> " + message);
                } else if (MqttClientHelper.MQTT_ACTION_MESSAGE_PUBLISHED.equals(action)) {
                    String topic = intent.getStringExtra("topic");
                    addLog("[发送] " + topic + " ✓");
                } else if (MqttClientHelper.MQTT_ACTION_SUBSCRIBE_SUCCESS.equals(action)) {
                    String topic = intent.getStringExtra("topic");
                    addLog("[订阅成功] " + topic);
                } else if (MqttClientHelper.MQTT_ACTION_SUBSCRIBE_FAILURE.equals(action)) {
                    String topic = intent.getStringExtra("topic");
                    String error = intent.getStringExtra("error");
                    addLog("[订阅失败] " + topic + ": " + error);
                } else if (MqttClientHelper.MQTT_ACTION_DISCONNECT.equals(action)) {
                    addLog("✓ MQTT 已断开连接");
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(MqttClientHelper.MQTT_ACTION_CONNECT_SUCCESS);
        filter.addAction(MqttClientHelper.MQTT_ACTION_CONNECT_FAILURE);
        filter.addAction(MqttClientHelper.MQTT_ACTION_MESSAGE_RECEIVED);
        filter.addAction(MqttClientHelper.MQTT_ACTION_MESSAGE_PUBLISHED);
        filter.addAction(MqttClientHelper.MQTT_ACTION_SUBSCRIBE_SUCCESS);
        filter.addAction(MqttClientHelper.MQTT_ACTION_SUBSCRIBE_FAILURE);
        filter.addAction(MqttClientHelper.MQTT_ACTION_DISCONNECT);

        LocalBroadcastManager.getInstance(this).registerReceiver(mqttReceiver, filter);
    }

    private void connectToBroker() {
        String brokerUrl = brokerUrlInput.getText().toString().trim();
        String clientId = clientIdInput.getText().toString().trim();

        if (brokerUrl.isEmpty() || clientId.isEmpty()) {
            Toast.makeText(this, "请输入 Broker 地址和客户端 ID", Toast.LENGTH_SHORT).show();
            return;
        }

        addLog("[连接中...] " + brokerUrl);
        mqttClientHelper.connect(brokerUrl, clientId);
    }

    private void disconnect() {
        addLog("[断开连接中...]");
        mqttClientHelper.disconnect();
    }

    private void subscribeTopic() {
        String topic = topicInput.getText().toString().trim();
        if (topic.isEmpty()) {
            Toast.makeText(this, "请输入订阅主题", Toast.LENGTH_SHORT).show();
            return;
        }

        addLog("[订阅中...] " + topic);
        mqttClientHelper.subscribe(topic);
    }

    private void publishMessage() {
        String topic = topicInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();

        if (topic.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "请输入主题和消息", Toast.LENGTH_SHORT).show();
            return;
        }

        addLog("[发送中...] " + topic + " -> " + message);
        mqttClientHelper.publish(topic, message);
    }

    private void addLog(String log) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
        String logMessage = "[" + timestamp + "] " + log + "\n";
        
        runOnUiThread(() -> {
            logTextView.append(logMessage);
            logScrollView.post(() -> logScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    private void clearLog() {
        logTextView.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mqttReceiver);
        mqttClientHelper.disconnect();
    }
}