package onthelive.oauth.as.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import lombok.extern.slf4j.Slf4j;
import onthelive.oauth.as.model.*;
import onthelive.oauth.as.repository.AuthorizationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Slf4j
@Service
public class AuthorizationService {
    private final ModelMapper modelMapper;
    private AuthorizationRepository authorizationRepository;
    private UtilService utilService;

    @Autowired
    public AuthorizationService(ModelMapper modelMapper,
                                AuthorizationRepository authorizationRepository,
                                UtilService utilService) {
        this.modelMapper = modelMapper;
        this.authorizationRepository = authorizationRepository;
        this.utilService = utilService;

    }

//
//    public String getAuthorize(String scopes, Request request) {
//        Client client = authorizationRepository.selectClient(request.getClientId());
//        if (client != null) {
//            ArrayList<String> scopesDiff = scopesDiff(scopes, client.getScopes());
//            if (client.getClientId().equals("")) {
//            log.error("알 수 없는 Client Id 접근 " + request.getClientId());
////            model.addAttribute("error", "Unknown Client Id");
//            return "/error";
////                throw new OauthException(ErrorCode.NotAcceptableId, "NotAcceptableId");
//            } else if (scopesDiff.size() > 0) {
////            log.error("invalid scope error in /authorize");
////            attributes.addAttribute("error", "invalid_scope");
//            return "redirect:"+client.getRedirectUri();
////                throw new OauthException(ErrorCode.InvalidScope, "InvalidScope");
//            } else {
////            String reqId = RandomStringUtils.randomAlphanumeric(8);
////            authorizationService.saveRequest(reqId, request);
////
////            model.addAttribute("client", client);
////            model.addAttribute("reqId", reqId);
////            model.addAttribute("scope", scopes);
//                return "/approve";
//            }
//        } else {
////            throw new OauthException(ErrorCode.NotAcceptableId, "NotAcceptableId");
//            return "/error";
//        }
//    }
    public Client getClient(String client_id) {
        return authorizationRepository.selectClient(client_id);
    }

    public ArrayList<String> scopesDiff(String rScope, String cScope) {
        final ArrayList<String> rScopeList = utilService.getScopes(rScope);
        final ArrayList<String> cScopeList = utilService.getScopes(cScope);

        final ArrayList<String> temp = rScopeList;
        temp.removeAll(cScopeList);

        return temp;
    }

    public void saveRequest(Request request) {
        authorizationRepository.insertRequest(request);
    }

    public Request getRequest(String requestId) {
        return authorizationRepository.selectRequest(requestId);
    }

    public void removeRequest(String requestId) {
        authorizationRepository.deleteRequest(requestId);
    }

    public void saveCode(String codeId, Request request, String scope) {
        Code code = Code.builder()
                .codeId(codeId)
                .requestId(request.getRequestId()) //TODO 삭제된 requestId를 가지고 가는 이유?
                .responseType(request.getResponseType()) //TODO responseType를 계속 가지고 가야하는 이유?
                .redirectUri(request.getRedirectUri())
                .state(request.getState())
                .clientId(request.getClientId())
                .scopes(scope)
                .user(null) //TODO user 사용자 인증?
                .build();


        authorizationRepository.insertCode(code);
    }

    public Code getCode(String codeId) {
        return authorizationRepository.selectCode(codeId);
    }

    public void removeCode(String codeId) {
        authorizationRepository.deleteCode(codeId);
    }

    public String generateSerializedIdToken(Code code) throws JOSEException {
        String stringSharedSecret = "shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!shared OAuth token secret!";
        byte[] sharedSecret = stringSharedSecret.getBytes();

        HashMap<String,Object> payload = new HashMap<>();
        payload.put("iss","http://localhost:8091/");
        payload.put("sub", code.getUser().getSub());
        payload.put("aud",code.getClientId());
        payload.put("iat", new Date().getTime());
        payload.put("exp", new Date().getTime() + (1000 * 60 * 5));

        JWSSigner signer = new MACSigner(sharedSecret);

        JWSObject jwsObject = new JWSObject(
                new JWSHeader(JWSAlgorithm.HS256), new Payload(payload)
        );

        jwsObject.sign(signer);

        return jwsObject.serialize();
    }
    public User getUser(String user) {
        return authorizationRepository.getUser(user);
    }

    public Token saveTokenResponse(Token token) {
        return authorizationRepository.saveTokenResponse(token);
    }

}
