#include <bottlenose.h>

bottlenose bn;
int flag = 0;        // make sure that you return the state only once
int globalIndex = 0;
String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete

void setup() {
    // sets the pins as outputs:
    inputString.reserve(200);
    Serial.begin(9600); // Default connection rate for my BT module
}

void loop() {
    //if some data is sent, read it and save it in the state variable
    if (stringComplete) {
      for(int i = 0; i < 4; i++){//
        bn.writeDigit(((int)inputString[i]-48));       
        delay(1000); 
      }
      // clear the string:
      inputString = "";
      stringComplete = false;
    }
}

void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputString += inChar;
    globalIndex++;
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (globalIndex == 4) {
      stringComplete = true;
      globalIndex = 0;    
    }
  }
}
