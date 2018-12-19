# AIDEdevelopmentHardware
[![Build Status](https://travis-ci.com/Melkoroth/AIDEdevelopmentHardware.svg?token=jLXVWnBdCix3QQKg7rsP&branch=master)](https://travis-ci.com/Melkoroth/AIDEdevelopmentHardware)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f610aeff73444cc1a1192f05cad7a57e)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Melkoroth/AIDEdevelopmentHardware&amp;utm_campaign=Badge_Grade)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

This project stems from [AIDE for People with Neurodegenerative Diseases](http://grasiagroup.fdi.ucm.es/aidendd/) and tries to solve a problem the patients have by the use of custom-made hardware. 
The problem revolves around the idea that these kind of patients often get lost or fall when waking up at night to go to the bathroom. This has serious implications for them and for the caregiver, which has to be aware of any movements the patient performs at night so they can help them. This often results in lack of proper sleep for the caregivers.

The proposed solution is simple but effective. It consists of three devices: a presence sensor installed in the patient's room, a warning receiver for the caregiver which has the task of waking her/him up and another warning receiver that should be given to an outside party for redundancy.

As it is a derived project which uses [PHATSIM](https://github.com/Grasia/phatsim) we can test the developed hardware in the simulator before implementing it in real-life.

## Projected Experiments
1. Usage with PHATSIM's *virtual* motion detector.
2. Usage with a *real world* motion detector.

## System's chain of events
1. Presence is detected. This means that the patient has woken up at night and that the caregiver must be warned in order to help her/him.
2. Circuit Python receives the warning. This hardware lights up in red/orange and speaks out lout "Emergency, Juliet is up, emergency".
3. The system waits now for the caregiver to acknowledge the warning by the press of a button.
4. If the time passes by and the system doesn't detect the caregiver's interaction it will alert the secondary device, the ESP8266. This device can rest in an external location so we get redundancy in case of an emergency.

## phatHardwareLink
[![OpenJDK Version](https://img.shields.io/badge/openjdk-v1.8-red.svg)](http://openjdk.java.net/)
[![Maven Version](https://img.shields.io/badge/maven-v3.1.1-orange.svg)](http://maven.apache.org/)

This is the main JAVA project and the *brain* of the system. It consists of two main classes: **PhatPresenceSensor** and **BeaglePresenceSensor**. Each one of them cover one of the proposed experiments.

Both perform the same duties of managing a presence sensor. The difference is that in PHATSim the sensor is virtual whereas in the BeagleBone a real presence sensor is used.

For this, both call the same methods that init the **HardwareLink** which is used to communicate with the caregiver's hardware when the presence is detected.
The important methods this class has are these:
* startHardwareLink() inits class, MQTT broker, client and serial interfaces.
* initWarnSequence() is called when movement in the room is detected

## circuitPlaygroundHardware
[![C++ Arduino](https://img.shields.io/badge/c%2B%2B-Arduino%20-red.svg)](https://github.com/adafruit/Adafruit_CircuitPlayground)
[![Platformio Version](https://img.shields.io/badge/platformio-3.6.2-orange.svg)](https://platformio.org/)
[![Platform](https://img.shields.io/badge/platform-Atmel%20SAM-yellow.svg)](https://platformio.org/platforms/atmelsam)

This is a C++ project developed using Platformio framework for the Adafruit Hardware. No external hardware is used in this example, just the included sensors in the PCB.

Internally the hardware keeps waiting for the serial character 'a' which means that the alarm has been triggered.
When the alarm is triggered it will flash lights, speak out loud and wait for the caregiver to acknowledge the warning with the press of any button. Once the user has acknowledged the message a 'b' is returned through serial.

## espHardware
[![C++ Arduino](https://img.shields.io/badge/c%2B%2B-Arduino%20-red.svg)](https://github.com/adafruit/Adafruit_CircuitPlayground)
[![Platformio Version](https://img.shields.io/badge/platformio-3.6.2-orange.svg)](https://platformio.org/)
[![Platform](https://img.shields.io/badge/platform-ESP8266-yellow.svg)](https://platformio.org/platforms/espressif8266)

This is a C++ project developed using Platformio framework for the Espresiff's ESP8266 hardware. The other hardware required is a [Seeedstudio OLED screen](http://wiki.seeedstudio.com/Grove-OLED_Display_1.12inch/) and a LED.

Internally the hardware keeps waiting for a MQTT message sent through the *presence* topic. Once this message arrives the OLED screen displays a big warning sign with the received message and a LED is switched on.

This is a wifi-enabled device so you should check ssid & password settings in the .ino.

## Working with BeagleBone
[Setting up JAVA, Gradle and Maven](https://hackaday.io/page/1243-setting-up-oracle-java-8-gradle-and-maven-on-beaglebone-green)
 
Once plugged in through USB various communication interfaces are set up.
You can SSH for example to 192.168.7.2 with the following default parameters:
```bash
debian | temppwd
```
Once inside you have to set up Wi-Fi if it's needed:
```bash
sudo connmanctl
connmanctl> scan wifi
connmanctl> services
connmanctl> agent on
connmanctl> connect wifi_XXXXXXXXX_XXXXX
connmanctl> quit
ifconfig -a
```

You now need to clone the project (if it's not already there).

```bash
git clone https://github.com/Melkoroth/AIDEdevelopmentHardware.git
cd AIDEdevelopmentHardware/phatHardwareLink/target/
java -jar PhatHardwareLink-1.0.0-SNAPSHOT.jar
```
Be warned that **first time compiling takes 1+ hours**


## **---DEPRECATED---**

## Using the MQTT broker
MQTT needs a server installed to be used as a MQTT broker. You can use Ubuntu's included one, mosquitto, or an external provider just by changing IP address and port inside the code.

### Installation and usage
```bash
sudo apt update && sudo apt install mosquitto mosquitto-clients
```
To start broker run:
```bash
mosquitto -p 1986 -v
```
### Monitoring Topics
You can also use mosquitto_sub to watch a specific topic:
```bash
mosquitto_sub -h localhost -p 1986 -t topicName
```
### Generating messages
You can use mosquitto_pub to send a message to any topic:
```bash
mosquitto_pub -h localhost -t "anyTopic" -m "message"
```
## Topics used
*  Hardware subscribes to *alertTopic*
*  Software subscribes to *alertButtonTopic*

## Hardware libraries are included as git submodules
```bash
git submodule init
git submodule update
```

## JAVA, Maven and ANT install
Please refer to the "Getting Started" section of [AIDEdevelopment's README](https://github.com/Melkoroth/AIDEdevelopment/blob/master/README.md)

## mqttJava - MqttPublishSample
[![OpenJDK Version](https://img.shields.io/badge/openjdk-v1.8-red.svg)](http://openjdk.java.net/)
[![Maven Version](https://img.shields.io/badge/maven-v3.1.1-orange.svg)](http://maven.apache.org/)

This is an example of usage of the MQTT JAVA library.

If you need help installing the neccesary JAVA stuff, please refer to [PHATSIM's Readme](https://github.com/Grasia/phatsim) 

To send a test message from JAVA:
```bash
cd mqttJava
mvn clean compile
mvn exec:java -Dexec.mainClass=aide.harware.mqttTest.MqttPublishSample
```
You should see message info in mosquitto's screen and in serial monitor.

## mqttJava - MqttPublishSample
[![OpenJDK Version](https://img.shields.io/badge/openjdk-v1.8-red.svg)](http://openjdk.java.net/)
[![Maven Version](https://img.shields.io/badge/maven-v3.1.1-orange.svg)](http://maven.apache.org/)

This is an example of usage of the MQTT Broker JAVA library.

## basePhatSim 
[![OpenJDK Version](https://img.shields.io/badge/openjdk-v1.8-red.svg)](http://openjdk.java.net/)
[![Maven Version](https://img.shields.io/badge/maven-v3.1.1-orange.svg)](http://maven.apache.org/)
[![Ant Version](https://img.shields.io/badge/ant-v1.8.2-yellow.svg)](http://ant.apache.org/)

This example, developed using [SociAALML Editor](https://github.com/Grasia/sociaalml), was used as an example in which the presence detector and rest of the development will be added. From this main experiments were done.

To run the editor:
```bash
cd basePhatSim
mvn clean compile
ant edit
```

To watch the simulation:
```bash
cd basePhatSim
mvn clean compile
ant runSimPresence
```

## espHardware
By default ESP publishes every 5 seconds to *pubESP* and any message sent to *subESP* is written to serial.

## Why ESP8266?
A Wemos D1 Mini Pro is being used for the rapid prototyping of AAL solutions. The board is based in the ESP8266 chip with 16MB of on-board flash memory.

Some of its advantaged over other platforms include:
*  Cheap. These boards can be found for around 4$ from different manufacturers. There are other variants (D1 Mini) with less memory but the same capabilities for around 2$, but these were the ones we had around.
*  Open-source software. All the ecosystem is public and being actively developed in GitHub. It has a very big community made up of manufacturers and makers that has developed libraries for every imaginable hardware. This makes development cycles a breeze.
*  Libraries are available for different programming languages. The most used ones come from Arduino environment which is written in C++ but there's also the option to use MicroPython or even LUA by using the NodeMCU firmware. 
*  Open-source hardware. Schematics and PCB designs are also available in easy-to-modify standard formats able to be opened from KiCAD or EAGLECAD. There are all kind of derived products: Some have built-in battery controllers, others have the option of adding an external antenna... There are also shields to allow adding external hardware without the need to use any cables.
*  32-bit microcontroller, much more powerful than typical Arduino-compatibles which use ATMEGA's 8-bit architecture. They are not as powerful as to allow a Linux kernel inside, but this is usually not needed for Cloud-based IoT. 
*  Full Wifi stack. The board can act as an Access Point or as a Station and can switch from one mode to another while running with no external hardware needed. Other wifi-related advantages are the ability to do Over The Air programming or the rapid deployment of Mesh Networks.
*  Low-power consumption. [Real-world tests](https://openhomeautomation.net/esp8266-battery) measure less than 90mA while sending data over wifi and can go lower than 10mA while using the included sleep modes.
*  On-board USB to UART converter, so no external programmers are required. This also allows to easily debug over serial while prototyping.
*  Actively used by the industry in real-world scenarios. The prime example is iTead's sonoff line which is based on this chipset, but there are a million chinese and occidental manufacturers which are basing their IoT developments around it. This allows [community-made firmwares](https://github.com/xoseperez/espurna/wiki/Hardware) to be deployed in actual commercial products.

Once the rapid prototyping phase is over, the ESP8266 is available in other formats, like the ESP12, which allow direct deployment into final PCB's with little to none modifications to the schematics. 

They are also prepared to be soldered as a SMD component directly into PCB's, so no external headers are needed.

They are typically sold for around 1,5$, much less if bought in bulk. The cost-reduction comes from the fact that some components are removed, like the USB-UART converter which is typically not needed in a final product.



