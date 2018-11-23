package aide.hardware.mqttTest;

import io.moquette.BrokerConstants;
import io.moquette.server.Server;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.nio.charset.StandardCharsets;

import static io.moquette.BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME;

public class MqttBrokerSample {

    static MqttClientPersistence s_dataStore;
    static MqttClientPersistence s_pubDataStore;

    private MqttCallback mqttCallback;

    static Server m_server;

    protected static void startServer() throws IOException {
        System.out.println("Starting broker server");
        String tmpDir = System.getProperty("java.io.tmpdir");
        s_dataStore = new MqttDefaultFilePersistence(tmpDir);
        s_pubDataStore = new MqttDefaultFilePersistence(tmpDir + File.separator + "publisher");

        Properties m_properties = new Properties();
        m_properties.put(PERSISTENT_STORE_PROPERTY_NAME, s_pubDataStore);
        m_properties.put(BrokerConstants.PORT_PROPERTY_NAME, "1986");
        // m_properties.put(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(BrokerConstants.PORT));
        // m_properties.put(BrokerConstants.HOST_PROPERTY_NAME, BrokerConstants.HOST);
        // m_properties.put(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME,
        // Integer.toString(BrokerConstants.WEBSOCKET_PORT));
        // m_properties.put(BrokerConstants.PASSWORD_FILE_PROPERTY_NAME, "");
        //m_properties.put(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME,
        //BrokerConstants.DEFAULT_PERSISTENT_PATH);
        m_properties.put(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, true);
        // m_properties.put(BrokerConstants.AUTHENTICATOR_CLASS_NAME, "");
        // m_properties.put(BrokerConstants.AUTHORIZATOR_CLASS_NAME, "");

        m_server = new Server();
        m_server.startServer(m_properties);
    }

    public static void main(String[] args) {

        try {
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String topic        = "subESP";
        String content      = "JAVA Mqtt Publish Sample";
        //QoS meaning for MQTT:
        //QoS 0 - Message is delivered at most once
        //QoS 1 - At least once
        //QoS 2 - Exactly once 
        //Higher QoS lowers performance
        int qos             = 2;
        String broker       = "tcp://localhost:1986";
        String clientId     = "JavaSampleClient";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Subscribing to topic: "+topic);
            sampleClient.subscribe(topic);

            MqttCallback mqttCallback = new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("Lost connection to Broker");
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    System.out.println("--> Message arrived on topic [" + s + "] ID: " + mqttMessage.getId() + " QoS: " + mqttMessage.getQos() + " Payload: " + mqttMessage.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    System.out.println("Message published successfully");
                }
            };
            sampleClient.setCallback(mqttCallback);

            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes(StandardCharsets.UTF_8));
            message.setQos(qos);
            sampleClient.publish(topic, message);
            //System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");

            System.out.println("Stopping server");
            m_server.stopServer();
            System.exit(0);

        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}