package com.example.ai_dev_assistent.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "github_repository")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String owner;
    private String name;
    private String url;


}
