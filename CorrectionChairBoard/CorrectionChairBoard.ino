
#define FRONT_LEFT_VALVE  36
#define FRONT_LEFT_PUMP   42
#define FRONT_RIGHT_VALVE 37
#define FRONT_RIGHT_PUMP  43
#define BACK_LEFT_VALVE   40
#define BACK_LEFT_PUMP    44
#define BACK_RIGHT_VALVE  41
#define BACK_RIGHT_PUMP   45

#define FRONT_LEFT_PRESSURE A10
#define FRONT_RIGHT_PRESSURE A11
#define BACK_LEFT_PRESSURE A12
#define BACK_RIGHT_PRESSURE A13

String recvData = "";
long measureTime = 3000;

int PUMP_UP = 1;
int PUMP_STOP = 2;
int PUMP_OUT = 3;
unsigned long setHeight[4];

bool flControl, frControl, blControl, brControl;

void setup() {
  Serial.begin(9600);
  Serial3.begin(9600);

  pinMode(FRONT_LEFT_VALVE, OUTPUT);
  pinMode(FRONT_LEFT_PUMP, OUTPUT);
  pinMode(FRONT_RIGHT_VALVE, OUTPUT);
  pinMode(FRONT_RIGHT_PUMP, OUTPUT);
  pinMode(BACK_LEFT_VALVE, OUTPUT);
  pinMode(BACK_LEFT_PUMP, OUTPUT);
  pinMode(BACK_RIGHT_VALVE, OUTPUT);
  pinMode(BACK_RIGHT_PUMP, OUTPUT);

  Serial.print(F("Start Loop."));
}

void loop() {
  while (Serial3.available()) {
    char data = Serial3.read();
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

void sendMessage(String data) {
  Serial3.print(data);
}

void setAirState(int valvePin, int pumpPin, int type) {
  if (type == 1) {                // up
    digitalWrite(valvePin, HIGH);
    digitalWrite(pumpPin, HIGH);
  } else if (type == 2) {         // stop
    digitalWrite(valvePin, HIGH);
    digitalWrite(pumpPin, LOW);
  } else {                        // out
    digitalWrite(valvePin, LOW);
    digitalWrite(pumpPin, LOW);
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
  Serial.println("# Stop Zero Pressure.");
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
    sendMessage(data.c_str());
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
      setHeight[position++] = recvData.substring(0, sepIndex).toFloat() * measureTime;
      recvData = recvData.substring(sepIndex + 1);
      delay(5);
    }
    else
    {
      setHeight[position] = recvData.toFloat() * measureTime;
      break;
    }
  }
}
