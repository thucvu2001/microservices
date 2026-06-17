package vn.thucvu.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.thucvu.controller.request.CheckPermissionRequest;
import vn.thucvu.controller.response.CheckPermissionResponse;
import vn.thucvu.controller.response.PermissionResponse;
import vn.thucvu.service.AuthorizationService;

import java.util.List;

@RestController
@Slf4j(topic = "ROLE-AND-PERMISSION-CONTROLLER")
public class RoleAndPermissionController {

    private final AuthorizationService authorizationService;

    public RoleAndPermissionController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping("/roles")
    public List<Long> getRoleIdsByUsername(@RequestParam String username) {
        log.info("getRoleIdsByUsername called");
        return authorizationService.getRoleIdsByUsername(username);
    }

    @GetMapping("/{roleId}/permissions")
    public List<PermissionResponse> getPermissionsByRoleId(@PathVariable Integer roleId) {
        log.info("getPermissionsByRoleId called");
        return authorizationService.getPermissionsByRoleId(roleId);
    }

    @PostMapping("/check-permissions")
    public CheckPermissionResponse countPermissionByRequestPathAndUser(@Valid @RequestBody CheckPermissionRequest request) {
        log.info("countPermissionByRequestPathAndUser called");
        return authorizationService.countPermissionByRequestPathAndUser(request);
    }
}
