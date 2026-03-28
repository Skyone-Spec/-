package edu.ruc.platform.auth.repository;

import edu.ruc.platform.auth.domain.LatestUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LatestUserRoleRepository extends JpaRepository<LatestUserRole, Long> {

    @Query(value = """
            select r.role_code
            from sys_user_role ur
            join sys_role r on r.id = ur.role_id
            where ur.user_id = :userId
            order by ur.id asc
            """, nativeQuery = true)
    List<String> findRoleCodesByUserId(Long userId);
}
