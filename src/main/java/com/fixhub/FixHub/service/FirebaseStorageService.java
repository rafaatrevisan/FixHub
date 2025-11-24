package com.fixhub.FixHub.service;

import com.google.cloud.storage.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class FirebaseStorageService {

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    @Value("${firebase.storage.base-url:https://storage.googleapis.com}")
    private String baseUrl;

    @Value("${firebase.storage.enabled:false}")
    private boolean firebaseEnabled;

    private Storage storage;

    public String uploadImagem(MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            log.debug("Arquivo de imagem não fornecido");
            return null;
        }

        // Se Firebase não estiver habilitado, retornar URL placeholder
        if (!firebaseEnabled) {
            log.warn("Firebase desabilitado. Retornando URL placeholder");
            return gerarUrlPlaceholder();
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.error("Firebase não está inicializado! Verifique as configurações.");
            return gerarUrlPlaceholder();
        }

        // Validar tipo de arquivo
        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Apenas arquivos de imagem são permitidos. Tipo recebido: " + contentType);
        }

        // Validar tamanho (máximo 5MB)
        long tamanhoMB = arquivo.getSize() / (1024 * 1024);
        if (arquivo.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Imagem deve ter no máximo 5MB. Tamanho atual: " + tamanhoMB + "MB");
        }

        try {
            // Pega o bucket usando o Firebase Admin (com suas credenciais)
            Bucket bucket = StorageClient.getInstance().bucket(bucketName);

            // Gera nome único
            String nomeOriginal = arquivo.getOriginalFilename();
            String extensao = "";
            if (nomeOriginal != null && nomeOriginal.contains(".")) {
                extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
            }
            String nomeArquivo = "tickets/" + UUID.randomUUID() + extensao;

            log.info("Enviando imagem: {}", nomeArquivo);

            bucket.create(nomeArquivo, arquivo.getBytes(), contentType);

            String url = String.format("%s/%s/%s", baseUrl, bucketName, nomeArquivo);
            log.info("✓ Imagem enviada: {}", url);

            return url;

        } catch (Exception e) {
            log.error("Erro ao enviar imagem: {}", e.getMessage(), e);
            throw new IOException("Erro ao enviar imagem para o Firebase: " + e.getMessage());
        }
    }

    public void deletarImagem(String imagemUrl) {
        if (imagemUrl == null || imagemUrl.isBlank()) return;

        // URLs placeholder não são deletadas
        if (imagemUrl.contains("placeholder") || imagemUrl.contains("via.placeholder")) {
            log.debug("URL placeholder detectada, ignorando deleção");
            return;
        }

        if (!firebaseEnabled) {
            log.warn("Firebase desabilitado. Deleção ignorada: {}", imagemUrl);
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase não inicializado. Ignorando deleção.");
            return;
        }

        try {
            // --> USANDO O ADMIN SDK CORRETAMENTE
            Bucket bucket = StorageClient.getInstance().bucket(bucketName);

            // Descobre o caminho do arquivo no Storage
            String[] parts = imagemUrl.split("/");
            String nomeArquivo = parts[parts.length - 1];
            String caminhoCompleto = "tickets/" + nomeArquivo;

            log.info("Deletando imagem: {}", caminhoCompleto);

            // Deletar arquivo
            boolean deleted = bucket.get(caminhoCompleto).delete();

            if (deleted) {
                log.info("✓ Imagem deletada com sucesso: {}", caminhoCompleto);
            } else {
                log.warn("Imagem não encontrada no bucket: {}", caminhoCompleto);
            }

        } catch (Exception e) {
            log.error("Erro ao deletar imagem: {}", e.getMessage(), e);
        }
    }

    private String gerarUrlPlaceholder() {
        return "https://via.placeholder.com/400x300/CCCCCC/666666?text=Imagem+Nao+Disponivel";
    }
}