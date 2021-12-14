package onthelive.oauth.as.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class UtilService {
    public HashMap getGetMappingParameters(HttpServletRequest httpRequest) {
        Map params = httpRequest.getParameterMap();
        Iterator it = params.keySet().iterator();

        HashMap result = new HashMap();

        while (it.hasNext()) {
            String key = (String) it.next();
            String value = ((String[]) params.get(key))[0];
            result.put(key, value);
        }

        return result;
    }

    public HashMap decodeClientCredentials(String context) {
        String[] basic_s = context.split("Basic "); // Basic b2F1dGgtY2xpZW50LTE6b2F1dGgtY2xpZW50LXNlY3JldC0x
        byte[] decode = Base64.getDecoder().decode(basic_s[1]); // b2F1dGgtY2xpZW50LTE6b2F1dGgtY2xpZW50LXNlY3JldC0x
        String decoded = new String(decode); // oauth-client-1:oauth-client-secret-1

        String[] arr = decoded.split(":");

        String id = arr[0];
        String secret = arr[1];

        HashMap result = new HashMap();
        result.put("id", id);
        result.put("secret", secret);

        return result;
    }

    public ArrayList<String> getScopes(String scope) {
        return new ArrayList<String>(Arrays.asList(scope.split(" ")));
    }
}
