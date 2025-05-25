package com.irankiai.backend.Grid;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GridRepository extends JpaRepository<Grid, Integer> {
    Optional<Grid> findFirstByXAndYAndZ(int x, int y, int z);

    List<Grid> findAllByXAndYAndZ(int x, int y, int z);
}