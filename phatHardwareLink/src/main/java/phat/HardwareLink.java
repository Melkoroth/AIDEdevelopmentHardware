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
 * Server as the intermediator between the presence sensors and the caregiver's hardware
 * @author melkoroth
 */
public class HardwareLink implements Runnable {

    //MQTT variables
    //private Server mqttBroker = new Server();
    private MqttBroker mqttBroker = new MqttBroker();
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
    private final String buttonPressedMessage = "b";
    private boolean serialOpened = false;

    //Logic variables
    //Time the caregiver has to react to the hardware warning
    private final long USERREACTIONTIMESECONDS = 10;
    private long lastPresenceTimestamp = 0;

    enum States { Waiting, Triggered, WButton, MQTT };
    private States actState = States.Waiting;

    //Thread that handles HW triggering
    //Triggers CircuitPlayground HW and waits for button press
    //If no button is pressed a MQTT message is sent to ESP8266
    @Override
    public void run() {
        while(true) {
            //Other state transitions not reflected here are:
            //initWarnSequence() sets to States.Triggered if in Wating
            //Serial event "b" sets States.Waiting if in Triggered
            if (actState.equals(States.Triggered)) {
                triggerSerialAlarm();
                lastPresenceTimestamp = System.currentTimeMillis() / 1000L;
                actState = States.WButton;
            } else if (actState.equals(States.WButton)) {
                //If time passes with no press we send mqtt warning
                if (((System.currentTimeMillis() / 1000L) - lastPresenceTimestamp) > USERREACTIONTIMESECONDS) {
                    actState = States.MQTT;
                }
            } else if (actState.equals(States.MQTT)) {
                sendMQTTmessage(mqttMessage);
                actState = States.Waiting;
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
            mqttBroker.startServer(mqttPort);
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
                    //alarmDeactivated = true;
                    if (actState.equals(States.WButton)) {
                        actState = States.Waiting;
                    }
                }
            }
        });

        //Start thread to check HW statuses
        Thread t = new Thread(this);
        t.start();

    }

    //Triggers flag to make run() trigger hardware
    public void initWarnSequence() {
        if (actState.equals(States.Waiting)) {
            actState = States.Triggered;
        }
    }

    private void triggerSerialAlarm() {
        if (serialOpened) {
            circuitPlayground.writeBytes(triggerAlarmMessage.getBytes(), 1);
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
