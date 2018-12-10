package phat;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import io.moquette.BrokerConstants;
import io.moquette.server.Server;
import static io.moquette.BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME;

import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;

public class MqttBroker {

    static MqttClientPersistence s_dataStore;
    static MqttClientPersistence s_pubDataStore;
    static Server m_server;

    protected static void startServer(String port) throws IOException {
        System.out.println("Starting broker server");
        String tmpDir = System.getProperty("java.io.tmpdir");
        s_dataStore = new MqttDefaultFilePersistence(tmpDir);
        s_pubDataStore = new MqttDefaultFilePersistence(tmpDir + File.separator + "publisher");

        Properties m_properties = new Properties();
        m_properties.put(PERSISTENT_STORE_PROPERTY_NAME, s_pubDataStore);
        m_properties.put(BrokerConstants.PORT_PROPERTY_NAME, port);
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
}