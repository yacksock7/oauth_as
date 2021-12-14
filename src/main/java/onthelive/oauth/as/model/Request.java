package onthelive.oauth.as.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String requestId;
    private String responseType;
    private String redirectUri;
    private String state;
    private String clientId;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
