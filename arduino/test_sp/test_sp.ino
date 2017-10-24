#include <TimeLib.h>
#include <Time.h>
#include <ArduinoJson.h>

int brink = 18;

void setup() {
  Serial.begin(19200);
}

void loop() {
  sendData();
  //readData();
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
  root["brink"] = random(17, 25);
  
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
  if (Serial.available() > 0) {
    Serial.flush();
    brink = Serial.readString().toInt();
    Serial.println(brink);
  }
}

//{"measurements":[{"sensor":"dht22","label":"insidetemperature","value":17},{"sensor":"thermistor","label":"outsidetemperature","value":22},{"sensor":"pt100","label":"moduletemperature","value":25},{"sensor":"dht22","label":"dampness","value":53}],"measured_at":"1/1/1970 0:5:44"}



