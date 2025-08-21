package bitc.full502.project2back.service;

import bitc.full502.project2back.dto.*;
import bitc.full502.project2back.entity.UserEntity;
import bitc.full502.project2back.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 회원가입
    public JoinResponseDTO joinUser(JoinRequestDTO request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            return new JoinResponseDTO(false, "이미 존재하는 ID입니다.");
        }

        UserEntity user = new UserEntity();
        user.setUserName(request.getUserName());
        user.setUserId(request.getUserId());
        user.setUserPw(request.getUserPw());
        user.setUserTel(request.getUserTel());
        user.setUserEmail(request.getUserEmail());

        userRepository.save(user);

        return new JoinResponseDTO(true, "회원가입 성공");
    }

    // 로그인
    public UserResponseDTO loginUser(LoginRequestDTO request) {
        Optional<UserEntity> optionalUser = userRepository.findByUserId(request.getUserId());
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            if (user.getUserPw().equals(request.getUserPw())) {
                UserResponseDTO response = new UserResponseDTO();
                response.setUserKey(user.getUserKey());
                response.setUserName(user.getUserName());
                response.setUserId(user.getUserId());
                response.setUserPw(user.getUserPw());
                response.setUserTel(user.getUserTel());
                response.setUserEmail(user.getUserEmail());
                response.setUserKey(user.getId());
                return response;
            }
        }
        return null; // 로그인 실패
    }

    // 회원정보 수정
    public EditResponseDTO updateUser(EditRequestDTO request) {
        Optional<UserEntity> optionalUser = userRepository.findByUserId(request.getUserId());
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            user.setUserName(request.getUserName());
            if (request.getUserPw() != null && !request.getUserPw().isEmpty()) {
                user.setUserPw(request.getUserPw());
            }
            user.setUserTel(request.getUserTel());
            user.setUserEmail(request.getUserEmail());

            userRepository.save(user);

            return new EditResponseDTO(true, "회원정보 수정 완료");
        }
        return new EditResponseDTO(false, "사용자를 찾을 수 없습니다.");
    }

    // 아이디 중복 체크
    public IdCheckResponseDTO checkIdDuplicate(String userId) {
        boolean available = !userRepository.existsByUserId(userId);
        String message = available ? "사용 가능한 ID입니다." : "이미 존재하는 ID입니다.";
        return new IdCheckResponseDTO(available, message);
    }


    // userKey로 UserEntity 찾기
    public UserEntity findByUserKey(Integer userKey) {
        return userRepository.findById(userKey)
            .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
    }
}
