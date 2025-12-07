package vn.thucvu.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.thucvu.common.Gender;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Builder
public class UserResponse implements Serializable {

    private Long id;
    private String firstName;
    private String lastName;
    private Gender gender;
    private Date birthday;
    private String username;
    private String email;
    private String phone;
}
