import java.util.ArrayList;
import java.util.List;

public class Company {
    private String name;
    private String email;
    private String password;
    private int company_id;

    private List<Employee> employees;
    private CompanyDatabaseREST db;

    public Company(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.employees = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }
    public String getEmail() {
        return this.email;
    }
    public String getPassword() {
        return this.password;
    }
    public List<Employee> getEmployees() {
        return this.employees;
    }
    public void setEmployees(List<Employee> employees) {
    	this.employees = employees;
    }
    public void setCompanyID(int id) {
        this.company_id = id;
    }
    public int getCompanyID() {
        return this.company_id;
    }
    public void setPassword(String p) {
    	this.password = p;
    }
}