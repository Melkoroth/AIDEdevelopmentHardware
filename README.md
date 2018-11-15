# AIDEdevelopmentHardware

## JAVA MQTT Test
At the moment the topics used are:
* ESP publishes to *pubESP*
* ESP subscribes to *subESP*
* JAVA publishes to *subESP*

### Running broker
Install mosquitto and in a terminal run:
```bash
mosquitto -p 1986 -v
```
### Monitoring Topics
You can also use mosquitto_sub to watch a specific topic:
```bash
mosquitto_sub -h localhost -p 1986 -t topicName
```
### How it works
By default ESP publishes every 5 seconds to *pubESP* and any message sent to *subESP* is written to serial.

To send a message from JAVA:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass=MqttPublishSample
```
You should see message info in mosquitto's screen and in serial monitor.

## ESP8266 Info
A Wemos D1 Mini Pro is being used for testing.