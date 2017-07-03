

int led = 13;
//Arrays for the 4 inputs**********************************************

float voltageValue[4] = {20,10.3,13.66,32.45};
 
//Char used for reading in Serial characters
char inbyte = 0;
//*******************************************************************************************

float i = 3;
void setup() {
  // initialise serial communications at 9600 bps:
  Serial.begin(9600);
  pinMode(led, OUTPUT);
  digitalWrite(led, LOW);
 }


int loop_count = 1;
void loop() {

  getVoltageValue();
  sendAndroidValues();
  //when serial values have been received this will be true
  if (Serial.available() > 0)
  {
    inbyte = Serial.read();
    if (inbyte == '0')
    {
      //LED off
      digitalWrite(led, LOW);
    }
    if (inbyte == '1')
    {
      //LED on
      digitalWrite(led, HIGH);
    }
  }
  loop_count += 2;
 //voltageValue[0] = i+ 10;
  //delay by 2s. Meaning we will be sent values every 2s approx
  //also means that it can take up to 2 seconds to change LED state
  delay(2000);
}
 

//sends the values from the sensor over serial to BT module
void sendAndroidValues()
 {
  //puts # before the values so our app knows what to do with the data
  Serial.print('#');
  //for loop cycles through 4 sensors and sends values via serial
  for(int k=0; k<4; k++)
  {
    Serial.print(voltageValue[k]);
    Serial.print('+');
    //technically not needed but I prefer to break up data values
    //so they are easier to see when debugging
  }
 Serial.print('~'); //used as an end of transmission character - used in app for string length
 Serial.println();
 delay(10);        //added a delay to eliminate missed transmissions
}

void getVoltageValue()
{
  for (int x = 0; x < 4; x++)
  {
    voltageValue[x] = i+10+x+loop_count;
    if (voltageValue[x] > 500)
    {
        voltageValue[x] = 12;  
    }
  }
}


