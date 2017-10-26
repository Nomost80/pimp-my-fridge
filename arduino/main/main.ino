#include <PID_v1.h>
#include <LiquidCrystal.h>
#include <TimeLib.h>
#include <Time.h>
#include <ArduinoJson.h>
#include "DHT.h"

#define DHT_PIN 2
#define DHT_TYPE DHT22
#define MOSFET_PIN 8
#define OTHERM_PIN 1
#define ITHERM_PIN 0
#define RS_PIN 12
#define EN_PIN 11
#define D4_PIN 5
#define D5_PIN 4
#define D6_PIN 3
#define D7_PIN 2 

const double Rref = 10000.0; // Résistance de référence à 25°C
const double V_IN = 5.0; // Alimentation électrique
 
/* Information de la thermistance */
const double A_1 = 3.354016E-3;
const double B_1 = 2.569850E-4;
const double C_1 = 2.620131E-6;
//const double D_1 = 6.383091E-8;

/* Composantes du PID */
double input; 
double output;
double brink;
double kp=10,ki=15,kd=5;

PID pid(&input, &output, &brink,kp,ki,kd, DIRECT);

DHT dht(DHT_PIN, DHT_TYPE);

void setup() {
  Serial.begin(19200);
  dht.begin();
  pinMode(13, OUTPUT);
  digitalWrite(13, HIGH);
  input = dht.readTemperature();
  brink = 18;
  pid.SetMode(AUTOMATIC);
}

void loop() {
  input = dht.readTemperature();
 // Serial.print("Input : ");
 // Serial.println(input);

  pid.Compute();
  
  output = 255 - output;
 // Serial.print("Output : ");
 // Serial.println(output);

  analogWrite(MOSFET_PIN, output);
  buildData(dht.readTemperature(), getTempFromThermistor(OTHERM_PIN), getTempFromThermistor(ITHERM_PIN), dht.readHumidity());
  readNewBrink();
 // delay(2000);
}

double SteinhartHart(double R) {
  /* Division de l'équation en 4 parties. La premiere est uniquement A1 */
  double equationB1 = B_1 * log(R/Rref);
  double equationC1 = C_1 * pow(log(R/Rref), 3);
 // double equationD1 = D_1 * pow(log(R/Rref), 3);
  double equation = 1/(A_1 + equationB1 + equationC1) ;//+ equationD1;
  return equation;
}

/* Calcul de la température à partir d'une thermistance */
double getTempFromThermistor(int pin) {
  /* Calcul de la tension sur la borne analogique */
  double valeurAnalog = analogRead(pin);
  double V =  valeurAnalog / 1024 * V_IN;
 
  /* Calcul de la résistance de la thermistance */
  double Rth = (Rref * V ) / (V_IN - V);
 
  /* Calcul de la température en kelvin(Steinhart and Hart) */
  double kelvin = SteinhartHart(Rth);
  return kelvin - 273.15; //Conversion en celsius
}

/* On crée un objet JSON qui comprend les données des captuers */
void buildData(double ins_temp, double out_temp, double m_temp, double dampness) {
  const size_t bufferSize = JSON_ARRAY_SIZE(4) + JSON_OBJECT_SIZE(2) + 5*JSON_OBJECT_SIZE(3);
  DynamicJsonBuffer jsonBuffer(bufferSize); 
  JsonObject& root = jsonBuffer.createObject();
  JsonArray& data = root.createNestedArray("measurements");
  JsonObject& data_0 = data.createNestedObject();
  JsonObject& data_1 = data.createNestedObject();
  JsonObject& data_2 = data.createNestedObject();
  JsonObject& data_3 = data.createNestedObject();

  root["measuredAt"] = String(year()) + '-' + String(month()) + '-' + String(day()) + ' ' + String(hour()) + ':' + String(minute()) + ':' + String(second());
  root["brink"] = brink;

  data_0["sensor"] = "dht22";
  data_0["label"] = "Inside temperature";
  data_0["value"] = ins_temp;

  data_1["sensor"] = "thermistor";
  data_1["label"] = "Outside temperature";
  data_1["value"] = out_temp;

  data_2["sensor"] = "thermistor";
  data_2["label"] = "Module temperature";
  data_2["value"] = m_temp;

  data_3["sensor"] = "dht22";
  data_3["label"] = "Dampness";
  data_3["value"] = dampness;

  sendJSON(root);
}

/* On envoie le JSON sur le port série */
void sendJSON(JsonObject& root) {
  root.printTo(Serial);
  Serial.println("\n");
}

/* On lit la nouvelle consigne envoyé par l'IHM */
void readNewBrink() {
  if (Serial.available() > 0) {
    digitalWrite(13, LOW);
    brink = Serial.readString().toDouble();
  }
}
