package onthelive.oauth.as.repository;

import onthelive.oauth.as.model.*;
import onthelive.oauth.as.repository.mapper.AuthorizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AuthorizationRepository {

    private AuthorizationMapper authorizationMapper;

    @Autowired
    public AuthorizationRepository(AuthorizationMapper authorizationMapper) {
        this.authorizationMapper = authorizationMapper;
    }

    public Client selectClient(String client_id) {
        Client client = authorizationMapper.selectClient(client_id);
        if (client == null)
            client = Client.builder()
                    .clientId("")
                    .scopes("")
                    .build();

            return client;
    }

    public void insertRequest(Request request) {
        authorizationMapper.insertRequest(request);
    }

    public Request selectRequest(String requestId) {
        return authorizationMapper.selectRequest(requestId);
    }
    public void deleteRequest(String requestId) {
        authorizationMapper.deleteRequest(requestId);
    }
    public void insertCode(Code code) {
        authorizationMapper.insertCode(code);
    }
    public Code selectCode(String codeId) {
        return authorizationMapper.selectCode(codeId);
    }
    public void deleteCode(String codeId) {
        authorizationMapper.deleteCode(codeId);
    }

    public User getUser(String user) {
        return authorizationMapper.getUser(user);
    }

    public Token saveTokenResponse(Token token) {
        return null;
    }
}
