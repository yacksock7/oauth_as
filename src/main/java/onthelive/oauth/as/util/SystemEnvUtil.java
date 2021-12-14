package onthelive.oauth.as.util;

import java.util.Map;

public class SystemEnvUtil {
    private Map<String, String> systemEnvMap;

    public SystemEnvUtil() {
        systemEnvMap = System.getenv();
    }

    public String getValue(String key) {
        return systemEnvMap.get(key);
    }

    public String getValue(String key, String defaultValue) {
        String value = systemEnvMap.get(key);

        return value != null ? value : defaultValue;
    }

    public int getValue(String key, int defaultValue) {
        String value = systemEnvMap.get(key);

        return value != null ? Integer.parseInt(value) : defaultValue;
    }
}
