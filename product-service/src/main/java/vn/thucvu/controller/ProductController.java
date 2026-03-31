package vn.thucvu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.thucvu.controller.request.ProductCreationRequest;
import vn.thucvu.controller.request.ProductUpdateRequest;
import vn.thucvu.controller.response.ApiResponse;
import vn.thucvu.service.ProductService;

@RestController
@RequiredArgsConstructor
@Slf4j(topic = "PRODUCT-CONTROLLER")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    public ApiResponse getAllProducts(@RequestParam(required = false) String name) {
        log.info("Get all products by {}", name);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("product list")
                .data(productService.searchProduct(name))
                .build();
    }

    @GetMapping("/{productId}")
    public ApiResponse getProductDetail(@PathVariable(required = false) long productId) {
        log.info("Get product detail, id={}", productId);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("product list")
                .data(productService.getProductById(productId))
                .build();
    }

    @PostMapping("/add")
    public ApiResponse addProduct(@Valid @RequestBody ProductCreationRequest request) {
        log.info("Add new product");

        return ApiResponse.builder()
                .status(HttpStatus.CREATED.value())
                .message("Product created successfully")
                .data(productService.addProduct(request))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse updateProduct(@RequestBody ProductUpdateRequest request) {
        log.info("Update product");

        productService.updateProduct(request);

        return ApiResponse.builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message("Product updated successfully")
                .build();
    }

    @DeleteMapping("/{productId}")
    public ApiResponse deleteProduct(@PathVariable long productId) {
        log.info("Remove product: {}", productId);

        productService.deleteProduct(productId);

        return ApiResponse.builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message("Product deleted successfully")
                .build();
    }
}
