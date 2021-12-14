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
public class TokenTransfer {
    private long id;
    private String client_id;
    private String access_token;
    private String refresh_token;
    private String token_type;
    private String scope;
    private String id_token;

    private LocalDateTime created_datetime;
    private LocalDateTime updated_datetime;
}
