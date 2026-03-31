package vn.thucvu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import vn.thucvu.model.Order;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    @Query("{ 'statusName' : ?0 }")
    Page<Order> findByStatusName(String statusName, Pageable pageable);
}
