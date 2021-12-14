package onthelive.oauth.as.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String sub;
    private String preferred_username;
    private String name;
    private String email;
    private boolean email_verified;

}
