package onthelive.oauth.as.repository.mapper;


import onthelive.oauth.as.model.Client;
import onthelive.oauth.as.model.Code;
import onthelive.oauth.as.model.Request;
import onthelive.oauth.as.model.User;

public interface AuthorizationMapper {
    Client selectClient(String client_id);
    void insertRequest(Request request);
    Request selectRequest(String requestId);
    void deleteRequest(String requestId);
    void insertCode(Code code);

    Code selectCode(String codeId);
    void deleteCode(String codeId);
    User getUser(String user);
}
