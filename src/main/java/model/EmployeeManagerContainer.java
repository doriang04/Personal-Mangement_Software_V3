package model;

import java.util.ArrayList;
import java.util.List;

public class EmployeeManagerContainer {
    private static EmployeeManagerContainer instance;
    private List<Employee> employees = new ArrayList<>();

    private EmployeeManagerContainer() {}

    public static synchronized EmployeeManagerContainer getInstance() {
        if (instance == null) {
            instance = new EmployeeManagerContainer();
        }
        return instance;
    }

    public void addEmployee(Employee employee) {
        this.employees.add(employee);
    }

    public List<Employee> getEmployees() {
        return new ArrayList<>(employees);
    }
}
