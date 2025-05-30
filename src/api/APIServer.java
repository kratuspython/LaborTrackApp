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
import spark.Spark; // <-- Add this import for staticFiles

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.nio.file.Paths;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import api.APIServer.ResponseError;
public class APIServer {
	
	
	
    public static void main(String[] args) {
    	
    	
    	// Set the server to listen to localhost:8080
        port(8088);
        
        String staticDir = Paths
            .get(System.getProperty("user.dir"), "public")
            .toAbsolutePath()
            .toString();
        System.out.println("⛳ Serving static files from: " + staticDir);
        staticFiles.externalLocation(staticDir);
        System.out.println("Static files served from: " + staticDir);
        
        
        // Convert java objects to JSON and vice versa
        Gson gson = new GsonBuilder()
        	    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        	    .registerTypeAdapter(Duration.class, new DurationAdapter())
        	    .create();
        System.out.println("=== APIServer is starting ===");


        
        // Handles interactions with database (SQLite DB)
        CompanyDatabaseREST db = new CompanyDatabaseREST();
        
        
        before((req, res) -> {
        req.raw().setAttribute(
            "org.eclipse.jetty.multipartConfig",
            new MultipartConfigElement("/tmp")
        );
        });
        
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
            int empId = Integer.parseInt(req.queryParams("empId"));
            boolean ok = CompanyDatabaseREST.insertPunchIn(empId);
            if (!ok) {
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to record punch-in"));
            }
            return gson.toJson(Map.of("success", true));
        });

        // 6️⃣ Add Punch Out time and closing a punching record (punchIn, PunchOut, Duration) for an employee
        post("/api/punchout", (Route) (req, res) -> {
            res.type("application/json");
            int empId = Integer.parseInt(req.queryParams("empId"));
            boolean ok = CompanyDatabaseREST.insertPunchOut(empId);
            if (!ok) {
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to record punch-out"));
            }
            return gson.toJson(Map.of("success", true));
        });

        // 7️⃣ Get punch History from an employee
        get("/api/punches", (Route) (req, res) -> {
            res.type("application/json");
            String empIdStr = req.queryParams("empId");
            if (empIdStr == null) {
                res.status(400);
                return gson.toJson(new ResponseError("Missing empId"));
            }
            int empId = Integer.parseInt(empIdStr);

            Employee emp = new Employee();
            emp.setEmployeeID(empId);

            List<String> openIns = db.readOpenPunchTimes(emp.getEmployeeID());
            return gson.toJson(openIns);
        });

        // 3. Insert a new employee for a specify company
        post("/api/employees", (Route) (req, res) -> {
            res.type("application/json");
            try {
                // 1️⃣ get companyId from session (set during /api/login)
                Integer companyId = req.session().attribute("companyId");
                if (companyId == null) {
                    res.status(401);
                    return gson.toJson(new ResponseError("Not logged in"));
                }

                // prints the form data into JSON: TO TEST
                String raw = req.body();
                System.out.println("🛑 RAW JSON BODY: " + raw);

                // 2️⃣ parse JSON
                JsonObject obj = gson.fromJson(req.body(), JsonObject.class);
                Employee e  = gson.fromJson(req.body(), Employee.class);

                // 3️⃣ Manually set the nested ContactInformation fields
                String email = obj.get("email").getAsString();
                String phoneNumber = obj.get("phoneNumber").getAsString();
                String address = obj.get("address").getAsString();
                e.getContactInfo().setEmail(email);
                e.getContactInfo().setPhoneNumber(phoneNumber);
                e.getContactInfo().setAddress(address);

                // 4️⃣ Similarly for JobDetails
                String position = obj.get("position").getAsString();
                String department = obj.get("department").getAsString();
                double wage = obj.get("hourlyWage").getAsDouble();
                e.getJobDetails().setPosition(position);
                e.getJobDetails().setDepartment(department);
                e.getJobDetails().setHourlyWage(wage);

                // 5️⃣ Similarly do for Person
                String name = obj.get("name").getAsString();

                // 3️⃣ insert into the correct company
                Company company = new Company("", "", "");
                company.setCompanyID(companyId);
                int newId = db.insertEmployeeAndGetId(company, e);

                // 4️⃣ respond
                if (newId < 1) {
                    res.status(400);
                    return "{\"error\":\"Employee with this email already exists.\"}";
                }

                // 3️⃣ return the new ID for the client to use
                return String.format(
                    "{\"success\":true,\"employeeId\":%d}",
                    newId
                );

            } catch (Exception ex) {
                ex.printStackTrace();  // logs the stack for debugging
                res.status(500);
                return gson.toJson(new ResponseError(ex.getMessage()));
            }
        });
        
        post("/api/employees/:id/image", (req, res) -> {
            int empId = Integer.parseInt(req.params(":id"));

            Part picPart = req.raw().getPart("profilePic");
            if (picPart == null || picPart.getSize() == 0) {
                res.status(400);
                return gson.toJson(new ResponseError("No file uploaded"));
            }

            // derive extension
            String submitted = Paths.get(picPart.getSubmittedFileName())
                                    .getFileName().toString();
            String ext = "";
            int dot = submitted.lastIndexOf('.');
            if (dot > 0) ext = submitted.substring(dot);

            //Debug: TO Test
            System.out.println(empId);

            // unique name
            String fileName = "profile_pic_" + empId + ext;

            //Debug: TO Test
            System.out.println(fileName);

            // write to your static Images folder
            Path out = Paths.get(staticDir, "Images", fileName);
            try {
                Files.createDirectories(out.getParent());
            } catch (java.io.IOException e) {
                res.status(500);
                return gson.toJson(new ResponseError("Failed to create directories: " + e.getMessage()));
            }
            try (var in = picPart.getInputStream()) {
                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
            }

            /// Error right here
            // update DB
            boolean ok = CompanyDatabaseREST.updateEmployeeProfilePic(empId, fileName);

            // Debug: TO TEST
            System.out.println("🛑 updateEmployeeProfilePic returned: " + ok);

            if (!ok) {
                res.status(500);
                return gson.toJson(new ResponseError("Failed to update DB"));
            }

            res.type("application/json");
            return "{\"success\":true}";
        });
        
        // 4. Delete an employee from a company by ID
        delete("/api/employees/", (req, res) -> {
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
