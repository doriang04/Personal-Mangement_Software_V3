package model;
import java.util.ArrayList;
import java.util.List;

// Company Container
public class CompanyContainer {
    private static CompanyContainer instance;
    private List<Company> companies = new ArrayList<>();

    private CompanyContainer() {}

    public static synchronized CompanyContainer getInstance() {
        if (instance == null) instance = new CompanyContainer();
        return instance;
    }

    public void addCompany(Company company) { companies.add(company); }
}
