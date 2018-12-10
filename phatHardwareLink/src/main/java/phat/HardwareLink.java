package phat;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.fazecast.jSerialComm.*;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HardwareLink {

    //MQTT variables
    private static MqttBroker broker = new MqttBroker();
    private int mqttQos = 2;
    private static String mqttPort = "1986";
    private static String mqttBroker = "tcp://localhost:" + mqttPort;
    private static String mqttClientId = "PhatHardwareLink";
    private static String mqttTopic = "presence";
    private static MemoryPersistence mqttPersistence = new MemoryPersistence();
    private static MqttClient mqttClient;
    private static MqttConnectOptions connOpts;

    //Serial variables
    private static SerialPort circuitPlayground;
    private static String cpName = "Circuit Playground Expresso";
    private static String triggerAlarmMessage = "a";
    private static String deactivateAlarmMessage = "d";

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
        //Start serial port
        SerialPort ports[] = SerialPort.getCommPorts();
        for (int i = 0; i < ports.length; i++) {
            if (ports[i].toString().equalsIgnoreCase(cpName)) {
                circuitPlayground = SerialPort.getCommPort("/dev/" + ports[i].getSystemPortName());
            }

            circuitPlayground.openPort();
            System.out.println(circuitPlayground.getBaudRate());
            //TODO:
            //Whatr if Circuit python is not connected??
            /*triggerAlarm();
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deactivateAlarm();*/
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
