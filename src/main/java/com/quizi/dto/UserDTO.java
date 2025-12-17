package com.quizi.dto;

import java.sql.Timestamp;

public class UserDTO {
    private Long id;
    private String email;
    private String password;
    private String name;
    private String role; // [추가됨] 권한 (USER or ADMIN)
    private Timestamp createdAt;

    // 기본 생성자
    public UserDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; } // [추가됨]
    public void setRole(String role) { this.role = role; } // [추가됨]

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}