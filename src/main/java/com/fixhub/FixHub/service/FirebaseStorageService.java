package com.fixhub.FixHub.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
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
            if (storage == null) {
                storage = StorageOptions.getDefaultInstance().getService();
            }

            // Gerar nome único para o arquivo
            String nomeOriginal = arquivo.getOriginalFilename();
            String extensao = "";
            if (nomeOriginal != null && nomeOriginal.contains(".")) {
                extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
            }
            String nomeArquivo = "tickets/" + UUID.randomUUID().toString() + extensao;

            log.info("Fazendo upload da imagem: {} ({}KB)", nomeOriginal, arquivo.getSize() / 1024);

            // Criar BlobInfo
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, nomeArquivo)
                    .setContentType(contentType)
                    .build();

            // Fazer upload
            Blob blob = storage.create(blobInfo, arquivo.getBytes());

            // Retornar URL pública
            String url = String.format("%s/%s/%s", baseUrl, bucketName, nomeArquivo);
            log.info("✓ Imagem enviada com sucesso: {}", url);
            return url;

        } catch (Exception e) {
            log.error("Erro ao fazer upload da imagem: {}", e.getMessage(), e);
            throw new IOException("Erro ao fazer upload da imagem para o Firebase: " + e.getMessage(), e);
        }
    }

    public void deletarImagem(String imagemUrl) {
        if (imagemUrl == null || imagemUrl.isBlank()) {
            return;
        }

        // Não deletar URLs placeholder
        if (imagemUrl.contains("placeholder") || imagemUrl.contains("via.placeholder")) {
            log.debug("URL placeholder detectada, ignorando deleção");
            return;
        }

        // Se Firebase não estiver habilitado, apenas logar
        if (!firebaseEnabled) {
            log.warn("Firebase desabilitado. Deleção de imagem ignorada: {}", imagemUrl);
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase não está inicializado. Deleção de imagem ignorada: {}", imagemUrl);
            return;
        }

        try {
            if (storage == null) {
                storage = StorageOptions.getDefaultInstance().getService();
            }

            // Extrair o nome do arquivo da URL
            // Exemplo: https://storage.googleapis.com/bucket-name/tickets/uuid.jpg
            String[] parts = imagemUrl.split("/");
            if (parts.length < 2) {
                log.warn("URL inválida para deletar: {}", imagemUrl);
                return;
            }

            String nomeArquivo = parts[parts.length - 1];
            String caminhoCompleto = "tickets/" + nomeArquivo;

            log.info("Deletando imagem: {}", caminhoCompleto);

            // Deletar do Firebase
            boolean deleted = storage.delete(bucketName, caminhoCompleto);

            if (deleted) {
                log.info("✓ Imagem deletada com sucesso: {}", caminhoCompleto);
            } else {
                log.warn("Imagem não encontrada no Firebase: {}", caminhoCompleto);
            }

        } catch (Exception e) {
            log.error("Erro ao deletar imagem do Firebase: {}", e.getMessage());
            // Não lançar exceção para não bloquear outras operações
        }
    }

    private String gerarUrlPlaceholder() {
        return "https://via.placeholder.com/400x300/CCCCCC/666666?text=Imagem+Nao+Disponivel";
    }
}