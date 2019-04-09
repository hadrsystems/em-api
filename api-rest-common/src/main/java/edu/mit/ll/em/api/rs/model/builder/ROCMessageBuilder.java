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
package edu.mit.ll.em.api.rs.model.builder;

import edu.mit.ll.em.api.rs.model.ROCLocationBasedData;
import edu.mit.ll.em.api.rs.model.ROCMessage;

import java.util.Date;
import java.util.List;

public class ROCMessageBuilder {
    private ROCMessage rocMessage = new ROCMessage();

    public ROCMessageBuilder buildReportDetails(String reportType, String additionalAffectedCounties, String generalLocation, List<String> fuelTypes, String otherFuelTypes) {
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
