package com.leander.lottery.lottery.repository;

import com.leander.lottery.lottery.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 用於登入：根據名稱尋找使用者
    Optional<User> findByName(String name);

    // 用於註冊檢查：判斷名稱是否存在
    boolean existsByName(String name);

    // 用於註冊檢查：判斷 Email 是否存在
    boolean existsByEmail(String email);
}