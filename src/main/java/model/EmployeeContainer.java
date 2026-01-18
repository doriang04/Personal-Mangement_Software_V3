package model;

import java.util.ArrayList;
import java.util.List;

public class EmployeeContainer {
    private static EmployeeContainer instance;
    private ArrayList<Employee> employees = new ArrayList<>();

    private EmployeeContainer() {}

    public static synchronized EmployeeContainer getInstance() {
        if (instance == null) {
            instance = new EmployeeContainer();
        }
        return instance;
    }

    public void addEmployee(Employee employee) {
        this.employees.add(employee);
    }

    public void removeEmployee(Employee employee) throws Exception {
        if (employee.hasReferences()) throw new Exception("Mitarbeiter darf nicht gelöscht werden, da er referenziert wird.");
        this.employees.remove(employee);
    }

    public ArrayList<Employee> getEmployees() {
        return new ArrayList<>(employees);
    }


    public Employee getEmployeeById(int EmployeeId) {
        for (Employee employee : employees) {
            if (employee.getId() == EmployeeId) {
                return employee;
            }
        }
        return null;
    }
}
