package com.example.ai_dev_assistent.service;

import com.example.ai_dev_assistent.domain.GitHubRepository;
import com.example.ai_dev_assistent.dto.Request;
import com.example.ai_dev_assistent.dto.Response;
import com.example.ai_dev_assistent.repository.GitHubRepositoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.ai_dev_assistent.service.ExtractService.extractOwnerAndRepo;

@Service
@Slf4j
@RequiredArgsConstructor
public class GitHubRepositoryService {

    private final GitHubRepositoryRepository gitHubRepositoryRepository;

    @Transactional
    private Response create(Request request) throws Exception {

        log.info("Creating GitHub Repository");

        GitHubRepository gitHubRepository = new GitHubRepository();

        String[] repoInfo = extractOwnerAndRepo(request.getUrl());
        String ownerName = repoInfo[0];
        String repoName = repoInfo[1];

        log.info("Connect GitHub API");
        GitHub gitHub = GitHub.connectAnonymously();

        String fullPath = ownerName + "/" + repoName;

        log.info("Connect GitHub Repository {} to User {}", gitHubRepository.getName(), ownerName);
        GHRepository repository = gitHub.getRepository(fullPath);

        gitHubRepository.setName(ownerName);
        gitHubRepository.setOwner(ownerName);
        gitHubRepository.setUrl(request.getUrl());

        gitHubRepositoryRepository.save(gitHubRepository);
        log.info("Created GitHub Repository");
        log.info("Description" + repository.getDescription());
        log.info("Stars" + repository.getStargazersCount());

        return new Response(ownerName, repoName, request.getUrl());

    }

    public List<Response> getAll() {
        log.info("Get All GitHub Repository");
        return gitHubRepositoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // mapper
    private Response toResponse(GitHubRepository gitHubRepository) {
        Response response = new Response();
        response.setUrl(gitHubRepository.getUrl());
        response.setName(gitHubRepository.getName());
        response.setOwner(gitHubRepository.getOwner());
        return  response;
    }



}
