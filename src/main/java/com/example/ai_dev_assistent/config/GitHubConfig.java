package com.example.ai_dev_assistent.config;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class GitHubConfig {

    @Value("${github.token:}")
    private String githubToken;

    @Bean
    public GitHub gitHubClient() throws IOException {
        if (githubToken != null && !githubToken.isBlank()) {
            log.info("Connecting to GitHub using provided OAuth token");
            return new GitHubBuilder().withOAuthToken(githubToken).build();
        }
        log.info("Connecting to GitHub anonymously (rate limit: 60 req/hour)");
        return GitHub.connectAnonymously();
    }
}
