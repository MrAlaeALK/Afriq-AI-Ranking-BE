package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Document findByYear(Integer year);
    List<Document> findAllByOrderByYearDesc();
    boolean existsByYear(Integer year);
} 