package phat;

import java.io.*;
import java.io.IOException;

import io.silverspoon.bulldog.beagleboneblack.*;
import io.silverspoon.bulldog.core.gpio.DigitalInput;
import io.silverspoon.bulldog.core.gpio.DigitalOutput;
import io.silverspoon.bulldog.core.platform.Board;
import io.silverspoon.bulldog.core.platform.Platform;
import io.silverspoon.bulldog.core.util.BulldogUtil;

public class BeaglePresenceSensor {
    static HardwareLink hwLink = new HardwareLink();

    private static String LED3_PATH = "/sys/class/leds/beaglebone:green:usr3";

    public static void main(String[] args) throws IOException {
        Board board = Platform.createBoard();

        DigitalOutput led = (DigitalOutput) board.getPinByAlias("P8_8");

        //Start link to hardware
        hwLink.startHardwareLink();

        /*writeLED("/trigger", "timer", LED3_PATH);
        writeLED("/delay_on", "50", LED3_PATH);
        writeLED("/delay_off", "50", LED3_PATH);*/

        hwLink.initWarnSequence();

        led.high();
        BulldogUtil.sleepMs(1000);
        led.low();
        BulldogUtil.sleepMs(1000);
        led.high();
        BulldogUtil.sleepMs(1000);
        led.low();
        BulldogUtil.sleepMs(1000);
        led.high();

        /*System.out.println("Starting the LED Java Application");
        if(args.length!=1) {
            System.out.println("There are an incorrect number of arguments.");
            System.out.println("  usage is: LEDExample command");
            System.out.println("where command is one of on, off, flash or status.");
            System.exit(2);
        }
        if (args[0].equalsIgnoreCase("On") || args[0].equalsIgnoreCase("Off")){
            System.out.println("Turning the LED " + args[0]);
            removeTrigger();
            writeLED("/brightness", args[0].equalsIgnoreCase("On")? "1":"0", LED3_PATH);
        }
        else if (args[0].equalsIgnoreCase("flash")){
            System.out.println("Flashing the LED");
            writeLED("/trigger", "timer", LED3_PATH);
            writeLED("/delay_on", "50", LED3_PATH);
            writeLED("/delay_off", "50", LED3_PATH);
        }
        else if (args[0].equalsIgnoreCase("status")){
            try{
                BufferedReader br = new BufferedReader(new FileReader(LED3_PATH+"/trigger"));
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                br.close();
            }
            catch(IOException e){
                System.err.println("Failed to access the BBB Sysfs file: /trigger");
            }
        }
        else {
            System.out.println("Invalid command");
        }*/

    }

    private static void writeLED(String filename, String value, String path){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter (path+filename));
            bw.write(value);
            bw.close();
        }
        catch(IOException e){
            System.err.println("Failed to access the BBB Sysfs file: " + filename);
        }
    }

    private static void removeTrigger(){
        writeLED("/trigger", "none", LED3_PATH);
    }
}