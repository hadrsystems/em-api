package edu.mit.ll.em.api.rs.model;

public class Jurisdiction {
    String sra;
    DirectProtectionArea directProtectionArea;

    public Jurisdiction(String sra, DirectProtectionArea directProtectionArea) {
        this.sra = sra;
        this.directProtectionArea = directProtectionArea;
    }

    public String getSRA() {
        return this.sra;
    }

    public String getDPA() {
        return this.directProtectionArea == null ? null : this.directProtectionArea.getDirectProtectionAreaGroup();
    }

    public String getJurisdiction() {
        return this.directProtectionArea == null ? null : this.directProtectionArea.getJurisdiction();
    }
}
