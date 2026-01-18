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

    public void removeCompany(Company company) throws Exception {
        if (company.hasReferences()) throw new Exception("Unternehmen darf nicht gelöscht werden, da es referenziert wird.");
        companies.remove(company);
    }

    public ArrayList<Company> getCompanies() {
        return companies;
    }

    public Company getCompanyById(int CompanyId) {
        for (Company Company: companies) {
            if (Company.getId() == CompanyId) return Company;
        }
        return null;
    }
}
