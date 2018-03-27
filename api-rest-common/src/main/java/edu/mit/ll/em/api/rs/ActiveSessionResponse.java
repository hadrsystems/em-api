package edu.mit.ll.em.api.rs;

public class ActiveSessionResponse extends APIResponse {
    boolean activeSession;

    public ActiveSessionResponse(int status, String message, boolean activeSession) {
        super(status, message);
        this.activeSession = activeSession;
    }

    public boolean isActiveSession() {
        return this.activeSession;
    }
}
