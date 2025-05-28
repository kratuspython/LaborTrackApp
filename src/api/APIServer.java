package api;

import models.Timing;
import models.Company;
import models.Employee;
import models.Person;
import models.JobDetails;
import models.ContactInformation;
import models.CompanyDatabaseREST;

import static spark.Spark.*;
import spark.Route; 
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
public class APIServer {
	
	
	
    public static void main(String[] args) {
    	
    	
    	// Set the server to listen to localhost:8080
        port(8088);
        
        String staticDir = Paths.get("public").toAbsolutePath().toString();
        System.out.println("Static files being served from: " + staticDir);
        staticFiles.externalLocation(staticDir);

        
        // Convert java objects to JSON and vice versa
        Gson gson = new GsonBuilder()
        	    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        	    .registerTypeAdapter(Duration.class, new DurationAdapter())
        	    .create();
        System.out.println("=== APIServer is starting ===");


        
        // Handles interactions with database (SQLite DB)
        CompanyDatabaseREST db = new CompanyDatabaseREST();
        
        
        // 1️⃣ authenticate a company login
        post("/api/login", (Route) (req, res) -> {
            res.type("application/json");
            System.out.println("✅ /login route hit");
            System.out.println("Body: " + req.body());

            System.out.println("Here");
            // 1) parse + validate
            LoginWrapper wrapper = gson.fromJson(req.body(), LoginWrapper.class);
            if (wrapper == null || wrapper.email == null || wrapper.password == null) {
                res.status(400);
                return gson.toJson(new ResponseError("Missing credentials"));
            }
            
            // 2) authenticate
            Company company = db.authenticate(wrapper.email, wrapper.password);
            if (company == null) {
                res.status(401);
                return gson.toJson(new ResponseError("Invalid email or password"));
            }

            
            // 3) store companyId *before* we return
            req.session(true).attribute("companyId", company.getCompanyID());
            System.out.println("🔐 Stored companyId = " + company.getCompanyID());

            // 4) hide the password (optional) and return success
            company.setPassword(null);
            res.status(200);
            return gson.toJson(company);
        });

        // 2️⃣ logout end sesion:
        post("/api/logout", (Route) (req, res) -> {
            // invalidate the session on the server
            req.session().invalidate();
            res.status(200);
            return "";  // empty body is fine
        });
        
        // 3️⃣ Register a new Company
        post("/api/register", (Route) (req, res) -> {
            res.type("application/json");

            // 1️⃣ Parse the incoming JSON
            RegisterWrapper w = gson.fromJson(req.body(), RegisterWrapper.class);
            if (w.companyName == null || w.email == null || w.password == null) {
                res.status(400);
                return "Missing required fields";
            }

            // 2️⃣ Create the new company in your DB
            Company newCompany = new Company(w.companyName, w.email, w.password);
            Company created = db.registerCompany(newCompany);

            if (created != null && created.getCompanyID() > 0) {
                // 3️⃣ Auto-login: store in session
                req.session(true).attribute("companyId", created.getCompanyID());
                res.status(201);
                return gson.toJson(created);
            } else {
                res.status(500);
                return "Failed to register company";
            }
        });

        // 4️⃣ Get all employees of a company
        get("/api/employees", (Route) (req, res) -> {        
            res.type("application/json");
            Integer companyId = req.session().attribute("companyId");
            System.out.println("🔍 [DEBUG] /employees session companyId = " + companyId);
            if (companyId == null) {
                res.status(401);
                return gson.toJson("Not logged in");
            }

            // 2) Fetch only that company’s employees
            Company current = new Company("","", "");
            current.setCompanyID(companyId); 
            List<Employee> emps = db.readAllEmployeesFromACompany(current);

            // 3) Return them
            return gson.toJson(emps);
        });
        
        // 5️⃣ Add Punch In Time to an employee
        post("/api/punchin", (Route) (req, res) -> {
            res.type("application/json");
            // 1️⃣ read empId from query
            String empIdStr = req.queryParams("empId");
            if (empIdStr == null) {
                res.status(400);
                return gson.toJson("Missing empId");
            }
            int empId = Integer.parseInt(empIdStr);

            // 2️⃣ build minimal Employee wrapper
            Employee emp = new Employee();
            emp.setEmployeeID(empId);

            // 3️⃣ call your DAO
            boolean ok = db.punchIn(emp, LocalDateTime.now());

            // 4️⃣ respond appropriately
            if (ok) {
                res.status(201);
                return gson.toJson("Punch-in recorded");
            } else {
                res.status(500);
                return gson.toJson("Failed to record punch-in");
            }
        });

        // 6️⃣ Add Punch Out time and closing a punching record (punchIn, PunchOut, Duration) for an employee
        post("/api/punchout", (Route) (req, res) -> {
            res.type("application/json");
            String empIdStr = req.queryParams("empId");
            if (empIdStr == null) {
                res.status(400);
                return gson.toJson("Missing empId");
            }
            int empId = Integer.parseInt(empIdStr);

            Employee emp = new Employee();
            emp.setEmployeeID(empId);

            boolean ok = db.punchOut(emp, LocalDateTime.now());

            if (ok) {
                res.status(200);
                return gson.toJson("Punch-out recorded");
            } else {
                res.status(500);
                return gson.toJson("Failed to record punch-out");
            }
        });

        // 7️⃣ Get punch History from an employee
        get("/api/punches", (Route) (req, res) -> {
            res.type("application/json");
            String empIdStr = req.queryParams("empId");
            if (empIdStr == null) {
                res.status(400);
                return gson.toJson("Missing empId");
            }
            int empId = Integer.parseInt(empIdStr);

            Employee emp = new Employee();
            emp.setEmployeeID(empId);

            // returns List<Timing> built from punch_records
            return gson.toJson(db.readPunchHistory(emp));
        });

        // 3. Insert a new employee for a specify company
        post("/employee", (req, res) -> {
            res.type("application/json");

            // 1️⃣ Get & validate companyId query param
            String companyIdStr = req.queryParams("companyId");
            if (companyIdStr == null) {
                res.status(400);
                return gson.toJson("Missing companyId param");
            }
            int companyId = Integer.parseInt(companyIdStr);

            // 2️⃣ Deserialize the exact JSON structure
            EmployeePayload payload = gson.fromJson(req.body(), EmployeePayload.class);

            // 3️⃣ Build your Employee object using a constructor you add to Employee.java
            Employee newEmp = new Employee(
                payload.person,
                payload.jobDetails,
                payload.contactInfo
            );

            // 4️⃣ Perform the insert
            Company company = new Company("placeholder", "none@none.com", "");
            company.setCompanyID(companyId);
            boolean success = db.insertEmployee(company, newEmp);

            // 5️⃣ Return appropriate status + message
            if (success) {
                res.status(201);
                return gson.toJson("Employee inserted successfully");
            } else {
                res.status(500);
                return gson.toJson("Failed to insert employee");
            }
        });
        
        
        // 4. Delete an employee from a company by ID
        delete("/employee/:id", (req, res) -> {
        	// obtain the employeeId
        	String idParam = req.params(":id");
        	
        	// Attempt to convert employeeId from str to int
        	int employeeId;
        	try {
        		employeeId = Integer.parseInt(idParam);
        	} catch (NumberFormatException e) {
        		res.status(400);
        		return gson.toJson("Invalid employee ID format");
        	}
        	
        	// Build an Employee with only the ID set
        	Employee target = new Employee(
        			  new Person("ToDelete", 1, 1, 2000, "default.jpg"),
        			  new JobDetails("", "", 0.0),
        			  new ContactInformation("", "", "")
        			);
            target.setEmployeeID(employeeId); // only thing that matters
        	
        	// Attempt to delete emp from the db
        	Employee deleted = db.deleteEmployee(target);
            if (deleted != null) {
                return gson.toJson("Employee deleted successfully");
            } else {
                res.status(404);
                return gson.toJson("Employee not found or could not be deleted");
            }
        });
        
        // 5. Get punch history for an employe
        get("/punches/:employeeId", (req, res) -> {
            try {
                int employeeId = Integer.parseInt(req.params(":employeeId"));
                Employee dummy = new Employee(); // used for lookup
                dummy.setEmployeeID(employeeId);

                Employee fullEmp = db.readEmployeeFromID(dummy); // fetch all fields
                if (fullEmp == null) return error(res, 404, "Employee not found");

                return gson.toJson(db.readPunchHistory(fullEmp));
            } catch (Exception ex) {
                ex.printStackTrace();
                return error(res, 500, "Server error: " + ex.getMessage());
            }
        });
        
        // 6. insert a punch record
        post("/punch", (req, res) -> {
        	// get data from PunchWrapper class
        	PunchWrapper wrapper = gson.fromJson(req.body(), PunchWrapper.class);
        	
        	// Validate punch data, only if requirements are meet
        	if (wrapper == null  || wrapper.employeeId <= 0 || wrapper.punchIn == null || wrapper.punchOut == null) {
        		res.status(400);
        		return gson.toJson("Missing or invalid punch data");
        	}
        	
        	// Build an Employee with only the ID set
            Employee employee = new Employee();
            employee.setEmployeeID(wrapper.employeeId);
        	
            boolean success = db.insertPunchRecord(employee, wrapper.punchIn, wrapper.punchOut);
            if (success) {
                res.status(201);
                return gson.toJson("Punch record inserted successfully");
            } else {
                res.status(500);
                return gson.toJson("Failed to insert punch record");
            }
        });
        
        
        
        // 8. Get a specify employee from a company by ID
        get("/employee/:id", (req, res) -> {
        	// Obtain data from SearchWrapper class
        	SearchWrapper wrapper = gson.fromJson(req.body(), SearchWrapper.class);
        	
        	// validate only if meet the condition
            if (wrapper == null || wrapper.companyId <= 0 || wrapper.employee == null) {
                res.status(400);
                return gson.toJson("Missing company ID or employee data");
            }
            
            //create a dummy company
            Company company = new Company("placeholder", "none@none.com", "");
            company.setCompanyID(wrapper.companyId);
            
            Employee result = db.readEmployeeFromCompany(company, wrapper.employee);
            if (result != null) {
                return gson.toJson(result);
            } else {
                res.status(404);
                return gson.toJson("Employee not found in company");
            }
        });
        
        
     // 10. Search for a specific employee in a company
        post("/employee/search", (req, res) -> {
            SearchWrapper wrapper = gson.fromJson(req.body(), SearchWrapper.class);
            if (wrapper == null || wrapper.companyId <= 0 || wrapper.employee == null)
                return error(res, 400, "Missing company ID or employee data");

            Company company = new Company("placeholder", "none@none.com", "");
            company.setCompanyID(wrapper.companyId);

            Employee result = db.readEmployeeFromCompany(company, wrapper.employee);
            return gson.toJson(result != null ? result : error(res, 404, "Employee not found in company"));
        });
       
        // Test Route: confirm server is working
        get("/hello", (req, res) -> {
            return gson.toJson("Spark is working!");
        });

        notFound((req, res) -> {
            res.type("application/json");
            return "{\"error\":\"Route not found\"}";
        });
        
        
    }
    
    // 1️⃣ Helper class to wrap login data from JSON: 
    static class LoginWrapper {
        String email; // Company email
        String password; // Company password
    }
    
    // 2️⃣ 
    static class RegisterWrapper {
        public String companyName;
        public String email;
        public String password;
    }
    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws java.io.IOException {
        	 if (value == null) {
                 out.nullValue();  // handle null safely
             } else {
                 out.value(value.format(formatter));
             }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws java.io.IOException {
        	 if (in.peek() == JsonToken.NULL) {
                 in.nextNull();
                 return null;
             }
             return LocalDateTime.parse(in.nextString(), formatter);
        }
    }
    
    static class DurationAdapter extends TypeAdapter<Duration> {
        @Override
        public void write(JsonWriter out, Duration value) throws java.io.IOException {
            if (value == null) {
                out.nullValue();  // safely output null
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public Duration read(JsonReader in) throws java.io.IOException {
        	if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return Duration.parse(in.nextString());
        }
    }
    
    // Helpers for JSON responses with status codes
    private static String success(spark.Response res, int status, String msg) {
        res.status(status);
        return msg;
    }

    private static String error(spark.Response res, int status, String msg) {
        res.status(status);
        return msg;
    }
    
    static class ResponseError {
        String error;
        ResponseError(String message) {
            this.error = message;
        }
    }
    
    // Helper class to parse combined JSON input
    static class EmployeeWrapper {
        Person person;
        JobDetails jobDetails;
        ContactInformation contactInfo;
    }
    
    static class SearchWrapper {
        int companyId;
        Employee employee;
    }
    
    // Helper class to wrap punch data from JSON
    static class PunchWrapper {
        int employeeId;  // ID of the employee
        LocalDateTime punchIn; // Punch-in time
        LocalDateTime punchOut; // Punch-out time
    }
    
    

    public class EmployeePayload {
        public Person person;
        public JobDetails jobDetails;
        public ContactInformation contactInfo;
    }
    
}
