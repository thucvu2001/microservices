package vn.thucvu.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class OneSignalNotificationRequest {
    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("include_player_ids")
    private List<String> playerIds; //device token

    @JsonProperty("contents")
    private Map<String, String> contents; //place order success message
}
