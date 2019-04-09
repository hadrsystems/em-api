/**
 * Copyright (c) 2008-2018, Massachusetts Institute of Technology (MIT)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.mit.ll.em.api.rs.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.mit.ll.em.api.json.deserializer.ROCMessageDeserializer;
import edu.mit.ll.nics.common.entity.IncidentType;
import edu.mit.ll.nics.common.entity.Weather;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonDeserialize(using = ROCMessageDeserializer.class)
public class ROCMessage implements Cloneable, Comparable {

    private Date dateCreated;
    //ROC info
    private String reportType;
    private Date date;
    private Date startTime;
    private List<IncidentType> incidentTypes;

    //Location info pre populated based on incident location
    private String location; //specific location
    private String generalLocation; //general location
    private String county;
    private String additionalAffectedCounties;
    private String state;

    //Jurisdiction info pre populated based on incident location
    private String sra;
    private String dpa;
    private String jurisdiction;

    //Weather data pre populated based on incident location
    private Double temperature;
    private Float relHumidity;
    private String evacuations;
    private Float windSpeed;
    private Double windDirection;
    private String scope;
    private String percentageContained;
    //Fuel types on vegetation fire
//    private String fuelTypes;
    private List<String> fuelTypes;
    private String otherFuelTypes;
    private String  evacuationsInProgress;
    private List<String> evacList;
    private String structuresThreat;
    private List<String> structuresThreats;
    private String infrastructuresThreat;
    private List<String> infrastructuresThreats;
    private List<String> resourcesAssigned;


    public ROCMessage() {
    }

    public ROCMessage(Date dateCreated, String reportType, Date date, Date startTime,
                      String location, String generalLocation, String county, String additionalAffectedCounties, String state,
                      String sra, String dpa, String jurisdiction,
                      Double temperature, Float relHumidity,  Float windSpeed, Double windDirection, String percentageContained,
                      String scope, List<String> fuelTypes, String otherFuelTypes, String evacuations, List<String> evacList,
                      String structuresThreat, List<String> structuresThreats, String infrastructuresThreat,
                      List<String> infrastructuresThreats, List<String> resourcesAssigned) {
        this.dateCreated = dateCreated;
        this.reportType = reportType;
        this.date = date;
        this.startTime = startTime;
        this.location = location;
        this.generalLocation = generalLocation;
        this.county = county;
        this.additionalAffectedCounties = additionalAffectedCounties;
        this.state = state;
        this.sra = sra;
        this.dpa = dpa;
        this.jurisdiction = jurisdiction;
        this.temperature = temperature;
        this.relHumidity = relHumidity;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.percentageContained = percentageContained;
        this.scope = scope;
        this.fuelTypes = fuelTypes;
        this.otherFuelTypes = otherFuelTypes;
        this.evacuations = evacuations;
        this.evacList = evacList;
        this.structuresThreat=structuresThreat;
        this.structuresThreats=structuresThreats;
        this.infrastructuresThreat=infrastructuresThreat;
        this.infrastructuresThreats=infrastructuresThreats;
        this.resourcesAssigned = resourcesAssigned;
    }

    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
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

    public String getAdditionalAffectedCounties() {
        return additionalAffectedCounties;
    }

    public void setAdditionalAffectedCounties(String additionalAffectedCounties) {
        this.additionalAffectedCounties = additionalAffectedCounties;
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

    public Float getRelHumidity() { return relHumidity; }


    public void setRelHumidity(Float relHumidity) {
        this.relHumidity = relHumidity;
    }
    public String getEvacuations() { return evacuations; }

    public void setEvacuations(String evacuations) { this.evacuations = evacuations; }

    public String getEvacuationsInProgress() { return evacuationsInProgress; }

    public void setEvacuationsInProgress(String evacuationsInProgress) { this.evacuationsInProgress = evacuationsInProgress; }
    public List<String> getEvacList() { return evacList; }

    public void setEvacList(List<String> evacList) { this.evacList = evacList; }

    public String getStructuresThreat() {
        return structuresThreat;
    }

    public void setStructuresThreat(String structuresThreat) {
        this.structuresThreat = structuresThreat;
    }

    public List<String> getStructuresThreats() { return structuresThreats; }

    public void setStructuresThreats(List<String> structuresThreats) { this.structuresThreats = structuresThreats; }

    public String getInfrastructuresThreat() {
        return infrastructuresThreat;
    }

    public void setInfrastructuresThreat(String infrastructuresThreat) {
        this.infrastructuresThreat = infrastructuresThreat;
    }

    public List<String> getInfrastructuresThreats() { return infrastructuresThreats; }

    public void setInfrastructuresThreats(List<String> infrastructuresThreats) { this.infrastructuresThreats = infrastructuresThreats; }

    public Float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Double getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(Double windDirection) {
        this.windDirection = windDirection;
    }

    public String getScope() { return scope; }

    public void setScope(String scope) { this.scope = scope; }

    public String getPercentageContained() { return percentageContained; }

    public void setPercentageContained(String percentageContained) { this.percentageContained = percentageContained; }

    public List<String> getFuelTypes() { return fuelTypes; }

    public void setFuelTypes(List<String> fuelTypes) {
        this.fuelTypes = fuelTypes;
    }

    public String getOtherFuelTypes() {
        return otherFuelTypes;
    }

    public void setOtherFuelTypes(String otherFuelTypes) {
        this.otherFuelTypes = otherFuelTypes;
    }

    public List<String> getResourcesAssigned() { return resourcesAssigned; }

    public void setResourcesAssigned(List<String> resourcesAssigned) { this.resourcesAssigned = resourcesAssigned; }

    public void updateWeatherInformation(Weather weather) {
        if(weather != null) {
            this.setTemperature(weather.getAirTemperature());
            this.setRelHumidity(weather.getHumidity());
            this.setWindSpeed(weather.getWindSpeed());
            this.setWindDirection(weather.getWindDirection());
        }
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
