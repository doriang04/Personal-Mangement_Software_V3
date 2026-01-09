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

    public void removeEmployee(Employee employee) {
        this.employees.remove(employee);
    }

    public ArrayList<Employee> getEmployees() {
        return new ArrayList<>(employees);
    }
}
