#include <TimeLib.h>
#include <Time.h>
#include <ArduinoJson.h>

const size_t bufferSize = JSON_ARRAY_SIZE(4) + JSON_OBJECT_SIZE(2) + 4*JSON_OBJECT_SIZE(3);
DynamicJsonBuffer jsonBuffer(bufferSize);

JsonObject& root = jsonBuffer.createObject();
JsonArray& data = root.createNestedArray("data");
JsonObject& data_0 = data.createNestedObject();
JsonObject& data_1 = data.createNestedObject();
JsonObject& data_2 = data.createNestedObject();
JsonObject& data_3 = data.createNestedObject();

void setup() {
  Serial.begin(9600);
}

void loop() {
  root["measured_at"] = String(day()) + '/' + String(month()) + '/' + String(year()) + ' ' + String(hour()) + ':' + String(minute()) + ':' + String(second());
  
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
  delay(1000);
}
