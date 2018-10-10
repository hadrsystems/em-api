package edu.mit.ll.em.api.rs.model.mapper;

import edu.mit.ll.em.api.rs.model.DirectProtectionArea;
import edu.mit.ll.em.api.rs.model.Jurisdiction;
import edu.mit.ll.em.api.rs.model.Location;
import edu.mit.ll.em.api.rs.model.ROCData;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.Weather;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ROCDataModelMapperTest {
    private String sra = "sra";
    private DirectProtectionArea directProtectionArea = new DirectProtectionArea("dpa", "contract county");
    private Jurisdiction jurisdiction = new Jurisdiction("sra", directProtectionArea, "jurisdiction county");

    private Incident incident = new Incident(1, "incidentname", -121.987987, 35.09809, new Date(), new Date(), true, "/root/incident/folder");
    private Weather weather = new Weather("objectId", "-123, 098", 78.9, 10.0f, 214.0, 2.3f, "OK", 10.0);
    private Location location = new Location("county", "state", "000 exact st, xm city, ca, USA, 90000");
    private ROCDataModelMapper rocDataModelMapper = new ROCDataModelMapper();
    private String latestReportType = "Update";
    private String incidentCause = "cause";
    private String generalLocation = "5 miles from xy";

    @Test
    public void givenValidIncidentBuildROCDataReturnsROCDataWithInputIncidentInfo() {
        ROCData rocData = rocDataModelMapper.convertToROCData(incident, jurisdiction, location, weather, latestReportType, incidentCause, generalLocation);
        assertEquals(rocData.getIncidentId(), incident.getIncidentid());
        assertEquals(rocData.getIncidentName(), incident.getIncidentname());
        assertEquals(rocData.getLongitude(), (Double) incident.getLon());
        assertEquals(rocData.getLatitude(), (Double) incident.getLat());
        assertEquals(rocData.getIncidentType(), StringUtils.join(incident.getIncidentIncidenttypes(), ','));
        assertEquals(rocData.getIncidentDescription(), incident.getDescription());
    }

    @Test
    public void givenNullIncidentBuildROCDataReturnsROCDataWithNullIncidentFields() {
        ROCData rocData = rocDataModelMapper.convertToROCData(null, jurisdiction, location, weather, latestReportType, incidentCause, generalLocation);
        assertNull(rocData.getIncidentId());
        assertNull(rocData.getIncidentName());
        assertNull(rocData.getLongitude());
        assertNull(rocData.getLatitude());
        assertNull(rocData.getIncidentType());
        assertNull(rocData.getIncidentDescription());
    }

    @Test
    public void givenValidJurisdictionBuildROCDataReturnsROCDataWithInputJurisdictionInfo() {
        ROCData rocData = rocDataModelMapper.convertToROCData(incident, jurisdiction, location, weather, latestReportType, incidentCause, generalLocation);
        assertEquals(rocData.getSra(), jurisdiction.getSRA());
        assertEquals(rocData.getDpa(), jurisdiction.getDPA());
        assertEquals(rocData.isContractCounty(), jurisdiction.isContractCounty());
        assertEquals(rocData.getJurisdiction(), jurisdiction.getJurisdictionEntity());
    }

    @Test
    public void givenNullJurisdictionBuildROCDataReturnsROCDataWithNullJurisdictionFields() {
        ROCData rocData = rocDataModelMapper.convertToROCData(incident, null, location, weather, latestReportType, incidentCause, generalLocation);
        assertNull(rocData.getSra());
        assertNull(rocData.getDpa());
        assertNull(rocData.isContractCounty());
        assertNull(rocData.getJurisdiction());
    }

    @Test
    public void givenValidLocationBuildROCDataReturnsROCDataWithInputLocationInfo() {
        ROCData rocData = rocDataModelMapper.convertToROCData(incident, jurisdiction, location, weather, latestReportType, incidentCause, generalLocation);
        assertEquals(rocData.getCounty(), location.getCounty());
        assertEquals(rocData.getState(), location.getState());
        assertEquals(rocData.getLocation(), location.getSpecificLocation());
    }

    @Test
    public void givenNullLocationBuildROCDataReturnsROCDataWithNullLocationFields() {
        ROCData rocData = rocDataModelMapper.convertToROCData(incident, jurisdiction, null, weather, latestReportType, incidentCause, generalLocation);
        assertNull(rocData.getCounty());
        assertNull(rocData.getState());
        assertNull(rocData.getLocation());
    }

    @Test
    public void givenValidWeatherBuildROCDataReturnsROCDataWithInputWeatherInfo() {
        ROCData rocData = rocDataModelMapper.convertToROCData(incident, jurisdiction, location, weather, latestReportType, incidentCause, generalLocation);
        assertEquals(rocData.getTemperature(), weather.getAirTemperature());
        assertEquals(rocData.getRelHumidity(), weather.getHumidity());
        assertEquals(rocData.getWindSpeed(), weather.getWindSpeed());
        assertEquals(rocData.getWindDirection(), weather.getDescriptiveWindDirection());
    }

    @Test
    public void givenNullWeatherBuildROCDataReturnsROCDataWithNullWeatherFields() {
        ROCData rocData = rocDataModelMapper.convertToROCData(incident, jurisdiction, location, null, latestReportType, incidentCause, generalLocation);
        assertNull(rocData.getTemperature());
        assertNull(rocData.getRelHumidity());
        assertNull(rocData.getWindSpeed());
        assertNull(rocData.getWindDirection());
    }

    @Test
    public void buildROCDataReturnsROCDataWithGivenPreviousROCForm() {
        ROCData rocData = rocDataModelMapper.convertToROCData(incident, jurisdiction, location, weather, latestReportType, incidentCause, generalLocation);
        assertEquals(rocData.getLatestReportType(), latestReportType);
        assertEquals(rocData.getIncidentCause(), incidentCause);
        assertEquals(rocData.getGeneralLocation(), generalLocation);
    }

    @Test
    public void buildROCDataReturnsROCDataWithBlankPreviousROCFormDataGivenNoPreviousROCFormData() {
        ROCData rocData = rocDataModelMapper.convertToROCData(incident, jurisdiction, location, weather, null, null, null);
        assertNull(rocData.getIncidentCause());
        assertNull(rocData.getLatestReportType());
        assertNull(rocData.getGeneralLocation());
    }

    @Test
    public void buildROCDataReturnsROCDataGivenNoIncidentAndPreviousROCFormData() {
        ROCData rocData = rocDataModelMapper.convertToROCData(jurisdiction, location, weather);

        //Incident details are null
        assertNull(rocData.getIncidentId());
        assertNull(rocData.getIncidentName());
        assertNull(rocData.getLongitude());
        assertNull(rocData.getLatitude());
        assertNull(rocData.getIncidentType());
        assertNull(rocData.getIncidentDescription());

        //previous ROC form data are null
        assertNull(rocData.getLatestReportType());
        assertNull(rocData.getIncidentCause());
        assertNull(rocData.getGeneralLocation());

        //roc data incident info match with given incident info
        assertEquals(rocData.getSra(), jurisdiction.getSRA());
        assertEquals(rocData.getDpa(), jurisdiction.getDPA());
        assertEquals(rocData.isContractCounty(), jurisdiction.isContractCounty());
        assertEquals(rocData.getJurisdiction(), jurisdiction.getJurisdictionEntity());

        //roc data location info match with given location info
        assertEquals(rocData.getCounty(), location.getCounty());
        assertEquals(rocData.getState(), location.getState());
        assertEquals(rocData.getLocation(), location.getSpecificLocation());

        //roc data weather info match with given weather info
        assertEquals(rocData.getTemperature(), weather.getAirTemperature());
        assertEquals(rocData.getRelHumidity(), weather.getHumidity());
        assertEquals(rocData.getWindSpeed(), weather.getWindSpeed());
        assertEquals(rocData.getWindDirection(), weather.getDescriptiveWindDirection());
    }
}
