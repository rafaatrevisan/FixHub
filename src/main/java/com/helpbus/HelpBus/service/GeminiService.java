package com.helpbus.HelpBus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpbus.HelpBus.model.enums.EquipeResponsavel;
import com.helpbus.HelpBus.model.enums.PrioridadeTicket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public GeminiResult avaliarTicket(String descricao, String localizacao, String descricaoLocalizacao, String andar) {
        try {
            String prompt = """
                Você é um assistente que classifica tickets de manutenção no Terminal Rodoviário de Campinas.
                O ticket contém as seguintes informações:
                - Andar: %s
                - Localização: %s
                - Detalhes da localização: %s
                - Descrição: %s
            
                Sua tarefa:
                1. Determinar a prioridade entre: BAIXA, REGULAR, IMPORTANTE, URGENTE
                2. Determinar a equipe responsável entre: LIMPEZA, MANUTENCAO, ADMINISTRACAO
            
                Responda SOMENTE em JSON, sem usar blocos de código ou markdown, no formato:
                {
                  "prioridade": "VALOR",
                  "equipeResponsavel": "VALOR"
                }
            """.formatted(andar, localizacao, descricaoLocalizacao, descricao);

            String requestBody = """
                {
                  "contents": [{
                    "parts":[{"text": "%s"}]
                  }]
                }
                """.formatted(prompt.replace("\"", "\\\""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL))
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", apiKey) // 🔹 Enviando a chave no header
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new RuntimeException("Resposta inválida do Gemini: " + response.body());
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                throw new RuntimeException("Resposta sem conteúdo no Gemini: " + response.body());
            }

            String rawText = parts.get(0).path("text").asText();
            if (rawText == null || rawText.isBlank()) {
                throw new RuntimeException("Texto retornado pelo Gemini está vazio: " + response.body());
            }

            rawText = rawText.replaceAll("(?s)```json\\s*|```", "").trim();

            JsonNode jsonResult = mapper.readTree(rawText);

            PrioridadeTicket prioridade = PrioridadeTicket.valueOf(jsonResult.get("prioridade").asText().toUpperCase());
            EquipeResponsavel equipe = EquipeResponsavel.valueOf(jsonResult.get("equipeResponsavel").asText().toUpperCase());

            return new GeminiResult(prioridade, equipe);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Erro ao chamar API do Gemini", e);
        }
    }

    public record GeminiResult(PrioridadeTicket prioridade, EquipeResponsavel equipeResponsavel) {}
}
