package model;

import java.util.ArrayList;

public class CompanyContainer {

    private static CompanyContainer instance;
    private ArrayList<Company> companies = new ArrayList<>();

    private CompanyContainer() {}

    public static synchronized CompanyContainer getInstance() {
        if (instance == null) instance = new CompanyContainer();
        return instance;
    }

    public void addCompany(Company company) { companies.add(company); }

    public void removeCompany(Company company) {
        companies.remove(company);
    }

    public ArrayList<Company> getCompanies() {
        return companies;
    }
}
