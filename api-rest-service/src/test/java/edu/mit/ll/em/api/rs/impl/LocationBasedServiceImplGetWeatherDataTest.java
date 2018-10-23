package edu.mit.ll.em.api.rs.impl;

import java.util.Map;
import com.vividsolutions.jts.geom.Coordinate;

import javax.ws.rs.core.Response;

import edu.mit.ll.nics.common.constants.SADisplayConstants;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.CollectionUtils;

import edu.mit.ll.em.api.rs.APIResponse;
import edu.mit.ll.em.api.rs.ValidationErrorResponse;
import edu.mit.ll.em.api.rs.WeatherResponse;
import edu.mit.ll.em.api.rs.model.WeatherModel;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.CRSTransformer;
import edu.mit.ll.nics.common.entity.Weather;
import edu.mit.ll.nics.nicsdao.WeatherDAO;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocationBasedServiceImplGetWeatherDataTest {

    private WeatherDAO weatherDAO = mock(WeatherDAO.class);
    private CRSTransformer crsTransformer = mock(CRSTransformer.class);
    private APILogger logger = mock(APILogger.class);
    private LocationBasedServiceImpl locationBasedService = new LocationBasedServiceImpl(weatherDAO, crsTransformer, logger);
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

    @Test
    public void getWeatherDataReturnsWeatherModelSuccessfully() {
        when(crsTransformer.transformCoordinatesToTargetCRS(longitude, latitude, locationCRS, SADisplayConstants.CRS_4326)).thenReturn(coordinatesInCRS4326);
        when(weatherDAO.getWeatherDataFromLocation(coordinatesInCRS4326, searchRangeInKM)).thenReturn(weather);
        APIResponse response = locationBasedService.getWeatherData(longitude, latitude, locationCRS, searchRange);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getMessage(), "OK");
        assertTrue(response instanceof WeatherResponse);
        WeatherResponse weatherResponse = (WeatherResponse) response;
        assertEquals(weatherResponse.getWeatherData().getObjectId(), weather.getObjectId());
    }

    @Test
    public void getWeatherDataReturnsNoResultsWhenNoDataIsFoundInGivenSearchRangeOfLocation() {
        when(crsTransformer.transformCoordinatesToTargetCRS(longitude, latitude, locationCRS, SADisplayConstants.CRS_4326)).thenReturn(coordinatesInCRS4326);
        when(weatherDAO.getWeatherDataFromLocation(coordinatesInCRS4326, searchRangeInKM)).thenReturn(null);
        APIResponse response = locationBasedService.getWeatherData(longitude, latitude, locationCRS, searchRange);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getMessage(), "NO_DATA_FOUND");
    }

    @Test
    public void getWeatherDataReturnsErrorWhenFailingToConvertGivenLocationToCRS4326() {
        RuntimeException exception = new RuntimeException("Test exception");
        when(crsTransformer.transformCoordinatesToTargetCRS(longitude, latitude, locationCRS, SADisplayConstants.CRS_4326)).thenThrow(exception);
        APIResponse response = locationBasedService.getWeatherData(longitude, latitude, locationCRS, searchRange);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getMessage(), "INTERNAL_SERVER_ERROR");
        assertTrue(response.getErrorMessage().contains(exception.getMessage()));
    }


    @Test
    public void getWeatherDataReturnsErrorWhenFailsToGetDataFromDB() {
        DataAccessException exception = new DataAccessResourceFailureException("Test exception");
        when(crsTransformer.transformCoordinatesToTargetCRS(longitude, latitude, locationCRS, SADisplayConstants.CRS_4326)).thenReturn(coordinatesInCRS4326);
        when(weatherDAO.getWeatherDataFromLocation(coordinatesInCRS4326, searchRangeInKM)).thenThrow(exception);

        APIResponse response = locationBasedService.getWeatherData(longitude, latitude, locationCRS, searchRange);

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(response.getMessage(), "INTERNAL_SERVER_ERROR");
        assertTrue(response.getErrorMessage().contains(exception.getMessage()));
    }

    @Test
    public void getWeatherDataReturnsValidationErrorsWhenLongitudeIsNotProvided() {
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) locationBasedService.getWeatherData(null, latitude, locationCRS, searchRange);

        assertEquals(validationErrorResponse.getStatus(), 200);
        assertEquals(validationErrorResponse.getMessage(), "BAD_REQUEST");
        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("longitude"), "Longitude is required");
    }

    @Test
    public void getWeatherDataReturnsValidationErrorsWhenLatitudeIsNotProvided() {
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) locationBasedService.getWeatherData(longitude, null, locationCRS, searchRange);

        assertEquals(validationErrorResponse.getStatus(), 200);
        assertEquals(validationErrorResponse.getMessage(), "BAD_REQUEST");
        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("latitude"), "Latitude is required");
    }

    @Test
    public void getWeatherDataReturnsValidationErrorsWhenLocationCRSIsInvalid() {
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) locationBasedService.getWeatherData(longitude, latitude, "InvalidCRS", searchRange);

        assertEquals(validationErrorResponse.getStatus(), 200);
        assertEquals(validationErrorResponse.getMessage(), "BAD_REQUEST");
        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("locationCRS"), "No Such CRS exists, please provide a valid CRS");
    }

    @Test
    public void getWeatherDataReturnsValidationErrorsWhenSearchRangeIsInvalid() {
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) locationBasedService.getWeatherData(longitude, latitude, "InvalidCRS", -1.0);

        assertEquals(validationErrorResponse.getStatus(), 200);
        assertEquals(validationErrorResponse.getMessage(), "BAD_REQUEST");
        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("searchRangeInMiles"), "Please provide valid search range > 0");
    }
}