package vn.thucvu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.thucvu.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    @Query(value = "select distinct r.name from Role r " +
            "inner join UserHasRole ur on ur.role.id = r.id " +
            "inner join User u on u.id = ur.user.id " +
            "where u.username = :username")
    List<String> findRolesByUsername(String username);
}
