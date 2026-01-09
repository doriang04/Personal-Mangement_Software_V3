package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;

// TODO test this as well, i did not check what gpt wrote here

/**
 * Manages skill history for one employee.
 * Mirrors table: skill_history (id, employee_id, skill_id, acquired_at)
 */
public class SkillManager {

    private final int employeeId;
    private final ArrayList<SkillHistoryEntry> skillHistory = new ArrayList<>();

    /**
     * Represents one row in skill_history.
     */
    public static class SkillHistoryEntry {
        private int historyId;        // skill_history.id (0 if not persisted yet)
        private int skillId;          // skill_history.skill_id
        private LocalDate acquiredAt; // skill_history.acquired_at

        public SkillHistoryEntry(int historyId, int skillId, LocalDate acquiredAt) {
            this.historyId = historyId;
            this.skillId = skillId;
            this.acquiredAt = acquiredAt;
        }

        public SkillHistoryEntry(int skillId, LocalDate acquiredAt) {
            this(0, skillId, acquiredAt);
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

        public LocalDate getAcquiredAt() {
            return acquiredAt;
        }

        public void setAcquiredAt(LocalDate acquiredAt) {
            this.acquiredAt = acquiredAt;
        }

        /**
         * Skill expires after 3 years.
         */
        public boolean isExpired() {
            return acquiredAt.plusYears(3).isBefore(LocalDate.now());
        }

        @Override
        public String toString() {
            return "SkillHistoryEntry{" +
                    "historyId=" + historyId +
                    ", skillId=" + skillId +
                    ", acquiredAt=" + acquiredAt +
                    '}';
        }
    }

    public SkillManager(Employee employee) {
        this.employeeId = employee.getId();
    }

    public SkillManager(int employeeId) {
        this.employeeId = employeeId;
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
        SkillHistoryEntry entry = new SkillHistoryEntry(skillId, acquiredAt);
        skillHistory.add(entry);
        return entry;
    }

    /**
     * Convenience overload using Skill object.
     */
    public SkillHistoryEntry addSkill(Skill skill, LocalDate acquiredAt) {
        return addSkill(skill.getSkillId(), acquiredAt);
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
}
