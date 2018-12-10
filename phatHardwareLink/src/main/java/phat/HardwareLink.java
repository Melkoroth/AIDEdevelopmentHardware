package phat;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HardwareLink {

    static MqttBroker broker = new MqttBroker();

    //MQTT settings
    int mqttQos = 2;
    static String mqttPort = "1986";
    static String mqttBroker = "tcp://localhost:" + mqttPort;
    static String mqttClientId = "PhatHardwareLink";
    static String mqttTopic = "presence";
    static MemoryPersistence mqttPersistence = new MemoryPersistence();
    static MqttClient mqttClient;
    static MqttConnectOptions connOpts;

    //TODO: Add Serial
    public static void startHardwareLink() {
        //Start MQTT server
        try {
            broker.startServer(mqttPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start MQTT client
        try {
            mqttClient = new MqttClient(mqttBroker, mqttClientId, mqttPersistence);
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

        } catch(MqttException me) {
            handleMQTTexception(me);
        }
    }

    public void warnHardware(String msg) {
        sendMQTTmessage(msg);
    }

    private void sendMQTTmessage(String msg) {
        try {
            //System.out.println("Connecting to broker: "+mqttBroker);
            mqttClient.connect(connOpts);
            //System.out.println("Connected");
            //System.out.println("Publishing message: "+msg);
            MqttMessage message = new MqttMessage(msg.getBytes(StandardCharsets.UTF_8));
            message.setQos(mqttQos);
            mqttClient.publish(mqttTopic, message);
            //System.out.println("Message published");
            mqttClient.disconnect();
            //System.out.println("Disconnected");
        } catch(MqttException me) {
            handleMQTTexception(me);
        }
    }

    private static void handleMQTTexception(MqttException me) {
        System.out.println("reason "+me.getReasonCode());
        System.out.println("msg "+me.getMessage());
        System.out.println("loc "+me.getLocalizedMessage());
        System.out.println("cause "+me.getCause());
        System.out.println("excep "+me);
        me.printStackTrace();
    }


}
