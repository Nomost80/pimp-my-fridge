#include <LiquidCrystal.h>
#include <TimeLib.h>
#include <Time.h>
#include <ArduinoJson.h>

const int rs = 12, en = 11, d4 = 5, d5 = 4, d6 = 3, d7 = 2;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);

void setup() {
  Serial.begin(19200);
  pinMode(13, OUTPUT);
  digitalWrite(13, HIGH);
  lcd.begin(16, 2);
  lcd.setCursor(0, 0);
  lcd.print("hello");
  lcd.setCursor(0, 1);
}

void loop() {
  sendData();
  readData();
}

void sendData() {
  const size_t bufferSize = JSON_ARRAY_SIZE(4) + JSON_OBJECT_SIZE(2) + 4*JSON_OBJECT_SIZE(3);
  DynamicJsonBuffer jsonBuffer(bufferSize);
  
  JsonObject& root = jsonBuffer.createObject();
  JsonArray& data = root.createNestedArray("measurements");
  JsonObject& data_0 = data.createNestedObject();
  JsonObject& data_1 = data.createNestedObject();
  JsonObject& data_2 = data.createNestedObject();
  JsonObject& data_3 = data.createNestedObject();

  root["measuredAt"] = String(year()) + '-' + String(month()) + '-' + String(day()) + ' ' + String(hour()) + ':' + String(minute()) + ':' + String(second());
  root["brink"] = 19;
  
  data_0["sensor"] = "dht22";
  data_0["label"] = "inside temperature";
  data_0["value"] = random(17, 20);

  data_1["sensor"] = "thermistor";
  data_1["label"] = "outside temperature";
  data_1["value"] = random(21, 23);

  data_2["sensor"] = "pt100";
  data_2["label"] = "module temperature";
  data_2["value"] = random(24, 27);

  data_3["sensor"] = "dht22";
  data_3["label"] = "dampness";
  data_3["value"] = random(40, 60);
  
  root.printTo(Serial);
  Serial.println("\n");
}

void readData() {
  //lcd.print(Serial.available());
  lcd.setCursor(0, 1);
  if (Serial.available() > 0) {
    digitalWrite(13, LOW);
    String data = Serial.readString();
    lcd.print(data);
  }
}
