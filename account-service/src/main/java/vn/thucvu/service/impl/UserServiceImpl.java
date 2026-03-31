package vn.thucvu.service.impl;


import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.thucvu.common.UserStatus;
import vn.thucvu.controller.request.UserCreationRequest;
import vn.thucvu.controller.request.UserPasswordRequest;
import vn.thucvu.controller.request.UserUpdateRequest;
import vn.thucvu.controller.response.UserPageResponse;
import vn.thucvu.controller.response.UserResponse;
import vn.thucvu.exception.InvalidDataException;
import vn.thucvu.exception.ResourceNotFoundException;
import vn.thucvu.model.Address;
import vn.thucvu.model.User;
import vn.thucvu.repository.AddressRepository;
import vn.thucvu.repository.UserRepository;
import vn.thucvu.service.UserService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic}")
    private String topic;


    @Override
    public UserPageResponse getAllUsers(String keyword, String sort, int page, int size) {
        log.info("getAllUsers");

        // Sorting
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // tencot:asc|desc
            Matcher matcher = pattern.matcher(sort);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    order = new Sort.Order(Sort.Direction.ASC, columnName);
                } else {
                    order = new Sort.Order(Sort.Direction.DESC, columnName);
                }
            }
        }

        // Xu ly paging
        int pageNo = 0;
        if (page > 0) {
            pageNo = page - 1;
        }

        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        Page<User> userPage;
        if (StringUtils.hasLength(keyword)) {
            // search by keyword
            keyword = "%" + keyword.trim().toLowerCase() + "%";
            userPage = userRepository.searchByKeyword(keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return getUserPageResponse(page, size, userPage);
    }

    private UserPageResponse getUserPageResponse(int page, int size, Page<User> userPage) {
        log.info("getUserPageResponse");
        List<UserResponse> userResponseList = userPage.stream()
                .map(entity -> UserResponse.builder()
                        .id(entity.getId())
                        .firstName(entity.getFirstName())
                        .lastName(entity.getLastName())
                        .gender(entity.getGender())
                        .birthday(entity.getBirthday())
                        .username(entity.getUsername())
                        .phone(entity.getPhone())
                        .email(entity.getEmail())
                        .build()).toList();

        return UserPageResponse.builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .users(userResponseList)
                .build();
    }

    @Override
    public UserResponse getUserDetail(Long id) {
        log.info("getUserDetail");
        User user = getUserById(id);
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long saveUser(UserCreationRequest req) {
        log.info("saveUser");

        // check email
        User userByEmail = userRepository.findByEmail(req.getEmail());
        if (userByEmail != null) {
            throw new InvalidDataException("`Email already exists");
        }

        // gan gia tri
        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setBirthday(req.getBirthday());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setType(req.getType());
        user.setStatus(UserStatus.NONE);

        User result = userRepository.save(user);

        Long userId = result.getId();

        if (result != null) {
            // luu dia chi

            List<Address> addresses = new ArrayList<>();
            req.getAddresses().forEach(address -> {
                Address addressEntity = new Address();
                addressEntity.setApartmentNumber(address.getApartmentNumber());
                addressEntity.setFloor(address.getFloor());
                addressEntity.setBuilding(address.getBuilding());
                addressEntity.setStreetNumber(address.getStreetNumber());
                addressEntity.setStreet(address.getStreet());
                addressEntity.setCity(address.getCity());
                addressEntity.setCountry(address.getCountry());
                addressEntity.setAddressType(address.getAddressType());
                addressEntity.setUserId(userId);
                addresses.add(addressEntity);
            });
            addressRepository.saveAll(addresses);
            log.info("Save addresses: {}", addresses);


        }

        // Send email
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("id", userId);
        message.put("email", req.getEmail());
        message.put("username", req.getUsername());
        message.put("secretCode", RandomStringUtils.randomAlphabetic(6));
        String jsonMessage = new Gson().toJson(message);
        kafkaTemplate.send(topic, jsonMessage);
        log.info("Send confirm account message: {}", jsonMessage);
        return userId;
    }

    @Override
    public void updateUser(UserUpdateRequest req) {

    }

    @Override
    public void changePassword(UserPasswordRequest req) {

    }

    @Override
    public void deleteUser(long id) {

    }

    private User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
