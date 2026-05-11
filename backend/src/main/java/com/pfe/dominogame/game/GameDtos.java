package com.pfe.dominogame.game;

import javax.validation.constraints.NotBlank;

public class GameDtos {
  public static class CreateGameResponse {
    public Long gameId;
    public String joinCode;
    public CreateGameResponse(Long gameId, String joinCode) {
      this.gameId = gameId;
      this.joinCode = joinCode;
    }
  }

  public static class JoinGameRequest {
    @NotBlank
    public String joinCode;
  }

  public static class PlayMoveRequest {
    public int handIndex;
    public String side; // LEFT / RIGHT
  }
}

