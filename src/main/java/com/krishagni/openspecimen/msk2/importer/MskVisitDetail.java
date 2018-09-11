package com.krishagni.openspecimen.msk2.importer;

import java.util.Date;

public class MskVisitDetail {
	
	private String firstName;
	
	private String lastName;
	
	private String ppid;
	
	private String mrn;
	
	private String cpShortTitle;
	
	private Date visitDate;
	
	private String siteName;

	private String studyPhase;

	private String visitComments;

	private String eventPoint;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPpid() {
		return ppid;
	}

	public void setPpid(String ppid) {
		this.ppid = ppid;
	}

	public String getMrn() {
		return mrn;
	}

	public void setMrn(String mrn) {
		this.mrn = mrn;
	}

	public String getCpShortTitle() {
		return cpShortTitle;
	}

	public void setCpShortTitle(String cpShortTitle) {
		this.cpShortTitle = cpShortTitle;
	}

	public Date getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(Date visitDate) {
		this.visitDate = visitDate;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getVisitComments() {
		return visitComments;
	}

	public void setVisitComments(String visitComments) {
		this.visitComments = visitComments;
	}

	public String getStudyPhase() {
		return studyPhase;
	}

	public void setStudyPhase(String studyPhase) {
		this.studyPhase = studyPhase;
	}

	public String getEventPoint() {
		return eventPoint;
	}

	public void setEventPoint(String eventPoint) {
		this.eventPoint = eventPoint;
	}
}