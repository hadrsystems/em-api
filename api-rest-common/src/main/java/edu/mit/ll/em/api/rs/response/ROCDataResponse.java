package edu.mit.ll.em.api.rs.response;

import edu.mit.ll.em.api.rs.APIResponse;
import edu.mit.ll.em.api.rs.model.ROCForm;

public class ROCDataResponse extends APIResponse {
    private ROCForm data;

    public ROCDataResponse(ROCForm rocFormData) {
        super(200, "OK");
        this.data = rocFormData;
    }

    public ROCForm getData() {
        return this.data;
    }
}
