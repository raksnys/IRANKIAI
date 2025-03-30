package com.irankiai.backend.Container;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContainerRepository extends JpaRepository<Container, Integer> {
    // Custom query methods can be added here if needed
}