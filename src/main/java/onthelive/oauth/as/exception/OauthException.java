package onthelive.oauth.as.exception;

public class OauthException extends RuntimeException {
    private ErrorCode code;
    
    public OauthException(ErrorCode code, String message) {
        super(message);
        
        this.code = code;
    }
    
    public ErrorCode getErrorCode() {
        return this.code;
    }
}