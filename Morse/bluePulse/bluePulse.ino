// This is to send a numbrt to the device over bluetooth. 
// As of right now, I can't get a pulse from the device
// Replacing the capacitor with an LED shows that it IS sending the pulse


int ledPin = 13;  // use the built in LED on pin 13 of the Uno
int state = 0;
int flag = 0;        // make sure that you return the state only once
int led = 4;
int globalIndex = 0;
String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete

// 0s are dots, 1s are dashes.
int numberCodes[10][5] ={
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

// Mode arrays, to change the speed
int mode[5][3] ={
    {5500, 250, 200 }, // 0 Tutorial
    {2000, 250, 200 }, // 1 Easy
    {1000, 200, 150 }, // 2 Medium
    {500, 150, 100 }, // 3 Hard
    {50, 100, 50 }  // 4 xTream
};
// Mode Variables
int mTutorial = 0;
int mEasy = 1;
int mMedium = 2;
int mHard = 3;
int mXTream = 4;

// Setting the mode here applies the delay variables across the project
int mCurrent = mMedium; 
 
// Input placeholder
byte byteRead;


void setup() {
    // sets the pins as outputs:
    pinMode(led, OUTPUT);
    digitalWrite(led, LOW);
    inputString.reserve(200);
    Serial.begin(9600); // Default connection rate for my BT module
}

void loop() {
    //if some data is sent, read it and save it in the state variable
    if (stringComplete) {
      for(int i = 0; i < 4; i++){
        byteRead = ((int)inputString[i]-48);
        Serial.print(byteRead);
        runNumber(numberCodes[byteRead]);
        // Mid-number (or word?) delay, where one ends and one begins
        // Need to find a comfotable balance here, using the speed settings maybe?
        delay(1000); 
      }
      // clear the string:
      inputString = "";
      stringComplete = false;
    }
}


// This creates the short/long pulses based on numberCodes
void runNumber(int numberMask[5]){
  Serial.println();
  for (int i = 0; i <5; i++){
    if(numberMask[i]>0){
      // If it"s long
      Serial.print("_");
      for(int j = 0;j<60;j++){
        digitalWrite(led, HIGH);
        delay(2);
        digitalWrite(led, LOW);
        delay(3);
      }
      delay(mode[mCurrent][1]);
    }else{
      // if its short
     Serial.print(".");
      
      for(int j = 0;j<10;j++){
        digitalWrite(led, HIGH);
        delay(2);
        digitalWrite(led, LOW);
        delay(3);
      }
      delay(mode[mCurrent][2]);
   }
  }
  Serial.println("");
  Serial.println("");
}
/*
  SerialEvent occurs whenever a new data comes in the
 hardware serial RX.  This routine is run between each
 time loop() runs, so using delay inside loop can delay
 response.  Multiple bytes of data may be available.
 */
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
