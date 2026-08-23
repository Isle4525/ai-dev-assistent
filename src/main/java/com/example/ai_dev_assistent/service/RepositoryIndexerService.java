package com.example.ai_dev_assistent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHBlob;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RepositoryIndexerService {

    private final GitHub gitHub;
    private final VectorStore vectorStore;

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            ".java", ".kt", ".scala", ".groovy",
            ".py", ".js", ".jsx", ".ts", ".tsx",
            ".go", ".rs", ".cpp", ".c", ".h", ".cs", ".php", ".rb",
            ".sql", ".xml", ".yml", ".yaml", ".properties", ".json",
            ".md", "Dockerfile"
    );

    private static final Set<String> IGNORED_PATHS = Set.of(
            ".git", ".idea", ".mvn", ".gradle", "node_modules", "target", "build", "dist", "out",
            "package-lock.json", "yarn.lock", "pnpm-lock.yaml", "gradle-wrapper.jar"
    );

    private static final int MAX_FILE_SIZE_BYTES = 200 * 1024; // 200 KB per file limit to avoid large binaries/dumps

    /**
     * Indexes the entire repository into pgvector VectorStore.
     *
     * @param owner repository owner
     * @param repoName repository name
     * @return count of indexed chunks
     */
    public int indexRepository(String owner, String repoName) throws IOException {
        String repoIdentifier = owner + "/" + repoName;
        log.info("Starting indexing for repository: {}", repoIdentifier);

        GHRepository repository = gitHub.getRepository(repoIdentifier);
        String defaultBranch = repository.getDefaultBranch();

        GHTree tree = repository.getTreeRecursive(defaultBranch, 1);
        List<Document> rawDocuments = new ArrayList<>();

        for (GHTreeEntry entry : tree.getTree()) {
            if (!"blob".equalsIgnoreCase(entry.getType())) {
                continue;
            }

            String path = entry.getPath();
            if (shouldIgnore(path)) {
                continue;
            }

            if (entry.getSize() > MAX_FILE_SIZE_BYTES) {
                log.debug("Skipping large file: {} (size: {} bytes)", path, entry.getSize());
                continue;
            }

            try {
                GHBlob blob = entry.asBlob();
                String content;
                try (InputStream in = blob.read()) {
                    content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }

                if (content.isBlank() || isBinaryContent(content)) {
                    continue;
                }

                String fileName = extractFileName(path);
                String extension = extractExtension(path);

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("repository", repoIdentifier);
                metadata.put("filePath", path);
                metadata.put("fileName", fileName);
                metadata.put("extension", extension);

                rawDocuments.add(new Document(content, metadata));
            } catch (Exception e) {
                log.warn("Failed to read file {} in {}: {}", path, repoIdentifier, e.getMessage());
            }
        }

        log.info("Collected {} source files for {}. Splitting into chunks...", rawDocuments.size(), repoIdentifier);

        // Split large files into smaller chunks for optimal embedding retrieval
        TokenTextSplitter splitter = new TokenTextSplitter(800, 350, 10, 100, true);
        List<Document> chunkedDocuments = splitter.apply(rawDocuments);

        if (!chunkedDocuments.isEmpty()) {
            log.info("Saving {} chunks to pgvector for {}...", chunkedDocuments.size(), repoIdentifier);
            vectorStore.add(chunkedDocuments);
            log.info("Successfully indexed {} chunks for repository {}", chunkedDocuments.size(), repoIdentifier);
        } else {
            log.warn("No documents to index for repository {}", repoIdentifier);
        }

        return chunkedDocuments.size();
    }

    private boolean shouldIgnore(String path) {
        for (String ignored : IGNORED_PATHS) {
            if (path.startsWith(ignored + "/") || path.contains("/" + ignored + "/") || path.endsWith("/" + ignored) || path.equals(ignored)) {
                return true;
            }
        }

        String lower = path.toLowerCase();
        boolean hasSupportedExt = SUPPORTED_EXTENSIONS.stream().anyMatch(lower::endsWith);
        return !hasSupportedExt;
    }

    private boolean isBinaryContent(String content) {
        int length = Math.min(content.length(), 512);
        for (int i = 0; i < length; i++) {
            if (content.charAt(i) == '\0') {
                return true;
            }
        }
        return false;
    }

    private String extractFileName(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    private String extractExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot) : "";
    }
}
