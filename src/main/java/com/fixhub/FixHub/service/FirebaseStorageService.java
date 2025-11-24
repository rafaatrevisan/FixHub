package com.fixhub.FixHub.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FirebaseStorageService {

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    @Value("${firebase.storage.base-url:https://storage.googleapis.com}")
    private String baseUrl;

    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    public String uploadImagem(MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Apenas arquivos de imagem são permitidos");
        }

        if (arquivo.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Imagem deve ter no máximo 5MB");
        }

        String nomeOriginal = arquivo.getOriginalFilename();
        String extensao = nomeOriginal != null && nomeOriginal.contains(".")
                ? nomeOriginal.substring(nomeOriginal.lastIndexOf("."))
                : "";
        String nomeArquivo = "tickets/" + UUID.randomUUID().toString() + extensao;

        // Criar BlobInfo
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, nomeArquivo)
                .setContentType(contentType)
                .build();

        // Fazer upload
        Blob blob = storage.create(blobInfo, arquivo.getBytes());

        return String.format("%s/%s/%s", baseUrl, bucketName, nomeArquivo);
    }

    public void deletarImagem(String imagemUrl) {
        if (imagemUrl == null || imagemUrl.isBlank()) {
            return;
        }

        try {
            String nomeArquivo = imagemUrl.substring(imagemUrl.lastIndexOf("/") + 1);
            String caminhoCompleto = "tickets/" + nomeArquivo;

            storage.delete(bucketName, caminhoCompleto);
        } catch (Exception e) {
            System.err.println("Erro ao deletar imagem do Firebase: " + e.getMessage());
        }
    }
}