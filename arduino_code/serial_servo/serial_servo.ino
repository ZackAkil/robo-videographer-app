
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

void loop()
{
  while(Serial.available())
  {//while there is data available on the serial monitor
//    message+=char(Serial.read());//store string from serial command
    pos = char(Serial.read());
    Serial.read();
    myservo.write(pos); 
    
  }


  delay(100); //delay
}
    

