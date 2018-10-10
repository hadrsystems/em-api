package edu.mit.ll.em.api.exception;

public class GeocodeException extends RuntimeException {

    private String responseCode;
    private String errorMessage;

    public GeocodeException(String responseCode, String errorMessage) {
        super();
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
    }

    public GeocodeException(String responseCode, String errorMessage, Throwable throwable) {
        super(throwable);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
