package com.i9brgroup.jbarreto.facial_auth_i9.web.controller;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.UserServiceImpl;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.UserLoginRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ActivateStatusResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ListUserResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.UserCreateResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserCreateResponse> createNewAccount(@RequestBody @Valid UserLoginRequest userLoginRequest){
        var user = userService.createNew(userLoginRequest);

        return ResponseEntity.ok().body(new UserCreateResponse(user));
    }

    @GetMapping("/list-users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<ListUserResponse>> getFilteredUsers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String orderBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        var usersPage = userService.getAllUsersPageable(page, size, orderBy, direction);
        return ResponseEntity.ok().body(usersPage);
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ActivateStatusResponse> deleteById(@PathVariable Long id) {
        var result = userService.toggleActive(id);

        return ResponseEntity.ok().body(new ActivateStatusResponse(result, result ? "Usuário ativado." : "Usuário desativado."));
    }


}
