#include <Arduino.h>
#include <ArduinoOTA.h>

void setup(void) {
	Serial.begin(115200);
    Serial.println();
    Serial.println("Hello World");

    setupOTA();
}

// *********************************************
// SETUP
// *********************************************
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