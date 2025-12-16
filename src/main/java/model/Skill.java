package model;

import java.sql.Time;
import java.util.ArrayList;

public class Skill {

    private int skillId;
    private Time requiredYears;
    private ArrayList<String> certification;
    private String description;

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public Time getRequiredYears() {
        return requiredYears;
    }

    public String getRequiredYearsAsString() {
        if (requiredYears == null) {
            return "0";
        }
        // Konvertiert Time zu Jahren (angenommen HH:MM = Stunden → Jahre)
        long millis = requiredYears.getTime();
        long hours = millis / (1000 * 60 * 60);
        return String.valueOf(hours);
    }

    public void setRequiredYears(Time requiredYears) {
        this.requiredYears = requiredYears;
    }

    public ArrayList<String> getCertification() {
        return certification;
    }

    public void setCertification(ArrayList<String> certification) {
        this.certification = certification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

