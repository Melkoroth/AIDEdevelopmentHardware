package phat;

import com.fazecast.jSerialComm.*;

import java.util.concurrent.TimeUnit;

public class SerialJava {
    static SerialPort circuitPlayground ;
    final static String triggerAlarmMessage = "a";
    final static String deactivateAlarmMessage = "d";

	public static void main(String[] args) {
		SerialPort ports[] = SerialPort.getCommPorts();
		for (int i = 0; i < ports.length; i++) {
		    System.out.println(ports[i].toString());
            System.out.println(ports[i].getDescriptivePortName());
            System.out.println(ports[i].getPortDescription());
            System.out.println(ports[i].getBaudRate());
            System.out.println(ports[i].getSystemPortName());
		    if (ports[i].toString().equalsIgnoreCase("Circuit Playground Expresso")) {
                circuitPlayground = SerialPort.getCommPort("/dev/" + ports[i].getSystemPortName());
            }

            circuitPlayground.openPort();
		    System.out.println(circuitPlayground.getBaudRate());
            triggerAlarm();
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deactivateAlarm();
        }
	}

	static private void triggerAlarm() {
        circuitPlayground.writeBytes(triggerAlarmMessage.getBytes(), 1);
    }

    static private void deactivateAlarm() {
        circuitPlayground.writeBytes(deactivateAlarmMessage.getBytes(), 1);
    }

}