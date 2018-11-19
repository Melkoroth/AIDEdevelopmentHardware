#include <Arduino.h>
#include <Wire.h>
#include <avr/pgmspace.h>

#include <SeeedGrayOLED.h>
#include <i2c_touch_sensor.h>

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

//Control if HW outputs to serial or not
#define Sprintln(x) (Serial.println(x))
#define Sprint(x) (Serial.print(x))

//LCD 96x96 -> 11 lines x 12 characters
const uint8_t MAXLCDLINES = 11;
const uint8_t MAXLCDCHARS = 12;

//Touch sensor
i2ctouchsensor touch;
uint32_t lastTouchMillis = 0;

//Wifi stuff
const uint32_t CONNECTTIMEOUT = 30000;
const char* ssid = "Ansible";
const char* password = "1qaz2wsx";
const char* mqttServer = "192.168.43.132";
const uint16_t mqttPort = 1986;
const char* mqttPubTopic = "pubESP";
const char* mqttSubTopic = "subESP";
WiFiClient wifiClient;

//MQTT vars
const uint8_t MAXMSGLEN = 50;
char mqttMSG[MAXMSGLEN];
uint32_t lastMsgMillis = 0;
uint32_t mqttCount = 0;
PubSubClient mqttClient(wifiClient);

// *********************************************
// SETUP
// *********************************************
void setup(void) {
    pinMode(LEDPIN, OUTPUT);

    //Init Serial
    Serial.begin(115200);
    Sprintln();
    Sprintln("Hello World!");
    //Init touch sensor
    touch.initialize();

    //Init LCD
    Wire.begin();
    SeeedGrayOled.init(SSD1327);   
    SeeedGrayOled.setNormalDisplay();
    SeeedGrayOled.setVerticalMode();
    SeeedGrayOled.setGrayLevel(15);
    clearLCD();
    SeeedGrayOled.setTextXY(0, 0);
    SeeedGrayOled.putString("Hello World!");
    delay(500);
    clearLCD();

    connectWifi();
    //setupOTA();

    //Init MQTT
    mqttClient.setServer(mqttServer, mqttPort);
    mqttClient.setCallback(mqttCallback);
}

void connectWifi() {
    clearLCD();
    Sprint("Connecting to: ");
    Sprint(ssid);
    Sprint(" | ");
    Sprintln(password);

    SeeedGrayOled.setTextXY(0, 0);
    SeeedGrayOled.putString("Connecting");
    SeeedGrayOled.setTextXY(1, 0);
    SeeedGrayOled.putString(ssid);
    SeeedGrayOled.setTextXY(2, 0);
    SeeedGrayOled.putString(password);

    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);

    uint32_t wifiConnectStartTime = millis();
    while ((WiFi.status() != WL_CONNECTED) 
            && ((millis() - wifiConnectStartTime) < CONNECTTIMEOUT)) {
        delay(500);
        digitalWrite(LEDPIN, LOW);
        delay(50);
        digitalWrite(LEDPIN, HIGH);
        Sprint(".");
    }

    
    clearLCD(0);
    SeeedGrayOled.setTextXY(0, 0);
    if (WiFi.status() == WL_CONNECTED) {
        Sprintln("");
        Sprint("Connected, IP: ");
        Sprintln(WiFi.localIP());
        SeeedGrayOled.putString("Connected");
        // SeeedGrayOled.setTextXY(1, 0);
        // SeeedGrayOled.putString(char*(WiFi.localIP()));
    } else {
        Sprintln("Could not connect to wifi");
        SeeedGrayOled.putString("Could not connect!");
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
    //ArduinoOTA.handle();
    if (!mqttClient.connected()) {
        mqttReconnect();
    }
    mqttClient.loop();

    //Outputs MQTT message
    uint32_t millisNow = millis();
    if (millisNow - lastMsgMillis > 5000) {
        lastMsgMillis = millisNow;
        mqttCount++;
        snprintf(mqttMSG, MAXMSGLEN, "Ping #%lu!", mqttCount);
        mqttClient.publish(mqttPubTopic, mqttMSG);
    }

    //Check button pressed
    if (millisNow - lastTouchMillis > 500) {
        lastTouchMillis = millisNow;
        touch.getTouchState();
    }
    for (int i=0;i<12;i++) {
        if (touch.touched&(1<<i)) {
            Serial.print("pin ");
            Serial.print(i);
            Serial.println(" was  touched");
        }
    }

}

//Clears the LCD "by hand". Library method sometimes crashes
//Can be called without parameters to clear all or with a specific line
void clearLCD() {
    clearLCD(255);
}
void clearLCD(uint8_t line) {
    if (line == 255) {
        for (uint8_t i = 0; i < MAXLCDLINES; i++) {
            SeeedGrayOled.setTextXY(i, 0);
            SeeedGrayOled.putString("            ");
            delay(1);
        }
    } else {
        SeeedGrayOled.setTextXY(line, 0);
        SeeedGrayOled.putString("            ");
    }
}

// *********************************************
// MQTT Functionality
// *********************************************
void mqttCallback(char* topic, uint8_t* payload, unsigned int length) {
    Sprint("Message arrived [");
    Sprint(topic);
    Sprint("] ");
    char msg[MAXMSGLEN + 1];
    //bzero(msg, sizeof(char) * (MAXMSGLEN + 1));
    for (uint8_t i = 0; i < length || i < MAXMSGLEN; i++) {
        //Sprint((char)payload[i]);
        msg[i] = (char)payload[i];
    }
    //Terminate char*
    (length < MAXMSGLEN) ? msg[length] = '\0' : msg[MAXMSGLEN] = '\0';
    Sprint("Length: ");
    Sprint(strlen(msg));
    Sprint(" ");
    Sprint(msg);
    Sprintln();

    displayMQTTmessage(topic, msg);
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
        // Once connected, publish an announcement...
        mqttClient.publish(mqttPubTopic, "Hello world!");
        // ... and resubscribe
        mqttClient.subscribe(mqttSubTopic);
    } else {
        Sprint("failed, rc=");
        Sprint(mqttClient.state());
        Sprintln(" try again in 5 seconds");
        // Wait 5 seconds before retrying
        delay(5000);
    }
  }
}

// *********************************************
// OLED FUNCTIONALITY
// *********************************************
//Displays topic and message data in OLED
void displayMQTTmessage(const char* topic, const char* msg) {
    clearLCD();
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

        //Delays are added to prevent WDT from triggering
        for (uint8_t i = 0; i < numLines; i++) {
            char msgO[MAXLCDCHARS + 1];
            //Fill with end character
            memset(msgO, '\0', MAXLCDCHARS + 1);
            delay(5);
            strncpy(msgO, &msg[MAXLCDCHARS * i], MAXLCDCHARS);
            delay(5);
            SeeedGrayOled.setTextXY(6 + i, 0);
            delay(5);
            SeeedGrayOled.putString(msgO);
            delay(5);
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
