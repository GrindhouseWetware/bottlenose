#ifndef BN_h
#define BN_h

#include <Arduino.h> // for byte data type

class bottlenose
{
  public:
    bottlenose(void);

    bool begin(int pin);
    bool writeDigit(int digit);
    bool writeChar(char character);
    bool writeString(String str);
    int  map(long sensorVal,long sensorLowerBound,long sensorUpperBound,long delayLowerBound,long delayUpperBound);

    int setDotLength(long ms);
    int setDashLength(long ms);
    int setCharGapLength(long ms);
    int setHighDelayLength(long ms);
    int setLowDelayLength(long ms);

    int getDotLength(void);
    int getDashLength(void);
    int getCharGapLength(void);
    int getHighDelayLength(void);
    int getLowDelayLength(void);
    int getPin(void);

  private:
    void runNumber(int digit[5]);
    void doDotOrDash(int count);

};
#endif
