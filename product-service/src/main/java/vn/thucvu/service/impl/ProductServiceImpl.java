package vn.thucvu.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.thucvu.controller.request.ProductCreationRequest;
import vn.thucvu.controller.request.ProductUpdateRequest;
import vn.thucvu.exception.ResourceNotFoundException;
import vn.thucvu.model.Product;
import vn.thucvu.model.ProductDocument;
import vn.thucvu.repository.ProductRepository;
import vn.thucvu.repository.ProductSearchRepository;
import vn.thucvu.service.ProductService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "PRODUCT-SERVICE")
public class ProductServiceImpl implements ProductService {

    private final ProductSearchRepository productSearchRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addProduct(ProductCreationRequest request) {
        log.info("Add product {}", request);

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUserId(request.getUserId());

        Product result = productRepository.save(product);

        if (result.getId() != null) {
            ProductDocument productDocument = new ProductDocument();
            productDocument.setId(product.getId());
            productDocument.setName(request.getName());
            productDocument.setDescription(request.getDescription());
            productDocument.setPrice(request.getPrice());
            productDocument.setUserId(request.getUserId());

            productSearchRepository.save(productDocument);
            log.info("ProductDocument saved with id {}", productDocument.getId());
        }

        return product.getId();
    }

    @Override
    public List<ProductDocument> searchProduct(String name) {
        log.info("Search products by name {}", name);

        List<ProductDocument> productDocuments = new ArrayList<>();

        if (StringUtils.hasLength(name)) {
            productDocuments = productSearchRepository.findByNameContaining(name);
        } else {
            Iterable<ProductDocument> documents = productSearchRepository.findAll();
            for (ProductDocument productDocument : documents) {
                productDocuments.add(productDocument);
            }
        }

        return productDocuments;
    }

    @Override
    public ProductDocument getProductById(Long id) {
        log.info("Get product by id, id={}", id);
        return getProductDocumentById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductUpdateRequest request) {
        log.info("Update product {}", request);

        Product product = getProductById(request.getId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setUserId(request.getUserId());

        productRepository.save(product);

        if (product.getId() != null) {
            ProductDocument productDocument = getProductDocumentById(request.getId());
            productDocument.setId(product.getId());
            productDocument.setName(request.getName());
            productDocument.setDescription(request.getDescription());
            productDocument.setPrice(request.getPrice());
            productDocument.setUserId(request.getUserId());

            productSearchRepository.save(productDocument);

            log.info("Update productDocument: {}", productDocument);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(long productId) {
        productRepository.deleteById(productId);
        productSearchRepository.deleteById(productId);
    }

    /**
     * Get product information by product id.
     *
     * @param id product identifier
     * @return product detail information
     */
    private Product getProductById(long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    /**
     * Get product document information by product id.
     *
     * @param id product identifier
     * @return product document information
     */
    private ProductDocument getProductDocumentById(long id) {
        return productSearchRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product document not found"));
    }
}
