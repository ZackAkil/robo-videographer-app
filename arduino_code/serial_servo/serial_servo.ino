
/**
 * Bluetoooth module connected
 * 
 * Servo connected to pin 9 
 */
 

#include <Servo.h>

Servo myservo;  // create servo object to control a servo
// twelve servo objects can be created on most boards


void setup()
{
  pinMode(2, OUTPUT);
  digitalWrite(2, HIGH); 
  
  Serial.begin(9600); //set baud rate
  myservo.attach(3);  // attaches the servo on pin 9 to the servo object
}

int pos = 0;
int targetPos = 0;

// reduce this value to speed up movment
int  updateInterval = 23; 
unsigned long lastUpdate = 0;

void loop()
{
  if(Serial.available())
  {
    targetPos = char(Serial.read());
    // use delays to give the serial time to catch up   
    delay(5); 
    Serial.read();
    delay(5); 
  }

  if((millis() - lastUpdate) > updateInterval){
    lastUpdate = millis();
    if (targetPos <  pos){
      pos --;
    }else if (targetPos >  pos){
      pos++;
    }
    myservo.write(pos); 
  }

}
    

