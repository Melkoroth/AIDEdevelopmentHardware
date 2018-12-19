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

## mqttJavaServer
[![OpenJDK Version](https://img.shields.io/badge/openjdk-v1.8-red.svg)](http://openjdk.java.net/)
[![Maven Version](https://img.shields.io/badge/maven-v3.1.1-orange.svg)](http://maven.apache.org/)

This is an example of usage of the MQTT Broker JAVA library.

## serialJava
[![OpenJDK Version](https://img.shields.io/badge/openjdk-v1.8-red.svg)](http://openjdk.java.net/)
[![Maven Version](https://img.shields.io/badge/maven-v3.1.1-orange.svg)](http://maven.apache.org/)

This is an example of using JAVA's serial library

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

## beagleBoneHardware
[![Python Version](https://img.shields.io/badge/python-v3.6-red.svg)](https://www.python.org/downloads/release/python-360/)

This example implements a MQTT client in Python which runs in the Beaglebone Black. The GPIO part is untested in the BB Black. In the BB Black Wireless it was unable to run.

