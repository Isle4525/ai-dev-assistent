package com.example.ai_dev_assistent.controller;

import com.example.ai_dev_assistent.dto.Request;
import com.example.ai_dev_assistent.dto.Response;
import com.example.ai_dev_assistent.service.GitHubRepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/repositories")
@Tag(name = "GitHub Repositories", description = "Endpoints for managing saved GitHub repositories")
@RequiredArgsConstructor
public class GitHubRepositoryController {

    private final GitHubRepositoryService gitHubRepositoryService;

    @PostMapping
    @Operation(summary = "Register or update a GitHub repository")
    public ResponseEntity<Response> create(@RequestBody Request request) throws Exception {
        return ResponseEntity.ok(gitHubRepositoryService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all tracked GitHub repositories")
    public ResponseEntity<List<Response>> getAll() {
        return ResponseEntity.ok(gitHubRepositoryService.getAll());
    }
}
