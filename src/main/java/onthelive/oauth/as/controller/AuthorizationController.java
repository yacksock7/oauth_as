package onthelive.oauth.as.controller;


import com.nimbusds.jose.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onthelive.oauth.as.model.*;
import onthelive.oauth.as.service.AuthorizationService;
import onthelive.oauth.as.service.TokenService;
import onthelive.oauth.as.service.UtilService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


@Controller
@RequiredArgsConstructor
//@RequestMapping("/api/v1/authorization")
@Slf4j
public class AuthorizationController {
    private final TokenService tokenService;
    private final AuthorizationService authorizationService;
    private final UtilService utilService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("error", "access denied");
        return "/error";

    }

    /*
     * 인가 엔드포인트 :
     * 클라이언트App 이 인가서버 에 등록되어 있는지 확인하고,
     * 사용자를 인가 승인/거부 화면으로 이동시킨다.
     * */
    @GetMapping("/authorize") // 인가 엔드포인트
    public String getAuthorize(HttpServletRequest httpServletRequest,
                               RedirectAttributes attributes,
                               Model model,
                               @RequestParam(value = "client_id", required = false, defaultValue = "") String clientId,
                               @RequestParam(value = "redirect_uri", required = false, defaultValue = "") String redirectUri,
                               @RequestParam(value = "response_type", required = false, defaultValue = "") String responseType,
                               @RequestParam(value = "state", required = false, defaultValue = "") String state,
                               @RequestParam(value = "scope", required = false, defaultValue = " ") String scope) throws IOException {
        // 임시 scope init
//        scope ="foo bar";

        Client client = authorizationService.getClient(clientId);
        ArrayList<String> scopesDiff = authorizationService.scopesDiff(scope, client.getScopes());

        if (client.getClientId().equals("")) {
            log.error("알 수 없는 Client Id 접근 " + clientId);
            model.addAttribute("error", "Unknown Client Id");
            return "/error";
        } else if (scopesDiff.size() > 0) {
            log.error("invalid scope error in /authorize");
            log.error("scope : " + scope);
            log.error("client.getScopes() : " + client.getScopes());
            attributes.addAttribute("error", "invalid_scope");
            return "redirect:" + client.getRedirectUri();
        } else {
            String reqId = RandomStringUtils.randomAlphanumeric(32);
            Request request = Request.builder()
                    .requestId(reqId)
                    .responseType(responseType)
                    .redirectUri(redirectUri)
                    .state(state)
                    .clientId(clientId)
                    .build();

            authorizationService.saveRequest(request);

            model.addAttribute("client", client);
            model.addAttribute("reqId", reqId);
            model.addAttribute("scope", scope);
            model.addAttribute("scopeList", scope.split(" "));

            return "/approve";
        }
    }

    @PostMapping("/approve") // 권한 위임을 위한 요청 처리
    public String postApprove(HttpServletRequest httpServletRequest,
                              RedirectAttributes attributes,
                              Model model,
                              @RequestParam(name = "reqId", required = false, defaultValue = "") String reqId,
                              @RequestParam(name = "scope", required = false, defaultValue = "") String scope,
                              @RequestParam(name = "approve", required = false, defaultValue = "") String approve) {

        Request request = authorizationService.getRequest(reqId);
        authorizationService.removeRequest(reqId);

        if (request == null) {
            model.addAttribute("error", "No matching authorization request");
            return "/error";
        }

        if (!approve.equals("approve")) {

            if (request.getResponseType().equals("code")) {
                Client client = authorizationService.getClient(request.getClientId());
                ArrayList<String> scopesDiff = authorizationService.scopesDiff(scope, client.getScopes());

                if(scopesDiff.size() > 0){ //ToDo scopesDiff action check
                    log.error("invalid scope error in /approve");
                    log.error("scope : " + scope);
                    log.error("client.getScopes() : " + client.getScopes());
                    attributes.addAttribute("error","invalid_scope");
                    return "redirect:" + request.getRedirectUri();
                }

                String code = RandomStringUtils.randomAlphanumeric(32);
                authorizationService.saveCode(code, request, scope);

                attributes.addAttribute("code", code);
                attributes.addAttribute("state", request.getState());
                return "redirect:" + request.getRedirectUri();
            } else {
                attributes.addAttribute("error", "unsupported_response_type");
                return "redirect:" + request.getRedirectUri();
            }
        } else {
            attributes.addAttribute("error", "access_denied");
            return "redirect:" + request.getRedirectUri();
        }
    }
    @PostMapping("/token")
    public ResponseEntity postToken(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    @RequestHeader(name = "authorization", required = false, defaultValue = "") String auth,
                                    @RequestParam(name = "code", required = false, defaultValue = "") String codeId,
                                    @RequestParam(name = "grant_type", required = false, defaultValue = "") String grantType,
                                    @RequestParam(name = "redirect_uri", required = false, defaultValue = "") String redirectUri,
                                    @RequestParam(name = "client_id", required = false, defaultValue = "") String clientIdByParam,
                                    @RequestParam(name = "client_secret", required = false, defaultValue = "") String clientSecretByParam,
                                    @RequestParam(name = "refresh_token", required = false, defaultValue = "") String refreshTokenByParam
                                    ) throws JOSEException {

        String clientId = "";
        String clientSecret = "";

        // header에 전달된 client_id와 client_secret 확인 -> null? error
        if (auth != null && !auth.equals("")) {
            HashMap clientCredentials = utilService.decodeClientCredentials(auth);
            clientId = (String) clientCredentials.get("id");
            clientSecret = (String) clientCredentials.get("secret");
        }

        // 클라이언트가 header와 form parameter 두가지 방법으로 인증하고 있음 -> status 401 반환 UNAUTHORIZED (하나만 선택해서 인증 진행해야한다.)
        if (!clientIdByParam.equals("")) {
            if (!clientId.equals("")) {
                log.error("클라이언트가 여러번 인증 시도하고 있음.");
                // status 401 반환
                // TODO OAuthException Test
                return new ResponseEntity(HttpStatus.UNAUTHORIZED);
            }
            clientId = clientIdByParam;
            clientSecret = clientSecretByParam;
        }

        // client_id에 해당되는 client get
        Client client = authorizationService.getClient(clientId);

        // can not found client by clientId -> 에러 반환
        if (client.getClientId().equals("")) {
            log.error("알 수 없는 클라이언트 " + clientId);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        // clientSecret 불일치 -> 에러 반환
        if (!client.getClientSecret().equals(clientSecret)) {
            log.error("클라이언트 비밀키가 일치하지 않음.");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        // authorization code grant flow
        if (grantType.equals("authorization_code")) {
            Code code = authorizationService.getCode(codeId);

            if (code != null) {
                authorizationService.removeCode(codeId);
                if (code.getClientId().equals(clientId)) {

//                    String accessToken = RandomStringUtils.randomAlphanumeric(32);
                    String accessToken = tokenService.getAccessToken(clientId); // Access Token 발급

                    // ID Token 발급
                    String serializedIdToken = "";
                    if (code.getScopes().contains("openid")) {
                        serializedIdToken = authorizationService.generateSerializedIdToken(code);
                    }
                    String refreshToken = RandomStringUtils.randomAlphanumeric(32);
//                    client_id 와 access_token db 저장

                    Token token = Token.builder()
                            .clientId(clientId)
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .tokenType("Bearer")
                            .scopes(code.getScopes())
                            .serializedIdToken(serializedIdToken)
                            .build();
                    tokenService.createToken(token);


                    // TODO 암호화된 해시 값으로 관리 방법 생각해보기.
                    // access_response = {
                    //      access_token : access_token,
                    //      token_type : 'Bearer'
                    // }
                    TokenTransfer tokenResponseTransfer = tokenService.convertTokenResponseTransferByTokenResponse(token);
                    return new ResponseEntity(tokenResponseTransfer, HttpStatus.OK);

                } else {
                    // DB에 있는 code의 client_id와 header에 전달된 client_id가 다른경우 -> error : invalid_grent
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
            } else {
                // code가 DB에 없는 경우 -> error : invalid_grent
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } else if (grantType.equals("refresh_token")) {

            Token token = tokenService.getTokenByRefreshToken(refreshTokenByParam);
            tokenService.removeTokenByRefreshToken(refreshTokenByParam);

            if (token != null) {
                // 리프레시토큰과 매핑된 클라이언트 아이디가 전달받은 클라이언트 아이디와 불일치
                if (!token.getClientId().equals(clientId)) {
                    Response response = Response.builder()
                            .error("invalid_grant")
                            .build();
                    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
                }

                // refresh token으로 access_token 재생성 및 기존 토큰 삭제


                //token DB table 어떻게 가져갈 것인가?
//                String accessToken = RandomStringUtils.randomAlphanumeric(32);

                Token newToken = token;
                String accessToken = tokenService.getAccessToken(clientId);
                newToken.setAccessToken(accessToken);
                tokenService.createToken(newToken);

                TokenTransfer tokenResponseTransfer = tokenService.convertTokenResponseTransferByTokenResponse(newToken);


                // TODO TAKE IDTOKEN
                // TODO 리프레쉬토큰을 사용할 때에도 ID TOKEN을 재발급 해야하는가? 고려해야함.

                return new ResponseEntity(tokenResponseTransfer, HttpStatus.OK);

            } else {
                log.error("매칭되는 리프레시토큰이 존재하지 않음");
                Response response = Response.builder()
                        .error("invalid_grant")
                        .build();
                return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
            }
        } else {
            // 지원하지 않는 grant_type일 경우 -> error : unsupported_grant_type
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }



}
