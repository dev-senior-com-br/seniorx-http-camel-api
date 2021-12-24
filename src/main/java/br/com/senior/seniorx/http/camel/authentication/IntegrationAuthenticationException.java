package br.com.senior.seniorx.http.camel.authentication;

public class IntegrationAuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IntegrationAuthenticationException(String message) {
        super(message);
    }

    public IntegrationAuthenticationException(Throwable cause) {
        super(cause);
    }

}
