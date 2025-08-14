package bitc.full502.project2back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinResponseDTO {
    private boolean success;
    private String message;
    private String userDay;
}
