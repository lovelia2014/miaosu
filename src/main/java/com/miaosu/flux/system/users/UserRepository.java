package com.miaosu.flux.system.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * User Repository
 * Created by angus on 15/9/28.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Modifying
    @Query("update User u set u.enabled = ?2 where u.username = ?1")
    void setEnabled(String username, boolean enabled);

    @Modifying
    @Query("update User u set u.password = ?3 where u.username= ?1 and u.password = ?2")
    int changePassword(String username, String oldPassword, String newPassword);
}
