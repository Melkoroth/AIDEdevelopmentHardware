# AIDEdevelopmentHardware

## JAVA MQTT Test
### To run example
Install mosquitto and in a terminal run:
```bash
mosquitto -p 1986 -v
```
Then, in another:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass=MqttPublishSample
```
You should see message info in mosquitto's screen.

You can also use mosquitto_sub to watch a specific channel:
```bash
mosquitto_sub -h localhost -p 1986 -t debug
```