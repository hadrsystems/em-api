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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.configuration.Configuration;
import edu.mit.ll.em.api.rs.*;
import edu.mit.ll.em.api.rs.validator.ReportValidator;
import edu.mit.ll.nics.common.constants.SADisplayConstants;
import edu.mit.ll.nics.common.entity.Form;
import edu.mit.ll.nics.common.entity.FormType;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.nicsdao.impl.*;
import edu.mit.ll.em.api.notification.RocReportNotification;
import org.apache.commons.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class ReportServiceImplPostROCReportAndIncidentTest {

    private static final IncidentDAOImpl incidentDao = mock(IncidentDAOImpl.class);
    private static final UserDAOImpl userDao = mock(UserDAOImpl.class);
    private static final FormDAOImpl formDao = mock(FormDAOImpl.class);
    private static final UserSessionDAOImpl userSessionDao = mock(UserSessionDAOImpl.class);
    private static final UxoreportDAOImpl uxoreportDao = mock(UxoreportDAOImpl.class);
    private IncidentService incidentService = mock(IncidentService.class);
    private RabbitPubSubProducer rabbitProducer = mock(RabbitPubSubProducer.class);
    private ReportValidator reportValidator = mock(ReportValidator.class);
    private Configuration emApConfiguration = mock(Configuration.class);
    public  RocReportNotification rocReportNotification = new RocReportNotification(emApConfiguration, rabbitProducer);
    public ReportServiceImpl reportServiceImpl = new ReportServiceImpl(incidentDao, userDao, formDao, userSessionDao, uxoreportDao, incidentService, rabbitProducer, reportValidator, rocReportNotification);
    private int userSessionId = 1;
    private Form form = null;
    private int formTypeId =  1;
    private String reportType = "ROC";
    private FormType formType = new FormType(1, reportType);
    private User user = mock(User.class);
    private int workspaceId = SADisplayConstants.DEFAULT_WORKSPACE_ID;
    private int orgId = 1;
    private int userId = 1;
    private int incidentId = 1;
    private int newIncidentId = 2;
    private Incident incident = new Incident();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        incident.setIncidentid(incidentId);
        form = new Form();
        form.setFormtypeid(1);
        form.setUsersessionid(1);
        form.setIncident(incident);
        form.setIncidentid(1);
        form.setIncidentname("Test Incident");
        form.setSeqtime(1000);
        form.setSeqnum(1000);
        form.setMessage("Test Message");
        when(user.getUserId()).thenReturn(userId);
    }

    @Test
    public void postROCReportAndIncidentReturnsErrorResponseGivenInvalidUserSession() {
        form.setUsersessionid(-1);
        Response response = reportServiceImpl.postIncidentAndROC(orgId, form);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Unauthorized, session with userSessionId " + form.getUsersessionid() + " is not active.", ((APIResponse)response.getEntity()).getMessage());
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), ((APIResponse)response.getEntity()).getStatus());

        form.setUsersessionid(1);
        when(userDao.getActiveUsers(form.getUsersessionid())).thenReturn(null);
        response = reportServiceImpl.postIncidentAndROC(orgId, form);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        APIResponse apiResponse = ((APIResponse)response.getEntity());
        assertEquals("Unauthorized, session with userSessionId " + form.getUsersessionid() + " is not active.", apiResponse.getMessage());
        assertTrue(Response.Status.UNAUTHORIZED.getStatusCode() == apiResponse.getStatus());
    }

    @Test
    public void postROCReportAndIncidentReturnsValidationErrorsGivenNullForm() throws Exception {
        Map<String, String> validationErrors = new HashMap<>();
        validationErrors.put("form", "validation error");
        when(reportValidator.validateForm(null, reportType, true)).thenReturn(validationErrors);
        Response response = reportServiceImpl.postIncidentAndROC(orgId, null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(validationErrors, ((ValidationErrorResponse)response.getEntity()).getValidationErrors());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ((ValidationErrorResponse)response.getEntity()).getStatus());
    }

    @Test //existing incident name
    public void postROCReportAndIncidentReturnsErrorResponseWhenFailsToCreateNewIncident() throws Exception {
        IncidentServiceResponse incidentServiceResponse = new IncidentServiceResponse();
        incidentServiceResponse.setMessage(IncidentServiceImpl.DUPLICATE_NAME);
        incidentServiceResponse.setCount(0);
        APIResponse apiResponse = new APIResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Error persisting incident & ROC: " + incidentServiceResponse.getMessage());
        Response response = Response.ok(apiResponse).build();

        when(userDao.getUserBySessionId(form.getUsersessionid())).thenReturn(user);
        when(incidentService.postIncident(workspaceId, orgId, userId, incident)).thenReturn(Response.ok(incidentServiceResponse).status(Response.Status.INTERNAL_SERVER_ERROR).build());
        Response returnedResponse = reportServiceImpl.postIncidentAndROC(orgId, form);
        assertEquals(response.getStatus(), returnedResponse.getStatus());
        assertEquals(apiResponse, (APIResponse)returnedResponse.getEntity());
    }

    @Test
    public void postROCReportAndIncidentReturnsErrorResponseWhenSuccessfullyPersistsGivenIncidentButFailsToCreateROC() throws Exception {
        Incident incidentPersisted = mock(Incident.class);
        when(incidentPersisted.getIncidentid()).thenReturn(newIncidentId);
        IncidentServiceResponse incidentServiceResponse = new IncidentServiceResponse();
        incidentServiceResponse.setMessage(Response.Status.OK.getReasonPhrase());
        incidentServiceResponse.setIncidents(Arrays.asList(incidentPersisted));
        incidentServiceResponse.setCount(1);
        Response incidentResponse = Response.ok(incidentServiceResponse).build();

        when(userDao.getUserBySessionId(form.getUsersessionid())).thenReturn(user);
        when(incidentService.postIncident(workspaceId, orgId, userId, incident)).thenReturn(incidentResponse);
        Exception exception = new Exception("Test Exception");
        when(formDao.persistForm(form)).thenThrow(exception);
        String expectedErrorMessage = "Successfully created new incident with name " + form.getIncident().getIncidentname() + ", but failed to persist ROC, Error: " + exception.getMessage();
        Response returnedMessage = reportServiceImpl.postIncidentAndROC(orgId, form);
        assertEquals(Response.Status.OK.getStatusCode(), returnedMessage.getStatus());
        APIResponse apiResponse = (APIResponse) returnedMessage.getEntity();
        assertEquals(expectedErrorMessage, apiResponse.getMessage());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), apiResponse.getStatus());
        assertEquals(newIncidentId, form.getIncidentid());
    }

    @Test
    public void postROCReportAndIncidentSuccessfullyPersistsIncidentAndROC() throws Exception {
        Incident incidentPersisted = mock(Incident.class);
        when(incidentPersisted.getIncidentid()).thenReturn(newIncidentId);
        IncidentServiceResponse incidentServiceResponse = new IncidentServiceResponse();
        incidentServiceResponse.setMessage(Response.Status.OK.getReasonPhrase());
        incidentServiceResponse.setIncidents(Arrays.asList(incidentPersisted));
        incidentServiceResponse.setCount(1);
        Response incidentResponse = Response.ok(incidentServiceResponse).build();

        when(userDao.getUserBySessionId(form.getUsersessionid())).thenReturn(user);
        when(incidentService.postIncident(workspaceId, orgId, userId, incident)).thenReturn(incidentResponse);
        Form persistedForm =  new Form();
        when(formDao.persistForm(form)).thenReturn(persistedForm);

        Response returnedMessage = reportServiceImpl.postIncidentAndROC(orgId, form);
        String topic = String.format("iweb.NICS.incident.%d.report.%s.new", form.getIncidentid(), reportType.toUpperCase());
        verify(rabbitProducer, times(1)).produce(topic, objectMapper.writeValueAsString(form));
        assertEquals(Response.Status.OK.getStatusCode(), returnedMessage.getStatus());
        ReportServiceResponse reportServiceResponse = (ReportServiceResponse) returnedMessage.getEntity();
        assertEquals("success: persisted report", reportServiceResponse.getMessage());
        assertEquals(Response.Status.OK.getStatusCode(), reportServiceResponse.getStatus().intValue());
        assertEquals(persistedForm, reportServiceResponse.getReports().iterator().next());
        assertEquals(1, reportServiceResponse.getCount());
    }


    @Test
    public void postROCReportAndIncidentReturnsOKWhenSuccessfullyPersistsIncidentAndROCButFailsToNotifyOfNewROC() throws Exception {
        Incident incidentPersisted = mock(Incident.class);
        when(incidentPersisted.getIncidentid()).thenReturn(newIncidentId);
        IncidentServiceResponse incidentServiceResponse = new IncidentServiceResponse();
        incidentServiceResponse.setMessage(Response.Status.OK.getReasonPhrase());
        incidentServiceResponse.setIncidents(Arrays.asList(incidentPersisted));
        incidentServiceResponse.setCount(1);
        Response incidentResponse = Response.ok(incidentServiceResponse).build();

        when(userDao.getUserBySessionId(form.getUsersessionid())).thenReturn(user);
        when(incidentService.postIncident(workspaceId, orgId, userId, incident)).thenReturn(incidentResponse);
        Form persistedForm =  new Form();
        when(formDao.persistForm(form)).thenReturn(persistedForm);
        String topic = String.format("iweb.NICS.incident.%d.report.%s.new", form.getIncidentid(), reportType.toUpperCase());
        doThrow(new RuntimeException("Test")).when(rabbitProducer).produce(topic, objectMapper.writeValueAsString(form));

        Response returnedMessage = reportServiceImpl.postIncidentAndROC(orgId, form);
        assertEquals(Response.Status.OK.getStatusCode(), returnedMessage.getStatus());
        ReportServiceResponse reportServiceResponse = (ReportServiceResponse) returnedMessage.getEntity();
        assertEquals("success: persisted report", reportServiceResponse.getMessage());
        assertEquals(persistedForm, reportServiceResponse.getReports().iterator().next());
        assertEquals(1, reportServiceResponse.getCount());
    }

    @After
    public void tearDown() {
        reset(incidentDao, userDao, formDao, userSessionDao, uxoreportDao, incidentService, rabbitProducer);
    }
}
