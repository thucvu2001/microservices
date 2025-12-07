package vn.thucvu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.thucvu.model.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
