#include <TimeLib.h>
#include <Time.h>
#include <ArduinoJson.h>

void setup() {
  Serial.begin(9600);
}

void loop() {
  sendData();
  delay(5000);
  readData();
}

void sendData() {
  size_t bufferSize = JSON_ARRAY_SIZE(4) + JSON_OBJECT_SIZE(2) + 4*JSON_OBJECT_SIZE(3);
  DynamicJsonBuffer jsonBuffer(bufferSize);
  
  JsonObject& root = jsonBuffer.createObject();
  JsonArray& data = root.createNestedArray("measurements");
  JsonObject& data_0 = data.createNestedObject();
  JsonObject& data_1 = data.createNestedObject();
  JsonObject& data_2 = data.createNestedObject();
  JsonObject& data_3 = data.createNestedObject();

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
}

void readData() {
  size_t bufferSize = JSON_ARRAY_SIZE(4) + JSON_OBJECT_SIZE(2) + 4*JSON_OBJECT_SIZE(3) + 250;
  DynamicJsonBuffer jsonBuffer(bufferSize);
  
  char* json = "{\"measured_at\":\"11/11/1111 11:11:11\",\"measurements\":[{\"sensor\":\"DHT22\",\"label\":\"inside temperature\",\"value\":55},{\"sensor\":\"DHT22\",\"label\":\"outside temperature\",\"value\":80},{\"sensor\":\"DHT22\",\"label\":\"module temperature\",\"value\":60},{\"sensor\":\"DHT22\",\"label\":\"dampness\",\"value\":50}]}";
  
  JsonObject& root = jsonBuffer.parseObject(json);
  
  char* measured_at = root["measured_at"]; // "11/11/1111 11:11:11"
  
  JsonArray& measurements = root["measurements"];
  
  JsonObject& measurements0 = measurements[0];
  char* measurements0_sensor = measurements0["sensor"]; // "DHT22"
  char* measurements0_label = measurements0["label"]; // "inside temperature"
  int measurements0_value = measurements0["value"]; // 55
  
  JsonObject& measurements1 = measurements[1];
  char* measurements1_sensor = measurements1["sensor"]; // "DHT22"
  char* measurements1_label = measurements1["label"]; // "outside temperature"
  int measurements1_value = measurements1["value"]; // 80
  
  JsonObject& measurements2 = measurements[2];
  char* measurements2_sensor = measurements2["sensor"]; // "DHT22"
  char* measurements2_label = measurements2["label"]; // "module temperature"
  int measurements2_value = measurements2["value"]; // 60
  
  JsonObject& measurements3 = measurements[3];
  char* measurements3_sensor = measurements3["sensor"]; // "DHT22"
  char* measurements3_label = measurements3["label"]; // "dampness"
  int measurements3_value = measurements3["value"]; // 50

  Serial.println(measured_at);
  Serial.println(measurements0_sensor);
  Serial.println(measurements0_label);
  Serial.println(measurements0_value);
}

