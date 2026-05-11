package com.pfe.dominogame.game.engine;

public class DominoTile {
  public int a;
  public int b;

  public DominoTile() {}

  public DominoTile(int a, int b) {
    this.a = a;
    this.b = b;
  }

  public boolean isDouble() {
    return a == b;
  }

  public int points() {
    return a + b;
  }
}

