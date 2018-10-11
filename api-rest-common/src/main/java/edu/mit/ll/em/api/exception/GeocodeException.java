package edu.mit.ll.em.api.exception;

public class GeocodeException extends RuntimeException {

    private final String responseCode;
    private final String errorMessage;

    public GeocodeException(String responseCode, String errorMessage) {
        super();
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getMessage() {
        return String.format("ErrorCode: %s, ErrorMessage: %s", this.responseCode, this.errorMessage);
    }
}
