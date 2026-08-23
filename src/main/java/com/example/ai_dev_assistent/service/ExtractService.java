package com.example.ai_dev_assistent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@Slf4j
public class ExtractService {

    public static String[] extractOwnerAndRepo(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Repository URL or identifier must not be empty");
        }

        String cleaned = url.trim();

        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        if (cleaned.endsWith(".git")) {
            cleaned = cleaned.substring(0, cleaned.length() - 4);
        }

        // Support plain "owner/repo" format
        if (!cleaned.contains("://") && !cleaned.startsWith("github.com")) {
            String[] parts = cleaned.split("/");
            if (parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank()) {
                return new String[]{parts[0], parts[1]};
            }
        }

        if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
            cleaned = "https://" + cleaned;
        }

        URI uri = URI.create(cleaned);
        String path = uri.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        String[] parts = path.split("/");
        if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalArgumentException("Invalid GitHub URL or format: " + url + ". Expected format: https://github.com/owner/repo or owner/repo");
        }

        String owner = parts[0];
        String repo = parts[1];

        return new String[]{owner, repo};
    }
}