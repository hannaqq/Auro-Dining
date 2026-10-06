package com.aurodining.repository;

import com.aurodining.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByPhone(String phone);

    User findByEmail(String email);
}
