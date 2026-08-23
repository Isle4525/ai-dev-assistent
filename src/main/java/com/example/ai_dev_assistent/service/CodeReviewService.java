package com.example.ai_dev_assistent.service;

import com.example.ai_dev_assistent.domain.CodeReview;
import com.example.ai_dev_assistent.dto.CodeReviewReport;
import com.example.ai_dev_assistent.repository.CodeReviewRepository;
import com.example.ai_dev_assistent.tools.GitHubMcpTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CodeReviewService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;
    private final GitHubMcpTools gitHubMcpTools;
    private final CodeReviewRepository codeReviewRepository;
    private final ObjectMapper objectMapper;

    /**
     * Conducts a comprehensive AI Code Review using RAG and MCP tools.
     */
    public CodeReviewReport reviewRepository(String owner, String repo) {
        String repoIdentifier = owner + "/" + repo;
        log.info("Starting AI Code Review for {}", repoIdentifier);

        // 1. Retrieve most relevant context from pgvector using multi-aspect queries
        List<Document> architectureChunks = queryVectorStore(repoIdentifier, "application entry point architecture configuration controller service", 5);
        List<Document> securityChunks = queryVectorStore(repoIdentifier, "security authentication authorization password sql query injection endpoint input validation", 5);
        List<Document> qualityChunks = queryVectorStore(repoIdentifier, "exception error handling business logic performance database transaction", 5);

        // Combine unique chunks
        Map<String, Document> uniqueChunks = new java.util.LinkedHashMap<>();
        for (Document doc : architectureChunks) {
            uniqueChunks.put(doc.getId(), doc);
        }
        for (Document doc : securityChunks) {
            uniqueChunks.put(doc.getId(), doc);
        }
        for (Document doc : qualityChunks) {
            uniqueChunks.put(doc.getId(), doc);
        }

        String ragContext = uniqueChunks.values().stream()
                .map(doc -> {
                    String filePath = (String) doc.getMetadata().getOrDefault("filePath", "unknown");
                    return "--- File: " + filePath + " ---\n" + doc.getText();
                })
                .collect(Collectors.joining("\n\n"));

        // 2. Prepare System Prompt & ChatClient
        String systemPrompt = """
            You are a Senior Principal Software Engineer, Tech Lead, and Application Security Auditor.
            Your mission is to perform a comprehensive, highly insightful, and constructive Code Review of the GitHub repository `%s`.

            Review criteria:
            1. Security: Check for vulnerabilities (hardcoded secrets, SQL injection, improper auth/validation, unsafe deserialization, etc.).
            2. Architecture & Design: Evaluate SOLID principles, modularity, layering, dependency injection, and separation of concerns.
            3. Code Quality & Bugs: Check for null safety, concurrency issues, transaction management, error handling, and code smells.
            4. Performance & Scalability: Look for N+1 queries, memory leaks, unclosed resources, inefficient algorithms.
            5. Best Practices & Maintainability: Testability, logging, naming conventions, and documentation.

            You have access to MCP Tools if you need to read full files or inspect directories.
            Provide concrete file paths, severity levels (CRITICAL, HIGH, MEDIUM, LOW, INFO), and actionable suggestions for every finding.
            Be direct, accurate, and provide realistic quality scores from 1 (poor) to 10 (exceptional).
            """.formatted(repoIdentifier);

        String userPrompt = """
            Repository: {repository}

            Here are relevant source code snippets retrieved from the project via RAG:
            {context}

            Please analyze the code thoroughly and return a structured Code Review Report.
            """;

        PromptTemplate template = new PromptTemplate(userPrompt, Map.of(
                "repository", repoIdentifier,
                "context", ragContext.isBlank() ? "No RAG context indexed yet. Please use tools to explore the repository." : ragContext
        ));

        ChatClient chatClient = chatClientBuilder.build();

        log.info("Invoking AI model with RAG context ({} chunks) and MCP tools...", uniqueChunks.size());

        CodeReviewReport report = chatClient.prompt(new Prompt(template.createMessage()))
                .system(systemPrompt)
                .tools(gitHubMcpTools)
                .call()
                .entity(CodeReviewReport.class);

        // 3. Persist review in database
        saveReview(owner, repo, report);

        log.info("Completed AI Code Review for {} with score {}", repoIdentifier, report.qualityScore());
        return report;
    }

    public List<CodeReview> getReviewHistory(String owner, String repo) {
        return codeReviewRepository.findByRepositoryOwnerAndRepositoryNameOrderByCreatedAtDesc(owner, repo);
    }

    public List<CodeReview> getAllReviews() {
        return codeReviewRepository.findAll();
    }

    private List<Document> queryVectorStore(String repoIdentifier, String query, int topK) {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(0.4)
                    .filterExpression("repository == '" + repoIdentifier + "'")
                    .build();
            return vectorStore.similaritySearch(request);
        } catch (Exception e) {
            log.warn("Vector search failed for query '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    private void saveReview(String owner, String repo, CodeReviewReport report) {
        try {
            String json = objectMapper.writeValueAsString(report);
            CodeReview entity = CodeReview.builder()
                    .repositoryOwner(owner)
                    .repositoryName(repo)
                    .qualityScore(report.qualityScore())
                    .summary(report.summary())
                    .reportJson(json)
                    .build();
            codeReviewRepository.save(entity);
            log.info("Saved review history for {}/{}", owner, repo);
        } catch (Exception e) {
            log.error("Failed to persist code review to database: {}", e.getMessage());
        }
    }
}
