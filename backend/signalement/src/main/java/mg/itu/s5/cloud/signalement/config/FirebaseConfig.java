package mg.itu.s5.cloud.signalement.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.project-id:}")
    private String projectId;

    @Value("${firebase.credentials:}")
    private String firebaseCredentialsJson;

    private boolean firebaseInitialized = false;

    @PostConstruct
    public void initializeFirebase() {
        if (firebaseCredentialsJson == null || firebaseCredentialsJson.isEmpty() || firebaseCredentialsJson.equals("{}")) {
            logger.warn("Firebase credentials not configured. Firebase features will be disabled.");
            return;
        }

        if (projectId == null || projectId.isEmpty()) {
            logger.warn("Firebase project ID not configured. Firebase features will be disabled.");
            return;
        }

        try {
            // Créer les credentials à partir du JSON
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8))
            );

            // Configurer les options Firebase
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();

            // Initialiser Firebase
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                firebaseInitialized = true;
                logger.info("Firebase initialized successfully for project: {}", projectId);
            } else {
                firebaseInitialized = true;
                logger.info("Firebase already initialized");
            }
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }

    public boolean isFirebaseInitialized() {
        return firebaseInitialized;
    }
}