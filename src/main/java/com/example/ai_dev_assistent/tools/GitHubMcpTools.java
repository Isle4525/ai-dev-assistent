package com.example.ai_dev_assistent.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class GitHubMcpTools {

    private final GitHub gitHub;

    @Tool(description = "Read the complete source code or text content of a specific file from a GitHub repository given owner, repo and relative file path")
    public String readFileContent(String owner, String repo, String filePath) {
        log.info("Tool called: readFileContent for {}/{} at {}", owner, repo, filePath);
        try {
            GHRepository repository = gitHub.getRepository(owner + "/" + repo);
            GHContent content = repository.getFileContent(filePath);
            try (InputStream in = content.read()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Tool error reading file {}: {}", filePath, e.getMessage());
            return "Error reading file " + filePath + ": " + e.getMessage();
        }
    }

    @Tool(description = "List files and subdirectories in a specific folder path of a GitHub repository")
    public String listDirectory(String owner, String repo, String path) {
        log.info("Tool called: listDirectory for {}/{} at path: {}", owner, repo, path);
        try {
            GHRepository repository = gitHub.getRepository(owner + "/" + repo);
            String targetPath = (path == null || path.isBlank()) ? "" : path;
            List<GHContent> contents = repository.getDirectoryContent(targetPath);

            return contents.stream()
                    .map(item -> (item.isDirectory() ? "[DIR]  " : "[FILE] ") + item.getPath() + " (" + item.getSize() + " bytes)")
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("Tool error listing directory {}: {}", path, e.getMessage());
            return "Error listing directory " + path + ": " + e.getMessage();
        }
    }

    @Tool(description = "Get general metadata and statistics of a GitHub repository (description, stars, primary language, default branch)")
    public String getRepositoryInfo(String owner, String repo) {
        log.info("Tool called: getRepositoryInfo for {}/{}", owner, repo);
        try {
            GHRepository repository = gitHub.getRepository(owner + "/" + repo);
            return """
                    Repository: %s
                    Description: %s
                    Language: %s
                    Default Branch: %s
                    Stars: %d
                    Open Issues: %d
                    """.formatted(
                    repository.getFullName(),
                    repository.getDescription(),
                    repository.getLanguage(),
                    repository.getDefaultBranch(),
                    repository.getStargazersCount(),
                    repository.getOpenIssueCount()
            );
        } catch (Exception e) {
            log.error("Tool error getting info: {}", e.getMessage());
            return "Error retrieving repository information: " + e.getMessage();
        }
    }
}
