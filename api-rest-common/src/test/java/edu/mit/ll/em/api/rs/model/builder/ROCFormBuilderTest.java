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
package edu.mit.ll.em.api.rs.model.builder;

import edu.mit.ll.em.api.rs.model.ROCForm;
import edu.mit.ll.em.api.rs.model.ROCMessage;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.IncidentIncidentType;
import edu.mit.ll.nics.common.entity.IncidentType;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.util.*;

public class ROCFormBuilderTest {
    private Incident incident = new Incident(1, "incidentname", -121.987987, 35.09809, new Date(), new Date(), true, "/root/incident/folder");
    private ROCMessage rocMessage = mock(ROCMessage.class);

    @Before
    public void setup() {
        incident = new Incident(1, "incidentname", -121.987987, 35.09809, new Date(), new Date(), true, "/root/incident/folder");
        Set<IncidentIncidentType> incidentIncidentTypeSet = new HashSet<IncidentIncidentType>();
        incidentIncidentTypeSet.add(new IncidentIncidentType(1, incident, new IncidentType(1, "Planned Event")));
        incidentIncidentTypeSet.add(new IncidentIncidentType(2, incident, new IncidentType(2, "Fun Event")));
        incident.setIncidentIncidenttypes(incidentIncidentTypeSet);
    }

    @Test
    public void buildsROCFormWithGivenIncidentDetailsAndLeavesROCMessageFieldNull() {
        ROCForm rocForm = new ROCFormBuilder().buildIncidentData(incident).build();
        assertEquals(rocForm.getIncidentId(), incident.getIncidentid());
        assertEquals(rocForm.getIncidentName(), incident.getIncidentname());
        assertEquals(rocForm.getLongitude(), (Double) incident.getLon());
        assertEquals(rocForm.getLatitude(), (Double) incident.getLat());
        assertEquals(incident.getIncidentTypes(), rocForm.getIncidentTypes());

        assertNull(rocForm.getMessage());
    }

    @Test
    public void buildsROCFormWithGivenROCMessageAndLeavesIncidentFieldsNull() {
        ROCForm rocForm = new ROCFormBuilder().buildROCMessage(rocMessage).build();
        assertEquals(rocMessage, rocForm.getMessage());

        assertNull(rocForm.getIncidentId());
        assertNull(rocForm.getIncidentName());
        assertNull(rocForm.getLongitude());
        assertNull(rocForm.getLatitude());
        assertNull(rocForm.getIncidentTypes());
    }

    @Test
    public void buildsROCFormWithGivenIncidentDetailsAndROCMessage() {
        ROCForm rocForm = new ROCFormBuilder()
                            .buildIncidentData(incident)
                            .buildROCMessage(rocMessage)
                            .build();

        assertEquals(rocForm.getIncidentId(), incident.getIncidentid());
        assertEquals(rocForm.getIncidentName(), incident.getIncidentname());
        assertEquals(rocForm.getLongitude(), (Double) incident.getLon());
        assertEquals(rocForm.getLatitude(), (Double) incident.getLat());

        assertEquals(incident.getIncidentTypes(), rocForm.getIncidentTypes());
        assertEquals(rocMessage, rocForm.getMessage());
    }
}
