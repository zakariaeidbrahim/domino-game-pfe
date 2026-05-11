package com.pfe.dominogame.game.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEngine {
  private static final SecureRandom RNG = new SecureRandom();

  public static GameState newWaitingState(String joinCode, String player1) {
    GameState s = new GameState();
    s.joinCode = joinCode;
    s.player1 = player1;
    return s;
  }

  public static GameState start(GameState s, String player2) {
    if (s.player2 != null) throw new IllegalStateException("Game already has 2 players");
    s.player2 = player2;

    List<DominoTile> tiles = fullSet();
    Collections.shuffle(tiles, RNG);

    s.hand1 = new ArrayList<DominoTile>(tiles.subList(0, 7));
    s.hand2 = new ArrayList<DominoTile>(tiles.subList(7, 14));
    s.stock = new ArrayList<DominoTile>(tiles.subList(14, tiles.size()));
    s.chain = new ArrayList<DominoTile>();

    String starter = decideStarter(s.player1, s.hand1, s.player2, s.hand2);
    s.currentTurn = starter;

    // first move: largest double from starter, else force re-deal (simplified: just pick best double; if none for both, reshuffle)
    for (int attempt = 0; attempt < 20; attempt++) {
      StarterMove m = findLargestDouble(starter.equals(s.player1) ? s.hand1 : s.hand2);
      StarterMove mOther = findLargestDouble(starter.equals(s.player1) ? s.hand2 : s.hand1);
      if (m.found || mOther.found) {
        StarterMove chosen = m;
        String chosenPlayer = starter;
        if (!m.found || (mOther.found && mOther.value > m.value)) {
          chosen = mOther;
          chosenPlayer = starter.equals(s.player1) ? s.player2 : s.player1;
          s.currentTurn = chosenPlayer;
        }
        DominoTile tile = removeAt(chosenPlayer.equals(s.player1) ? s.hand1 : s.hand2, chosen.index);
        placeFirst(s, tile);
        s.currentTurn = opponentOf(s, chosenPlayer);
        return s;
      }
      // redeal
      Collections.shuffle(tiles, RNG);
      s.hand1 = new ArrayList<DominoTile>(tiles.subList(0, 7));
      s.hand2 = new ArrayList<DominoTile>(tiles.subList(7, 14));
      s.stock = new ArrayList<DominoTile>(tiles.subList(14, tiles.size()));
    }
    // fallback: place first tile from player1
    DominoTile t = s.hand1.remove(0);
    placeFirst(s, t);
    s.currentTurn = s.player2;
    return s;
  }

  public static void play(GameState s, String player, int handIndex, String side) {
    ensurePlayableState(s, player);

    List<DominoTile> hand = player.equals(s.player1) ? s.hand1 : s.hand2;
    if (handIndex < 0 || handIndex >= hand.size()) throw new IllegalArgumentException("Invalid hand index");
    DominoTile tile = hand.get(handIndex);

    if (s.chain.isEmpty()) {
      hand.remove(handIndex);
      placeFirst(s, tile);
      s.currentTurn = opponentOf(s, player);
      return;
    }

    int left = s.leftEnd.intValue();
    int right = s.rightEnd.intValue();

    boolean canLeft = tile.a == left || tile.b == left;
    boolean canRight = tile.a == right || tile.b == right;
    if (!canLeft && !canRight) throw new IllegalArgumentException("Tile not playable");

    String normalizedSide = side == null ? "" : side.trim().toUpperCase();
    if ("LEFT".equals(normalizedSide) && !canLeft) throw new IllegalArgumentException("Cannot play on LEFT");
    if ("RIGHT".equals(normalizedSide) && !canRight) throw new IllegalArgumentException("Cannot play on RIGHT");

    // auto choose if ambiguous
    String chosen = normalizedSide;
    if (!"LEFT".equals(chosen) && !"RIGHT".equals(chosen)) {
      chosen = canLeft ? "LEFT" : "RIGHT";
    }

    hand.remove(handIndex);
    if ("LEFT".equals(chosen)) {
      DominoTile oriented = orientToMatchLeft(tile, left);
      s.chain.add(0, oriented);
      s.leftEnd = oriented.a;
    } else {
      DominoTile oriented = orientToMatchRight(tile, right);
      s.chain.add(oriented);
      s.rightEnd = oriented.b;
    }

    if (hand.isEmpty()) {
      finishByEmptyHand(s);
      return;
    }

    s.currentTurn = opponentOf(s, player);
  }

  public static void draw(GameState s, String player) {
    ensurePlayableState(s, player);

    if (hasPlayableMove(s, player)) {
      throw new IllegalStateException("You still have a playable move");
    }
    if (s.stock.isEmpty()) {
      // cannot draw -> must pass
      s.currentTurn = opponentOf(s, player);
      checkBlockedFinish(s);
      return;
    }
    DominoTile drawn = s.stock.remove(0);
    List<DominoTile> hand = player.equals(s.player1) ? s.hand1 : s.hand2;
    hand.add(drawn);

    // if now playable, keep turn (player can play)
    if (!hasPlayableMove(s, player)) {
      s.currentTurn = opponentOf(s, player);
      checkBlockedFinish(s);
    }
  }

  private static void ensurePlayableState(GameState s, String player) {
    if (s.finished) throw new IllegalStateException("Game finished");
    if (s.player2 == null) throw new IllegalStateException("Waiting for second player");
    if (!player.equals(s.currentTurn)) throw new IllegalStateException("Not your turn");
    if (!player.equals(s.player1) && !player.equals(s.player2)) throw new IllegalArgumentException("Not a player in this game");
  }

  private static void placeFirst(GameState s, DominoTile tile) {
    s.chain.add(tile);
    s.leftEnd = tile.a;
    s.rightEnd = tile.b;
  }

  private static void finishByEmptyHand(GameState s) {
    s.finished = true;
    s.finishReason = "EMPTY_HAND";
    s.winner = opponentByLowestRemainingPoints(s);
  }

  private static void checkBlockedFinish(GameState s) {
    if (!s.stock.isEmpty()) return;
    boolean p1 = hasPlayableMove(s, s.player1);
    boolean p2 = hasPlayableMove(s, s.player2);
    if (p1 || p2) return;

    s.finished = true;
    s.finishReason = "BLOCKED";

    int p1Points = totalPoints(s.hand1);
    int p2Points = totalPoints(s.hand2);
    if (p1Points == p2Points) s.winner = "DRAW";
    else s.winner = p1Points < p2Points ? s.player1 : s.player2;
  }

  private static String opponentByLowestRemainingPoints(GameState s) {
    // winner is the one who emptied hand (current turn owner just played)
    int p1Points = totalPoints(s.hand1);
    int p2Points = totalPoints(s.hand2);
    if (p1Points == 0 && p2Points != 0) return s.player1;
    if (p2Points == 0 && p1Points != 0) return s.player2;
    // fallback
    if (p1Points == p2Points) return "DRAW";
    return p1Points < p2Points ? s.player1 : s.player2;
  }

  private static int totalPoints(List<DominoTile> hand) {
    int sum = 0;
    for (DominoTile t : hand) sum += t.points();
    return sum;
  }

  private static DominoTile orientToMatchLeft(DominoTile t, int leftEnd) {
    // new tile becomes first, its b must match previous leftEnd; so resulting a is new leftEnd
    if (t.b == leftEnd) return new DominoTile(t.a, t.b);
    if (t.a == leftEnd) return new DominoTile(t.b, t.a);
    throw new IllegalArgumentException("Cannot match left");
  }

  private static DominoTile orientToMatchRight(DominoTile t, int rightEnd) {
    // new tile becomes last, its a must match previous rightEnd; so resulting b is new rightEnd
    if (t.a == rightEnd) return new DominoTile(t.a, t.b);
    if (t.b == rightEnd) return new DominoTile(t.b, t.a);
    throw new IllegalArgumentException("Cannot match right");
  }

  public static boolean hasPlayableMove(GameState s, String player) {
    List<DominoTile> hand = player.equals(s.player1) ? s.hand1 : s.hand2;
    if (s.chain.isEmpty()) return !hand.isEmpty();
    int left = s.leftEnd.intValue();
    int right = s.rightEnd.intValue();
    for (DominoTile t : hand) {
      if (t.a == left || t.b == left || t.a == right || t.b == right) return true;
    }
    return false;
  }

  private static List<DominoTile> fullSet() {
    List<DominoTile> tiles = new ArrayList<DominoTile>(28);
    for (int i = 0; i <= 6; i++) {
      for (int j = i; j <= 6; j++) {
        tiles.add(new DominoTile(i, j));
      }
    }
    return tiles;
  }

  private static String decideStarter(String p1, List<DominoTile> h1, String p2, List<DominoTile> h2) {
    int best1 = bestDoubleValue(h1);
    int best2 = bestDoubleValue(h2);
    if (best1 == best2) return p1; // tie -> p1
    return best1 > best2 ? p1 : p2;
  }

  private static int bestDoubleValue(List<DominoTile> hand) {
    int best = -1;
    for (DominoTile t : hand) {
      if (t.isDouble()) best = Math.max(best, t.a);
    }
    return best;
  }

  private static class StarterMove {
    boolean found;
    int index;
    int value;
  }

  private static StarterMove findLargestDouble(List<DominoTile> hand) {
    StarterMove m = new StarterMove();
    m.found = false;
    m.value = -1;
    m.index = -1;
    for (int i = 0; i < hand.size(); i++) {
      DominoTile t = hand.get(i);
      if (t.isDouble() && t.a > m.value) {
        m.found = true;
        m.value = t.a;
        m.index = i;
      }
    }
    return m;
  }

  private static DominoTile removeAt(List<DominoTile> list, int idx) {
    DominoTile t = list.get(idx);
    list.remove(idx);
    return t;
  }

  private static String opponentOf(GameState s, String player) {
    return player.equals(s.player1) ? s.player2 : s.player1;
  }

  public static String toJson(ObjectMapper om, GameState s) {
    try {
      return om.writeValueAsString(s);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Cannot serialize game state");
    }
  }

  public static GameState fromJson(ObjectMapper om, String json) {
    try {
      return om.readValue(json, GameState.class);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot parse game state");
    }
  }
}

