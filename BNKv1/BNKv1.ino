// Debug flag, uncomment to enable debug output
//#define DEBUG
#define SAMPLES   10  // Number of distance samples to average
                      // Each sample should take less than 3ms
#define trigPin   5   // Connected to trigger on the Ping)))
#define echoPin   6   // Connected to echo on the Ping)))
#define inductor  3   // Output line to the inductor

void setup() {
  #ifdef DEBUG
    Serial.begin (9600);
  #endif
  // Set pins to input/output
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(inductor, OUTPUT);
  // Ensure that the trigger is set low
  digitalWrite(trigPin, LOW);
}

void loop() {
  // Variable for the length of the pulse
  long duration;
  // Variable for the distance to the object
  long distance;
  // Number of samples to average
  int samples = SAMPLES;
  while(samples--){
    digitalWrite(trigPin, HIGH);  // Send pulse to Ping)))
    delayMicroseconds(5);
    digitalWrite(trigPin, LOW);   // End pulse
    duration += pulseIn(echoPin, HIGH); // Cumulative duration
  }
  duration /= SAMPLES;  // Average the samples
  distance = duration / 58;       // Convert to centimeters

  // Sanity check the distance
  if (distance >= 400 || distance <= 0){
    #ifdef DEBUG
      Serial.println("Out of range");
    #endif
    distance = 400;
  }
  #ifdef DEBUG
    else {
      Serial.print(distance);
      Serial.println(" cm");
    }
  #endif

  // Send pulse to the inductor
  digitalWrite (inductor,HIGH);
  delay(5);
  digitalWrite (inductor, LOW);
  delay(distance);
}
