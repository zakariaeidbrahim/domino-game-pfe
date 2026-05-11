package com.pfe.dominogame.game;

import com.pfe.dominogame.game.GameDtos.CreateGameResponse;
import com.pfe.dominogame.game.GameDtos.JoinGameRequest;
import com.pfe.dominogame.game.GameDtos.PlayMoveRequest;
import com.pfe.dominogame.game.engine.GameState;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class GameController {
  private final GameService gameService;

  public GameController(GameService gameService) {
    this.gameService = gameService;
  }

  @PostMapping
  public ResponseEntity<CreateGameResponse> create(Authentication auth) {
    String username = (String) auth.getPrincipal();
    Game g = gameService.createGame(username);
    return ResponseEntity.ok(new CreateGameResponse(g.getId(), g.getJoinCode()));
  }

  @PostMapping("/join")
  public ResponseEntity<CreateGameResponse> join(Authentication auth, @Valid @RequestBody JoinGameRequest req) {
    String username = (String) auth.getPrincipal();
    Game g = gameService.joinGame(req.joinCode, username);
    return ResponseEntity.ok(new CreateGameResponse(g.getId(), g.getJoinCode()));
  }

  @GetMapping("/{gameId}")
  public ResponseEntity<GameState> state(Authentication auth, @PathVariable Long gameId) {
    String username = (String) auth.getPrincipal();
    return ResponseEntity.ok(gameService.getState(gameId, username));
  }

  @PostMapping("/{gameId}/play")
  public ResponseEntity<GameState> play(Authentication auth, @PathVariable Long gameId, @RequestBody PlayMoveRequest req) {
    String username = (String) auth.getPrincipal();
    return ResponseEntity.ok(gameService.play(gameId, username, req.handIndex, req.side));
  }

  @PostMapping("/{gameId}/draw")
  public ResponseEntity<GameState> draw(Authentication auth, @PathVariable Long gameId) {
    String username = (String) auth.getPrincipal();
    return ResponseEntity.ok(gameService.drawOrPass(gameId, username));
  }
}

