package vn.thucvu.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@Builder
@ToString
public class CheckPermissionResponse implements Serializable {
    private int status;
    private String path;
    private String message;
}
