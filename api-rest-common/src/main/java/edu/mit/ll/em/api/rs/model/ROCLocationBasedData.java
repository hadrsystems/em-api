package edu.mit.ll.em.api.rs.model;

public class ROCLocationBasedData {

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

    public ROCLocationBasedData() {
    }

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

    public void setLocation(String location) {
        this.location = location;
    }

    public void setGeneralLocation(String generalLocation) {
        this.generalLocation = generalLocation;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setSra(String sra) {
        this.sra = sra;
    }

    public void setDpa(String dpa) {
        this.dpa = dpa;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setRelHumidity(Float relHumidity) {
        this.relHumidity = relHumidity;
    }

    public void setWindSpeed(Float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }
}
