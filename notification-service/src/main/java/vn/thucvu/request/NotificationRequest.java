package vn.thucvu.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NotificationRequest {
    private List<String> playerIds;
    private String message;
}
