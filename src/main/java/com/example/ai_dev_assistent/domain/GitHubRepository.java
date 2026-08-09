package com.example.ai_dev_assistent.domain;


import jakarta.persistence.*;

@Entity
@Table(name = "github_repository")
public class GitHubRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String owner;

    private String name;

    private String url;


}
