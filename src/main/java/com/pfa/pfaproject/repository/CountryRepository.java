package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findByName(String name);
    List<Country> findByCode(String code);
    List<Country> findByRegion(String region);
}

