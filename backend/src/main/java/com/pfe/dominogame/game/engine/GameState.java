package com.pfe.dominogame.game.engine;

import java.util.ArrayList;
import java.util.List;

public class GameState {
  public String joinCode;
  public String player1;
  public String player2;
  public String currentTurn;

  public List<DominoTile> stock = new ArrayList<DominoTile>();
  public List<DominoTile> chain = new ArrayList<DominoTile>();
  public List<DominoTile> hand1 = new ArrayList<DominoTile>();
  public List<DominoTile> hand2 = new ArrayList<DominoTile>();

  public Integer leftEnd;
  public Integer rightEnd;

  public int score1 = 0;
  public int score2 = 0;

  public boolean finished = false;
  public String winner; // username or "DRAW"
  public String finishReason;
}

