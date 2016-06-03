/*
Code for the easy use of Bottlenose Devices
Copyright (C) 2016  Tim Cannon

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/
#include <bottlenose.h>
//DEFAULTS
int _currentPin = 8;     //Pin that the inductor will on
int _msDot = 50;         //length of a dot
int _msDash = 350;       //length of a dash
int _msCharGap = 100;    //gap between items
int _highDelay = 2;      //how long to delay while HIGH
int _lowDelay = 3;       // how long to delay will LOW

//map of the digits. I will be soon converting this
//to a bit mask and use byte values
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
  pinMode(_currentPin,OUTPUT);
}
//PUBLIC
/******************************************************
* writeDigit
* Summary: generates a morse code signal on the inductor
*          for the digit specified
* Params:  digit - digit to represent in Morse Code
*******************************************************/
void bottlenose::writeDigit(int digit)
{
  runNumber(_digits[digit]);
}
/******************************************************
* NAME
* Summary:
* Params:
* Returns:
*******************************************************/
bool bottlenose::writeChar(char character)
{
  //TODO Alphabet
  return 0;
}
/******************************************************
* NAME
* Summary:
* Params:
* Returns:
*******************************************************/
bool bottlenose::writeString(String character)
{
  //TODO String implementation
  return 0;
}
/******************************************************
* map
* Summary: maps the appropriate delay value.
* Params: sensorVal        - sensor value to map
*         sensorLowerBound - lowest possible value
*         sensorUpperBound - 
*         delayLowerBound  -
*         delayUpperBound  -
* Returns: calculated delay
*******************************************************/
int bottlenose::map(long sensorVal,long sensorLowerBound,long sensorUpperBound,long delayLowerBound,long delayUpperBound){
  if(sensorVal < sensorLowerBound) sensorVal = sensorLowerBound;
  if(sensorVal > sensorUpperBound) sensorVal = sensorUpperBound;
  int thisDelay =  (sensorVal - sensorLowerBound) * (delayUpperBound - delayLowerBound) / (sensorUpperBound - sensorLowerBound) + delayLowerBound;
  digitalWrite(_currentPin,HIGH);
  delay(_highDelay + _lowDelay);
  digitalWrite(_currentPin,LOW);
  delay(thisDelay);
  return thisDelay;
}

//CONFIGS N SUCH
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
int bottlenose::setPin(int pin){
  if(pin != _currentPin){
    pinMode(_currentPin, INPUT);
    _currentPin = pin;
    pinMode(_currentPin, OUTPUT);
  }
  return _currentPin;
}
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
