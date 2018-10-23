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

import com.vividsolutions.jts.geom.Coordinate;
import edu.mit.ll.em.api.rs.APIResponse;
import edu.mit.ll.em.api.rs.model.*;
import edu.mit.ll.em.api.rs.ROCReportService;
import edu.mit.ll.em.api.rs.ValidationErrorResponse;
import edu.mit.ll.em.api.rs.response.ROCDataResponse;
import edu.mit.ll.em.api.rs.response.ROCLocationBasedDataResponse;
import edu.mit.ll.em.api.service.ROCService;
import edu.mit.ll.em.api.util.APILogger;
import edu.mit.ll.em.api.util.CRSTransformer;
import edu.mit.ll.nics.common.constants.SADisplayConstants;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.nicsdao.IncidentDAO;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.Response;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ROCReportServiceImplTest {
    private IncidentDAO incidentDAO = mock(IncidentDAO.class);
    private ROCService rocService = mock(ROCService.class);
    private CRSTransformer crsTransformer = mock(CRSTransformer.class);
    private APILogger logger = mock(APILogger.class);
    private ROCReportService rocReportService = new ROCReportServiceImpl(incidentDAO, rocService, crsTransformer, logger);
    private Double latitude = 89.0;
    private Double longitude = 88.0;
    private String locationCRS = "EPSG:3857";
    private Double searchRange = 10.0;
    private Double searchRangeInKM = searchRange * SADisplayConstants.KM_PER_MILE;
    private Coordinate coordinatesInCRS4326 = new Coordinate(-69.0, 76.0);
    private ROCLocationBasedData rocLocationBasedData = mock(ROCLocationBasedData.class);

    private int incidentId = 1;
    private Incident incident = mock(Incident.class);
    private ROCForm rocForm = mock(ROCForm.class);

    @Before
    public void setup() throws Exception {
        when(crsTransformer.transformCoordinatesToTargetCRS(longitude, latitude, locationCRS, SADisplayConstants.CRS_4326)).thenReturn(coordinatesInCRS4326);
        when(rocService.getROCLocationBasedData(coordinatesInCRS4326, searchRangeInKM)).thenReturn(rocLocationBasedData);
        when(incident.getIncidentid()).thenReturn(incidentId);
        when(incidentDAO.getIncident(incidentId)).thenReturn(incident);
    }

    @Test
    public void getROCFormDataByLocationReturnsDataSuccessfully() {
        Response response = rocReportService.getROCLocationBasedData(longitude, latitude, locationCRS, searchRange);
        ROCLocationBasedDataResponse locationBasedDataResponse = (ROCLocationBasedDataResponse) response.getEntity();
        ROCLocationBasedData rocLocationBasedDataInResponse  = locationBasedDataResponse.getData();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(locationBasedDataResponse.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(rocLocationBasedData, rocLocationBasedDataInResponse);
    }

    @Test
    public void getROCFormDataByLocationReturnsValidationErrorsWhenLongitudeIsNotProvided() {
        Response response = rocReportService.getROCLocationBasedData(null, latitude, locationCRS, searchRange);
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) response.getEntity();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), validationErrorResponse.getStatus());
        assertEquals(Response.Status.BAD_REQUEST.name(), validationErrorResponse.getMessage());
        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("longitude"), "Longitude is required");
    }

    @Test
    public void getROCFormDataByLocationReturnsValidationErrorsWhenLatitudeIsNotProvided() {
        Response response =  rocReportService.getROCLocationBasedData(longitude, null, locationCRS, searchRange);
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) response.getEntity();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), validationErrorResponse.getStatus());
        assertEquals("BAD_REQUEST", validationErrorResponse.getMessage());
        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("latitude"), "Latitude is required");
    }

    @Test
    public void getROCFormDataByLocationReturnsValidationErrorsWhenLocationCRSIsInvalid() {
        Response response = rocReportService.getROCLocationBasedData(longitude, latitude, "InvalidCRS", searchRange);
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) response.getEntity();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), validationErrorResponse.getStatus());
        assertEquals("BAD_REQUEST", validationErrorResponse.getMessage());
        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("locationCRS"), "No Such CRS exists, please provide a valid CRS");
    }

    @Test
    public void getROCFormDataByLocationReturnsValidationErrorsWhenSearchRangeIsInvalid() {
        Response response = rocReportService.getROCLocationBasedData(longitude, latitude, "InvalidCRS", -1.0);
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) response.getEntity();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), validationErrorResponse.getStatus());
        assertEquals("BAD_REQUEST", validationErrorResponse.getMessage());
        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("searchRangeInMiles"), "Please provide valid search range > 0");
    }

    @Test
    public void getROCFormDataByLocationReturnsErrorResponseWhenFailsToGetWeatherData() throws Exception {
        RuntimeException exception = new RuntimeException("Test data");

        when(rocService.getROCLocationBasedData(coordinatesInCRS4326, searchRangeInKM)).thenThrow(exception);
        Response response = rocReportService.getROCLocationBasedData(longitude, latitude, locationCRS, searchRange);
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(apiResponse.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertTrue(apiResponse.getErrorMessage().contains(exception.getMessage()));
    }

    @Test
    public void getEditROCFormForAnIncidentReturnsEditROCFormSuccessfullyGivenValidIncidentId() throws Exception {

        when(rocService.getEditROCForm(incident, searchRangeInKM)).thenReturn(rocForm);
        Response response = rocReportService.getEditROCFormForAnIncident(incidentId, searchRange);
        ROCDataResponse rocDataResponse = (ROCDataResponse) response.getEntity();
        ROCForm rocFormInResponse  = rocDataResponse.getData();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(rocDataResponse.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(rocFormInResponse, rocForm);
    }

    @Test
    public void getROCFormDataByLocationReturnsErrorResponseWhenFailsToGetEditROCForm() throws Exception {
        RuntimeException exception = new RuntimeException("Test data");

        when(rocService.getEditROCForm(incident, searchRangeInKM)).thenThrow(exception);
        Response response = rocReportService.getEditROCFormForAnIncident(incidentId, searchRange);
        APIResponse apiResponse = (APIResponse) response.getEntity();
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        assertEquals(apiResponse.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertTrue(apiResponse.getErrorMessage().contains(exception.getMessage()));
    }

    @Test
    public void getROCFormDataByLocationReturnsValidationErrorsGivenInvalidIncidentId() {
        int invalidIncidentId = 112;
        when(incidentDAO.getIncident(invalidIncidentId)).thenReturn(null);
        Response response = rocReportService.getEditROCFormForAnIncident(invalidIncidentId, searchRange);
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) response.getEntity();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), validationErrorResponse.getStatus());
        assertEquals(Response.Status.BAD_REQUEST.name(), validationErrorResponse.getMessage());
        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("incidentId"), "Invalid incidentId " + invalidIncidentId);
    }

    @Test
    public void getEditROCFormReturnsValidationErrorsWhenSearchRangeIsInvalid() {
        Response response = rocReportService.getEditROCFormForAnIncident(incidentId, -1.0);
        ValidationErrorResponse validationErrorResponse = (ValidationErrorResponse) response.getEntity();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), validationErrorResponse.getStatus());
        assertEquals(Response.Status.BAD_REQUEST.name(), validationErrorResponse.getMessage());
        Map<String, String> validationErrors = validationErrorResponse.getValidationErrors();
        assertFalse(CollectionUtils.isEmpty(validationErrors));
        assertEquals(validationErrors.get("searchRangeInMiles"), "Please provide valid search range > 0");
    }

//    @Test
//    public void getROCFormDataByLocationReturnsErrorResponseWhenFailsToGetWeatherData() {
//        RuntimeException exception = new RuntimeException("Test data");
//
////        when(weatherDAO.getWeatherDataFromLocation(coordinatesInCRS4326, searchRangeInKM)).thenThrow(exception);
//        Response response = rocReportService.getROCLocationBasedData(longitude, latitude, locationCRS, searchRange);
//        APIResponse apiResponse = (APIResponse) response.getEntity();
//        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
//        assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
//        assertTrue(apiResponse.getMessage().contains(exception.getMessage()));
//    }
//
//    @Test
//    public void getROCFormDataByLocationReturnsErrorWhenFailingToGetJurisdictionData() throws Exception {
//        RuntimeException exception = new RuntimeException("Test data");
//
//        Response response = rocReportService.getROCLocationBasedData(longitude, latitude, locationCRS, searchRange);
//        APIResponse apiResponse = (APIResponse) response.getEntity();
//        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
//        assertEquals(apiResponse.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
//        assertTrue(apiResponse.getMessage().contains(exception.getMessage()));
//    }
//
//    @Test
//    public void getROCFormDataByLocationReturnsErrorResponseWhenFailsToGetLocationData() throws IOException {
//        GeocodeException exception = new GeocodeException("ERROR_CODE", "Test data");
//
//        Response response = rocReportService.getROCLocationBasedData(longitude, latitude, locationCRS, searchRange);
//        APIResponse apiResponse = (APIResponse) response.getEntity();
//        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
//        assertEquals(apiResponse.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
//        assertTrue(apiResponse.getMessage().contains(exception.getMessage()));
//    }

}
