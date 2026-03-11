package com.fsse2510.fsse2510_project_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:}")
    private String firebaseCredentialsPath;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = null;
                try {
                    if (firebaseCredentialsPath != null && !firebaseCredentialsPath.isBlank()) {
                        log.info("Initializing Firebase using credentials path: {}", firebaseCredentialsPath);
                        serviceAccount = new FileInputStream(firebaseCredentialsPath);
                    } else {
                        log.info("Initializing Firebase using bundled resource credentials.");
                        serviceAccount = getClass().getClassLoader()
                                .getResourceAsStream("fsse2510-project-john-firebase-adminsdk.json");
                        if (serviceAccount == null) {
                            throw new RuntimeException("Firebase Admin key json not found in resources!");
                        }
                    }

                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();

                    FirebaseApp.initializeApp(options);
                    log.info("Firebase Admin SDK initialized successfully!");
                } finally {
                    if (serviceAccount != null) {
                        try {
                            serviceAccount.close();
                        } catch (IOException e) {
                            log.warn("Failed to close Firebase service account stream", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
        }
    }
}
