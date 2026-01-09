package model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class SkillManager implements ISkillManager {

    public static class SkillHistoryEntry {
        private int skillId;
        private Date acquireDate;

        public SkillHistoryEntry(int skillId, Date acquireDate) {
            this.skillId = skillId;
            this.acquireDate = acquireDate;
        }

        public int getSkillId() {
            return skillId;
        }

        public Date getAcquireDate() {
            return acquireDate;
        }

        // Prüft, ob die Skill abgelaufen ist (älter als 3 Jahre)
        public boolean isExpired() {
        LocalDate acquired = acquireDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return acquired.plusYears(3).isBefore(LocalDate.now());

        }
    }
    private int id;
    private ArrayList<SkillHistoryEntry> aktiveSkillHistory;
    private ArrayList<SkillHistoryEntry> inactiveSkillHistory;

    public SkillManager(int id) {
        this.id = id;
        this.aktiveSkillHistory = new ArrayList<>();
        this.inactiveSkillHistory = new ArrayList<>();
    }
    public SkillManager(){

    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

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

    // add/remove/update‑Methoden der SkillHistory
    public void addAktiveSkillHistory(Skill skill, Date date) {
        aktiveSkillHistory.add(new SkillHistoryEntry(skill.getSkillId(), date)
    );}
    public void removeAktiveSkillHistory(ArrayList<SkillHistoryEntry> list) {
        aktiveSkillHistory.removeAll(list);
    }
    public void addInactiveSkillHistory(Skill skill, Date date) {
        inactiveSkillHistory.add(new SkillHistoryEntry(skill.getSkillId(), date));
    }
    public void removeInactiveSkillHistory(ArrayList<SkillHistoryEntry> list) {
        inactiveSkillHistory.removeAll(list);
    }
    
    public void updateSelf() {
        Iterator<SkillHistoryEntry> iterator = aktiveSkillHistory.iterator(); //iterieren durch SkillHistory
        while (iterator.hasNext()) {
            SkillHistoryEntry entry = iterator.next();

            if (entry.isExpired()) {
                iterator.remove();              // aus aktiv entfernen
                inactiveSkillHistory.add(entry); // in inaktiv verschieben
            }
        }
    }
}

