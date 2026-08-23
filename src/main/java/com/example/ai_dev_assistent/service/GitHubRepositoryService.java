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

import java.io.IOException;
import java.util.List;

import static com.example.ai_dev_assistent.service.ExtractService.extractOwnerAndRepo;

@Service
@Slf4j
@RequiredArgsConstructor
public class GitHubRepositoryService {

    private final GitHubRepositoryRepository gitHubRepositoryRepository;
    private final GitHub gitHub;

    @Transactional
    public Response create(Request request) throws IOException {
        String[] repoInfo = extractOwnerAndRepo(request.getUrl());
        String ownerName = repoInfo[0];
        String repoName = repoInfo[1];

        String fullPath = ownerName + "/" + repoName;
        log.info("Fetching repository info for {}", fullPath);
        GHRepository repository = gitHub.getRepository(fullPath);

        GitHubRepository gitHubRepository = gitHubRepositoryRepository.findByOwnerAndName(ownerName, repoName)
                .orElseGet(GitHubRepository::new);

        gitHubRepository.setName(repoName);
        gitHubRepository.setOwner(ownerName);
        gitHubRepository.setUrl("https://github.com/" + fullPath);
        gitHubRepositoryRepository.save(gitHubRepository);

        log.info("Saved repository {} (Stars: {})", fullPath, repository.getStargazersCount());

        return new Response(ownerName, repoName, gitHubRepository.getUrl());
    }

    public List<Response> getAll() {
        log.info("Get All GitHub Repositories");
        return gitHubRepositoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Response toResponse(GitHubRepository gitHubRepository) {
        Response response = new Response();
        response.setUrl(gitHubRepository.getUrl());
        response.setName(gitHubRepository.getName());
        response.setOwner(gitHubRepository.getOwner());
        return response;
    }
}
