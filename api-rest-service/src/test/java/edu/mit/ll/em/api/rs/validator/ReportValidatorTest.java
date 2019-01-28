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
