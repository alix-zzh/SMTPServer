package smtp;

class SmtpResponse {
    private int code;
    private String message;
    private SmtpState nextState;

    SmtpResponse(int code, String message, SmtpState next) {
        this.code = code;
        this.message = message;
        this.nextState = next;
    }

    int getCode() {
        return code;
    }

    String getMessage() {
        return message;
    }

    SmtpState getNextState() {
        return nextState;
    }
}
