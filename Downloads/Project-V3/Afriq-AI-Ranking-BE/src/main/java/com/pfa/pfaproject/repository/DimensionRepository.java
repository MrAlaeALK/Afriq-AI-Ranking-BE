package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.Dimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DimensionRepository extends JpaRepository<Dimension, Long> {
    Dimension findByName(String name);
    List<Dimension> findAllByOrderByDisplayOrderAsc();
    boolean existsByName(String name);
} 