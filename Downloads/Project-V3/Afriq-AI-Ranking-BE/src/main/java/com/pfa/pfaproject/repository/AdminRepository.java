package com.pfa.pfaproject.repository;

import com.pfa.pfaproject.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByUsername(String username);
    Admin findByUsernameOrEmail(String username, String email);
    Admin findByEmail(String email);
    boolean existsByUsernameOrEmail(String username, String email);
}

