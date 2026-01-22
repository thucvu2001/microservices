package vn.thucvu.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ProductCreationRequest {

    @NotBlank(message = "name must be not blank")
    private String name;

    private String description;

    @NotNull(message = "price must be not null")
    @Min(value = 1, message = "price must be equals or greater than 1")
    private Double price;

    @NotNull(message = "userId must be not null")
    @Min(value = 1, message = "userId must be equals or greater than 1")
    private Integer userId;
}
