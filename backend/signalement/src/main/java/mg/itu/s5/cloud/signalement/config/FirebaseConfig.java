package mg.itu.s5.cloud.signalement.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials-file:/app/config/firebase-credentials.json}")
    private String firebaseCredentialsFile;

    private boolean firebaseInitialized = false;

    @PostConstruct
    public void initializeFirebase() {
        try {
            // Créer les credentials à partir du fichier JSON avec les scopes nécessaires
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream(firebaseCredentialsFile)
            ).createScoped(Arrays.asList(
                "https://www.googleapis.com/auth/firebase.messaging",
                "https://www.googleapis.com/auth/cloud-platform"
            ));

            // Configurer les options Firebase
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            // Initialiser Firebase
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                firebaseInitialized = true;
                logger.info("Firebase initialized successfully from file: {}", firebaseCredentialsFile);
            } else {
                firebaseInitialized = true;
                logger.info("Firebase already initialized");
            }
        } catch (FileNotFoundException e) {
            logger.warn("Firebase credentials file not found: {}. Firebase features will be disabled.", firebaseCredentialsFile);
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }

    public boolean isFirebaseInitialized() {
        return firebaseInitialized;
    }
}