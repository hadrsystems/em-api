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
package edu.mit.ll.em.api.rs.validator;

import edu.mit.ll.em.api.dataaccess.EntityCacheMgr;
import edu.mit.ll.nics.common.entity.Form;
import edu.mit.ll.nics.common.entity.FormType;
import edu.mit.ll.nics.common.entity.Incident;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class ReportValidatorTest {
    private EntityCacheMgr entityCacheMgr = mock(EntityCacheMgr.class);
    private ReportValidator reportValidator = new ReportValidator(entityCacheMgr);

    private Form form = null;
    private String reportType = "ROC";
    private FormType formType = new FormType(1, reportType);
    private Incident incident = mock(Incident.class);

    @Before
    public void setup() {
        form = new Form();
        form.setFormtypeid(1);
        form.setUsersessionid(1);
        form.setIncidentid(1);
        form.setIncidentname("Test Incident");
        form.setSeqtime(1000);
        form.setSeqnum(1000);
        form.setMessage("Test Message");
    }

    @Test
    public void validateFormReturnsValidationErrorsGivenNullForm() throws Exception {
        Map<String, String> validationErrors = reportValidator.validateForm(null, reportType, false);

        assertEquals("Form cannot be null", validationErrors.get("form"));
    }

    @Test
    public void validateFormReturnsValidationErrorsGivenInvalidIncidentId() throws Exception {
        form.setIncidentid(-1);
        when(entityCacheMgr.getFormTypeById(form.getFormtypeid())).thenReturn(formType);
        Map<String, String> validationErrors = reportValidator.validateForm(form, reportType, false);

        assertEquals("incidentId(" + form.getIncidentid() + ") is not valid", validationErrors.get("incidentId"));

        form.setIncidentid(1);
        when(entityCacheMgr.getIncidentEntity(form.getIncidentid())).thenReturn(null);
        validationErrors = reportValidator.validateForm(form, reportType, false);

        assertEquals("incidentId(" + form.getIncidentid() + ") is not found", validationErrors.get("incidentId"));
    }


    @Test
    public void validateFormReturnsValidationErrorsGivenInvalidFormTypeIdAndMissingFormTypeId() throws Exception {
        form.setFormtypeid(-1);
        when(entityCacheMgr.getIncidentEntity(form.getIncidentid())).thenReturn(incident);
        when(entityCacheMgr.getFormTypeByName(reportType)).thenReturn(null);
        Map<String, String> validationErrors = reportValidator.validateForm(form, reportType, false);

        assertEquals("Invalid report type: " + reportType, validationErrors.get("reportType"));
    }

    @Test
    public void validateFormSkipsValidatingIncidentIdGivenAFormWithNewIncident() throws Exception {
        form.setIncidentid(-1);
        when(entityCacheMgr.getFormTypeById(form.getFormtypeid())).thenReturn(formType);
        assertFalse(reportValidator.validateForm(form, reportType, true).containsKey("incidentId"));
        verify(entityCacheMgr, never()).getIncidentEntity(form.getIncidentid());
    }


    @Test
    public void validateFormReturnsValidationErrorsGivenNoIncidentDetails()  throws Exception {
        form.setIncident(null);
        when(entityCacheMgr.getFormTypeById(form.getFormtypeid())).thenReturn(formType);
        Map<String, String> validationErrors = reportValidator.validateForm(form, reportType, true);
        assertEquals("Incident details are required to create incident", validationErrors.get("incident"));
    }


    @After
    public void tearDown() {
        reset(entityCacheMgr);
    }
}
