package vn.thucvu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.thucvu.model.SaleOrder;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, String> {

}
