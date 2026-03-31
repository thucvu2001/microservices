package vn.thucvu.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.thucvu.model.Order;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderListResponse implements Serializable {
    private int pageNumber;
    private int pageSize;
    private long totalPages;
    private long totalElements;
    private List<Order> orders;
}
