package model;

import java.util.ArrayList;

public class DepartmentContainer {

    private static DepartmentContainer instance;
    private ArrayList<Department> departments = new ArrayList<>();

    private DepartmentContainer() {}

    public static synchronized DepartmentContainer getInstance() {
        if (instance == null) instance = new DepartmentContainer();
        return instance;
    }

    public void addDepartment(Department department) {
        departments.add(department);
    }

    public void removeDepartment(Department department) throws Exception {
        if (department.hasReferences()) throw new Exception("Die Abteilung darf nicht gelöscht werden, da sie referenziert wird.");
        departments.remove(department);
    }

    public ArrayList<Department> getDepartments() {
        return departments;
    }

    public Department getDepartmentById(int id) {
        for (Department d : departments) {
            if (d.getId() == id) return d;
        }
        return null;
    }

    public int getNextFreeId() {
        int i = 0;
        while (true) {
            if (getDepartmentById(i) == null) return i;
            i++;
        }
    }
}
