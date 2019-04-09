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
