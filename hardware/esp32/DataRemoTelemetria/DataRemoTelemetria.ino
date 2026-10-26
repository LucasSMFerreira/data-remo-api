// DataRemo: bancada com ESP32 e valores FICTÍCIOS. Sem sensores nesta etapa.
// Envia 5 eventos; reenvia o MESMO pacote em falhas transitórias.
#include <WiFi.h>
#include <HTTPClient.h>
#include <esp_system.h>
#include "secrets.h"

unsigned long ultimoEnvio = 0;
unsigned long ultimoWifi = 0;
unsigned int amostra = 0;
char sessao[24];
String pendente;
bool interrompido = false;

void setup() {
  Serial.begin(115200);
  snprintf(sessao, sizeof(sessao), "%08lx%08lx", (unsigned long)esp_random(), (unsigned long)esp_random());
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.println("DataRemo: SIMULACAO, 5 amostras, sem GPS ou acelerometro reais.");
}

void loop() {
  if (amostra >= 5 || interrompido) return;
  unsigned long agora = millis();
  if (WiFi.status() != WL_CONNECTED) {
    if (agora - ultimoWifi >= 10000) {
      ultimoWifi = agora;
      WiFi.reconnect();
      Serial.println("Aguardando Wi-Fi...");
    }
    return;
  }
  if (agora - ultimoEnvio < 5000) return;
  ultimoEnvio = agora;
  if (pendente.length() == 0) {
    char json[650];
    snprintf(json, sizeof(json),
      "{\"eventoId\":\"esp32-%s-%u\",\"dispositivoId\":\"ESP32-DEMO\",\"treinoId\":%ld,"
      "\"tempoDecorridoMs\":%lu,\"latitude\":%.6f,\"longitude\":%.6f,\"velocidadeKmh\":%.2f,"
      "\"aceleracaoX\":%.2f,\"aceleracaoY\":0.05,\"aceleracaoZ\":9.81,\"simulacao\":true}",
      sessao, amostra, TREINO_ID, (unsigned long)amostra * 5000,
      -23.5505 + amostra * 0.0001, -46.6333 + amostra * 0.0001,
      7.0 + amostra * 0.5, 0.1 + amostra * 0.02);
    pendente = json;
  }
  NetworkClient client;
  HTTPClient http;
  http.setConnectTimeout(5000);
  http.setTimeout(5000);
  if (!http.begin(client, API_URL)) {
    Serial.println("Falha ao preparar HTTP. Confira API_URL.");
    return;
  }
  http.addHeader("Content-Type", "application/json");
  int status = http.POST(pendente);
  Serial.printf("Amostra %u: HTTP %d\n", amostra, status);
  if (status > 0) Serial.println(http.getString());
  if (status >= 200 && status < 300) {
    amostra++;
    pendente = "";
    if (amostra == 5) Serial.println("Concluido: 5 amostras confirmadas. Reinicie para uma nova simulacao.");
  } else if (status >= 400 && status < 500 && status != 409 && status != 429) {
    Serial.println("Envio interrompido: confira treinoId e formato. Corrija e reinicie.");
    interrompido = true;
  }
  http.end();
}
