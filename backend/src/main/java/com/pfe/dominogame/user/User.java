package com.pfe.dominogame.user;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 80)
  private String username;

  @Column(nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false, length = 30)
  private String role = "USER";

  protected User() {}

  public User(String username, String passwordHash) {
    this.username = username;
    this.passwordHash = passwordHash;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getRole() {
    return role;
  }
}

