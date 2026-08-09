package com.example.ai_dev_assistent.service;

import com.example.ai_dev_assistent.domain.GitHubRepository;
import com.example.ai_dev_assistent.dto.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubRepositoryService {

    private final GitHubRepositoryService gitHubRepositoryService;




    // mapper
    private Response toResponse(GitHubRepository gitHubRepository) {
        Response response = new Response();

        response.setName(gitHubRepository.getName());
        response.setOwner(gitHubRepository.getOwner());
        response.setUrl(gitHubRepository.getUrl());
        return response;
    }



}
