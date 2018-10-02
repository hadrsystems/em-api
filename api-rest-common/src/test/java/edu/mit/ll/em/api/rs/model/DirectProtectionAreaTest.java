package edu.mit.ll.em.api.rs.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class DirectProtectionAreaTest {

    @Test
    public void getDirectProtectionAreaGroupReturnsCamelCasedValue() {
        DirectProtectionArea directProtectionArea = new DirectProtectionArea("LOCAL", "contract county");
        assertEquals(directProtectionArea.getDirectProtectionAreaGroup(), "Local");
    }

    @Test
    public void getDirectProtectionAreaGroupReturnsNull() {
        DirectProtectionArea directProtectionArea = new DirectProtectionArea(null, "contract county");
        assertNull(directProtectionArea.getDirectProtectionAreaGroup());
    }

    @Test
    public void isContractCountyReturnsTrue() {
        DirectProtectionArea directProtectionArea = new DirectProtectionArea("LOCAL", "contract county");
        assertTrue(directProtectionArea.isContractCounty());
    }

    @Test
    public void isContractCountyReturnsFalse() {
        DirectProtectionArea directProtectionArea = new DirectProtectionArea("LOCAL", "Not C county");
        assertFalse(directProtectionArea.isContractCounty());
    }

    @Test
    public void isContractCountyReturnsFalseGivenNullAgreements() {
        DirectProtectionArea directProtectionArea = new DirectProtectionArea("LOCAL", null);
        assertFalse(directProtectionArea.isContractCounty());
    }

    @Test
    public void isContractCountyReturnsFalseGivenEmptyAgreements() {
        DirectProtectionArea directProtectionArea = new DirectProtectionArea("LOCAL", "");
        assertFalse(directProtectionArea.isContractCounty());
    }
}
