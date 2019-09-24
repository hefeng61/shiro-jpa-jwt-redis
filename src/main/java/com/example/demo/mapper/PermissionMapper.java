package com.example.demo.mapper;

import com.example.demo.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionMapper extends JpaRepository<Permission, Integer> {

    @Query(value = "select p.* from permission p left join role_permission rp on p.id=rp.permission_id left join role r on r.id=rp.role_id where r.name=?1", nativeQuery = true)
    List<Permission> findByRoleName(String name);
}
