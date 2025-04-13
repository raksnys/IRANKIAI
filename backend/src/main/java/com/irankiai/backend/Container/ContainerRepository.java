package com.irankiai.backend.Container;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContainerRepository extends JpaRepository<Container, Integer> {
    List<Container> findByProductsId(int productId);
}