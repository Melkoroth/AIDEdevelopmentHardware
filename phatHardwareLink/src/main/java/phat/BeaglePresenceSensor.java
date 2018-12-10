package phat;

import phat.HardwareLink;

import java.io.IOException;

public class BeaglePresenceSensor {
    static HardwareLink hwLink = new HardwareLink();

    public static void main(String[] args) throws IOException {

        //Start link to hardware
        hwLink.startHardwareLink();

    }
}
