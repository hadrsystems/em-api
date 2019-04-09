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