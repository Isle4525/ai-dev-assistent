package com.example.ai_dev_assistent.controller;

import com.example.ai_dev_assistent.domain.CodeReview;
import com.example.ai_dev_assistent.dto.CodeReviewReport;
import com.example.ai_dev_assistent.dto.Request;
import com.example.ai_dev_assistent.service.CodeReviewService;
import com.example.ai_dev_assistent.service.ExtractService;
import com.example.ai_dev_assistent.service.GitHubRepositoryService;
import com.example.ai_dev_assistent.service.RepositoryIndexerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "AI Code Review", description = "Endpoints for RAG indexing, MCP exploration and AI-powered Code Review")
@Slf4j
@RequiredArgsConstructor
public class CodeReviewController {

    private final RepositoryIndexerService indexerService;
    private final CodeReviewService reviewService;
    private final GitHubRepositoryService repositoryService;

    @PostMapping
    @Operation(summary = "Perform AI Code Review on a GitHub Repository",
            description = "Indexes repository files into pgvector (RAG) and runs an AI Code Review agent with MCP tools")
    public ResponseEntity<CodeReviewReport> reviewRepository(@RequestBody Request request) throws Exception {
        log.info("Received code review request for: {}", request.getUrl());

        String[] repoInfo = ExtractService.extractOwnerAndRepo(request.getUrl());
        String owner = repoInfo[0];
        String repo = repoInfo[1];

        // 1. Save or update repository record
        repositoryService.create(request);

        // 2. Index codebase into pgvector VectorStore (RAG)
        int chunksIndexed = indexerService.indexRepository(owner, repo);
        log.info("Indexed {} chunks for {}/{}", chunksIndexed, owner, repo);

        // 3. Run AI Code Review agent
        CodeReviewReport report = reviewService.reviewRepository(owner, repo);

        return ResponseEntity.ok(report);
    }

    @PostMapping("/index")
    @Operation(summary = "Index repository into Vector Store",
            description = "Fetches code files from GitHub, chunks them and saves embeddings into pgvector")
    public ResponseEntity<Map<String, Object>> indexRepository(@RequestBody Request request) throws Exception {
        String[] repoInfo = ExtractService.extractOwnerAndRepo(request.getUrl());
        String owner = repoInfo[0];
        String repo = repoInfo[1];

        int chunks = indexerService.indexRepository(owner, repo);
        return ResponseEntity.ok(Map.of(
                "repository", owner + "/" + repo,
                "chunksIndexed", chunks,
                "status", "SUCCESS"
        ));
    }

    @GetMapping("/history")
    @Operation(summary = "Get review history for a specific repository")
    public ResponseEntity<List<CodeReview>> getHistory(
            @RequestParam String owner,
            @RequestParam String repo) {
        return ResponseEntity.ok(reviewService.getReviewHistory(owner, repo));
    }

    @GetMapping
    @Operation(summary = "Get all previous code reviews across repositories")
    public ResponseEntity<List<CodeReview>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }
}
