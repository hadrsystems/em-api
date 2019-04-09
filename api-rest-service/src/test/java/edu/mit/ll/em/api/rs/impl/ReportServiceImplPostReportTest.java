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
import edu.mit.ll.em.api.dataaccess.EntityCacheMgr;
import edu.mit.ll.em.api.rs.validator.ReportValidator;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.em.api.rs.IncidentService;
import edu.mit.ll.em.api.rs.ReportServiceResponse;
import edu.mit.ll.nics.common.entity.Form;
import edu.mit.ll.nics.common.entity.FormType;
import edu.mit.ll.nics.common.entity.User;
import edu.mit.ll.nics.common.rabbitmq.RabbitPubSubProducer;
import edu.mit.ll.nics.nicsdao.impl.*;
import edu.mit.ll.em.api.notification.RocReportNotification;
import org.apache.commons.configuration.Configuration;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReportServiceImplPostReportTest {

    private static final IncidentDAOImpl incidentDao = mock(IncidentDAOImpl.class);
    private static final UserDAOImpl userDao = mock(UserDAOImpl.class);
    private static final FormDAOImpl formDao = mock(FormDAOImpl.class);
    private static final UserSessionDAOImpl userSessionDao = mock(UserSessionDAOImpl.class);
    private static final UxoreportDAOImpl uxoreportDao = mock(UxoreportDAOImpl.class);
    private IncidentService incidentService = mock(IncidentService.class);
    private RabbitPubSubProducer rabbitProducer = mock(RabbitPubSubProducer.class);
    private EntityCacheMgr entityCacheMgr = mock(EntityCacheMgr.class);
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
    private int userId = 1;
    private int incidentId = 1;
    private Incident incident = mock(Incident.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() throws Exception {
        form = new Form();
        form.setFormtypeid(1);
        form.setUsersessionid(1);
        form.setIncidentid(1);
        form.setIncidentname("Test Incident");
        form.setSeqtime(1000);
        form.setSeqnum(1000);
        form.setMessage("Test Message");
        when(user.getUserId()).thenReturn(userId);
        when(incident.getIncidentid()).thenReturn(incidentId);
    }

    @Test
    public void postReportReturnsErrorResponseGivenInvalidUserSession() {
        form.setUsersessionid(-1);
        Response response = reportServiceImpl.postReport(form.getIncidentid(), reportType, form);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Unauthorized, session with userSessionId " + form.getUsersessionid() + " is not active.", ((ReportServiceResponse)response.getEntity()).getMessage());

        form.setUsersessionid(1);
        when(userDao.getActiveUsers(form.getUsersessionid())).thenReturn(null);
        response = reportServiceImpl.postReport(form.getIncidentid(), reportType, form);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Unauthorized, session with userSessionId " + form.getUsersessionid() + " is not active.", ((ReportServiceResponse)response.getEntity()).getMessage());
    }

    @Test
    public void postReportReturnsValidationErrorsGivenInvalidForm() throws Exception {
        Map<String, String> validationErrors = new HashMap<>();
        validationErrors.put("form" , "validation error");
        when(userDao.getUserBySessionId(form.getUsersessionid())).thenReturn(user);
        when(reportValidator.validateForm(null, reportType, false)).thenReturn(validationErrors);
        Response response = reportServiceImpl.postReport(form.getIncidentid(), reportType, null);

        assertEquals(Response.Status.EXPECTATION_FAILED.getStatusCode(), response.getStatus());
        assertEquals("failure: [" + validationErrors.get("form") + "]", ((ReportServiceResponse)response.getEntity()).getMessage());
    }

    @Test
    public void postReportReturnsErrorResponseWhenFailsToPersistReport() throws Exception {
        when(userDao.getUserBySessionId(form.getUsersessionid())).thenReturn(user);
        when(entityCacheMgr.getIncidentEntity(form.getIncidentid())).thenReturn(incident);
        when(entityCacheMgr.getFormTypeById(form.getFormtypeid())).thenReturn(formType);
        Exception exception = new Exception("Test Exception");
        when(formDao.persistForm(form)).thenThrow(exception);

        Response response = reportServiceImpl.postReport(form.getIncidentid(), reportType, form);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Failed to persist report: " + exception.getMessage(), ((ReportServiceResponse)response.getEntity()).getMessage());
    }

    @Test
    public void postReportReturnsSuccessfulResponseWhenReportIsSuccessfullyPersisted() throws Exception {
        when(userDao.getUserBySessionId(form.getUsersessionid())).thenReturn(user);
        when(entityCacheMgr.getIncidentEntity(form.getIncidentid())).thenReturn(incident);
        when(entityCacheMgr.getFormTypeById(form.getFormtypeid())).thenReturn(formType);
        Form persistedForm =  mock(Form.class);
        when(formDao.persistForm(form)).thenReturn(persistedForm);

        Response response = reportServiceImpl.postReport(form.getIncidentid(), reportType, form);
        String topic = String.format("iweb.NICS.incident.%d.report.%s.new", form.getIncidentid(), reportType.toUpperCase());
        verify(rabbitProducer, times(1)).produce(topic, objectMapper.writeValueAsString(form));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ReportServiceResponse reportServiceResponse = ((ReportServiceResponse)response.getEntity());
        assertEquals("success: persisted report", reportServiceResponse.getMessage());
        assertEquals(1, reportServiceResponse.getCount());
        assertEquals(persistedForm, reportServiceResponse.getReports().iterator().next());
    }

    @Test
    public void postReportReturnsSuccessfulResponseWhenReportIsPersistedButFailsToNotifyOfNewReport() throws Exception {
        when(userDao.getUserBySessionId(form.getUsersessionid())).thenReturn(user);
        when(entityCacheMgr.getIncidentEntity(form.getIncidentid())).thenReturn(incident);
        when(entityCacheMgr.getFormTypeById(form.getFormtypeid())).thenReturn(formType);
        Form persistedForm =  new Form();
        when(formDao.persistForm(form)).thenReturn(persistedForm);
        String topic = String.format("iweb.NICS.incident.%d.report.%s.new", form.getIncidentid(), reportType.toUpperCase());
        doThrow(new IOException("Test")).when(rabbitProducer).produce(topic, objectMapper.writeValueAsString(form));

        Response response = reportServiceImpl.postReport(form.getIncidentid(), reportType, form);
        verify(rabbitProducer, times(1)).produce(topic, objectMapper.writeValueAsString(form));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ReportServiceResponse reportServiceResponse = ((ReportServiceResponse)response.getEntity());
        assertEquals("success: persisted report", reportServiceResponse.getMessage());
        assertEquals(1, reportServiceResponse.getCount());
        assertEquals(persistedForm, reportServiceResponse.getReports().iterator().next());
    }

    @After
    public void tearDown() {
        reset(incidentDao, userDao, formDao, userSessionDao, uxoreportDao, incidentService, rabbitProducer, entityCacheMgr);
    }
}
