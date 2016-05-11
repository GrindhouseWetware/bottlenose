#include <bottlenose.h>
bottlenose bn;
int sensorUpperBound,sensorLowerBound,delayUpperBound,delayLowerBound, sensorVal;
int i = 0;
void setup()
{
  Serial.begin(9600);
  bn.begin(13);
  sensorUpperBound = 500;
  sensorLowerBound = 10;
  delayUpperBound = 100,
  delayLowerBound = 2;
}

void loop()
{
  sensorVal = analogRead(A0);
  bn.map(sensorUpperBound,sensorLowerBound,delayUpperBound,delayLowerBound, sensorVal);
}
