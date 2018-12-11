package phat;

import java.io.IOException;
import java.util.Properties;
import java.nio.charset.StandardCharsets;

import io.moquette.BrokerConstants;
import io.moquette.server.Server;
import static io.moquette.BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import com.fazecast.jSerialComm.*;

/**
 * Hardware Link
 * @author melkoroth
 */
public class HardwareLink implements Runnable {

    //MQTT variables
    private Server mqttBroker = new Server();
    private final int mqttQos = 2;
    private final String mqttPort = "1986";
    private final String mqttBrokerURL = "tcp://localhost:" + mqttPort;
    private final String mqttClientId = "JAVAHardwareLink";
    private final String mqttTopic = "presence";
    private final String mqttMessage = "Warning!";
    private final MemoryPersistence mqttPersistence = new MemoryPersistence();
    private MqttClient mqttClient;
    private MqttConnectOptions connOpts;

    //Serial variables
    private SerialPort circuitPlayground;
    private final int serialBauds = 115200;
    private final String cpName = "Circuit Playground Expresso";
    private final String triggerAlarmMessage = "a";
    private final String deactivateAlarmMessage = "d";
    private final String buttonPressedMessage = "d";
    private boolean serialOpened = false;

    //Logic variables
    private final long USERREACTIONTIMESECONDS = 5;
    private boolean alarmTriggered = false;
    private boolean alarmServiced = false;
    private boolean alarmDeactivated = false;
    private long lastPresenceTimestamp = 0;

    //Thread that handles HW triggering
    @Override
    public void run() {
        while(true) {
            //Chain of events starts when alarmTriggered
            if (alarmTriggered && !alarmServiced) {
                lastPresenceTimestamp = System.currentTimeMillis() / 1000L;
                alarmServiced = true;
                alarmDeactivated = false;
                triggerSerialAlarm();
                //sendMQTTmessage("Presence detected!");
                System.out.println("Alarm Triggered!");
            }

            //Alarm has been triggered and serviced so we check if time has passed to warn external agent
            //TODO: Protect against multiple calls!
            if (alarmTriggered && alarmServiced && !alarmDeactivated
                    && (((System.currentTimeMillis() / 1000L) - lastPresenceTimestamp) > USERREACTIONTIMESECONDS)) {
                sendMQTTmessage("Presence detected!");
            }

            //Caregiver has responded to call. No need to warn external agent
            if (alarmDeactivated) {
                alarmTriggered = false;
                alarmServiced = false;
                alarmDeactivated = false;
                lastPresenceTimestamp = 0;
            }

            //Sleeps thread
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Starts MQTT Broker, Client, Serial port and Thread
    public void startHardwareLink() {
        //Start MQTT Broker
        try {
            Properties mqttBrokerProps = new Properties();
            mqttBrokerProps.put(PERSISTENT_STORE_PROPERTY_NAME, mqttPersistence);
            mqttBrokerProps.put(BrokerConstants.PORT_PROPERTY_NAME, mqttPort);
            mqttBrokerProps.put(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, true);
            mqttBroker.startServer(mqttBrokerProps);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start MQTT client
        try {
            mqttClient = new MqttClient(mqttBrokerURL, mqttClientId, mqttPersistence);
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
        }
        circuitPlayground.setBaudRate(serialBauds);
        serialOpened = circuitPlayground.openPort();
        //Attach listener for incoming serial data
        circuitPlayground.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;
                byte[] newData = new byte[circuitPlayground.bytesAvailable()];
                int numRead = circuitPlayground.readBytes(newData, newData.length);
                //System.out.println("Read " + numRead + " bytes.");
                //System.out.println("Message: " + new String(newData));
                if (new String(newData).equalsIgnoreCase(buttonPressedMessage)) {
                    alarmDeactivated = true;
                }
            }
        });

        //Start thread to check HW statuses
        Thread t = new Thread(this);
        t.start();

    }

    //Triggers flag to make run() trigger hardware
    public void initWarnSequence() {
        alarmTriggered = true;
    }

    public void stopWarnSequence() {
        alarmTriggered = false;
    }

    private void triggerSerialAlarm() {
        if (serialOpened) {
            circuitPlayground.writeBytes(triggerAlarmMessage.getBytes(), 1);
        }
    }

    private void deactivateSerialAlarm() {
        if (serialOpened) {
            circuitPlayground.writeBytes(deactivateAlarmMessage.getBytes(), 1);
        }
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
