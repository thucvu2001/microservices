package vn.thucvu.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import vn.thucvu.model.PermissionHash;

@Repository
public interface PermissionRepository extends CrudRepository<PermissionHash, String> {
}
