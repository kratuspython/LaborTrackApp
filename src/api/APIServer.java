import static spark.Spark.*;

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
        
        
        // 7. authenticate a company login
        post("/login", (req, res) -> {
            res.type("application/json");
            System.out.println("✅ /login route hit");
            System.out.println("Headers: " + req.headers());
            System.out.println("Body: " + req.body());

            LoginWrapper wrapper;
            try {
                wrapper = gson.fromJson(req.body(), LoginWrapper.class);
            } catch (Exception ex) {
                ex.printStackTrace();
                res.status(400);
                return gson.toJson(new ResponseError("Invalid JSON format"));
            }

            if (wrapper == null || wrapper.email == null || wrapper.password == null) {
                res.status(400);
                return gson.toJson(new ResponseError("Missing credentials"));
            }

            Company company = db.authenticate(wrapper.email, wrapper.password);
            if (company != null) {
                company.setEmployees(db.readAllEmployeesFromACompany(company));
                company.setPassword(null);
                res.status(200);
                return gson.toJson(company);
            } else {
                res.status(401);
                return gson.toJson(new ResponseError("Invalid email or password"));
            }
        });
        
        // 1. Test Route: confirm server is working
        get("/hello", (req, res) -> {
            return gson.toJson("Spark is working!");
        });
        
        // 2. Get all employees of a company
        get("/employees", (req, res) -> {
        	System.out.println("It gets to /employees route");
        	// Extract the companyId from the query parameters
        	String companyIdStr = req.queryParams("companyId");
        	
        	// If companyId is not provided, return 400 (bad request)
        	if (companyIdStr == null) {
        		res.status(400);
        		return gson.toJson("Missing companyID param");
        	}
        	
        	// Attempt to convert companyId from str to int
        	int companyId;
        	try {
        		companyId = Integer.parseInt(companyIdStr);
        	}
        	catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson("Invalid companyId format");
            }
        	
        	// Create a placeHolder company object 
        	Company fakeCompany = new Company("placeholder", "none@none.com", "");
        	fakeCompany.setCompanyID(companyId);
        	
        	// Retrieve all employees for the given company from the DB
        	List<Employee> employees = db.readAllEmployeesFromACompany(fakeCompany);
        	
        	// Convert the employee list to JSON and return it as a response
        	return gson.toJson(employees);
        });
        
        // 3. Insert a new employee for a specify company
        post("/employee", (req, res) -> {
        	// Deserialize JSON request body to an Employee object
        	Employee newEmp = gson.fromJson(req.body(), Employee.class);
        	
        	// Extract companyId from query parameter
        	String companyIdStr = req.queryParams("companyId");
        	
        	// If companyId is not provided, return 400 (bad request)
        	if (companyIdStr == null) {
        		res.status(400);
        		return gson.toJson("Missing companyID param");
        	}
        	
        	// Attempt to convert companyId from str to int
        	int companyId;
        	try {
        		companyId = Integer.parseInt(companyIdStr);
        	}
        	catch (NumberFormatException e) {
                res.status(400);
                return gson.toJson("Invalid companyId format");
            }
        	
        	// Create a placeholder Company object
            Company company = new Company("placeholder", "none@none.com", "");
            company.setCompanyID(companyId);
            
            // Insert the employee using the backend method
            boolean success = db.insertEmployee(company, newEmp);
            
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

                return gson.toJson(db.readAllPunchingRecordsFromAEmployee(fullEmp));
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
        
        // 9. Register a new Company
        post("/register", (req, res) -> {
        	Company newCompany = gson.fromJson(req.body(), Company.class);
        	
        	if (newCompany == null || newCompany.getName() == null || newCompany.getEmail() == null || newCompany.getPassword() == null) {
        		return error(res, 400, "Missing company registration info");
        	}
        	
        	Company registerAttempt = db.registerCompany(newCompany);
        	boolean registered;
        	if (registerAttempt != null) {
        		registered = true;
        	}
        	else {
        		registered = false;
        	}
      
        	if (registered) {
        		res.status(201);
        		return gson.toJson("Company registered successfully");
        	}
        	else {
        	    res.status(500);
        	    return gson.toJson("Failed to register company");
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
       
        
        notFound((req, res) -> {
            res.type("application/json");
            return "{\"error\":\"Route not found\"}";
        });
        
        
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
    
    // Helper class to wrap login data from JSON
    static class LoginWrapper {
        String email; // Company email
        String password; // Company password
    }
    
}
