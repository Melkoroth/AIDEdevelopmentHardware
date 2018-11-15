#include <Arduino.h>
#include <Wire.h>
#include <avr/pgmspace.h>

#include <SeeedGrayOLED.h>

#include <ArduinoOTA.h>
#include <ESP8266WiFi.h>
#include <PubSubClient.h>

// *********************************************
// PIN CONFIG AND OBJECT CREATION
// *********************************************
const uint8_t LEDPIN = 2;
const uint8_t RXPIN = 3;
const uint8_t TXPIN = 1;
const uint8_t SDAPIN = 4;
const uint8_t SCLPIN = 5;

const uint32_t CONNECTTIMEOUT = 30000;
const char* ssid = "Ansible";
const char* password = "1qaz2wsx";
const char* mqttServer = "192.168.43.132";
const uint16_t mqttPort = 1986;
const char* mqttPubTopic = "pubESP";
const char* mqttSubTopic = "subESP";

WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

//MQTT publish vars
uint32_t lastMsgMillis = 0;
char mqttMSG[50];
uint32_t mqttCount = 0;

// *********************************************
// SETUP
// *********************************************
void setup(void) {
    pinMode(LEDPIN, OUTPUT);

    //Init Serial
    Serial.begin(115200);
    Serial.println();
    Serial.println("Hello World!");

    //Init LCD
    Wire.begin();
    SeeedGrayOled.init(SSD1327);   
    SeeedGrayOled.clearDisplay();
    SeeedGrayOled.setNormalDisplay();
    SeeedGrayOled.setVerticalMode();
    SeeedGrayOled.setTextXY(0, 0);
    SeeedGrayOled.setGrayLevel(15);
    SeeedGrayOled.putString("Hello World!");

    connectWifi();
    setupOTA();

    //Init MQTT
    mqttClient.setServer(mqttServer, mqttPort);
    mqttClient.setCallback(mqttCallback);

}

void connectWifi() {
    Serial.print("Connecting to: ");
    Serial.print(ssid);
    Serial.print(" | ");
    Serial.println(password);
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);

    uint32_t wifiConnectStartTime = millis();
    while ((WiFi.status() != WL_CONNECTED) 
            && ((millis() - wifiConnectStartTime) < CONNECTTIMEOUT)) {
        delay(500);
        digitalWrite(LEDPIN, LOW);
        delay(50);
        digitalWrite(LEDPIN, HIGH);
        Serial.print(".");
    }

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("");
        Serial.print("Connected, IP: ");
        Serial.println(WiFi.localIP());
    } else {
        Serial.println("Could not connect to wifi");
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

    uint32_t millisNow = millis();
    if (millisNow - lastMsgMillis > 5000) {
        lastMsgMillis = millisNow;
        mqttCount++;
        snprintf(mqttMSG, 50, "Ping #%lu!", mqttCount);
        mqttClient.publish(mqttPubTopic, mqttMSG);
    }
}

// *********************************************
// MQTT Functionality
// *********************************************
void mqttCallback(char* topic, byte* payload, unsigned int length) {
    Serial.print("Message arrived [");
    Serial.print(topic);
    Serial.print("] ");
    for (unsigned int i = 0; i < length; i++) {
        Serial.print((char)payload[i]);
    }
    Serial.println();
}

void mqttReconnect() {
  // Loop until we're reconnected
  while (!mqttClient.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Create a random client ID
    String clientId = "ESP8266Client-";
    clientId += String(random(0xffff), HEX);
    // Attempt to connect
    if (mqttClient.connect(clientId.c_str())) {
        Serial.println("connected");
        // Once connected, publish an announcement...
        mqttClient.publish(mqttPubTopic, "Hello world!");
        // ... and resubscribe
        mqttClient.subscribe(mqttSubTopic);
    } else {
        Serial.print("failed, rc=");
        Serial.print(mqttClient.state());
        Serial.println(" try again in 5 seconds");
        // Wait 5 seconds before retrying
        delay(5000);
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
    Serial.println("OTA Start Updating " + type);
}

void otaOnEnd() {
    Serial.println("\nOTA End");
}

void otaOnProgress(unsigned int progress, unsigned int total) {
    Serial.printf("OTA Progress: %u%%\r\n", (progress / (total / 100)));
}

void otaOnError(ota_error_t error) {
    Serial.printf("OTA Error[%u]: ", error);
    if (error == OTA_AUTH_ERROR) {
        Serial.println("OTA Auth Failed");
    }
    else if (error == OTA_BEGIN_ERROR) {
        Serial.println("OTA Begin Failed");
    }
    else if (error == OTA_CONNECT_ERROR) { 
        Serial.println("OTA Connect Failed");
    }
    else if (error == OTA_RECEIVE_ERROR) {
        Serial.println("OTA Receive Failed");
    }
    else if (error == OTA_END_ERROR) {
        Serial.println("OTA End Failed");
    }
}
