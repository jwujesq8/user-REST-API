package com.test.api.controller;

import com.test.api.dto.DeleteUsersListByIdDto;
import com.test.api.dto.IdDto;
import com.test.api.dto.request.POSTUserRequestDto;
import com.test.api.dto.request.PUTUserRequestDto;
import com.test.api.dto.response.ErrorMessageResponseDto;
import com.test.api.dto.response.UserResponseDto;
import com.test.api.dto.response.ValidationErrorMessageResponseDto;
import com.test.api.service.interfaces.GenderService;
import com.test.api.service.NotificationService;
import com.test.api.service.interfaces.UserService;
//import com.test.api.service.WebSocketNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name="User controller", description="Interaction with users")
public class UserController {

    private final UserService userService;
    private final GenderService genderService;
    private final NotificationService notificationService;
    @Getter
    private static Object requestBody;

    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "get user info (login, full name, gender) by id inside the body request")
    @SecurityRequirement(name = "JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful request to get user by id", content = @Content(schema = @Schema(implementation = UserResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (id not found)", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Non valid token", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Authentication error",  content = @Content(mediaType = "none")),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json"))
    }
    )
    public UserResponseDto getUserById(@Parameter(description = "id inside the body request", required = true)
            @RequestBody @Valid IdDto idDto){

        requestBody = idDto;
        return userService.getUserById(idDto.getId());
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "add new user")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successful request to create new user", content = @Content(schema = @Schema(implementation = UserResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (validation error)", content = @Content(schema = @Schema(implementation = ValidationErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Non valid token", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Authentication error",  content = @Content(mediaType = "none")),
            @ApiResponse(responseCode = "409", description = "Conflict (user is already exists)", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Server error or db error", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json"))
    }
    )
    public UserResponseDto addUser(@Parameter(description = "postUser(without id, it's auto generated) inside the body request", required = true)
            @RequestBody @Valid POSTUserRequestDto postUserRequestDto){

        requestBody = postUserRequestDto;
        String userRequiter = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificationService.sendUserActionNotification(userRequiter, "uses request POST /user/new");
        return userService.addUser(postUserRequestDto);
    }

    @PutMapping("")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "update user info")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successful request to update user info", content = @Content(schema = @Schema(implementation = UserResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (validation error | user not found)", content = @Content(schema = @Schema(implementation = ValidationErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Non valid token", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Authentication error",  content = @Content(mediaType = "none")),
            @ApiResponse(responseCode = "500", description = "Server error or db error", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json"))
    }
    )
    public UserResponseDto updateUser(@Parameter(description = "putUser( with id !!! ) inside the body request", required = true)
            @RequestBody @Valid PUTUserRequestDto putUserRequestDto){

        requestBody = putUserRequestDto;
        return userService.updateUser(putUserRequestDto);
    }

    @DeleteMapping("")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "delete user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful request to delete user by id", content = @Content(schema = @Schema(implementation = UserResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (user not found)", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Authentication error",  content = @Content(mediaType = "none")),
            @ApiResponse(responseCode = "500", description = "Server error or db error", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json"))
    }
    )
    public UserResponseDto deleteUserById(@Parameter(description = "id inside the body request", required = true, content = @Content(
            schema = @Schema(implementation = IdDto.class, example = "{ \"id\": 1}")))
            @RequestBody @Valid IdDto idDto){

            requestBody = idDto;
            String userRequiter = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            notificationService.sendUserActionNotification(userRequiter, "uses request DELETE /user");
            return userService.deleteUser(idDto.getId());
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "get all users list (login, full name, gender)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful request to get all users", content = @Content(schema = @Schema(implementation = UserResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Non valid token", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Authentication error",  content = @Content(mediaType = "none")),
            @ApiResponse(responseCode = "500", description = "Server error or db error", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json"))
    }
    )
    public List<UserResponseDto> getAllUsers(){

        requestBody = null;
        return userService.getAllUsers();

    }

    @DeleteMapping("/list/range")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "delete a list of users by start and end id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful request to delete list of users by id range", content = @Content(schema = @Schema(implementation = UserResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (0 users with id in provided range)", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Non valid token", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Authentication error", content = @Content(mediaType = "none")),
            @ApiResponse(responseCode = "500", description = "Server error or db error", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json"))
    }
    )
    public List<UserResponseDto> deleteListOfUsersByStartAndEndId(@Parameter(description = "start and end users id inside the request body", required = true)
            @RequestBody @Valid DeleteUsersListByIdDto deleteUsersListByIdDto){

        requestBody = deleteUsersListByIdDto;
        String userRequiter = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificationService.sendUserActionNotification(userRequiter, "uses request DELETE user/list/range");
        return userService.deleteListOfUsersByStartAndEndId(
                    deleteUsersListByIdDto.getStartId(),deleteUsersListByIdDto.getEndId()
            );
    }

    @DeleteMapping("/list/asc")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "delete a list of users by start id (ASCENT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful request to delete list of users by start id", content = @Content(schema = @Schema(implementation = UserResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (0 users with id in provided range)", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Non valid token", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Authentication error",  content = @Content(mediaType = "none")),
            @ApiResponse(responseCode = "500", description = "Server error or db error", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json"))
    }
    )
    public List<UserResponseDto> deleteListOfUsersByStartIdAsc(@Parameter(description = "user id inside the request body", required = true)
            @RequestBody @Valid IdDto idDto){

        requestBody = idDto;
        String userRequiter = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        notificationService.sendUserActionNotification(userRequiter, "uses request DELETE user/list/asc");
        return userService.deleteListOfUsersByStartIdAsc(idDto.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/checkGenderTable")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "check if gender table has appropriate content (1-male, 2-female, 3-none)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gender table has an appropriate content", content = @Content(schema = @Schema(implementation = String.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Non valid token", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Authentication error",  content = @Content(mediaType = "none")),
            @ApiResponse(responseCode = "500", description = "Server error or db error", content = @Content(schema = @Schema(implementation = ErrorMessageResponseDto.class), mediaType = "application/json"))
    }
    )
    public ResponseEntity<String> checkGenderTableAndWelcome(){

        genderService.checkGenderTable();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Gender table has an appropriate content");
    }

}

