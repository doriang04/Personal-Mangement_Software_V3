package model;

import java.util.ArrayList;
import java.util.Date;

public interface ISkillManager {

    int getId();

    void setId(int id);

    ArrayList<SkillManager.SkillHistoryEntry> getAktiveSkillHistory();

    void setAktiveSkillHistory(ArrayList<SkillManager.SkillHistoryEntry> aktiveSkillHistory);

    ArrayList<SkillManager.SkillHistoryEntry> getInactiveSkillHistory();

    void setInactiveSkillHistory(ArrayList<SkillManager.SkillHistoryEntry> inactiveSkillHistory);

    void addAktiveSkillHistory(Skill skill, Date date);

    void removeAktiveSkillHistory(ArrayList<SkillManager.SkillHistoryEntry> list);

    void addInactiveSkillHistory(Skill skill, Date date);

    void removeInactiveSkillHistory(ArrayList<SkillManager.SkillHistoryEntry> list);

    void updateSelf();
}

