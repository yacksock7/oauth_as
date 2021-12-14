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
public class Test {
    private String grant_type;
    private String code;
    private String redirect_uri;
    private String client_id;
    private String client_secret;

    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}
