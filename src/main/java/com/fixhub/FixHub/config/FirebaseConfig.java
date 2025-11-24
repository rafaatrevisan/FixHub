package com.fixhub.FixHub.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    @Value("${firebase.storage.enabled:false}")
    private boolean firebaseEnabled;

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.credentials.json:}")
    private String credentialsJson;

    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) {
            log.info("Firebase Storage está desabilitado (firebase.storage.enabled=false)");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            try {
                log.info("Inicializando Firebase Storage...");

                InputStream serviceAccount = null;

                // Prioridade 1: JSON inline (variável de ambiente ou application.properties)
                if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
                    log.info("✓ Usando credenciais Firebase via JSON inline");
                    serviceAccount = new ClassPathResource("firebase-credentials.json").getInputStream();
                }
                // Prioridade 2: Arquivo externo (caminho absoluto)
                else if (credentialsPath != null && !credentialsPath.trim().isEmpty()) {
                    log.info("✓ Usando credenciais Firebase do arquivo: {}", credentialsPath);
                    serviceAccount = new ClassPathResource("firebase-credentials.json").getInputStream();
                }
                else {
                    log.error("Nenhuma credencial Firebase configurada!");
                    log.error("Configure uma das opções no application.properties:");
                    log.error("  1. firebase.credentials.path=C:/firebase/firebase-credentials.json");
                    log.error("  2. firebase.credentials.json=${FIREBASE_CREDENTIALS_JSON}");
                    log.warn("A aplicação continuará sem Firebase Storage");
                    return;
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket(bucketName)
                        .build();

                FirebaseApp.initializeApp(options);

                log.info("════════════════════════════════════════");
                log.info("✓ Firebase Storage inicializado com sucesso!");
                log.info("✓ Bucket: {}", bucketName);
                log.info("✓ Upload de imagens: ATIVO");
                log.info("════════════════════════════════════════");

            } catch (Exception e) {
                log.error("════════════════════════════════════════");
                log.error("Erro ao inicializar Firebase: {}", e.getMessage());
                log.error("Stack trace:", e);
                log.warn("A aplicação continuará sem Firebase Storage");
                log.warn("Imagens não serão salvas no Firebase");
                log.error("════════════════════════════════════════");
            }
        }
    }
}