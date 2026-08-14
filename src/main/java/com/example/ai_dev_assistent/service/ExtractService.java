package com.example.ai_dev_assistent.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@Slf4j
public class ExtractService {

    public static String[] extractOwnerAndRepo(String url) {

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        URI uri = URI.create(url);
        String path = uri.getPath();

        String[] parts = path.split("/");

        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
        String owner = parts[1];
        String repo = parts[2];

        return new String[]{owner, repo};

    }

}