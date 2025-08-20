package bitc.full502.project2back.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Integer userKey;
    private String userName;
    private String userId;
    private String userPw;
    private String userTel;
    private String userEmail;
}
