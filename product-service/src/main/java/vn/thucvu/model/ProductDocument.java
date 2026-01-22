package vn.thucvu.model;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;

@Setter
@Getter
@Document(indexName = "product")
public class ProductDocument {

    @Id
    private Long id;
    private String name;
    private String description;
    private double price;
    private int userId;
}
