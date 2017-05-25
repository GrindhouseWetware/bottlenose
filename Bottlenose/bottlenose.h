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
#ifndef BN_h
#define BN_h

#include <Arduino.h> // for byte data type

class bottlenose
{
  public:
    bottlenose(void);

    void writeDigit(int digit);
    bool writeChar(char character);
    bool writeString(String str);
    int  map(long sensorVal,long sensorLowerBound,long sensorUpperBound,long delayLowerBound,long delayUpperBound);

    int setDotLength(long ms);
    int setDashLength(long ms);
    int setCharGapLength(long ms);
    int setHighDelayLength(long ms);
    int setLowDelayLength(long ms);
    int setPin(int pin);

    int getDotLength(void);
    int getDashLength(void);
    int getCharGapLength(void);
    int getHighDelayLength(void);
    int getLowDelayLength(void);
    int getPin(void);

  private:
    void runNumber(int digit[5]);
    void runAlpha(int alpha[4]);
    void doDotOrDash(int count);

};
#endif
