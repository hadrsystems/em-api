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
package edu.mit.ll.em.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.gateway.geocode.GeocodeAPIGateway;
import edu.mit.ll.em.api.rs.model.*;
import edu.mit.ll.em.api.rs.model.Location;
import edu.mit.ll.em.api.rs.model.builder.ROCFormBuilder;
import edu.mit.ll.em.api.rs.model.builder.ROCLocationBasedDataBuilder;
import edu.mit.ll.em.api.rs.model.builder.ROCMessageBuilder;
import edu.mit.ll.nics.common.entity.*;
import edu.mit.ll.nics.common.entity.Jurisdiction;
import edu.mit.ll.nics.nicsdao.FormDAO;
import edu.mit.ll.nics.nicsdao.JurisdictionDAO;
import edu.mit.ll.nics.nicsdao.WeatherDAO;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

public class ROCService {

    private static final int FORM_TYPE_ROC_ID = 1;
    private FormDAO formDao = null;
    private JurisdictionDAO jurisdictionDAO = null;
    private WeatherDAO weatherDao = null;
    private GeocodeAPIGateway geocodeAPIGateway = null;
    private ObjectMapper objectMapper = null;

    public ROCService(FormDAO formDao, JurisdictionDAO jurisdictionDAO, WeatherDAO weatherDao, GeocodeAPIGateway geocodeAPIGateway, ObjectMapper objectMapper) {
        this.formDao = formDao;
        this.jurisdictionDAO = jurisdictionDAO;
        this.weatherDao = weatherDao;
        this.geocodeAPIGateway = geocodeAPIGateway;
        this.objectMapper = objectMapper;
    }

    public ROCLocationBasedData getROCLocationBasedData(Coordinate coordinatesIn4326, Double searchRangeInKiloMeters) throws Exception {
        Jurisdiction jurisdiction = jurisdictionDAO.getJurisdiction(coordinatesIn4326);
        Weather weather = weatherDao.getWeatherDataFromLocation(coordinatesIn4326, searchRangeInKiloMeters);
        Location location = geocodeAPIGateway.getLocationByGeocode(coordinatesIn4326);
        ROCLocationBasedData rocLocationBasedData = new ROCLocationBasedDataBuilder()
                .buildJurisdictionData(jurisdiction)
                .buildLocationData(location)
                .buildWeatherData(weather)
                .build();
        return rocLocationBasedData;
    }

    public ROCForm getEditROCForm(Incident incident, Double searchRangeInKiloMeters) throws Exception {
        ROCMessage rocMessage = this.getLatestROCMessage(incident.getIncidentid());
        if(rocMessage == null) {
            Coordinate coordinate = new Coordinate(incident.getLon(), incident.getLat());
            ROCLocationBasedData rocLocationBasedData = getROCLocationBasedData(coordinate, searchRangeInKiloMeters);
            return this.buildNewROCForm(incident, rocLocationBasedData);
        } else {
            return this.getROCForm(incident, rocMessage, searchRangeInKiloMeters);
        }
    }

    /** Returns ROC Message FINAL if FINAL ROC exists for given incidentId
     else returns Update ROC Form with latest create date
     **/
    private ROCMessage getLatestROCMessage(int incidentId) throws IOException {
        List<Form> rocForms = formDao.getForms(incidentId, FORM_TYPE_ROC_ID);
        TreeSet<ROCMessage> rocMessageSet = new TreeSet<ROCMessage>();
        if(rocForms.isEmpty())
            return null;
        for(Form form: rocForms) {
            ROCMessage currentRocMessage = objectMapper.readValue(form.getMessage(), ROCMessage.class);
            rocMessageSet.add(currentRocMessage);
        }
        return rocMessageSet.last();
    }

    private ROCForm getROCForm(Incident incident, ROCMessage latestROCMessage, Double searchRange) throws Exception {
        ROCForm rocForm = null;
        if("FINAL".equals(latestROCMessage.getReportType())) {
            return new ROCFormBuilder().buildIncidentData(incident)
                    .buildROCMessage(latestROCMessage)
                    .build();
        }

        ROCMessage editROCMessage = latestROCMessage.clone();
        editROCMessage.setDateCreated(new Date());
        if("NEW".equals(latestROCMessage.getReportType())) {
            editROCMessage.setReportType("UPDATE");
        }
        if(incident.getLastUpdate()!=null && incident.getLastUpdate().compareTo(latestROCMessage.getDateCreated()) > 0) {
            ROCLocationBasedData rocLocationBasedData = this.getROCLocationBasedData(new Coordinate(incident.getLon(), incident.getLat()), searchRange);
            rocForm = buildROCForm(incident, editROCMessage, rocLocationBasedData);
        } else {
            Coordinate coordinate = new Coordinate(incident.getLon(), incident.getLat());
            Weather weather = weatherDao.getWeatherDataFromLocation(coordinate, searchRange);
            editROCMessage.updateWeatherInformation(weather);
            rocForm = new ROCFormBuilder().buildIncidentData(incident)
                    .buildROCMessage(editROCMessage)
                    .build();
        }
        return rocForm;
    }

    private ROCForm buildROCForm(Incident incident, ROCMessage existingROCMessage, ROCLocationBasedData rocLocationBasedData) {
        ROCMessage rocMessage = new ROCMessageBuilder()
                .buildReportDetails(existingROCMessage.getReportType(), existingROCMessage.getAdditionalAffectedCounties(), existingROCMessage.getGeneralLocation(),
                        existingROCMessage.getFuelTypes(), existingROCMessage.getOtherFuelTypes())
                .buildReportDates(new Date(), existingROCMessage.getDate(), existingROCMessage.getStartTime())
                .buildLocationBasedData(rocLocationBasedData)
                .build();

        return new ROCFormBuilder()
                .buildIncidentData(incident)
                .buildROCMessage(rocMessage).build();
    }

    private ROCForm buildNewROCForm(Incident incident, ROCLocationBasedData rocLocationBasedData) {
        Date dateCreated = new Date();
        ROCMessage rocMessage = new ROCMessageBuilder()
                .buildReportDetails("NEW", null, null, null, null)
                .buildReportDates(dateCreated, dateCreated, dateCreated)
                .buildLocationBasedData(rocLocationBasedData)
                .build();

        return new ROCFormBuilder()
                .buildIncidentData(incident)
                .buildROCMessage(rocMessage).build();
    }
}
