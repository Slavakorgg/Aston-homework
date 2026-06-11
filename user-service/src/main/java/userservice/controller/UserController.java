package userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import userservice.dto.UserRequestDto;
import userservice.model.UserModel;
import userservice.model.UserModelAssembler;
import userservice.service.UserService;

@Tag(name = "Пользователи", description = "CRUD API для управления пользователями") //http://localhost:8080/swagger-ui.html
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserModelAssembler userModelAssembler;

    public UserController(UserService userService, UserModelAssembler userModelAssembler) {
        this.userService = userService;
        this.userModelAssembler = userModelAssembler;
    }

    @Operation(summary = "Получить всех пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей")
    @GetMapping
    public CollectionModel<UserModel> getAllUsers() {
        return userModelAssembler.toCollectionModel(userService.getAllUsers());
    }

    @Operation(summary = "Получить пользователя по id")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @GetMapping("/{id}")
    public ResponseEntity<UserModel> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(userModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Создать пользователя")
    @ApiResponse(responseCode = "201", description = "Пользователь создан")
    @PostMapping
    public ResponseEntity<UserModel> createUser(@RequestBody UserRequestDto request) {
        UserModel created = userModelAssembler.toModel(
                userService.createUser(request.getName(), request.getEmail(), request.getAge())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Обновить пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь обновлён")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @PutMapping("/{id}")
    public ResponseEntity<UserModel> updateUser(@PathVariable Long id, @RequestBody UserRequestDto request) {
        return userService.updateUser(id, request.getName(), request.getEmail(), request.getAge())
                .map(userModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Удалить пользователя")
    @ApiResponse(responseCode = "204", description = "Пользователь удалён")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
