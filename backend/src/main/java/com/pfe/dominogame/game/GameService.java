package com.pfe.dominogame.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.dominogame.game.engine.GameEngine;
import com.pfe.dominogame.game.engine.GameState;
import java.security.SecureRandom;
import java.util.Locale;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameService {
  private static final SecureRandom RNG = new SecureRandom();
  private final GameRepository gameRepository;
  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messaging;

  public GameService(GameRepository gameRepository, ObjectMapper objectMapper, SimpMessagingTemplate messaging) {
    this.gameRepository = gameRepository;
    this.objectMapper = objectMapper;
    this.messaging = messaging;
  }

  @Transactional
  public Game createGame(String player1) {
    String code = generateJoinCode();
    GameState state = GameEngine.newWaitingState(code, player1);
    Game game = new Game(code, player1, GameEngine.toJson(objectMapper, state));
    return gameRepository.save(game);
  }

  @Transactional
  public Game joinGame(String joinCode, String player2) {
    String code = joinCode.trim().toUpperCase(Locale.ROOT);
    Game game = gameRepository.findByJoinCode(code).orElseThrow(() -> new IllegalArgumentException("Game not found"));
    if (game.getStatus() != GameStatus.WAITING) throw new IllegalStateException("Game already started");
    if (game.getPlayer1().equals(player2)) throw new IllegalArgumentException("Cannot join your own game");

    GameState state = GameEngine.fromJson(objectMapper, game.getStateJson());
    state = GameEngine.start(state, player2);

    game.setPlayer2(player2);
    game.setStatus(GameStatus.IN_PROGRESS);
    game.setStateJson(GameEngine.toJson(objectMapper, state));
    Game saved = gameRepository.save(game);

    publishState(saved.getId(), state);
    return saved;
  }

  @Transactional(readOnly = true)
  public GameState getState(Long gameId, String username) {
    Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
    GameState state = GameEngine.fromJson(objectMapper, game.getStateJson());
    if (!username.equals(state.player1) && (state.player2 == null || !username.equals(state.player2))) {
      throw new IllegalArgumentException("Not a player in this game");
    }
    // hide opponent hand
    if (username.equals(state.player1)) {
      state.hand2 = java.util.Collections.emptyList();
    } else {
      state.hand1 = java.util.Collections.emptyList();
    }
    return state;
  }

  @Transactional
  public GameState play(Long gameId, String username, int handIndex, String side) {
    Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
    GameState state = GameEngine.fromJson(objectMapper, game.getStateJson());
    GameEngine.play(state, username, handIndex, side);
    if (state.finished) {
      game.setStatus(GameStatus.FINISHED);
    }
    game.setStateJson(GameEngine.toJson(objectMapper, state));
    gameRepository.save(game);
    publishState(gameId, state);
    return stateForPlayer(state, username);
  }

  @Transactional
  public GameState drawOrPass(Long gameId, String username) {
    Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
    GameState state = GameEngine.fromJson(objectMapper, game.getStateJson());
    GameEngine.draw(state, username);
    if (state.finished) {
      game.setStatus(GameStatus.FINISHED);
    }
    game.setStateJson(GameEngine.toJson(objectMapper, state));
    gameRepository.save(game);
    publishState(gameId, state);
    return stateForPlayer(state, username);
  }

  private void publishState(Long gameId, GameState state) {
    messaging.convertAndSend("/topic/games/" + gameId, state);
  }

  private GameState stateForPlayer(GameState s, String username) {
    GameState copy = GameEngine.fromJson(objectMapper, GameEngine.toJson(objectMapper, s));
    if (username.equals(copy.player1)) copy.hand2 = java.util.Collections.emptyList();
    else copy.hand1 = java.util.Collections.emptyList();
    return copy;
  }

  private String generateJoinCode() {
    final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    for (int attempt = 0; attempt < 20; attempt++) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 6; i++) sb.append(alphabet.charAt(RNG.nextInt(alphabet.length())));
      String code = sb.toString();
      if (!gameRepository.findByJoinCode(code).isPresent()) return code;
    }
    throw new IllegalStateException("Could not generate join code");
  }
}

