// This is just some same code to send out the current (faked) time on curcut closure (Via button)
// Eventually I'd like to have a different data set send depending on specific circut closure
// EG: touch thumb to index, get time. Thumb to middle, get temp. Etc. 

// @TODO:  Good time to try abstracting out the actual morse code and pulse logic. 
//         Maybe look into what it takes to build a library & import it?

// Fake current time
int currentTime[4] = {1,0,3,4};

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
int mode[6][3] ={
    {5500, 250, 200 }, // 0 Tutorial
    {2000, 250, 200 }, // 1 Random
    {2000, 250, 200 }, // 2 Easy
    {1000, 200, 150 }, // 3 Medium
    {500, 150, 100 }, // 4 Hard
    {50, 100, 50 }  // 5 xTream
};

// Placeholder array for the numbers we'll be woring with
int numberSet[10];


// Mode Variables
int mTutorial = 0;
int mRandom = 1;
int mEasy = 2;
int mMedium = 3;
int mHard = 4;
int mXTream = 5;

// Setting the mode here applies the delay variables across the project
int mCurrent = mMedium; 


// constants won't change. They're used here to
// set pin numbers:
const int buttonPin = 2;     // the number of the pushbutton pin
const int ledPin =  4;      // the number of the LED pin

// variables will change:
int buttonState = 0;         // variable for reading the pushbutton status

void setup() {
  // initialize the LED pin as an output:
  pinMode(ledPin, OUTPUT);      
  // initialize the pushbutton pin as an input:
  pinMode(buttonPin, INPUT);
  for(int y=0; y<4; y++){
      numberSet[y]=currentTime[y];
        Serial.println(y);
    }  
}

void loop(){
  // read the state of the pushbutton value:
  buttonState = digitalRead(buttonPin);

  if (buttonState == HIGH || buttonState == LOW) {    
      for (int i = 0; i <4; i++){
        Serial.println(numberSet[i]);
        runNumber(numberCodes[numberSet[i]]);
        delay(mode[mCurrent][0]);
      }
  }
  else {
    digitalWrite(ledPin, LOW);
  }
}


// This creates the short/long pulses based on numberCodes
void runNumber(int numberMask[5]){
  for (int i = 0; i <5; i++){
    if(numberMask[i]>0){
      // If it"s long
      Serial.print("_");
      for(int j = 0;j<60;j++){
        digitalWrite(ledPin, HIGH);
        delay(2);
        digitalWrite(ledPin, LOW);
        delay(3);
      }
      delay(mode[mCurrent][1]);
    }else{
      // if its short
     Serial.print(".");
      
      for(int j = 0;j<10;j++){
        digitalWrite(ledPin, HIGH);
        delay(2);
        digitalWrite(ledPin, LOW);
        delay(3);
      }
      delay(mode[mCurrent][2]);
    }
  }
  Serial.println("");
  Serial.println("");
}
