package edu.mit.ll.em.api.rs.model.builder;

import edu.mit.ll.em.api.rs.model.ROCForm;
import edu.mit.ll.em.api.rs.model.ROCMessage;
import edu.mit.ll.nics.common.entity.Incident;
import org.apache.commons.lang.StringUtils;

public class ROCFormBuilder {
    private ROCForm rocForm = new ROCForm();

    public ROCFormBuilder buildIncidentData(Incident incident) {
        if(incident != null) {
            rocForm.setIncidentId(incident.getIncidentid());
            rocForm.setIncidentName(incident.getIncidentname());
            rocForm.setLongitude(incident.getLon());
            rocForm.setLatitude(incident.getLat());
            rocForm.setIncidentType(StringUtils.join(incident.getIncidentTypeNames(), ", "));
            rocForm.setIncidentDescription(incident.getDescription());
        }
        return this;
    }

    public ROCFormBuilder buildROCMessage(ROCMessage rocMessage) {
        this.rocForm.setMessage(rocMessage);
        return this;
    }

    public ROCForm build() {
        return this.rocForm;
    }
}
