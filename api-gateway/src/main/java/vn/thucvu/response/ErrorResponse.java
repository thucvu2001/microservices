package vn.thucvu.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ErrorResponse implements Serializable {
    private Date timestamp;
    private String path;
    private int status;
    private String error;
    private String message;
}
