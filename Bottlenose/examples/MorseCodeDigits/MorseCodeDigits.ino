#include <bottlenose.h>

bottlenose bn;
int i = 0;
void setup()
{
  Serial.begin(9600);
  bn.begin(13);
}

void loop()
{
  bn.writeDigit(i++);
  Serial.println(i);
  delay(500);
  if(i == 10) i = 0;
}
