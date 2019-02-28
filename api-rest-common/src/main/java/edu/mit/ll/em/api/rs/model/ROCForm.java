package edu.mit.ll.em.api.rs.model;

import edu.mit.ll.nics.common.entity.IncidentType;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.List;

public class ROCForm {

    //Incident Info
    private Integer incidentId;
    private String incidentName;
    private Double longitude;
    private Double latitude;
    private List<IncidentType> incidentTypes;
    private String incidentDescription;
    private String incidentCause;

    private ROCMessage message;

    public ROCForm() {
    }

    public ROCForm(ROCMessage rocMessage) {
        this.message = rocMessage;
    }

    public ROCForm(Integer incidentId, String incidentName, Double longitude, Double latitude,
                   List<IncidentType> incidentTypes, ROCMessage rocMessage) {
        this.incidentId = incidentId;
        this.incidentName = incidentName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.incidentTypes = new ArrayList<>(incidentTypes);
        this.message = rocMessage;
    }

    public Integer getIncidentId() {
        return incidentId;
    }

    public String getIncidentName() {
        return incidentName;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public List<IncidentType> getIncidentTypes() {
        return incidentTypes;
    }

    public ROCMessage getMessage() {
        return this.message;
    }

    public String getReportType() {
        return this.message == null ? null : this.message.getReportType();
    }

    public void setIncidentId(Integer incidentId) {
        this.incidentId = incidentId;
    }

    public void setIncidentName(String incidentName) {
        this.incidentName = incidentName;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setIncidentTypes(List<IncidentType> incidentTypes) {
        this.incidentTypes = new ArrayList<>(incidentTypes);
    }

    public void setMessage(ROCMessage message) {
        this.message = message;
    }

    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
}
