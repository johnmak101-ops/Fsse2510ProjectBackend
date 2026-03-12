package com.fsse2510.fsse2510_project_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.Resource;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:}")
    private String firebaseCredentialsPath;

    @Value("classpath:fsse2510-project-john-firebase-adminsdk.json")
    private Resource defaultCredentials;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream serviceAccount = getCredentialsStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully!");
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
            throw new RuntimeException("Firebase Admin SDK initialization failed — app cannot start without auth", e);
        }
    }

    private InputStream getCredentialsStream() throws IOException {
        if (firebaseCredentialsPath != null && !firebaseCredentialsPath.isBlank()) {
            log.info("Initializing Firebase using credentials file: {}", firebaseCredentialsPath);
            return new org.springframework.core.io.FileSystemResource(firebaseCredentialsPath).getInputStream();
        }
        
        log.info("Initializing Firebase using bundled resource credentials.");
        if (defaultCredentials == null || !defaultCredentials.exists()) {
            throw new RuntimeException("Firebase Admin key json not found in resources!");
        }
        return defaultCredentials.getInputStream();
    }
}
