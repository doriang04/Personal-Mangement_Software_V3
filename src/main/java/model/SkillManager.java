package model;

import core.ServiceLocator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;

public class SkillManager {

    private int employeeId;
    private final ArrayList<SkillHistoryEntry> skillHistory = new ArrayList<>();

    public int getId() {
        return employeeId;
    }

    public Skill getSkillById(int id) {
        for (SkillHistoryEntry skill: skillHistory) {
            if (skill.skillId == id) return ServiceLocator.getSkillContainer().getSkillById(id);
        }
        return null;
    }
    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public void addSkillsFromTraining(Training training, LocalDate completion_date) {
        SkillContainer skillContainer = ServiceLocator.getSkillContainer();

        for (TrainingSkillManager.TrainingSkillEntry tsk: training.getSkillManager().getSkills()) {
            Skill skill = skillContainer.getSkillById(tsk.getSkillId());
            addSkill(skill, completion_date);
        }
    }

    public static class SkillHistoryEntry {
        private int historyId;
        private int skillId;
        private LocalDate acquireDate;

        public SkillHistoryEntry(int historyId, int skillId, LocalDate acquireDate) {
            this.historyId = historyId;
            this.skillId = skillId;
            this.acquireDate = acquireDate;
        }

        public SkillHistoryEntry() {

        }

        public int getHistoryId() {
            return historyId;
        }

        public void setHistoryId(int historyId) {
            this.historyId = historyId;
        }

        public int getSkillId() {
            return skillId;
        }

        public void setSkillId(int skillId) {
            this.skillId = skillId;
        }

        public LocalDate getAcquireDate() {
            return acquireDate;
        }

        public void setAcquireDate(LocalDate acquireDate) {
            this.acquireDate = acquireDate;
        }

        public boolean isExpired() {
            return acquireDate.plusYears(3).isBefore(LocalDate.now());
        }

        @Override
        public String toString() {
            return "SkillHistoryEntry{" +
                    "historyId=" + historyId +
                    ", skillId=" + skillId +
                    ", acquiredAt=" + acquireDate +
                    '}';
        }
    }

    private SkillHistoryEntry getEntryById(int id) {
        for (SkillHistoryEntry skillHistoryEntry: skillHistory) {
            if (skillHistoryEntry.getHistoryId() == id) return skillHistoryEntry;
        }
        return null;
    }

    private int getNextFreeId() {
        int i = 0;
        while (true) {
            if (getEntryById(i) == null) return i;
            i++;
        }
    }

    public SkillManager(Employee employee) {
        this.employeeId = employee.getId();
    }

    public SkillManager() {

    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setSkillHistory(ArrayList<SkillHistoryEntry> entries) {
        skillHistory.clear();
        if (entries != null) {
            skillHistory.addAll(entries);
        }
    }

    public ArrayList<SkillHistoryEntry> getSkillHistory() {
        return new ArrayList<>(skillHistory);
    }

    public ArrayList<SkillHistoryEntry> getActiveSkills() {
        ArrayList<SkillHistoryEntry> active = new ArrayList<>();
        for (SkillHistoryEntry entry : skillHistory) {
            if (!entry.isExpired()) {
                active.add(entry);
            }
        }
        return active;
    }

    public ArrayList<SkillHistoryEntry> getInactiveSkills() {
        ArrayList<SkillHistoryEntry> inactive = new ArrayList<>();
        for (SkillHistoryEntry entry : skillHistory) {
            if (entry.isExpired()) {
                inactive.add(entry);
            }
        }
        return inactive;
    }

    public SkillHistoryEntry addSkill(int skillId, LocalDate acquiredAt) {

        SkillHistoryEntry existing = getEntryBySkillId(skillId);

        if (existing != null) {
            existing.setAcquireDate(acquiredAt);
            return existing;
        }

        SkillHistoryEntry entry =
                new SkillHistoryEntry(getNextFreeId(), skillId, acquiredAt);
        skillHistory.add(entry);
        return entry;
    }


    public SkillHistoryEntry addSkill(Skill skill, LocalDate acquiredAt) {
        return addSkill(skill.getId(), acquiredAt);
    }

    public void removeExpiredSkills() {
        Iterator<SkillHistoryEntry> iterator = skillHistory.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isExpired()) {
                iterator.remove();
            }
        }
    }

    public void removeSkillEntry(SkillHistoryEntry entryToRemove) {
        if (entryToRemove != null) {
            skillHistory.remove(entryToRemove);
        }
    }

    private SkillHistoryEntry getEntryBySkillId(int skillId) {
        for (SkillHistoryEntry entry : skillHistory) {
            if (entry.getSkillId() == skillId) {
                return entry;
            }
        }
        return null;
    }

    public boolean hasReferences() {
        for (Employee employee: ServiceLocator.getEmployeeContainer().getEmployees()) {
            if (employee.getId() == employeeId) return true;
        }
        return false;
    }
}
