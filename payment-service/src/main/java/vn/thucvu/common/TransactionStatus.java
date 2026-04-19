package vn.thucvu.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TransactionStatus {
    @JsonProperty("created")
    CREATED,
    @JsonProperty("succeeded")
    SUCCEEDED,
    @JsonProperty("canceled")
    CANCELED,
    @JsonProperty("failed")
    FAILED
}
