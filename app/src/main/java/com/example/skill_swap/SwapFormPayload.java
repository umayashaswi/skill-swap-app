package com.example.skill_swap;

public class SwapFormPayload {
    public String fullName, bio, offeredSkill, offeredLevel;
    public int yearsExp;
    public String expDesc, portfolioUrl, sampleFileUrl;
    public String seekSkill, seekLevel, seekWhy;
    public String sessionDuration;
    public int sessionCount;
    public String proposedDate, timeSlot, commMode, commModeOther, personalMsg;

    public SwapFormPayload(String fullName, String bio, String offeredSkill,
                           String offeredLevel, int yearsExp, String expDesc,
                           String portfolioUrl, String sampleFileUrl, String seekSkill,
                           String seekLevel, String seekWhy, String sessionDuration,
                           int sessionCount, String proposedDate, String timeSlot,
                           String commMode, String commModeOther, String personalMsg) {
        this.fullName = fullName;
        this.bio = bio;
        this.offeredSkill = offeredSkill;
        this.offeredLevel = offeredLevel;
        this.yearsExp = yearsExp;
        this.expDesc = expDesc;
        this.portfolioUrl = portfolioUrl;
        this.sampleFileUrl = sampleFileUrl;
        this.seekSkill = seekSkill;
        this.seekLevel = seekLevel;
        this.seekWhy = seekWhy;
        this.sessionDuration = sessionDuration;
        this.sessionCount = sessionCount;
        this.proposedDate = proposedDate;
        this.timeSlot = timeSlot;
        this.commMode = commMode;
        this.commModeOther = commModeOther;
        this.personalMsg = personalMsg;
    }
}
