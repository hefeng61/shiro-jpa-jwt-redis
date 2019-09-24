package com.example.demo.mapper;

import com.example.demo.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMapper extends JpaRepository<Role, Integer> {

    @Query(value = "select r.* from role r left join user_role ur on r.id=ur.role_id left join user u on ur.user_id=u.id where u.account=?1", nativeQuery = true)
    List<Role> findByAccount(String account);
}
