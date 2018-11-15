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

//LCD 96x96 -> 11 lines x 12 characters
const uint8_t MAXLCDLINES = 11;
const uint8_t MAXLCDCHARS = 12;

//MQTT publish vars
const uint8_t MAXMSGLEN = 50;
char mqttMSG[MAXMSGLEN];
uint32_t lastMsgMillis = 0;
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
    SeeedGrayOled.setNormalDisplay();
    SeeedGrayOled.setVerticalMode();
    SeeedGrayOled.setGrayLevel(15);
    SeeedGrayOled.clearDisplay();

    SeeedGrayOled.setTextXY(0, 0);
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
        snprintf(mqttMSG, MAXMSGLEN, "Ping #%lu!", mqttCount);
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
    char msg[MAXMSGLEN];
    for (unsigned int i = 0; i < length; i++) {
        Serial.print((char)payload[i]);
        msg[i] = (char)payload[i];
    }
    Serial.println();

    displayMQTTmessage(topic, msg);
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
// OLED FUNCTIONALITY
// *********************************************
//Displays topic and message data in OLED
void displayMQTTmessage(char* topic, const char* msg) {
    SeeedGrayOled.clearDisplay();
    SeeedGrayOled.setTextXY(0, 0);
    SeeedGrayOled.putString("Got data!");
    SeeedGrayOled.setTextXY(2, 0);
    SeeedGrayOled.putString("@ [");
    SeeedGrayOled.putString(topic);
    SeeedGrayOled.putString("]");
    SeeedGrayOled.setTextXY(4, 0);
    SeeedGrayOled.putString("Message:");

    SeeedGrayOled.setTextXY(5, 0);
    uint8_t msgLen = strlen(msg);
    //Message fits one line
    if (msgLen < MAXLCDCHARS) {
        SeeedGrayOled.putString(msg);
    //Message has to be split into lines
    } else {
        uint8_t numLines = msgLen / MAXLCDCHARS;
        if (msgLen % MAXLCDCHARS > 0)
            numLines++;
        Serial.print("NumLines: ");
        Serial.println(numLines);

        for (int i = 0; i < numLines; i++) {
            char msg0[MAXLCDCHARS + 1];
            bzero(msg0, MAXLCDCHARS + 1);
            strncpy(msg0, &msg[MAXLCDCHARS * i], MAXLCDCHARS);
            Serial.print("Len msg0: ");
            Serial.println(strlen(msg0));
            Serial.println(msg0);
            SeeedGrayOled.setTextXY(6 + i, 0);
            SeeedGrayOled.putString(msg0);
        }
        /*
        //Second
        char msg1[MAXLCDCHARS + 1];
        strncpy(msg1, &msg[MAXLCDCHARS + 1], MAXLCDCHARS);
        msg1[MAXLCDCHARS + 1] = '\0';
        Serial.print("Len msg1: ");
        Serial.println(strlen(msg1));
        Serial.println(msg1);
        SeeedGrayOled.setTextXY(6, 0);
        SeeedGrayOled.putString(msg1);
        //More than three lines
        if (numLines > 2) {
            char msg2[MAXLCDCHARS + 1];
            strncpy(msg2, &msg[MAXLCDCHARS * 2], MAXLCDCHARS);
            msg2[MAXLCDCHARS + 1] = '\0';
            Serial.print("Len msg2: ");
            Serial.println(strlen(msg2));
            Serial.println(msg2);
            SeeedGrayOled.setTextXY(7, 0);
            SeeedGrayOled.putString(msg2);
        }
        //More than four lines
        if (numLines > 3) {
            char msg3[MAXLCDCHARS + 1];
            strncpy(msg3, &msg[MAXLCDCHARS * 3], MAXLCDCHARS);
            msg3[MAXLCDCHARS + 1] = '\0';
            Serial.print("Len msg3: ");
            Serial.println(strlen(msg3));
            Serial.println(msg3);
            SeeedGrayOled.setTextXY(8, 0);
            SeeedGrayOled.putString(msg3);
        }*/
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
