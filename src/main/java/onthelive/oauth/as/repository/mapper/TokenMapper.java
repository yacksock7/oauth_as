package onthelive.oauth.as.repository.mapper;


import onthelive.oauth.as.model.Token;

public interface TokenMapper {
    void insertToken(Token token);
    Token selectTokenByRefreshToken(String clientId);
    void deleteTokenByRefreshToken(String refreshToken);
}
