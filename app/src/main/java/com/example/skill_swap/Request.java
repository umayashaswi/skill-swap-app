package com.example.skill_swap;

import com.google.firebase.firestore.DocumentSnapshot;

public class Request {

    private String id;

    private String fromUid;
    private String fromUserSkills;
    private String fromUserName;
    private String status;
    private String preferredDate;
    private String preferredTime;
    private String reason;

    private String skillOffered;
    private String skillWanted;
    private String skillLevel;
    private String yearsOfExperience;
    private String experienceDescription;
    private String portfolioLink;
    private String whyLearn;
    private String communicationMode;
    private String numberOfSessions;
    private String sessionDuration;
    private String expectedProficiency;
    private String otherCommunication;
    private String message;

    private String toUid;
    private String toUserName;
    private String toUserSkills;
    private String fromUserArea;
    private String fromUserBio;
    private float fromUserRating;

    // Required: empty constructor for Firebase
    public Request() {
    }

    // ID setter from Firestore doc
    public void setId(String id) {
        this.id = id;
    }

    // Factory method to create Request from Firestore document snapshot
    public static Request fromDoc(DocumentSnapshot doc) {
        Request r = doc.toObject(Request.class);
        if (r != null) {
            r.setId(doc.getId());
        }
        return r;
    }

    // Getters and Setters for all fields

    public String getId() {
        return id;
    }
    public String getFromUserArea() {
        return fromUserArea;
    }

    public void setFromUserArea(String fromUserArea) {
        this.fromUserArea = fromUserArea;
    }

    public String getFromUserBio() {
        return fromUserBio;
    }

    public void setFromUserBio(String fromUserBio) {
        this.fromUserBio = fromUserBio;
    }

    public float getFromUserRating() {
        return fromUserRating;
    }

    public void setFromUserRating(float fromUserRating) {
        this.fromUserRating = fromUserRating;
    }
    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getFromUserSkills() {
        return fromUserSkills;
    }

    public void setFromUserSkills(String fromUserSkills) {
        this.fromUserSkills = fromUserSkills;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(String preferredTime) {
        this.preferredTime = preferredTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSkillOffered() {
        return skillOffered;
    }

    public void setSkillOffered(String skillOffered) {
        this.skillOffered = skillOffered;
    }

    public String getSkillWanted() {
        return skillWanted;
    }

    public void setSkillWanted(String skillWanted) {
        this.skillWanted = skillWanted;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    public String getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(String yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getExperienceDescription() {
        return experienceDescription;
    }

    public void setExperienceDescription(String experienceDescription) {
        this.experienceDescription = experienceDescription;
    }

    public String getPortfolioLink() {
        return portfolioLink;
    }

    public void setPortfolioLink(String portfolioLink) {
        this.portfolioLink = portfolioLink;
    }

    public String getWhyLearn() {
        return whyLearn;
    }

    public void setWhyLearn(String whyLearn) {
        this.whyLearn = whyLearn;
    }

    public String getCommunicationMode() {
        return communicationMode;
    }

    public void setCommunicationMode(String communicationMode) {
        this.communicationMode = communicationMode;
    }

    public String getNumberOfSessions() {
        return numberOfSessions;
    }

    public void setNumberOfSessions(String numberOfSessions) {
        this.numberOfSessions = numberOfSessions;
    }

    public String getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(String sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public String getExpectedProficiency() {
        return expectedProficiency;
    }

    public void setExpectedProficiency(String expectedProficiency) {
        this.expectedProficiency = expectedProficiency;
    }

    public String getOtherCommunication() {
        return otherCommunication;
    }

    public void setOtherCommunication(String otherCommunication) {
        this.otherCommunication = otherCommunication;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getToUserSkills() {
        return toUserSkills;
    }

    public void setToUserSkills(String toUserSkills) {
        this.toUserSkills = toUserSkills;
    }
}
