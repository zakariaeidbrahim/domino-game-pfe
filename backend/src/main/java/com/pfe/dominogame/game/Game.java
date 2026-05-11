package com.pfe.dominogame.game;

import java.time.Instant;
import javax.persistence.*;

@Entity
@Table(name = "games")
public class Game {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 12)
  private String joinCode;

  @Column(nullable = false)
  private String player1;

  @Column(nullable = true)
  private String player2;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private GameStatus status = GameStatus.WAITING;

  @Lob
  @Column(nullable = false)
  private String stateJson;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  protected Game() {}

  public Game(String joinCode, String player1, String stateJson) {
    this.joinCode = joinCode;
    this.player1 = player1;
    this.stateJson = stateJson;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getJoinCode() {
    return joinCode;
  }

  public String getPlayer1() {
    return player1;
  }

  public String getPlayer2() {
    return player2;
  }

  public void setPlayer2(String player2) {
    this.player2 = player2;
  }

  public GameStatus getStatus() {
    return status;
  }

  public void setStatus(GameStatus status) {
    this.status = status;
  }

  public String getStateJson() {
    return stateJson;
  }

  public void setStateJson(String stateJson) {
    this.stateJson = stateJson;
  }
}

