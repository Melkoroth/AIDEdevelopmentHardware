package phat;

import com.fazecast.jSerialComm.*;

public class SerialJava {
    static SerialPort circuitPlayground ;

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
        }
	}

}