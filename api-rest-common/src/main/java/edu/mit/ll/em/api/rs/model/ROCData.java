package edu.mit.ll.em.api.rs.model;

import org.apache.commons.lang.builder.EqualsBuilder;

public class ROCData {

    //Incident Info
    private Integer incidentId;
    private String incidentName;
    private Double longitude;
    private Double latitude;
    private String incidentType;
    private String incidentDescription;
    private String incidentCause;
    private String latestReportType;

    //Location info pre populated based on incident location
    private String location; //specific location
    private String generalLocation; //general location
    private String county;
    private String state;

    //Jurisdiction info pre populated based on incident location
    private String sra;
    private String dpa;
    private String jurisdiction;

    //Weather data pre populated based on incident location
    private Double temperature;
    private Float relHumidity;
    private Float windSpeed;
    private String windDirection;

    public ROCData(String location, String generalLocation, String county, String state,
                   String sra, String dpa, String jurisdiction,
                   Double temperature, Float relHumidity, Float windSpeed, String windDirection) {
        this.location = location;
        this.generalLocation = generalLocation;
        this.county = county;
        this.state = state;
        this.sra = sra;
        this.dpa = dpa;
        this.jurisdiction = jurisdiction;
        this.temperature = temperature;
        this.relHumidity = relHumidity;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
    }

    public ROCData(Integer incidentId, String incidentName, Double longitude, Double latitude,
                   String incidentType, String incidentDescription, String incidentCause, String latestReportType,
                   String location, String generalLocation, String county, String state,
                   String sra, String dpa, String jurisdiction,
                   Double temperature, Float relHumidity, Float windSpeed, String windDirection) {
        this.incidentId = incidentId;
        this.incidentName = incidentName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.incidentType = incidentType;
        this.incidentDescription = incidentDescription;
        this.incidentCause = incidentCause;
        this.latestReportType = latestReportType;
        this.location = location;
        this.generalLocation = generalLocation;
        this.county = county;
        this.state = state;
        this.sra = sra;
        this.dpa = dpa;
        this.jurisdiction = jurisdiction;
        this.temperature = temperature;
        this.relHumidity = relHumidity;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
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

    public String getLatestReportType() { return latestReportType; }

    public String getLocation() {
        return location;
    }

    public String getGeneralLocation() {
        return generalLocation;
    }

    public String getCounty() {
        return county;
    }

    public String getState() {
        return state;
    }

    public String getSra() {
        return sra;
    }

    public String getDpa() {
        return dpa;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Float getRelHumidity() {
        return relHumidity;
    }

    public Float getWindSpeed() {
        return windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
}
