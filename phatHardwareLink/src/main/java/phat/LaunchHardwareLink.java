package phat;

import java.io.IOException;

public class LaunchHardwareLink {
    static HardwareLink hwLink = new HardwareLink();

    public static void main(String[] args) throws IOException {
        //Start link to hardware
        hwLink.startHardwareLink();
    }
}