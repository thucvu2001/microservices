package vn.thucvu.service;

import vn.thucvu.controller.request.ProductCreationRequest;
import vn.thucvu.controller.request.ProductUpdateRequest;
import vn.thucvu.model.ProductDocument;

import java.util.List;

public interface ProductService {
    List<ProductDocument> searchProduct(String name);

    ProductDocument getProductById(Long id);

    long addProduct(ProductCreationRequest request);

    void updateProduct(ProductUpdateRequest product);

    void deleteProduct(long productId);
}
