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
/*
* ResponseEntity 로 전달 될 객체임.
* Client Application 에서 Json 으로 받을 때, Key 값이 변수명 과 동일해야 전달 받아짐.
* */
public class Token {
    private long id;
    private String clientId;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String scopes;
    private String serializedIdToken;

    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
}