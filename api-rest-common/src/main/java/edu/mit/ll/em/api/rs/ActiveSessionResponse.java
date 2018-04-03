package edu.mit.ll.em.api.rs;

public class ActiveSessionResponse extends APIResponse {
    private boolean activeSession;

    public ActiveSessionResponse(int status, String message, boolean activeSession) {
        super(status, message);
        this.activeSession = activeSession;
    }

    public boolean isActiveSession() {
        return this.activeSession;
    }

    public boolean equals(Object other) {
        if(other == null || !(other instanceof ActiveSessionResponse)) {
            return false;
        }

        ActiveSessionResponse otherResponse = (ActiveSessionResponse) other;
        if(this.getStatus() != otherResponse.getStatus() || this.isActiveSession() != otherResponse.isActiveSession()) {
            return false;
        }
        return (this.getMessage() == null) ? false : this.getMessage().equals(otherResponse.getMessage());
    }
}