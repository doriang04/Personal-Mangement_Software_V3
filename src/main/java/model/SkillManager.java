package model;

import core.ServiceLocator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;

// TODO test this as well, i did not check what gpt wrote here

/**
 * Manages skill history for one employee.
 * Mirrors table: skill_history (id, employee_id, skill_id, acquired_at)
 */
public class SkillManager {

    private int employeeId; // was final
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

    /**
     * Represents one row in skill_history.
     */
    public static class SkillHistoryEntry {
        private int historyId;        // skill_history.id (0 if not persisted yet)
        private int skillId;          // skill_history.skill_id
        private LocalDate acquireDate; // skill_history.acquired_at

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

        /**
         * Skill expires after 3 years.
         */
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

    /**
     * Replace history with data loaded from DB.
     */
    public void setSkillHistory(ArrayList<SkillHistoryEntry> entries) {
        skillHistory.clear();
        if (entries != null) {
            skillHistory.addAll(entries);
        }
    }

    /**
     * Full history (immutable copy).
     */
    public ArrayList<SkillHistoryEntry> getSkillHistory() {
        return new ArrayList<>(skillHistory);
    }

    /**
     * Active skills = not expired.
     */
    public ArrayList<SkillHistoryEntry> getActiveSkills() {
        ArrayList<SkillHistoryEntry> active = new ArrayList<>();
        for (SkillHistoryEntry entry : skillHistory) {
            if (!entry.isExpired()) {
                active.add(entry);
            }
        }
        return active;
    }

    /**
     * Inactive skills = expired.
     */
    public ArrayList<SkillHistoryEntry> getInactiveSkills() {
        ArrayList<SkillHistoryEntry> inactive = new ArrayList<>();
        for (SkillHistoryEntry entry : skillHistory) {
            if (entry.isExpired()) {
                inactive.add(entry);
            }
        }
        return inactive;
    }

    /**
     * Adds a new skill acquisition.
     */
    public SkillHistoryEntry addSkill(int skillId, LocalDate acquiredAt) {
        SkillHistoryEntry entry = new SkillHistoryEntry(getNextFreeId(), skillId, acquiredAt);
        skillHistory.add(entry);
        return entry;
    }

    /**
     * Convenience overload using Skill object.
     */
    public SkillHistoryEntry addSkill(Skill skill, LocalDate acquiredAt) {
        return addSkill(skill.getId(), acquiredAt);
    }

    /**
     * Removes expired skills permanently (optional cleanup).
     * Normally you would keep them for history, but this is available if needed.
     */
    public void removeExpiredSkills() {
        Iterator<SkillHistoryEntry> iterator = skillHistory.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isExpired()) {
                iterator.remove();
            }
        }
    }

    /**
     * Removes a specific skill history entry from the employee's record.
     * @param entryToRemove The entry to be removed.
     */
    public void removeSkillEntry(SkillHistoryEntry entryToRemove) {
        if (entryToRemove != null) {
            skillHistory.remove(entryToRemove);
        }
    }

    public boolean hasReferences() {
        // TODO write out this method
        return false;
    }
}
