package edu.mit.ll.em.api.rs.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.mit.ll.em.api.json.deserializer.ROCMessageDeserializer;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.Date;

@JsonDeserialize(using = ROCMessageDeserializer.class)
public class ROCMessage implements Cloneable, Comparable {

    private Date dateCreated;
    //ROC info
    private String rocDisplayName;
    private String reportType;
    private Date date;
    private Date startTime;
    private String incidentCause;
    private String incidentType;

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

    public ROCMessage() {
    }

    public ROCMessage(Date dateCreated, String rocDisplayName, String reportType, Date date, Date startTime,
                      String incidentCause, String incidentType,
                      String location, String generalLocation, String county, String state,
                      String sra, String dpa, String jurisdiction,
                      Double temperature, Float relHumidity, Float windSpeed, String windDirection) {
        this.dateCreated = dateCreated;
        this.rocDisplayName = rocDisplayName;
        this.reportType = reportType;
        this.date = date;
        this.startTime = startTime;
        this.incidentCause = incidentCause;
        this.incidentType = incidentType;
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

    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getRocDisplayName() {
        return rocDisplayName;
    }

    public void setRocDisplayName(String rocDisplayName) {
        this.rocDisplayName = rocDisplayName;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public Date getDate() {
        return date;
    }

    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public void setDate(Date date) {
        this.date = date;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getIncidentCause() {
        return incidentCause;
    }

    public void setIncidentCause(String incidentCause) {
        this.incidentCause = incidentCause;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGeneralLocation() {
        return generalLocation;
    }

    public void setGeneralLocation(String generalLocation) {
        this.generalLocation = generalLocation;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSra() {
        return sra;
    }

    public void setSra(String sra) {
        this.sra = sra;
    }

    public String getDpa() {
        return dpa;
    }

    public void setDpa(String dpa) {
        this.dpa = dpa;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Float getRelHumidity() {
        return relHumidity;
    }

    public void setRelHumidity(Float relHumidity) {
        this.relHumidity = relHumidity;
    }

    public Float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    public int compareTo(Object other) {
        ROCMessage otherROCMessage =  (ROCMessage) other;
        if(this == otherROCMessage) {
            return 0;
        }
        if(other == null) {
            return 1;
        }
        if("FINAL".equals(this.getReportType()) ) {
            return "FINAL".equals(otherROCMessage.getReportType()) ? compareDates(this.getDateCreated(), otherROCMessage.getDateCreated()) : 1;
        }
        if("FINAL".equals(otherROCMessage.getReportType())) {
            return -1;
        }
        return compareDates(this.getDateCreated(), otherROCMessage.getDateCreated());
    }

    private int compareDates(Date date1, Date date2) {
        if(date1 == null) {
            return date2 == null ? 0 : -1;
        }
        return date2 == null ? 1 : date1.compareTo(date2);
    }

    public ROCMessage clone() throws CloneNotSupportedException {
        return (ROCMessage) super.clone();
    }
}
