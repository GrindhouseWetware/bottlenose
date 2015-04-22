#define trigPin 5
#define echoPin 6
#define inductor 3
void setup() {
//Serial.begin (9600);
pinMode(trigPin, OUTPUT);
pinMode(echoPin, INPUT);
pinMode(inductor, OUTPUT);
}
void loop() {
{
long duration, distance;
digitalWrite(trigPin, LOW);
delayMicroseconds(2);
digitalWrite(trigPin, HIGH);
delayMicroseconds(10);
digitalWrite(trigPin, LOW);
duration = pulseIn(echoPin, HIGH);
distance = (duration/2) / 29.1;
if (distance >= 400 || distance <= 0){
Serial.println("Out of range");
distance = 400;
}
else {
//Serial.print(distance);
//Serial.println(" cm");
}

digitalWrite (inductor,HIGH);
delay(5);
digitalWrite (inductor, LOW);
delay(distance);
}
}
