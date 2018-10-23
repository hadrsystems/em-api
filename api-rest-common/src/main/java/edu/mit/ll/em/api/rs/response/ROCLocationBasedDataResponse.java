package edu.mit.ll.em.api.rs.response;

import edu.mit.ll.em.api.rs.APIResponse;
import edu.mit.ll.em.api.rs.model.ROCForm;
import edu.mit.ll.em.api.rs.model.ROCLocationBasedData;

public class ROCLocationBasedDataResponse extends APIResponse {
    private ROCLocationBasedData data;

    public ROCLocationBasedDataResponse(ROCLocationBasedData rocLocationBasedData) {
        super(200, "OK");
        this.data = rocLocationBasedData;
    }

    public ROCLocationBasedData getData() {
        return this.data;
    }
}
