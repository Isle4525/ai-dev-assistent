package com.example.ai_dev_assistent.repository;

import com.example.ai_dev_assistent.domain.GitHubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GitHubRepositoryRepository extends JpaRepository<GitHubRepository, Long> {
    Optional<GitHubRepository> findByOwnerAndName(String owner, String name);
}
