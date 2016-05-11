#include <bottlenose.h>
//DEFAULTS
int _currentPin = 8;
int _msDot = 50;
int _msDash = 350;
int _msCharGap = 100;
int _highDelay = 2;
int _lowDelay = 3;

int _digits[10][5] ={
    {1,1,1,1,1}, // 0
    {0,1,1,1,1}, // 1
    {0,0,1,1,1}, // 2
    {0,0,0,1,1}, // 3
    {0,0,0,0,1}, // 4
    {0,0,0,0,0}, // 5
    {1,0,0,0,0}, // 6
    {1,1,0,0,0}, // 7
    {1,1,1,0,0}, // 8
    {1,1,1,1,0}, // 9
};
//CONSTRUCTOR
bottlenose::bottlenose(void)
{
  //ADD STUFF
}
//PUBLIC
bool bottlenose::begin(int pin)
{
  _currentPin = pin;
  pinMode(_currentPin,OUTPUT);
  return true;
}
bool bottlenose::writeDigit(int digit)
{
  runNumber(_digits[digit]);
  return true;
}
bool bottlenose::writeChar(char character)
{
  //TODO Alphabet
  return 0;
}
bool bottlenose::writeString(String character)
{
  //TODO String implementation
  return 0;
}
int bottlenose::map(long sensorVal,long sensorLowerBound,long sensorUpperBound,long delayLowerBound,long delayUpperBound){
  if(sensorVal < sensorLowerBound) sensorVal = sensorLowerBound;
  if(sensorVal > sensorUpperBound) sensorVal = sensorUpperBound;
  int thisDelay =  (sensorVal - sensorLowerBound) * (delayUpperBound - delayLowerBound) / (sensorUpperBound - sensorLowerBound) + delayLowerBound;
  digitalWrite(_currentPin,HIGH);
  delay(5);
  digitalWrite(_currentPin,LOW);
  delay(thisDelay);
  return thisDelay;
}
//CONFIGS N SUCH
//SETTERS
int bottlenose::setDotLength(long ms)
{
  if(ms <= _msDash && ms >= 50) _msDot = ms;
  return _msDot;
}
int bottlenose::setDashLength(long ms)
{
  if(ms > _msDot) _msDash = ms;
  return _msDash;
}
int bottlenose::setCharGapLength(long ms)
{
  if(ms > 0) _msCharGap = ms;
  return _msCharGap;
}
int bottlenose::setHighDelayLength(long ms)
{
  if(ms > 0) _highDelay = ms;
  return _highDelay;
}
int bottlenose::setLowDelayLength(long ms)
{
  if(ms > 0) _lowDelay = ms;
  return _lowDelay;
}
//GETTERS
int bottlenose::getDotLength(void)
{
  return _msDot;
}
int bottlenose::getDashLength(void)
{
  return _msDash;
}
int bottlenose::getCharGapLength(void)
{
  return _msCharGap;
}
int bottlenose::getHighDelayLength(void)
{
  return _highDelay;
}
int bottlenose::getLowDelayLength(void)
{
  return _lowDelay;
}
int bottlenose::getPin(void)
{
  return _currentPin;
}

//PRIVATE
void bottlenose::runNumber(int numberMask[5]){
  int dashCount = _msDash/(_highDelay + _lowDelay);
  int dotCount  = _msDot/(_highDelay + _lowDelay);
  for (int i = 0; i <5; i++)
  {
    if(numberMask[i] > 0) doDotOrDash(dashCount);
    else doDotOrDash(dotCount);
    delay(_msCharGap);
  }
}

void bottlenose::doDotOrDash(int count){
  for(int j = 0; j < count; j++)
  {
    digitalWrite(_currentPin, HIGH);
    delay(_highDelay);
    digitalWrite(_currentPin, LOW);
    delay(_lowDelay);
  }
}
