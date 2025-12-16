package model;

import java.util.ArrayList;
import java.util.Date;

public class SkillManager {

    public static class SkillHistoryEntry {
        private String skillId;
        private Date acquireDate;

        public SkillHistoryEntry(String skillId, Date acquireDate) {
            this.skillId = skillId;
            this.acquireDate = acquireDate;
        }

        public String getSkillId() {
            return skillId;
        }

        public Date getAcquireDate() {
            return acquireDate;
        }
    }

    private ArrayList<SkillHistoryEntry> aktiveSkillHistory;
    private ArrayList<SkillHistoryEntry> inactiveSkillHistory;

    public ArrayList<SkillHistoryEntry> getAktiveSkillHistory() {
        return aktiveSkillHistory;
    }

    public void setAktiveSkillHistory(ArrayList<SkillHistoryEntry> aktiveSkillHistory) {
        this.aktiveSkillHistory = aktiveSkillHistory;
    }

    public ArrayList<SkillHistoryEntry> getInactiveSkillHistory() {
        return inactiveSkillHistory;
    }

    public void setInactiveSkillHistory(ArrayList<SkillHistoryEntry> inactiveSkillHistory) {
        this.inactiveSkillHistory = inactiveSkillHistory;
    }

    // Platzhalter für add/remove/update‑Methoden
    public void addAktiveSkillHistory(Skill skill, Date date) {}
    public void removeAktiveSkillHistory(ArrayList<SkillHistoryEntry> list) {}
    public void addInactiveSkillHistory(Skill skill, Date date) {}
    public void removeInactiveSkillHistory(ArrayList<SkillHistoryEntry> list) {}
    public void updateSelf() {}
}

