#include <SoftwareSerial.h>
#include<Servo.h>

Servo servo;
int value = 0;

int servoPin = 10;

SoftwareSerial BlueToothSerial(2, 3);
String myString=""; //받는 문자열

String ARDUINO_CONNECT = "connect\n";
String ARDUINO_LOCK = "lock\n";

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  BlueToothSerial.begin(115200);

  pinMode(8, OUTPUT);
  pinMode(9, OUTPUT);
  digitalWrite(8, LOW);
  digitalWrite(9, LOW);
}

void loop() {
  while(BlueToothSerial.available())  //mySerial 값이 있으면
  {
    char myChar = (char)BlueToothSerial.read();  //mySerial int형식의 값을 char형식으로 변환
    myString+=myChar;   //수신되는 문자열을 myString에 모두 붙임 (1바이트씩 전송되는 것을 모두 붙임)
    delay(5);           //수신 문자열 끊김 방지
  }

  if(!myString.equals(""))  //myString 값이 있다면
  {
    Serial.print("input value: "+myString); //시리얼모니터에 myString값 출력

      if(myString.equals(ARDUINO_CONNECT))  //myString 값이 'on' 이라면
      {
        digitalWrite(8, HIGH);
        digitalWrite(9, LOW);
        Serial.println("input Connect OK"); //시리얼모니터에 myString값 출력
        delay(10);
        BlueToothSerial.write("inputConnectOK"); //시리얼모니터에 myString값 출력
        delay(10);
        servo_OFF();

      } else if(myString.equals(ARDUINO_LOCK)) {
        digitalWrite(8, LOW);
        digitalWrite(9, HIGH);
        Serial.println("input Lock OK"); //시리얼모니터에 myString값 출력
        BlueToothSerial.write("input Lock OK");
        delay(10);
        servo_ON();
      }

      servo.write(value);
      myString="";  //myString 변수값 초기화   //입력된 문자열 없애고 초기화하여 문자열 입력대기
  }

}

void servo_ON(){
  servo.attach(servoPin);  // attaches the servo on pin 9 to the servo object
  servo.write(90);
  delay(500);
  servo.detach();
}

void servo_OFF(){
  servo.attach(servoPin);  // attaches the servo on pin 9 to the servo object
  servo.write(0);
  delay(500);
  servo.detach();
}
