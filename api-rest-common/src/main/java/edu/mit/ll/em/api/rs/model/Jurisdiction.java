package edu.mit.ll.em.api.rs.model;

public class Jurisdiction {
    String sra;
    DirectProtectionArea directProtectionArea;
    String jurisdictionEntity;

    public Jurisdiction(String sra, DirectProtectionArea directProtectionArea, String jurisdictionEntity) {
        this.sra = sra;
        this.directProtectionArea = directProtectionArea;
        this.jurisdictionEntity = jurisdictionEntity;
    }

    public String getSRA() {
        return this.sra;
    }

    public String getDPA() {
        return this.directProtectionArea == null ? null : this.directProtectionArea.getDirectProtectionAreaGroup();
    }

    public boolean isContractCounty() {
        return this.directProtectionArea == null ? false : this.directProtectionArea.isContractCounty();
    }

    public String getJurisdictionEntity() {
        return this.jurisdictionEntity;
    }
}
