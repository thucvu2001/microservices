package vn.thucvu.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TransactionType {
    @JsonProperty("in")
    IN,
    @JsonProperty("out")
    OUT
}
