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
    {2000, 250, 200 }, // 2 Easy
    {1000, 200, 150 }, // 3 Medium
    {500, 150, 100 }, // 4 Hard
    {50, 100, 50 }  // 5 xTream
};
// Mode Variables
int mTutorial = 0;
int mEasy = 1;
int mMedium = 2;
int mHard = 3;
int mXTream = 4;

// Setting the mode here applies the delay variables across the project
int mCurrent = mTutorial; 

// Placeholder array for the numbers we'll be woring with
int numberSet[10];
 
// Input placeholder
byte byteRead;

// TOGGLES
// Toggle input or generation approach
bool useInput = false;

// Toggles random status
bool isRandom = false;


void setup() {         
  // initialize the digital pin as an output.
  pinMode(led, OUTPUT);   
  Serial.begin(9600);
  // Generate our array of numbers
  generatedNumbers();
}

// Generates our set of numbers we'll be looping over
void generatedNumbers(){
  for(int x=0; x<10; x++){
    if(isRandom == true){
      // if we're random, generate random #s
      int randNumber = (int)random(10);
      numberSet[x] = randNumber;
      Serial.println(randNumber);
    }else{
      // if we're NOT random, go 0-9
      numberSet[x]=x;
      Serial.println(x);
    }
  }
}

// the loop routine runs over and over again forever:
void loop() {
  if(useInput == true){
    // Are we going by user input?
    if (Serial.available()) {
      /* read the most recent byte */
      byteRead = (int)Serial.read()-48;
      Serial.println(byteRead);
      /*ECHO the value that was read, back to the serial port. */
      Serial.write(byteRead);
      runNumber(numberCodes[byteRead]);
    }
  }else{
    // Nope, we're generating!
    for (int i = 0; i <9; i++){
      Serial.println(numberSet[i]);
      runNumber(numberCodes[numberSet[i]]);
      delay(mode[mCurrent][0]);
    }
  }
}

// This creates the short/long pulses based on numberCodes
void runNumber(int numberMask[5]){
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
