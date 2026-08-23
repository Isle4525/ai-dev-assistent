package com.example.ai_dev_assistent.repository;

import com.example.ai_dev_assistent.domain.CodeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {
    List<CodeReview> findByRepositoryOwnerAndRepositoryNameOrderByCreatedAtDesc(String owner, String name);
}
