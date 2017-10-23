void setup() {
  Serial.begin(9600);
}

void loop() {
  Serial.print("temperature:40");
  Serial.print("dampness:60");
  delay(1000);
}
