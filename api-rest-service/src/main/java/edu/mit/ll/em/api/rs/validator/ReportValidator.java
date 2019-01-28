package edu.mit.ll.em.api.rs.validator;

import edu.mit.ll.em.api.dataaccess.EntityCacheMgr;
import edu.mit.ll.em.api.dataaccess.ICSDatastoreException;
import edu.mit.ll.nics.common.entity.Form;
import edu.mit.ll.nics.common.entity.FormType;

import java.util.HashMap;
import java.util.Map;

public class ReportValidator {
    private EntityCacheMgr entityCacheMgr = null;

    public ReportValidator(EntityCacheMgr entityCacheMgr) {
        this.entityCacheMgr = entityCacheMgr;
    }

    public Map<String, String> validateForm(Form form, String reportType, boolean newIncident) throws ICSDatastoreException {
        Map<String, String> validationErrors = new HashMap<>();

        if(form == null) {
            validationErrors.put("form", "Form cannot be null");
            return validationErrors;
        }

        if(!newIncident) {
            int incidentId = form.getIncidentid();
            if (incidentId < 0) {
                validationErrors.put("incidentId", "incidentId(" + incidentId + ") is not valid");
            } else {
                if (entityCacheMgr.getIncidentEntity(incidentId) == null) {
                    validationErrors.put("incidentId", "incidentId(" + incidentId + ") is not found");
                }
            }
        } else {
            if(form.getIncident() == null) {
                validationErrors.put("incident", "Incident details are required to create incident");
            }
        }

        FormType formType;
        if(form.getFormtypeid() <= 0 || entityCacheMgr.getFormTypeById(form.getFormtypeid()) == null) {
            formType = entityCacheMgr.getFormTypeByName(reportType);
            if (formType == null) {
                validationErrors.put("reportType", "Invalid report type: " + reportType);
            } else {
                form.setFormtypeid(formType.getFormTypeId());
            }
        }

        return validationErrors;
    }
}
