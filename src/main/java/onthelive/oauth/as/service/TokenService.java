package onthelive.oauth.as.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import onthelive.oauth.as.model.*;
import onthelive.oauth.as.repository.TokenRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

@Slf4j
@Service
public class TokenService {
    private final ModelMapper modelMapper;
    private TokenRepository tokenRepository;

    /* TODO onthelive.kr.authServer.configuration.RsaKeyGenerator.initRsaKey() 를 통해 서버 실행 시점에 RSA 키를 생성하고 있음.
                    키 생성 시점 및 방법에 대해 고려할 것. */
    private final RSAKey initRsaKey;

    @Autowired
    public TokenService(ModelMapper modelMapper,
                        TokenRepository tokenRepository,
                        RSAKey initRsaKey) {
        this.modelMapper = modelMapper;
        this.tokenRepository = tokenRepository;
        this.initRsaKey = initRsaKey;
    }

    public void createToken(Token token) {
        tokenRepository.insertToken(token);
    }

    public Token getTokenByRefreshToken(String refreshToken) { return tokenRepository.selectTokenByRefreshToken(refreshToken); }

    public void removeTokenByRefreshToken(String refreshToken) { tokenRepository.deleteTokenByRefreshToken(refreshToken); }

    public TokenTransfer convertTokenResponseTransferByTokenResponse(Token token) {
        return TokenTransfer.builder()
                .client_id(token.getClientId())
                .access_token(token.getAccessToken())
                .refresh_token(token.getRefreshToken())
                .token_type(token.getTokenType())
                .scope(token.getScopes())
                .id_token(token.getSerializedIdToken())
                .build();
    }

    public String getAccessToken(String clientId) throws JOSEException {
        // RS256 알고리즘의 비대칭 시그니처
        JWSSigner signer = new RSASSASigner(initRsaKey);

        HashMap<String, Object> payload = new HashMap<>();
        payload.put("iss", "http://localhost:8091/");
        payload.put("sub", clientId);
        payload.put("aud", "http://localhost:9002/");
        payload.put("iat", LocalDateTime.now().toString());
        payload.put("exp", LocalDateTime.now().plusMinutes(5).toString());
        payload.put("iat", new Date().getTime());
        payload.put("exp", new Date().getTime() + (1000 * 60 * 5));
        payload.put("jti", RandomStringUtils.randomAlphanumeric(8));

        JWSObject jwsObject = new JWSObject(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(initRsaKey.getKeyID())
                        .build(),
                new Payload(payload)
        );

        jwsObject.sign(signer);

        String access_token = jwsObject.serialize();
        return access_token;
    }

//    private String getAccessToken(String clientId) throws JOSEException {
        // RS256 알고리즘의 대칭 시그니처
//        String stringSharedSecret = "shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!";
//        byte[] sharedSecret = stringSharedSecret.getBytes();
//
//        HashMap<String, Object> payload = new HashMap<>();
//        payload.put("iss", "http://localhost:8091/");
//        payload.put("sub", clientId);
//        payload.put("aud", "http://localhost:9002/");
//        payload.put("iat", LocalDateTime.now().toString());
//        payload.put("exp", LocalDateTime.now().plusMinutes(5).toString());
//        payload.put("iat", new Date().getTime());
//        payload.put("exp", new Date().getTime() + (1000 * 60 * 5));
//        payload.put("jti", RandomStringUtils.randomAlphanumeric(8));
//
//        // HS256을 이용한 대칭 시그니처
//        // TODO 시크릿의 최소 크기는 256비트임.
//        JWSSigner signer = new MACSigner(sharedSecret);
//
//        JWSObject jwsObject = new JWSObject(
//                new JWSHeader(JWSAlgorithm.HS256), new Payload(payload)
//        );
//
//        jwsObject.sign(signer);
//
//        String access_token = jwsObject.serialize();
//        return access_token;
//    }
}
