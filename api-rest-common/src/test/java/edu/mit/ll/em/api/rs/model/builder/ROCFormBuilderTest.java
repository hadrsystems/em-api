package edu.mit.ll.em.api.rs.model.builder;

import edu.mit.ll.em.api.rs.model.ROCForm;
import edu.mit.ll.em.api.rs.model.ROCMessage;
import edu.mit.ll.nics.common.entity.Incident;
import edu.mit.ll.nics.common.entity.IncidentIncidentType;
import edu.mit.ll.nics.common.entity.IncidentType;
import org.apache.commons.lang.StringUtils;

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
        assertEquals(rocForm.getIncidentType(), getIncidentTypeNames(incident.getIncidentIncidenttypes()));
        assertEquals(rocForm.getIncidentDescription(), incident.getDescription());

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
        assertNull(rocForm.getIncidentType());
        assertNull(rocForm.getIncidentDescription());
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

        assertEquals(rocForm.getIncidentType(), getIncidentTypeNames(incident.getIncidentIncidenttypes()));
        assertEquals(rocForm.getIncidentDescription(), incident.getDescription());
        assertEquals(rocMessage, rocForm.getMessage());
    }

    private String getIncidentTypeNames(Set<IncidentIncidentType> incidentIncidentTypeSet) {
        List<String> incidentTypeNameList = new ArrayList<>();
        for(IncidentIncidentType incidentIncidentType : incidentIncidentTypeSet) {
            incidentTypeNameList.add(incidentIncidentType.getIncidentType().getIncidentTypeName());
        }
        return StringUtils.join(incidentTypeNameList, ", ");
    }
}
