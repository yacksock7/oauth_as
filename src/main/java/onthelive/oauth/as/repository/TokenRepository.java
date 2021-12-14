package onthelive.oauth.as.repository;

import onthelive.oauth.as.model.*;
import onthelive.oauth.as.repository.mapper.AuthorizationMapper;
import onthelive.oauth.as.repository.mapper.TokenMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TokenRepository {

    private TokenMapper tokenMapper;

    @Autowired
    public TokenRepository(TokenMapper tokenMapper) {
        this.tokenMapper = tokenMapper;
    }

    public void insertToken(Token token) {
        tokenMapper.insertToken(token);
    }

    public Token selectTokenByRefreshToken(String refreshToken) {
        return tokenMapper.selectTokenByRefreshToken(refreshToken);
    }

    public void deleteTokenByRefreshToken(String refreshToken) {
        tokenMapper.deleteTokenByRefreshToken(refreshToken);
    }
}
