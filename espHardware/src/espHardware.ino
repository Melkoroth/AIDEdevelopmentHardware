#include <Arduino.h>
#include <Wire.h>
#include <avr/pgmspace.h>

#include <SeeedGrayOLED.h>
#include <Adafruit_MPR121.h>

#include <ArduinoOTA.h>
#include <ESP8266WiFi.h>
#include <PubSubClient.h>

// *********************************************
// PIN CONFIG AND OBJECT CREATION
// *********************************************
//For pin definitions see: https://github.com/esp8266/Arduino/blob/master/variants/nodemcu/pins_arduino.h
//PCB LED pins are recycled for external LED
const uint8_t PCBLEDPIN = 2;
const uint8_t LEDGNDPIN = 2; 
const uint8_t LEDVCCPIN = 0;
const uint8_t RXPIN = 3;
const uint8_t TXPIN = 1;
const uint8_t SDAPIN = 4;
const uint8_t SCLPIN = 5;

//I2C Addresses
uint8_t OLEDADDRESS = 0x3C;
uint8_t TOUCHADDRESS = 0x5A;

//Control if HW outputs to serial or not
//Declare as empty to disable output
#define Sprintln(x) (Serial.println(x))
#define Sprint(x) (Serial.print(x))

//LCD 96x96 -> 11 lines x 12 characters
const uint8_t MAXLCDLINES = 11;
const uint8_t MAXLCDCHARS = 12;

//Touch sensor
Adafruit_MPR121 touch;
uint32_t lastTouchMillis = 0;
// Keeps track of the last pins touched
// so we know when buttons are 'released'
uint16_t lastTouched = 0;
uint16_t currtouched = 0;

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
    //Init pins
    pinMode(PCBLEDPIN, OUTPUT);
    pinMode(LEDGNDPIN, OUTPUT);
    pinMode(LEDVCCPIN, OUTPUT);
    digitalWrite(LEDGNDPIN, false);
    digitalWrite(LEDVCCPIN, false);
    setWarningLed(true);

    //Init Serial
    Serial.begin(115200);
    Sprintln();
    Sprintln("ESP8266 is Alive!");

    //Init I2C
    Wire.begin();
    //listI2Cdevices();

    //Init touch sensor
    touch.begin(TOUCHADDRESS);

    //Init LCD
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
    setupOTA();

    //Init MQTT
    mqttClient.setServer(mqttServer, mqttPort);
    mqttClient.setCallback(mqttCallback);

    //Init random seed
    randomSeed(micros());

    setWarningLed(false);
}

//Connects to Wifi
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
        digitalWrite(PCBLEDPIN, LOW);
        delay(50);
        digitalWrite(PCBLEDPIN, HIGH);
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
        SeeedGrayOled.putString("Wifi failed");
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
    sendMQTT(millisNow);
    checkButtons(millisNow);
}

//Outputs MQTT message
void sendMQTT(uint32_t millisNow) {
    if (millisNow - lastMsgMillis > 5000) {
        lastMsgMillis = millisNow;
        mqttCount++;
        snprintf(mqttMSG, MAXMSGLEN, "Ping #%lu!", mqttCount);
        mqttClient.publish(mqttPubTopic, mqttMSG);
    } 
}

//Check button presses
void checkButtons(uint32_t millisNow) {
    if (millisNow - lastTouchMillis > 100) {
        lastTouchMillis = millisNow;
        // Get the currently touched pads
        currtouched = touch.touched();

        for (uint8_t i=0; i<12; i++) {
            // it if *is* touched and *wasnt* touched before, alert!
            if ((currtouched & _BV(i)) && !(lastTouched & _BV(i)) ) {
                Sprint(i); Sprintln(" touched");
            }
            // if it *was* touched and now *isnt*, alert!
            if (!(currtouched & _BV(i)) && (lastTouched & _BV(i)) ) {
                Sprint(i); Sprintln(" released");
            }
        }
        // reset our state
        lastTouched = currtouched;
    } 
}

//Handles red LED
//We use PWM at low duty cycle in order not to burn it
//as we aren't using a resistor as we should :P
//The LED will probably burn-out and it could cause the pin to break too
//In the long-run THIS IS PRETTY BAD -> Solder resistor
void setWarningLed(boolean state) {
    if (state) {
        analogWrite(LEDVCCPIN, 127);
    } else {
        analogWrite(LEDVCCPIN, 0);       
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
        clearLCD(4);
        clearLCD(5);
        SeeedGrayOled.setTextXY(4, 0);
        SeeedGrayOled.putString("MQTT On");
        // Once connected, publish an announcement...
        mqttClient.publish(mqttPubTopic, "Hello world!");
        // ... and resubscribe
        mqttClient.subscribe(mqttSubTopic);
    } else {
        Sprint("failed, rc=");
        SeeedGrayOled.setTextXY(4, 0);
        SeeedGrayOled.putString("MQTT Fail...");
        SeeedGrayOled.setTextXY(5, 0);
        SeeedGrayOled.putString("retry in 5s");
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
//Clears the LCD "by hand". Library method sometimes crashes
//Can be called without parameters to clear all or with a specific line
void clearLCD() {
    clearLCD(255);
}
void clearLCD(uint8_t line) {
    if (line == 255) {
        for (uint8_t i = 0; i <= MAXLCDLINES; i++) {
            SeeedGrayOled.setTextXY(i, 0);
            SeeedGrayOled.putString("            ");
            delay(1);
        }
    } else {
        SeeedGrayOled.setTextXY(line, 0);
        SeeedGrayOled.putString("            ");
    }
}

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