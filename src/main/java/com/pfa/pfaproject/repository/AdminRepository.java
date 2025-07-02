package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    List<Admin> findByUsername(String username);
    List<Admin> findByUsernameOrEmail(String username, String email);
    List<Admin> findByEmail(String email);
    boolean existsByUsernameOrEmail(String username, String email);
}

