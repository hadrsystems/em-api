package edu.mit.ll.em.api.rs.model.builder;

import edu.mit.ll.em.api.rs.model.ROCForm;
import edu.mit.ll.em.api.rs.model.ROCMessage;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.IncidentIncidentType;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

public class ROCFormBuilder {
    private ROCForm rocForm = new ROCForm();

    public ROCFormBuilder buildIncidentData(Incident incident) {
        if(incident != null) {
            rocForm.setIncidentId(incident.getIncidentid());
            rocForm.setIncidentName(incident.getIncidentname());
            rocForm.setLongitude(incident.getLon());
            rocForm.setLatitude(incident.getLat());
            rocForm.setIncidentType(getIncidentTypes(incident.getIncidentIncidenttypes()));
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

    private String getIncidentTypes(Set<IncidentIncidentType> incidentTypeSet) {
        StringBuilder builder = new StringBuilder();
        for(IncidentIncidentType incidentIncidentType : incidentTypeSet) {
            if(incidentIncidentType.getIncidentType() != null && StringUtils.isNotBlank(incidentIncidentType.getIncidentType().getIncidentTypeName())) {
                builder.append(incidentIncidentType.getIncidentType().getIncidentTypeName()).append(", ");
            }
        }
        return builder.length() > 2 ? builder.substring(0, builder.length()-2) : "";
    }
}
