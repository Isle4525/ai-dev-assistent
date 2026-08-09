package com.example.ai_dev_assistent.repository;

import com.example.ai_dev_assistent.domain.GitHubRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitHubRepositoryRepository extends CrudRepository<GitHubRepository, Long> {
}
