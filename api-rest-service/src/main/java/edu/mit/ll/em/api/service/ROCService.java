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
import org.apache.commons.lang.StringUtils;

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
            rocForm = new ROCFormBuilder().buildIncidentData(incident)
                    .buildROCMessage(editROCMessage)
                    .build();
        }
        return rocForm;
    }

    private ROCForm buildROCForm(Incident incident, ROCMessage existingROCMessage, ROCLocationBasedData rocLocationBasedData) {
        ROCMessage rocMessage = new ROCMessageBuilder()
                .buildReportDetails(existingROCMessage.getRocDisplayName(), existingROCMessage.getReportType(), existingROCMessage.getIncidentCause(), existingROCMessage.getIncidentType(), existingROCMessage.getGeneralLocation())
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
                .buildReportDetails(null, "NEW", null, StringUtils.join(incident.getIncidentTypeNames(), ", "), null)
                .buildReportDates(dateCreated, dateCreated, dateCreated)
                .buildLocationBasedData(rocLocationBasedData)
                .build();

        return new ROCFormBuilder()
                .buildIncidentData(incident)
                .buildROCMessage(rocMessage).build();
    }
}
