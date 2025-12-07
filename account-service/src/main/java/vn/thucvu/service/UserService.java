package vn.thucvu.service;

import vn.thucvu.controller.request.UserCreationRequest;
import vn.thucvu.controller.request.UserPasswordRequest;
import vn.thucvu.controller.request.UserUpdateRequest;
import vn.thucvu.controller.response.UserPageResponse;
import vn.thucvu.controller.response.UserResponse;

public interface UserService {

    UserPageResponse getAllUsers(String keyword, String sort, int page, int size);

    UserResponse getUserDetail(Long id);

    long saveUser(UserCreationRequest req);

    void updateUser(UserUpdateRequest req);

    void changePassword(UserPasswordRequest req);

    void deleteUser(long id);
}
