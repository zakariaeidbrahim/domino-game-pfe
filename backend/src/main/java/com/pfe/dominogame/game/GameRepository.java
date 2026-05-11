package com.pfe.dominogame.game;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
  Optional<Game> findByJoinCode(String joinCode);
}

