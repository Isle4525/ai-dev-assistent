package com.example.ai_dev_assistent.service;


import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHRepository;
import com.example.ai_dev_assistent.dto.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

@Service
@Slf4j
public class GitHubRepoInfoParser {

//    private Response parseResponse(String url) throws Exception {
//
//        String[] repoInfo = extractOwnerAndRepo(url);
//        String ownerName = repoInfo[0];
//        String repoName = repoInfo[1];
//
//        GitHub gitHub = GitHub.connectAnonymously();
//
//        String fullPath = ownerName + "/" + repoName;
//        GHRepository repository = gitHub.getRepository(fullPath);
//
//        log.info("Description" + repository.getDescription());
//        log.info("Stars" + repository.getStargazersCount());
//
//    }

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