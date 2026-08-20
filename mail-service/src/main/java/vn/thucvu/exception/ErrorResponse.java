package vn.thucvu.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Represents the error response body returned by all exception handlers.
 */
@Getter
@Setter
public class ErrorResponse {
    private Date timestamp;
    private int status;
    private String path;
    private String error;
    private String message;
    private String traceId;
}
