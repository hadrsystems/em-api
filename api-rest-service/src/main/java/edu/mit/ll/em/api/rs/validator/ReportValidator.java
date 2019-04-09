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
