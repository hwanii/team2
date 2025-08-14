package bitc.full502.project2back.controller;

import bitc.full502.project2back.dto.*;
import bitc.full502.project2back.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입
    @PostMapping("/join")
    public JoinResponseDTO join(@RequestBody JoinRequestDTO request) {
        return userService.joinUser(request);
    }

    // 아이디 중복 체크
    @GetMapping("/check-id")
    public IdCheckResponseDTO checkId(@RequestParam String id) {
        return userService.checkIdDuplicate(id);
    }

    // 로그인
    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody LoginRequestDTO request) {
        return userService.loginUser(request);
    }

    // 회원정보 수정
    @PostMapping("/edit")
    public EditResponseDTO edit(@RequestBody EditRequestDTO request) {
        return userService.updateUser(request);
    }
}
