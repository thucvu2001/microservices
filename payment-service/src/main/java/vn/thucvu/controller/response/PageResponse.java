package vn.thucvu.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class PageResponse<T> implements Serializable {
    private int pageNumber;
    private int pageSize;
    private long totalPages;
    private long totalElements;
    private T transactions;
}
