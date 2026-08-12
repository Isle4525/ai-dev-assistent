package com.example.ai_dev_assistent.service;

import com.example.ai_dev_assistent.domain.GitHubRepository;
import com.example.ai_dev_assistent.dto.Request;
import com.example.ai_dev_assistent.dto.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import static com.example.ai_dev_assistent.service.GitHubRepoInfoParser.extractOwnerAndRepo;

@Service
@Slf4j
@RequiredArgsConstructor
public class GitHubRepositoryService {

    private final GitHubRepositoryService gitHubRepositoryService;





}
