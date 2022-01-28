package br.com.senior.seniorx.http.camel;

public class SeniorXHTTPException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SeniorXHTTPException(String message) {
        super(message);
    }

    public SeniorXHTTPException(Throwable cause) {
        super(cause);
    }

}
