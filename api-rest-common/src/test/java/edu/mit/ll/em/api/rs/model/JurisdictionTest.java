package edu.mit.ll.em.api.rs.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class JurisdictionTest {

    private String sra = "sra";
    private DirectProtectionArea directProtectionArea = new DirectProtectionArea("dpa", "contract county");
    private String jurisdictionEntity = "yyy county";
    private Jurisdiction jurisdiction = new Jurisdiction(sra, directProtectionArea, jurisdictionEntity);

    @Test
    public void getSRAReturnsResponsibilityArea() {
        assertEquals(jurisdiction.getSRA(), sra);
    }

    @Test
    public void getDPAReturnsDirectProtectionAreaGroup() {
        assertEquals(jurisdiction.getDPA(), directProtectionArea.getDirectProtectionAreaGroup());
    }

    @Test
    public void getDPAReturnsNullGivenDirectProtectionAreaInstanceIsNull() {
        Jurisdiction jurisdiction = new Jurisdiction(sra, null, "yyy county");
        assertNull(jurisdiction.getDPA());
    }

    @Test
    public void isContractCountyReturnsTrue() {
        assertTrue(jurisdiction.isContractCounty());
    }

    @Test
    public void isContractCountyReturnsFalseGivenDirectProtectionAreaWhichIsNotAContractCounty() {
        DirectProtectionArea directProtectionArea = new DirectProtectionArea("dpa", "Not A C county");
        Jurisdiction jurisdiction = new Jurisdiction(sra, directProtectionArea, "yyy county");
        assertFalse(jurisdiction.isContractCounty());
    }

    @Test
    public void isContractCountyReturnsFalseGivenDirectProtectionAreaInstanceIsNull() {
        Jurisdiction jurisdiction = new Jurisdiction(sra, null, "yyy county");
        assertFalse(jurisdiction.isContractCounty());
    }

    @Test
    public void getJurisdictionAreaReturnsJurisdictionEntity() {
        assertEquals(jurisdiction.getJurisdictionEntity(), jurisdictionEntity);
    }

}