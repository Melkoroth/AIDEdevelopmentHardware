package phat;

import com.fazecast.jSerialComm.*;

import java.util.concurrent.TimeUnit;

public class SerialJava {
    private static SerialPort circuitPlayground;
    private static final int serialBauds = 115200;
    private static final String cpName = "Circuit Playground Expresso";
    private static final String triggerAlarmMessage = "a";
    private static final String deactivateAlarmMessage = "d";
    private static boolean serialOpened = false;

	public static void main(String[] args) {
        //Start serial port
        SerialPort ports[] = SerialPort.getCommPorts();
        for (int i = 0; i < ports.length; i++) {
            if (ports[i].toString().equalsIgnoreCase(cpName)) {
                circuitPlayground = SerialPort.getCommPort("/dev/" + ports[i].getSystemPortName());
            }
        }
        circuitPlayground.setBaudRate(serialBauds);
        serialOpened = circuitPlayground.openPort();
        triggerAlarm();

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
                System.out.println("Read " + numRead + " bytes.");
                System.out.println("Message: " + new String(newData));
            }
        });
        /*System.out.println("#########################");
        System.out.println(serialOpened);
        System.out.println(circuitPlayground.getBaudRate());
        System.out.println("#########################");*/
        //TODO:
        //What if Circuit python is not connected??
        /*triggerAlarm();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        deactivateAlarm();*/

        /*try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        deactivateAlarm();*/
	}

	static private void triggerAlarm() {
        circuitPlayground.writeBytes(triggerAlarmMessage.getBytes(), 1);
    }

    static private void deactivateAlarm() {
        circuitPlayground.writeBytes(deactivateAlarmMessage.getBytes(), 1);
    }

}