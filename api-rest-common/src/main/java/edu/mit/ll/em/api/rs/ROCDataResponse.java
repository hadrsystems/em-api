package edu.mit.ll.em.api.rs;

import edu.mit.ll.em.api.rs.model.ROCData;

public class ROCDataResponse extends APIResponse {
    private ROCData data;

    public ROCDataResponse(ROCData rocFormData) {
        this.data = rocFormData;
    }

    public ROCData getData() {
        return this.data;
    }
}
