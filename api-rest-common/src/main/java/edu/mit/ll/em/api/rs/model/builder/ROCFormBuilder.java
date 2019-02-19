package edu.mit.ll.em.api.rs.model.builder;

import edu.mit.ll.em.api.rs.model.ROCForm;
import edu.mit.ll.em.api.rs.model.ROCMessage;
import edu.mit.ll.nics.common.entity.Incident;

public class ROCFormBuilder {
    private ROCForm rocForm = new ROCForm();

    public ROCFormBuilder buildIncidentData(Incident incident) {
        if(incident != null) {
            rocForm.setIncidentId(incident.getIncidentid());
            rocForm.setIncidentName(incident.getIncidentname());
            rocForm.setLongitude(incident.getLon());
            rocForm.setLatitude(incident.getLat());
            rocForm.setIncidentTypes(incident.getIncidentTypes());
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
