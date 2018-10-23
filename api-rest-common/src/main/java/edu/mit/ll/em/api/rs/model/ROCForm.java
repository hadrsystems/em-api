package edu.mit.ll.em.api.rs.model;

import org.apache.commons.lang.builder.EqualsBuilder;

public class ROCForm {

    //Incident Info
    private Integer incidentId;
    private String incidentName;
    private Double longitude;
    private Double latitude;
    private String incidentType;
    private String incidentDescription;
    private String incidentCause;

    private ROCMessage message;

    public ROCForm() {
    }

    public ROCForm(ROCMessage rocMessage) {
        this.message = rocMessage;
    }

    public ROCForm(Integer incidentId, String incidentName, Double longitude, Double latitude,
                   String incidentType, String incidentDescription, String incidentCause,
                   ROCMessage rocMessage) {
        this.incidentId = incidentId;
        this.incidentName = incidentName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.incidentType = incidentType;
        this.incidentDescription = incidentDescription;
        this.incidentCause = incidentCause;
        this.message = rocMessage;
    }

    public ROCForm(Integer incidentId, String incidentName, Double longitude, Double latitude,
                   String incidentType, String incidentDescription,
                   ROCMessage rocMessage) {
        this.incidentId = incidentId;
        this.incidentName = incidentName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.incidentType = incidentType;
        this.incidentDescription = incidentDescription;
        this.incidentCause = incidentCause;
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

    public String getIncidentType() {
        return incidentType;
    }

    public String getIncidentDescription() { return incidentDescription; }

    public String getIncidentCause() {
        return incidentCause;
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

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public void setIncidentDescription(String incidentDescription) {
        this.incidentDescription = incidentDescription;
    }

    public void setIncidentCause(String incidentCause) {
        this.incidentCause = incidentCause;
    }

    public void setMessage(ROCMessage message) {
        this.message = message;
    }

    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
}
