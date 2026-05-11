package com.pfe.dominogame.common;

import java.time.Instant;

public class ApiError {
  public final String message;
  public final Instant timestamp = Instant.now();

  public ApiError(String message) {
    this.message = message;
  }
}

