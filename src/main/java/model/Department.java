package model;

public class Department {

    private int departmentId;
    private String departmentName;
    private int companyId;   // wie im Diagramm benannt

    public Department(int departmentId, String departmentName, int companyId) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.companyId = companyId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }
}

