#include <SoftwareSerial.h>

#define BT_UART_RX 12
#define BT_UART_TX 13

#define FRONT_LEFT_VALVE  24
#define FRONT_LEFT_PUMP   25
#define FRONT_RIGHT_VALVE 26
#define FRONT_RIGHT_PUMP  27
#define BACK_LEFT_VALVE   28
#define BACK_LEFT_PUMP    29
#define BACK_RIGHT_VALVE  30
#define BACK_RIGHT_PUMP   31

#define FRONT_LEFT_PRESSURE A0
#define FRONT_RIGHT_PRESSURE A1
#define BACK_LEFT_PRESSURE A2
#define BACK_RIGHT_PRESSURE A3


SoftwareSerial bleSerial(BT_UART_RX, BT_UART_TX);

String recvData = "";
long measureTime = 3000;

int PUMP_UP = 1;
int PUMP_STOP = 2;
int PUMP_OUT = 3;
unsigned long setHeight[4];
int secondWeight = 1000;

bool flControl, frControl, blControl, brControl;

void setup() {
  Serial.begin(115200);
  bool bResult = bleEnable(9600);
  Serial.print(F("# Bluetooth LE Enable : "));
  Serial.println(bResult);

  pinMode(FRONT_LEFT_VALVE, OUTPUT);
  pinMode(FRONT_LEFT_PUMP, OUTPUT);
  pinMode(FRONT_RIGHT_VALVE, OUTPUT);
  pinMode(FRONT_RIGHT_PUMP, OUTPUT);
  pinMode(BACK_LEFT_VALVE, OUTPUT);
  pinMode(BACK_LEFT_PUMP, OUTPUT);
  pinMode(BACK_RIGHT_VALVE, OUTPUT);
  pinMode(BACK_RIGHT_PUMP, OUTPUT);

  digitalWrite(FRONT_LEFT_VALVE, HIGH);
  digitalWrite(FRONT_LEFT_PUMP, HIGH);
  digitalWrite(FRONT_RIGHT_VALVE, HIGH);
  digitalWrite(FRONT_RIGHT_PUMP, HIGH);
  digitalWrite(BACK_LEFT_VALVE, HIGH);
  digitalWrite(BACK_LEFT_PUMP, HIGH);
  digitalWrite(BACK_RIGHT_VALVE, HIGH);
  digitalWrite(BACK_RIGHT_PUMP, HIGH);

}

void loop() {
  while (bleSerial.available()) {
    char data = bleSerial.read();
    recvData += data;
    delay(1);
  }

  if (recvData != "") {
    Serial.print(F("> Receive Message : "));
    Serial.println(recvData);
    if (recvData.equals("ZS")) {
      setZeroPressure();
      delay(10);
      sendMessage("ED");
    } else if (recvData.equals("SM")) {
      setZeroPressure();
      startMeasurePressure();
      delay(10);
      sendMessage("ED");
    } else if (recvData.startsWith("MH")) {
      setZeroPressure();
      setControlData();
      setupHeight();
      delay(10);
      sendMessage("ED");
    }
    recvData = "";
  }

  delay(50);
}

void setAirState(int valvePin, int pumpPin, int type) {
  if (type == 1) {                // up
    digitalWrite(valvePin, LOW);
    digitalWrite(pumpPin, LOW);
  } else if (type == 2) {         // stop
    digitalWrite(valvePin, LOW);
    digitalWrite(pumpPin, HIGH);
  } else {                        // out
    digitalWrite(valvePin, HIGH);
    digitalWrite(pumpPin, HIGH);
  }
}

void setZeroPressure() {
  sendMessage("ZS");
  delay(10);
  Serial.println("# Start Zero Pressure.");
  setAirState(FRONT_LEFT_VALVE, FRONT_LEFT_PUMP, PUMP_OUT);
  setAirState(FRONT_RIGHT_VALVE, FRONT_RIGHT_PUMP, PUMP_OUT);
  setAirState(BACK_LEFT_VALVE, BACK_LEFT_PUMP, PUMP_OUT);
  setAirState(BACK_RIGHT_VALVE, BACK_RIGHT_PUMP, PUMP_OUT);
  delay(3000);
}

void startMeasurePressure() {
  sendMessage("SM");
  delay(10);
  Serial.println("# Start Measure Pressure.");
  Serial.println("> Pump UP.");
  setAirState(FRONT_LEFT_VALVE, FRONT_LEFT_PUMP, PUMP_UP);
  setAirState(FRONT_RIGHT_VALVE, FRONT_RIGHT_PUMP, PUMP_UP);
  setAirState(BACK_LEFT_VALVE, BACK_LEFT_PUMP, PUMP_UP);
  setAirState(BACK_RIGHT_VALVE, BACK_RIGHT_PUMP, PUMP_UP);
  long startTime = millis();
  while (millis() - startTime < measureTime) {
    String data = "C" + String(analogRead(FRONT_LEFT_PRESSURE)) + ","
                  + String(analogRead(FRONT_RIGHT_PRESSURE)) + ","
                  + String(analogRead(BACK_LEFT_PRESSURE)) + ","
                  + String(analogRead(BACK_RIGHT_PRESSURE));
    sendMessage(data);
    delay(100);
  }
  Serial.println("> Pump Stop.");
  setAirState(FRONT_LEFT_VALVE, FRONT_LEFT_PUMP, PUMP_STOP);
  setAirState(FRONT_RIGHT_VALVE, FRONT_RIGHT_PUMP, PUMP_STOP);
  setAirState(BACK_LEFT_VALVE, BACK_LEFT_PUMP, PUMP_STOP);
  setAirState(BACK_RIGHT_VALVE, BACK_RIGHT_PUMP, PUMP_STOP);
}


void setupHeight() {
  sendMessage("MH");
  delay(10);

  Serial.println("# Start Setup Height : ");
  Serial.print(setHeight[0]);
  Serial.print(", ");
  Serial.print(setHeight[1]);
  Serial.print(", ");
  Serial.print(setHeight[2]);
  Serial.print(", ");
  Serial.print(setHeight[3]);
  Serial.println();

  Serial.println("> Pump UP.");
  setAirState(FRONT_LEFT_VALVE, FRONT_LEFT_PUMP, PUMP_UP);
  setAirState(FRONT_RIGHT_VALVE, FRONT_RIGHT_PUMP, PUMP_UP);
  setAirState(BACK_LEFT_VALVE, BACK_LEFT_PUMP, PUMP_UP);
  setAirState(BACK_RIGHT_VALVE, BACK_RIGHT_PUMP, PUMP_UP);
  
  long startTime = millis();
  flControl = frControl = blControl = brControl = true;
  while (flControl || frControl || blControl || brControl) {
    long elapseTime = millis() - startTime;
    if (flControl && elapseTime > setHeight[0]) {
      flControl = false;
      setAirState(FRONT_LEFT_VALVE, FRONT_LEFT_PUMP, PUMP_STOP);
      Serial.println("> Stop Front Left.");
    }
    if (frControl && elapseTime > setHeight[1]) {
      frControl = false;
      setAirState(FRONT_RIGHT_VALVE, FRONT_RIGHT_PUMP, PUMP_STOP);
      Serial.println("> Stop Front Right.");
    }
    if (blControl && elapseTime > setHeight[2]) {
      blControl = false;
      setAirState(BACK_LEFT_VALVE, BACK_LEFT_PUMP, PUMP_STOP);
      Serial.println("> Stop Back Left.");
    }
    if (brControl && elapseTime > setHeight[3]) {
      brControl = false;
      setAirState(BACK_RIGHT_VALVE, BACK_RIGHT_PUMP, PUMP_STOP);
      Serial.println("> Stop Back Right.");
    }
  }
}

void setControlData() {
  int position = 0;
  recvData = recvData.substring(2);
  while (true)
  {
    int sepIndex = recvData.indexOf(",");
    if (sepIndex != -1)
    {
      setHeight[position++] = recvData.substring(0, sepIndex).toFloat() * secondWeight;
      recvData = recvData.substring(sepIndex + 1);
      delay(5);
    }
    else
    {
      setHeight[position] = recvData.toFloat() * secondWeight;
      break;
    }
  }
}


/*
    ========== BLE ==========
*/
bool bleEnable(int baudrate) {
  bleSerial.begin(baudrate);
  bool connected = false;
  for (int j = 0; j < 3; j++) {
    sendMessage("AT");
    if (reply().startsWith("OK")) {
      connected = true;
      break;
    }
  }
  return connected;
}

void sendMessage(String data) {
  bleSerial.print(data);
}

String reply() {
  String replyData = "";
  bool isValid = true;
  long time = millis() + 1000;
  while (!bleSerial.available()) {
    if (millis() > time) {
      isValid = false;
      break;
    }
  }
  if (isValid) {
    while (bleSerial.available()) {
      char data = bleSerial.read();
      replyData += data;
      delay(1);
    }
  }
  else {
    Serial.println(F("#(Err)Response Timeout.."));
  }
  return replyData;
}
