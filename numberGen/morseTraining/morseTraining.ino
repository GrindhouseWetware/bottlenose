/* This is the system for generating morse code numerical input 
/  for subdermal magnetic systems
*/

int led = 4;

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

// TOGGLES
// Toggle input or generation approach
bool useInput = false;

// Toggles random status
bool isRandom = false;

// Flags for event process
bool sBegin = true;
bool sBeginPrompt = true;
bool sGenNumber = false;
bool sRunNumber = false;
bool sPrompt = true;
bool sAsk = false;
bool sCompare = false;
int randNumber = 0;
int startRead = 0;


void setup() {         
  // initialize the digital pin as an output.
  pinMode(led, OUTPUT);   
  Serial.begin(9600);
  
  // Open with some pleasent instructions
  Serial.println("The system will produce 5 pulses that represent a number between 0 and 9 in morse code.");
  Serial.println("Try guessing which number is produced!");
  
}

// the loop routine runs over and over again forever:
void loop() {
  
  // Let's let the user kick off the process
  // And also give us a seed for better randomness
  if(sBegin == true){
    if(sBeginPrompt == true){
      Serial.println("Enter any key to start!");
      sBeginPrompt = false;
    }
    if (Serial.available()) {
      startRead = (int)Serial.read()-48;
      if(startRead >0){
        sBegin = false;
        sGenNumber = true;
      }
    }
  }
  
  // Can we generate a number?
  // If so, generate and output the value
  if(sGenNumber == true){
    randomSeed(startRead);
    randNumber = (int)random(0,9);
    Serial.println(" ");
    sGenNumber = false;
    sRunNumber = true;
  }
  
  if(sRunNumber == true){
    Serial.print("3, ");
    delay(1000);
    Serial.print("2, ");
    delay(1000);
    Serial.print("1, ");
    delay(1000);
    Serial.print(" ");
    runNumber(numberCodes[randNumber]);
    sRunNumber = false;
    sPrompt = true;
    sAsk = true;
  }
  
  // Ask the user what they think the number was
  if(sAsk == true){
    if(sPrompt == true){
      Serial.println("Please enter your guess (or 'r' to repeat the pulse)");
      sPrompt = false;
    }
    if (Serial.available()) {
      byteRead = (int)Serial.read()-48;
      if(byteRead == 66){
        Serial.println("Repeating...");
        sAsk = false;
        sRunNumber = true;
      }else{
        Serial.print("You Guessed: ");
        Serial.print(byteRead);
        Serial.print("...");
        Serial.println(" ");
        sCompare = true;
      }
      sAsk = false;
    }
  }
  
  // We have the number, AND the guess. Let's compare!
  if(sCompare == true){
    if(byteRead == randNumber){
      Serial.print("Correct! The answer was: ");
      Serial.print(randNumber);
    }else{
      Serial.print("Wrong!! The answer was: ");
      Serial.print(randNumber);
    }
    Serial.println("!");
    Serial.println(" ");
    Serial.println("Let's Try again!");
    Serial.println("--------------------------------- ");
    Serial.println(" ");
    sCompare = false;
    sBeginPrompt = true;
    sBegin = true;
  }
}


// This creates the short/long pulses based on numberCodes
void runNumber(int numberMask[5]){
  Serial.print("*****");
  for (int i = 0; i <5; i++){
    if(numberMask[i]>0){
      // If it"s long
      //Serial.print("_");
      for(int j = 0;j<60;j++){
        digitalWrite(led, HIGH);
        delay(2);
        digitalWrite(led, LOW);
        delay(3);
      }
      delay(mode[mCurrent][1]);
    }else{
      // if its short
     //Serial.print(".");
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

