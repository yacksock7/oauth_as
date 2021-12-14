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
public class Client {
    private long id;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scopes;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;

}
