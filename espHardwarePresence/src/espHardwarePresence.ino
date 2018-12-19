#include <Arduino.h>
#include <Wire.h>
#include <avr/pgmspace.h>

#include <ArduinoOTA.h>
#include <ESP8266WiFi.h>
#include <PubSubClient.h>

// *********************************************
// PIN CONFIG AND OBJECT CREATION
// *********************************************
//For pin definitions see: https://github.com/esp8266/Arduino/blob/master/variants/nodemcu/pins_arduino.h
//PCB LED pins are recycled for external LED
const uint8_t PCBLEDPIN = 2;
const uint8_t PIRPIN = 5;

//Wifi stuff
const uint32_t CONNECTTIMEOUT = 30000;
const char* ssid = "pruebas";
const char* password = "cfbefe695318db2120e8";
const char* mqttServer = "192.168.1.34";
const uint16_t mqttPort = 1986;
const char* mqttPubTopic = "presence";
const char* mqttPubMsg = "alarm";
WiFiClient wifiClient;

//Control if HW outputs to serial or not
//Declare as empty to disable output
#define Sprintln(x) (Serial.println(x))
#define Sprint(x) (Serial.print(x))

//MQTT vars
const uint8_t MAXMSGLEN = 50;
char mqttMSG[MAXMSGLEN];
uint32_t lastMsgMillis = 0;
uint32_t mqttCount = 0;
PubSubClient mqttClient(wifiClient);

uint32_t lastPIRcheck = 0;
const uint32_t DELAYPIRCHECK = 3000;

// *********************************************
// SETUP
// *********************************************
void setup(void) {
    //Init pins
    pinMode(PCBLEDPIN, OUTPUT);
    pinMode(PIRPIN, INPUT);

    //Init Serial
    Serial.begin(115200);
    Sprintln();
    Sprintln("ESP8266 is Alive!");

    //Init I2C
    Wire.begin();
    //listI2Cdevices();

    connectWifi();
    setupOTA();

    //Init MQTT
    mqttClient.setServer(mqttServer, mqttPort);
    //mqttClient.setCallback(mqttCallback);

    //Init random seed
    randomSeed(micros());
}

//Connects to Wifi
void connectWifi() {
    Sprint("Connecting to: ");
    Sprint(ssid);
    Sprint(" | ");
    Sprintln(password);

    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);

    uint32_t wifiConnectStartTime = millis();
    while ((WiFi.status() != WL_CONNECTED) 
            && ((millis() - wifiConnectStartTime) < CONNECTTIMEOUT)) {
        delay(500);
        digitalWrite(PCBLEDPIN, LOW);
        delay(50);
        digitalWrite(PCBLEDPIN, HIGH);
        Sprint(".");
    }

    if (WiFi.status() == WL_CONNECTED) {
        Sprintln("");
        Sprint("Connected, IP: ");
        Sprintln(WiFi.localIP());
    } else {
        Sprintln("Could not connect to wifi");
    }
}

void setupOTA() {
    ArduinoOTA.onStart(otaOnStart);
    ArduinoOTA.onEnd(otaOnEnd);
    ArduinoOTA.onProgress(otaOnProgress);
    ArduinoOTA.onError(otaOnError);
    ArduinoOTA.setPort(8266);
    //ArduinoOTA.setHostname(HOSTNAME);
    ArduinoOTA.begin();
}

// *********************************************
// MAIN LOOP LOGIC
// *********************************************
void loop(void) {
    ArduinoOTA.handle();
    if (!mqttClient.connected()) {
        mqttReconnect();
    }
    mqttClient.loop();
    checkPIR();
}

void checkPIR() {
    if ((millis() - lastPIRcheck) > DELAYPIRCHECK) {
        if (digitalRead(PIRPIN)) {
            digitalWrite(PCBLEDPIN, LOW);
            sendMQTTalarm();
            //Sprintln("DETECTED");
        } else {
            //Sprintln("NOTHING");
            digitalWrite(PCBLEDPIN, HIGH);
        }
        lastPIRcheck = millis();
    }
}

// *********************************************
// MQTT Functionality
// *********************************************
void sendMQTTalarm() {
    mqttClient.publish(mqttPubTopic, mqttPubMsg);
}

//Checks if MQTT is connected and reconnects if needed
void mqttReconnect() {
  // Loop until we're reconnected
  while (!mqttClient.connected()) {
    Sprint("Attempting MQTT connection...");
    // Create a random client ID
    String clientId = "ESP8266Client-";
    clientId += String(random(0xffff), HEX);
    // Attempt to connect
    if (mqttClient.connect(clientId.c_str())) {
        Sprintln("connected");
        //mqttClient.subscribe(mqttSubTopic);
    } else {
        Sprint("failed, rc=");
        Sprint(mqttClient.state());
        Sprintln(" try again in 1 seconds");
        // Wait 5 seconds before retrying
        delay(1000);
    }
  }
}

// *********************************************
// OTA FUNCTIONALITY
// *********************************************
void otaOnStart() {
    String type;
    if (ArduinoOTA.getCommand() == U_FLASH)
        type = "sketch";
    else // U_SPIFFS
        type = "filesystem";
    // NOTE: if updating SPIFFS this would be the place to unmount SPIFFS using SPIFFS.end()
    Sprintln("OTA Start Updating " + type);
}

void otaOnEnd() {
    Sprintln("\nOTA End");
}

void otaOnProgress(unsigned int progress, unsigned int total) {
    Serial.printf("OTA Progress: %u%%\r\n", (progress / (total / 100)));
}

void otaOnError(ota_error_t error) {
    Serial.printf("OTA Error[%u]: ", error);
    if (error == OTA_AUTH_ERROR) {
        Sprintln("OTA Auth Failed");
    }
    else if (error == OTA_BEGIN_ERROR) {
        Sprintln("OTA Begin Failed");
    }
    else if (error == OTA_CONNECT_ERROR) { 
        Sprintln("OTA Connect Failed");
    }
    else if (error == OTA_RECEIVE_ERROR) {
        Sprintln("OTA Receive Failed");
    }
    else if (error == OTA_END_ERROR) {
        Sprintln("OTA End Failed");
    }
}

// *********************************************
// AUX FUNCTIONS
// *********************************************
void listI2Cdevices() {
  byte error, address;
  int nDevices;
  nDevices = 0;
  Sprintln("Scanning for I2C devices...");
  for (address = 1; address < 127; address++ )  {
    // The i2c_scanner uses the return value of
    // the Write.endTransmisstion to see if
    // a device did acknowledge to the address.
    Wire.beginTransmission(address);
    error = Wire.endTransmission();

    if (error == 0){
      Serial.print("I2C device found at address 0x");
      if (address < 16)
        Serial.print("0");
      Serial.print(address, HEX);
      Serial.println("  !");

      nDevices++;
    } else if (error == 4) {
      Serial.print("Unknow error at address 0x");
      if (address < 16)
        Serial.print("0");
      Serial.println(address, HEX);
    }
  } //for loop
  if (nDevices == 0)
    Serial.println("No I2C devices found");
  else
    Serial.println("**********************************\n");
  //delay(1000);           // wait 1 seconds for next scan, did not find it necessary
}