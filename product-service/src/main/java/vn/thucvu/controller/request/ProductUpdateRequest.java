package vn.thucvu.controller.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ProductUpdateRequest {
    private long id;
    private String name;
    private String description;
    private double price;
    private int userId;
}
