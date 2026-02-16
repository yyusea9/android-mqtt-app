package com.example.mqttapp;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttClientHelper {

    // Broadcast Actions
    public static final String MQTT_ACTION_CONNECT_SUCCESS = "mqtt_action_connect_success";
    public static final String MQTT_ACTION_CONNECT_FAILURE = "mqtt_action_connect_failure";
    public static final String MQTT_ACTION_MESSAGE_RECEIVED = "mqtt_action_message_received";
    public static final String MQTT_ACTION_MESSAGE_PUBLISHED = "mqtt_action_message_published";
    public static final String MQTT_ACTION_SUBSCRIBE_SUCCESS = "mqtt_action_subscribe_success";
    public static final String MQTT_ACTION_SUBSCRIBE_FAILURE = "mqtt_action_subscribe_failure";
    public static final String MQTT_ACTION_DISCONNECT = "mqtt_action_disconnect";

    private Context context;
    private MqttClient mqttClient;
    private LocalBroadcastManager broadcastManager;

    public MqttClientHelper(Context context) {
        this.context = context;
        this.broadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void connect(String brokerUrl, String clientId) {
        new Thread(() -> {
            try {
                mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
                
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setAutomaticReconnect(true);
                
                mqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        sendBroadcast(MQTT_ACTION_DISCONNECT, null, null);
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        String payload = new String(message.getPayload());
                        Intent intent = new Intent(MQTT_ACTION_MESSAGE_RECEIVED);
                        intent.putExtra("topic", topic);
                        intent.putExtra("message", payload);
                        broadcastManager.sendBroadcast(intent);
                    }

                    @Override
                    public void deliveryComplete(IMqttToken token) {
                    }
                });

                mqttClient.connect(options, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        sendBroadcast(MQTT_ACTION_CONNECT_SUCCESS, null, null);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        sendBroadcast(MQTT_ACTION_CONNECT_FAILURE, null, exception.getMessage());
                    }
                });
            } catch (MqttException e) {
                sendBroadcast(MQTT_ACTION_CONNECT_FAILURE, null, e.getMessage());
            }
        }).start();
    }

    public void disconnect() {
        new Thread(() -> {
            try {
                if (mqttClient != null && mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void subscribe(String topic) {
        new Thread(() -> {
            try {
                if (mqttClient != null && mqttClient.isConnected()) {
                    mqttClient.subscribe(topic, 1, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Intent intent = new Intent(MQTT_ACTION_SUBSCRIBE_SUCCESS);
                            intent.putExtra("topic", topic);
                            broadcastManager.sendBroadcast(intent);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Intent intent = new Intent(MQTT_ACTION_SUBSCRIBE_FAILURE);
                            intent.putExtra("topic", topic);
                            intent.putExtra("error", exception.getMessage());
                            broadcastManager.sendBroadcast(intent);
                        }
                    });
                }
            } catch (MqttException e) {
                Intent intent = new Intent(MQTT_ACTION_SUBSCRIBE_FAILURE);
                intent.putExtra("topic", topic);
                intent.putExtra("error", e.getMessage());
                broadcastManager.sendBroadcast(intent);
            }
        }).start();
    }

    public void publish(String topic, String message) {
        new Thread(() -> {
            try {
                if (mqttClient != null && mqttClient.isConnected()) {
                    mqttClient.publish(topic, message.getBytes(), 1, false, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Intent intent = new Intent(MQTT_ACTION_MESSAGE_PUBLISHED);
                            intent.putExtra("topic", topic);
                            broadcastManager.sendBroadcast(intent);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            sendBroadcast(MQTT_ACTION_MESSAGE_PUBLISHED, topic, exception.getMessage());
                        }
                    });
                }
            } catch (MqttException e) {
                sendBroadcast(MQTT_ACTION_MESSAGE_PUBLISHED, topic, e.getMessage());
            }
        }).start();
    }

    private void sendBroadcast(String action, String topic, String error) {
        Intent intent = new Intent(action);
        if (topic != null) {
            intent.putExtra("topic", topic);
        }
        if (error != null) {
            intent.putExtra("error", error);
        }
        broadcastManager.sendBroadcast(intent);
    }
}