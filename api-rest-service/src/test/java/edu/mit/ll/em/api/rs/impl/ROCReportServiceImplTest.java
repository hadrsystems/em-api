package edu.mit.ll.em.api.rs.impl;

import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.rs.APIResponse;
import edu.mit.ll.em.api.rs.ROCDataResponse;
import edu.mit.ll.em.api.rs.ROCReportService;
import edu.mit.ll.em.api.rs.ValidationErrorResponse;
import edu.mit.ll.em.api.rs.model.DirectProtectionArea;
import edu.mit.ll.em.api.rs.model.Jurisdiction;
import edu.mit.ll.em.api.rs.model.ROCData;
import edu.mit.ll.em.api.rs.model.WeatherModel;
import edu.mit.ll.em.api.rs.model.mapper.ROCDataModelMapper;
import edu.mit.ll.em.api.service.JurisdictionLocatorService;
import edu.mit.ll.em.api.gateway.geocode.GeocodeAPIGateway;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.CRSTransformer;
import edu.mit.ll.nics.common.constants.SADisplayConstants;
import edu.mit.ll.nics.common.entity.Weather;
import edu.mit.ll.nics.nicsdao.WeatherDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ROCReportServiceImplTest {
    private WeatherDAO weatherDAO = mock(WeatherDAO.class);
    private CRSTransformer crsTransformer = mock(CRSTransformer.class);
    private GeocodeAPIGateway locationService = mock(GeocodeAPIGateway.class);
    private APILogger logger = mock(APILogger.class);
    private JurisdictionLocatorService jurisdictionLocatorService = mock(JurisdictionLocatorService.class);
    private ROCDataModelMapper rocDataModelMapper = mock(ROCDataModelMapper.class);
    private ROCReportService rocReportService = new ROCReportServiceImpl(jurisdictionLocatorService, weatherDAO, locationService, crsTransformer, rocDataModelMapper, logger);
    private Double latitude = 89.0;
    private Double longitude = 88.0;
    private String locationCRS = "EPSG:3857";
    private Double searchRange = 10.0;
    private Double defaultSearchRange = 10.0;
    private Double searchRangeInKM = searchRange * SADisplayConstants.KM_PER_MILE;
    private Coordinate coordinatesInCRS4326 = new Coordinate(-69.0, 76.0);
    private Weather weather = new Weather("objectid", "location",
            80.01, 9.22f, 310.0, 38.85f, "OK", 8.018);
    private WeatherModel weatherModel = new WeatherModel(weather);
    private String sra = "sra";
    private DirectProtectionArea directProtectionArea = new DirectProtectionArea("Local", "Contract County");
    private String jurisdictionEntity = "jurisdiction entity xx county";
    private Jurisdiction jurisdiction = new Jurisdiction(sra, directProtectionArea, jurisdictionEntity);
    private ROCData rocData = mock(ROCData.class);

    @Before
    public void setup() throws Exception {
        when(crsTransformer.transformCoordinatesToTargetCRS(longitude, latitude, locationCRS, SADisplayConstants.CRS_4326)).thenReturn(coordinatesInCRS4326);
        when(jurisdictionLocatorService.getJurisdiction(coordinatesInCRS4326, SADisplayConstants.CRS_4326)).thenReturn(jurisdiction);
        when(weatherDAO.getWeatherDataFromLocation(coordinatesInCRS4326, searchRangeInKM)).thenReturn(weather);
        when(rocDataModelMapper.convertToROCData(null, jurisdiction, null, weather, null, null, null)).thenReturn(rocData);
        //when(locationService.getLocation(coordinatesInCRS4326)).thenReturn(location);
    }

    @Test
    public void getROCFormDataByLocationReturnsDataSuccessfully() {
        ROCDataResponse response = (ROCDataResponse) rocReportService.getROCFormDataByLocation(longitude, latitude, locationCRS, searchRange);
        ROCData rocDataReturned = response.getData();
        assertNotNull(rocDataReturned);

        assertEquals(rocDataReturned, rocData);
    }

    @Test
    public void getROCFormDataByLocationReturnsValidationErrorsWhenLongitudeIsNotProvided() {
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) rocReportService.getROCFormDataByLocation(null, latitude, locationCRS, searchRange);

        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("longitude"), "Longitude is required");
    }

    @Test
    public void getROCFormDataByLocationReturnsValidationErrorsWhenLatitudeIsNotProvided() {
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) rocReportService.getROCFormDataByLocation(longitude, null, locationCRS, searchRange);

        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("latitude"), "Latitude is required");
    }

    @Test
    public void getROCFormDataByLocationReturnsValidationErrorsWhenLocationCRSIsInvalid() {
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) rocReportService.getROCFormDataByLocation(longitude, latitude, "InvalidCRS", searchRange);

        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("locationCRS"), "No Such CRS exists, please provide a valid CRS");
    }

    @Test
    public void getROCFormDataByLocationReturnsValidationErrorsWhenSearchRangeIsInvalid() {
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) rocReportService.getROCFormDataByLocation(longitude, latitude, "InvalidCRS", -1.0);

        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("searchRangeInMiles"), "Please provide valid search range > 0");
    }

    @Test
    public void getROCFormDataByLocationReturnsErrorResponseWhenFailsToGetWeatherData() {
        RuntimeException exception = new RuntimeException("Test data");
        Mockito.reset(weatherDAO);
        when(weatherDAO.getWeatherDataFromLocation(coordinatesInCRS4326, searchRangeInKM)).thenThrow(exception);
        APIResponse response = rocReportService.getROCFormDataByLocation(longitude, latitude, locationCRS, searchRange);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertTrue(response.getMessage().contains(exception.getMessage()));
    }

    @Test
    public void getROCFormDataByLocationReturnsErrorWhenFailingToGetJurisdictionData() throws Exception {
        RuntimeException exception = new RuntimeException("Test data");
        Mockito.reset(jurisdictionLocatorService);
        when(jurisdictionLocatorService.getJurisdiction(coordinatesInCRS4326, SADisplayConstants.CRS_4326)).thenThrow(exception);
        APIResponse response = rocReportService.getROCFormDataByLocation(longitude, latitude, locationCRS, searchRange);
        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertTrue(response.getMessage().contains(exception.getMessage()));
    }

    @Test
    public void getROCFormDataByLocationReturnsErrorResponseWhenFailsToGetLocationData() {
        //to be filled later
    }

}
