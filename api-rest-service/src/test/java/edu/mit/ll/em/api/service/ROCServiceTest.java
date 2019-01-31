package edu.mit.ll.em.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.gateway.geocode.GeocodeAPIGateway;
import edu.mit.ll.em.api.rs.model.*;
import edu.mit.ll.em.api.rs.model.Location;
import edu.mit.ll.em.api.rs.model.builder.ROCLocationBasedDataBuilder;
import edu.mit.ll.em.api.rs.model.builder.ROCMessageBuilder;
import edu.mit.ll.nics.common.constants.SADisplayConstants;
import edu.mit.ll.nics.common.entity.*;
import edu.mit.ll.nics.common.entity.DirectProtectionArea;
import edu.mit.ll.nics.nicsdao.FormDAO;
import edu.mit.ll.nics.nicsdao.JurisdictionDAO;
import edu.mit.ll.nics.nicsdao.WeatherDAO;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;

import java.io.IOException;
import java.util.*;

import org.junit.After;
import org.mockito.Mockito;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.util.CollectionUtils;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ROCServiceTest {
    private static final int FORM_TYPE_ROC_ID = 1;
    private FormDAO formDao = mock(FormDAO.class);
    private JurisdictionDAO jurisdictionDAO = mock(JurisdictionDAO.class);
    private WeatherDAO weatherDao = mock(WeatherDAO.class);
    private GeocodeAPIGateway geocodeAPIGateway = mock(GeocodeAPIGateway.class);
    private ObjectMapper objectMapper = mock(ObjectMapper.class);
    private ROCService rocService = new ROCService(formDao, jurisdictionDAO, weatherDao, geocodeAPIGateway, objectMapper);

    private Integer incidentId = 113;
    private String incidentName = "incident name";
    private Incident incident = mock(Incident.class);
    private Double longitude = 1.0;
    private Double latitude = 1.0;
    private List<String> incidentTypeNames = Arrays.asList("abc", "zyx");
    private Double searchRange = 10.0;
    private Coordinate coordinate = new Coordinate(longitude, latitude);
    private String incidentDescription = "it is a planned event";
    private Location location = new Location("xx y st, abc city, up state, CA", "def county", "CA");
    private Weather weather = new Weather("objectid2", "location x",
            80.01, 9.22f, 310.0, 38.85f, "OK", 8.018);
    private Jurisdiction jurisdiction = new Jurisdiction("SRA", new DirectProtectionArea("Local", "Contract County", "unitid", "respondid"));
    private ROCLocationBasedData rocLocationBasedData = null;

    private Location newLocation = new Location("xx y st, abc city, up state, CA", "xyz county", "CA");
    private Weather newWeather = new Weather("objectid2", "location y",
            98.01, 6.22f, 20.0, 21.1f, "OK", 9.2);
    private Jurisdiction newJurisdiction = new Jurisdiction("LRA", new DirectProtectionArea("Federal", "Fed County", "unitidx", "respondidm"));
    private Form newForm = mock(Form.class);
    private Form update1Form = mock(Form.class);
    private Form update2Form = mock(Form.class);
    private Form finalForm = mock(Form.class);
    private String newMessageStr = "New msg";
    private String update1MessageStr = "Update1 msg";
    private String update2MessageStr = "Update2 msg";
    private String finalMessageStr = "Final msg";

    private ROCMessage rocMessageNew = null;
    private ROCMessage rocMessageUpdate1 = null;
    private ROCMessage rocMessageUpdate2 = null;
    private ROCMessage rocMessageFinal = null;
    private Date startDate = new Date(new Date().getTime() - 20000);
    private Date rocUpdate1CreateDate = new Date(startDate.getTime() + 1000);
    private Date rocUpdate2CreateDate = new Date(rocUpdate1CreateDate.getTime() + 1000);
    private Date rocFinalCreateDate = new Date(rocUpdate2CreateDate.getTime() + 1000);

    @Before
    public void setup() throws Exception {
        when(incident.getIncidentid()).thenReturn(incidentId);
        when(incident.getIncidentname()).thenReturn(incidentName);
        when(incident.getLon()).thenReturn(longitude);
        when(incident.getLat()).thenReturn(latitude);
        when(incident.getIncidentTypeNames()).thenReturn(incidentTypeNames);
        when(incident.getDescription()).thenReturn(incidentDescription);

        when(geocodeAPIGateway.getLocationByGeocode(coordinate)).thenReturn(newLocation);
        when(jurisdictionDAO.getJurisdiction(coordinate)).thenReturn(newJurisdiction);
        when(weatherDao.getWeatherDataFromLocation(coordinate, searchRange)).thenReturn(newWeather);

        rocLocationBasedData = new ROCLocationBasedDataBuilder().buildLocationData(location)
                    .buildWeatherData(weather)
                    .buildJurisdictionData(jurisdiction)
                    .build();

        rocMessageNew = new ROCMessageBuilder().buildReportDetails("name", "NEW", "cause", "planned eventx", "general location")
                    .buildReportDates(startDate, startDate, startDate)
                    .buildLocationBasedData(rocLocationBasedData)
                    .build();

        rocMessageUpdate1 = new ROCMessageBuilder().buildReportDetails("name", "UPDATE", "cause update1", "planned eventx", "general location")
                .buildReportDates(rocUpdate1CreateDate, startDate, startDate)
                .buildLocationBasedData(rocLocationBasedData)
                .build();

        rocMessageUpdate2 = new ROCMessageBuilder().buildReportDetails("name", "UPDATE", "cause Update2", "planned eventx", "general location")
                .buildReportDates(rocUpdate2CreateDate, startDate, startDate)
                .buildLocationBasedData(rocLocationBasedData)
                .build();

        rocMessageFinal = new ROCMessageBuilder().buildReportDetails("name", "FINAL", "cause", "planned eventx", "general location")
                .buildReportDates(rocFinalCreateDate, startDate, startDate)
                .buildLocationBasedData(rocLocationBasedData)
                .build();

        when(newForm.getMessage()).thenReturn(newMessageStr);
        when(update1Form.getMessage()).thenReturn(update1MessageStr);
        when(update2Form.getMessage()).thenReturn(update2MessageStr);
        when(finalForm.getMessage()).thenReturn(finalMessageStr);

        when(objectMapper.readValue(newMessageStr, ROCMessage.class)).thenReturn(rocMessageNew);
        when(objectMapper.readValue(update1MessageStr, ROCMessage.class)).thenReturn(rocMessageUpdate1);
        when(objectMapper.readValue(update2MessageStr, ROCMessage.class)).thenReturn(rocMessageUpdate2);
        when(objectMapper.readValue(finalMessageStr, ROCMessage.class)).thenReturn(rocMessageFinal);
    }

    @Test(expected = Exception.class)
    public void getROCEditFormThrowsExceptionIfFailsToGetFormsFromDB() throws Exception {
        when(formDao.getForms(incidentId, FORM_TYPE_ROC_ID)).thenThrow(new NonTransientDataAccessException("Test msg") {
            @Override
            public String getMessage() {
                return super.getMessage();
            }
        });
        rocService.getEditROCForm(incident, searchRange);
    }

    @Test(expected = IOException.class)
    public void getROCEditFormThrowsExceptionIfFailsToDeserializeExistingROCForms() throws Exception {
        List<Form> rocForms = new ArrayList<Form>();
        rocForms.add(newForm);
        when(formDao.getForms(incidentId, FORM_TYPE_ROC_ID)).thenReturn(rocForms);
        when(objectMapper.readValue(newMessageStr, ROCMessage.class)).thenThrow(IOException.class);
        rocService.getEditROCForm(incident, searchRange);
    }

    @Test
    public void getROCEditFormReturnsNewFormWhenNoROCReportExistsForGivenInicdent() throws Exception {
        List<Form> emptyROCForms = new ArrayList<Form>();
        when(formDao.getForms(incidentId.intValue(), FORM_TYPE_ROC_ID)).thenReturn(emptyROCForms);

        ROCForm rocForm = rocService.getEditROCForm(incident, searchRange);
        assertEquals("NEW", rocForm.getReportType());
        assertEquals(incidentId, rocForm.getIncidentId());
        assertEquals(incidentName, rocForm.getIncidentName());
        assertEquals(longitude, rocForm.getLongitude());
        assertEquals(latitude, rocForm.getLongitude());
        assertEquals(StringUtils.join(incidentTypeNames, ", "), rocForm.getIncidentType());
        assertEquals(incidentDescription, rocForm.getIncidentDescription());

        assertNull(rocForm.getMessage().getRocDisplayName());
        assertNull(rocForm.getMessage().getIncidentCause());
        assertNull(rocForm.getMessage().getGeneralLocation());

        assertEquals(newLocation.getSpecificLocation(), rocForm.getMessage().getLocation());
        assertEquals(newLocation.getCounty(), rocForm.getMessage().getCounty());
        assertEquals(newLocation.getState(), rocForm.getMessage().getState());

        assertEquals(newJurisdiction.getSra(), rocForm.getMessage().getSra());
        assertEquals(newJurisdiction.getDpa(), rocForm.getMessage().getDpa());
        assertEquals(newJurisdiction.getJurisdiction(), rocForm.getMessage().getJurisdiction());

        assertEquals(newWeather.getAirTemperature(), rocForm.getMessage().getTemperature());
        assertEquals(newWeather.getHumidity(), rocForm.getMessage().getRelHumidity());
        assertEquals(newWeather.getWindSpeed(), rocForm.getMessage().getWindSpeed());
        assertEquals(newWeather.getWindDirection(), rocForm.getMessage().getWindDirection());
    }

    @Test
    public void getROCEditFormReturnsFinalFormWhenFinalROCFormExistsForGivenIncident() throws Exception {
        List<Form> rocForms = new ArrayList<Form>();
        rocForms.add(newForm);
        rocForms.add(update1Form);
        rocForms.add(update2Form);
        rocForms.add(finalForm);
        when(formDao.getForms(incidentId.intValue(), FORM_TYPE_ROC_ID)).thenReturn(rocForms);
        ROCForm rocForm = rocService.getEditROCForm(incident, searchRange);
        assertEquals("FINAL", rocForm.getReportType());
        assertEquals(incidentId, rocForm.getIncidentId());
        assertEquals(incidentName, rocForm.getIncidentName());
        assertEquals(longitude, rocForm.getLongitude());
        assertEquals(latitude, rocForm.getLongitude());
        assertEquals(StringUtils.join(incidentTypeNames, ", "), rocForm.getIncidentType());
        assertEquals(incidentDescription, rocForm.getIncidentDescription());

        assertEquals(rocMessageFinal.getRocDisplayName(), rocForm.getMessage().getRocDisplayName());
        assertEquals(rocMessageFinal.getIncidentCause(), rocForm.getMessage().getIncidentCause());
        assertEquals(rocMessageFinal.getGeneralLocation(), rocForm.getMessage().getGeneralLocation());

        assertNotNull(rocForm.getMessage().getDateCreated());
        assertEquals(startDate, rocForm.getMessage().getDate());
        assertEquals(startDate, rocForm.getMessage().getStartTime());

        verifyZeroInteractions(geocodeAPIGateway);
        verifyZeroInteractions(jurisdictionDAO);
        verifyZeroInteractions(weatherDao);
    }

    @Test
     public void getROCEditFormReturnsUpdateFormWhenROCFormExistsForGivenIncident()  throws Exception {
        List<Form> rocForms = new ArrayList<Form>();
        rocForms.add(newForm);
        rocForms.add(update1Form);
        rocForms.add(update2Form);

        when(formDao.getForms(incidentId.intValue(), FORM_TYPE_ROC_ID)).thenReturn(rocForms);
        ROCForm rocForm = rocService.getEditROCForm(incident, searchRange);
        assertEquals("UPDATE", rocForm.getReportType());
        assertEquals(incidentId, rocForm.getIncidentId());
        assertEquals(incidentName, rocForm.getIncidentName());
        assertEquals(longitude, rocForm.getLongitude());
        assertEquals(latitude, rocForm.getLongitude());
        assertEquals(StringUtils.join(incidentTypeNames, ", "), rocForm.getIncidentType());
        assertEquals(incidentDescription, rocForm.getIncidentDescription());

        assertEquals(rocMessageUpdate2.getRocDisplayName(), rocForm.getMessage().getRocDisplayName());
        assertEquals(rocMessageUpdate2.getIncidentCause(), rocForm.getMessage().getIncidentCause());
        assertEquals(rocMessageUpdate2.getGeneralLocation(), rocForm.getMessage().getGeneralLocation());
        assertEquals(rocMessageUpdate2.getIncidentType(), rocForm.getMessage().getIncidentType());

        assertTrue(rocForm.getMessage().getDateCreated().getTime() > rocMessageUpdate2.getDateCreated().getTime());
        assertEquals(startDate, rocForm.getMessage().getDate());
        assertEquals(startDate, rocForm.getMessage().getStartTime());

        assertEquals(jurisdiction.getSra(), rocForm.getMessage().getSra());
        assertEquals(jurisdiction.getDpa(), rocForm.getMessage().getDpa());
        assertEquals(jurisdiction.getJurisdiction(), rocForm.getMessage().getJurisdiction());

        assertEquals(location.getSpecificLocation(), rocForm.getMessage().getLocation());
        assertEquals(location.getCounty(), rocForm.getMessage().getCounty());
        assertEquals(location.getState(), rocForm.getMessage().getState());

        assertEquals(weather.getAirTemperature(), rocForm.getMessage().getTemperature());
        assertEquals(weather.getHumidity(), rocForm.getMessage().getRelHumidity());
        assertEquals(weather.getWindSpeed(), rocForm.getMessage().getWindSpeed());
        assertEquals(weather.getWindDirection(), rocForm.getMessage().getWindDirection());

        verifyZeroInteractions(geocodeAPIGateway);
        verifyZeroInteractions(jurisdictionDAO);
        verifyZeroInteractions(weatherDao);
    }

    @Test
    public void getEditROCFormReturnsUpdateROCFormWhenOnlyNewROCFormExistsForGivenIncident() throws Exception {
        List<Form> rocForms = new ArrayList<Form>();
        rocForms.add(newForm);
        when(formDao.getForms(incidentId.intValue(), FORM_TYPE_ROC_ID)).thenReturn(rocForms);
        ROCForm rocForm = rocService.getEditROCForm(incident, searchRange);
        assertEquals("UPDATE", rocForm.getReportType());
        assertEquals(incidentId, rocForm.getIncidentId());
        assertEquals(incidentName, rocForm.getIncidentName());
        assertEquals(longitude, rocForm.getLongitude());
        assertEquals(latitude, rocForm.getLongitude());
        assertEquals(incidentDescription, rocForm.getIncidentDescription());

        verifyZeroInteractions(geocodeAPIGateway);
        verifyZeroInteractions(jurisdictionDAO);
        verifyZeroInteractions(weatherDao);
    }

    @Test
    public void getEditROCFormReturnsROCFormWithNewLocationBasedDataWhenGivenIncidentIsUpdatedSinceLastROCFormSubmission() throws Exception {
        List<Form> rocForms = new ArrayList<Form>();
        rocForms.add(newForm);
        when(incident.getLastUpdate()).thenReturn(new Date(rocMessageNew.getDateCreated().getTime() + 1000));
        when(formDao.getForms(incidentId.intValue(), FORM_TYPE_ROC_ID)).thenReturn(rocForms);
        ROCForm rocForm = rocService.getEditROCForm(incident, searchRange);
        assertEquals("UPDATE", rocForm.getReportType());
        assertEquals(incidentId, rocForm.getIncidentId());
        assertEquals(incidentName, rocForm.getIncidentName());
        assertEquals(longitude, rocForm.getLongitude());
        assertEquals(latitude, rocForm.getLongitude());
        assertEquals(incidentDescription, rocForm.getIncidentDescription());

        assertTrue(rocForm.getMessage().getDateCreated().getTime() > rocMessageNew.getDateCreated().getTime());
        assertEquals(startDate, rocForm.getMessage().getDate());
        assertEquals(startDate, rocForm.getMessage().getStartTime());

        assertEquals(rocMessageNew.getRocDisplayName(), rocForm.getMessage().getRocDisplayName());
        assertEquals(rocMessageNew.getIncidentCause(), rocForm.getMessage().getIncidentCause());
        assertEquals(rocMessageNew.getGeneralLocation(), rocForm.getMessage().getGeneralLocation());

        assertEquals(newJurisdiction.getSra(), rocForm.getMessage().getSra());
        assertEquals(newJurisdiction.getDpa(), rocForm.getMessage().getDpa());
        assertEquals(newJurisdiction.getJurisdiction(), rocForm.getMessage().getJurisdiction());

        assertEquals(newLocation.getSpecificLocation(), rocForm.getMessage().getLocation());
        assertEquals(newLocation.getCounty(), rocForm.getMessage().getCounty());
        assertEquals(newLocation.getState(), rocForm.getMessage().getState());

        assertEquals(newWeather.getAirTemperature(), rocForm.getMessage().getTemperature());
        assertEquals(newWeather.getHumidity(), rocForm.getMessage().getRelHumidity());
        assertEquals(newWeather.getWindSpeed(), rocForm.getMessage().getWindSpeed());
        assertEquals(newWeather.getWindDirection(), rocForm.getMessage().getWindDirection());
    }

    @After
    public void cleanup() {
        Mockito.reset(geocodeAPIGateway, jurisdictionDAO, weatherDao, objectMapper, incident);
        Mockito.reset(newForm, update1Form, update2Form, finalForm);
    }
}
