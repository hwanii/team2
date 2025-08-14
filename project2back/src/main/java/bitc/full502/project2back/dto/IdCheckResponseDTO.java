package bitc.full502.project2back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IdCheckResponseDTO {
    private boolean available;
    private String message;
}
