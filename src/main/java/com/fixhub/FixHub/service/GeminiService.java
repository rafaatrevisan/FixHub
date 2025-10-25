package com.fixhub.FixHub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixhub.FixHub.model.entity.Ticket;
import com.fixhub.FixHub.model.entity.TicketMestre;
import com.fixhub.FixHub.model.enums.EquipeResponsavel;
import com.fixhub.FixHub.model.enums.PrioridadeTicket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;
    private static final double THRESHOLD_COMPARACAO_MESTRE = 0.85;
    private static final double THRESHOLD_MESMO_PROBLEMA = 0.65;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    /**
     * Verifica as informações do ticket recebidas e deteriman a prioridade e equipe responsável
     */
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

    /**
     * Calcula similaridade entre dois tickets usando IA.
     * Retorna um valor entre 0 e 1.
     */
    public Object compararComListaTicketsMestre(Ticket novoTicket, List<TicketMestre> ticketsMestreLista) {
        double threshold = THRESHOLD_COMPARACAO_MESTRE;
        int idMaisSimilar = -1;
        double maiorSimilaridade = 0.0;

        for (TicketMestre mestre : ticketsMestreLista) {
            try {
                String prompt = """
                Você é um assistente que gerencia tickets de manutenção no Terminal Rodoviário de Campinas.
                Sua tarefa é comparar dois tickets de manutenção e determinar a similaridade entre eles,
                considerando que 1.0 significa idênticos e 0.0 significa completamente diferentes.
                
                Ticket Novo:
                - Andar: %s
                - Localização: %s
                - Detalhes da localização: %s
                - Descrição: %s
                
                Ticket Mestre (já existente):
                - Andar: %s
                - Localização: %s
                - Detalhes da localização: %s
                - Descrição: %s
                
                Responda SOMENTE com um número decimal entre 0 e 1 indicando a similaridade.
                """.formatted(
                        novoTicket.getAndar(),
                        novoTicket.getLocalizacao(),
                        novoTicket.getDescricaoLocalizacao(),
                        novoTicket.getDescricaoTicketUsuario(),
                        mestre.getAndar(),
                        mestre.getLocalizacao(),
                        mestre.getDescricaoLocalizacao(),
                        mestre.getDescricaoTicketUsuario()
                );

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
                        .header("X-goog-api-key", apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());

                String rawText = root.path("candidates").get(0)
                        .path("content").path("parts").get(0)
                        .path("text").asText().trim();

                double similarityScore = Double.parseDouble(rawText);

                if (similarityScore > threshold && similarityScore > maiorSimilaridade) {
                    maiorSimilaridade = similarityScore;
                    idMaisSimilar = mestre.getId();
                }

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Erro ao chamar API do Gemini para similaridade", e);
            }
        }
        // Se encontrou algum ticket mestre acima do threshold, retorna o ID
        if (idMaisSimilar != -1) {
            return idMaisSimilar;
        }
        return "false";
    }

    /**
     * Verifica se o problema do ticket a ser atualizado ainda é o mesmo, usando threshold de similaridade
     */
    public boolean mesmoProblema(Ticket ticketOriginal, Ticket ticketAtualizado) {
        double threshold = 0.70;

        try {
            String prompt = """
        Você é um especialista em manutenção predial do Terminal Rodoviário de Campinas.
        Sua tarefa é determinar se dois tickets se referem ao MESMO PROBLEMA FÍSICO/TÉCNICO, mesmo que a descrição tenha sido refinada ou corrigida.
        
        IMPORTANTE: Considere o mesmo problema quando:
        - O defeito/problema técnico é o mesmo (ex: vazamento, rachadura, lâmpada queimada)
        - A localização é a mesma ou muito próxima
        - Apenas houve correção/melhoria na descrição ou detalhes adicionais
        
        Considere problema DIFERENTE quando:
        - O tipo de defeito mudou completamente (ex: de elétrico para hidráulico)
        - A localização mudou significativamente (ex: de banheiro para área de embarque)
        - É claramente um problema novo e não relacionado
        
        TICKET ORIGINAL:
        - Andar: %s
        - Localização: %s
        - Detalhes da localização: %s
        - Descrição do problema: %s
        
        TICKET ATUALIZADO:
        - Andar: %s
        - Localização: %s
        - Detalhes da localização: %s
        - Descrição do problema: %s
        
        Analise se estes tickets se referem ao mesmo problema técnico/físico.
        Responda SOMENTE com um número decimal entre 0 e 1, onde:
        - 1.0 = Definitivamente o mesmo problema
        - 0.8-0.9 = Muito provavelmente o mesmo problema com mais detalhes
        - 0.5-0.7 = Pode ser o mesmo problema, mas com mudanças significativas
        - 0.0-0.4 = Problemas diferentes
        """.formatted(
                    ticketOriginal.getAndar(),
                    ticketOriginal.getLocalizacao(),
                    ticketOriginal.getDescricaoLocalizacao(),
                    ticketOriginal.getDescricaoTicketUsuario(),
                    ticketAtualizado.getAndar(),
                    ticketAtualizado.getLocalizacao(),
                    ticketAtualizado.getDescricaoLocalizacao(),
                    ticketAtualizado.getDescricaoTicketUsuario()
            );

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
                    .header("X-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            String rawText = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();

            double similarityScore = Double.parseDouble(rawText);

            return similarityScore >= threshold;

        } catch (NumberFormatException e) {
            System.err.println("Erro ao parsear resposta da IA para similaridade: " + e.getMessage());
            return false;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Erro ao chamar API do Gemini para comparação de tickets", e);
        }
    }

    public record GeminiResult(PrioridadeTicket prioridade, EquipeResponsavel equipeResponsavel) {}
}
