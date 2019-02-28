package edu.mit.ll.em.api.rs.model.builder;

import edu.mit.ll.em.api.rs.model.ROCLocationBasedData;
import edu.mit.ll.em.api.rs.model.ROCMessage;

import java.util.Date;

public class ROCMessageBuilder {
    private ROCMessage rocMessage = new ROCMessage();

    public ROCMessageBuilder buildReportDetails(String reportType, String additionalAffectedCounties, String generalLocation, String fuelTypes, String otherFuelTypes) {
        this.rocMessage.setReportType(reportType);
        this.rocMessage.setAdditionalAffectedCounties(additionalAffectedCounties);
        this.rocMessage.setGeneralLocation(generalLocation);
        this.rocMessage.setFuelTypes(fuelTypes);
        this.rocMessage.setOtherFuelTypes(otherFuelTypes);
        return this;
    }

    public ROCMessageBuilder buildReportDates(Date dateCreated, Date startDate, Date startTime) {
        Date startDateTime = new Date();
        if(dateCreated == null) {
            this.rocMessage.setDateCreated(startDateTime);
        } else {
            this.rocMessage.setDateCreated(dateCreated);
        }
        if(startDate == null) {
            this.rocMessage.setDate(startDateTime);
        } else {
            this.rocMessage.setDate(startDate);
        }
        if(startTime == null) {
            this.rocMessage.setStartTime(startDateTime);
        } else {
            this.rocMessage.setStartTime(startTime);
        }
        return this;
    }

    public ROCMessageBuilder buildLocationBasedData(ROCLocationBasedData rocLocationBasedData) {
        if(rocLocationBasedData != null) {
            this.rocMessage.setLocation(rocLocationBasedData.getLocation());
            this.rocMessage.setCounty(rocLocationBasedData.getCounty());
            this.rocMessage.setState(rocLocationBasedData.getState());
            this.rocMessage.setSra(rocLocationBasedData.getSra());
            this.rocMessage.setDpa(rocLocationBasedData.getDpa());
            this.rocMessage.setJurisdiction(rocLocationBasedData.getJurisdiction());
            this.rocMessage.setTemperature(rocLocationBasedData.getTemperature());
            this.rocMessage.setRelHumidity(rocLocationBasedData.getRelHumidity());
            this.rocMessage.setWindSpeed(rocLocationBasedData.getWindSpeed());
            this.rocMessage.setWindDirection(rocLocationBasedData.getWindDirection());
        }
        return this;
    }

    public ROCMessage build() {
        return this.rocMessage;
    }
}
