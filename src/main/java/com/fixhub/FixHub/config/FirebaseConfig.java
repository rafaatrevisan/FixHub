package com.fixhub.FixHub.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    @Value("${firebase.storage.enabled:false}")
    private boolean firebaseEnabled;

    // → AGORA ESTA VARIÁVEL RECEBE O JSON COMPLETO
    @Value("${firebase.credentials.path:}")
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

                if (credentialsJson == null || credentialsJson.trim().isEmpty()) {
                    log.error("Nenhuma credencial Firebase configurada!");
                    log.error("Defina a variável FIREBASE_CREDENTIALS com o JSON do service account");
                    return;
                }

                log.info("✓ Usando credenciais Firebase via variável de ambiente");

                // Converte a STRING JSON em stream
                InputStream serviceAccount =
                        new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));

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