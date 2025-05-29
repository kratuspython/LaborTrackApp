package models;
import db.LaborTrackDBConnector; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CompanyDatabaseREST {
    private PreparedStatement pstmt; 

    // LOGIN to Company from DB(companies)
    public Company authenticate(String email, String password) {
        String command = "SELECT * FROM companies WHERE email=? AND password=?";

        try {
            Connection connect = LaborTrackDBConnector.connect();
            pstmt = connect.prepareStatement(command);

            pstmt.setString(1, email);
            pstmt.setString(2, password);
            
            ResultSet result_set = pstmt.executeQuery();

            if (result_set.next()) { // true if finds a match
                int getID = result_set.getInt(1);
                String getName= result_set.getString(2);
                String getEmail = result_set.getString(3);
                String getPassword = result_set.getString(4);
                Company newCompany = new Company(getName, getEmail, getPassword);
                newCompany.setCompanyID(getID);
                return newCompany;
            }else {
                System.out.println("User wasn't found. Please insert another email or password!");
            }
        } catch (SQLException e) {
            System.out.println("Couldn't Find Matching: " + e.getMessage());
        }
        return null;
    }

    // SIGN-UP a Company to DB(companies)
    public Company registerCompany(Company c) {
    	String command = "INSERT INTO companies (company_name, email, password) VALUES (?, ?, ?)";
        try {
            Connection connect = LaborTrackDBConnector.connect();
            pstmt = connect.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);           
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getEmail());
            pstmt.setString(3, c.getPassword());

            int rowAdded = pstmt.executeUpdate();
            System.out.println("Rows inserted: " + rowAdded);

            if (rowAdded > 0) {
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    c.setCompanyID(id);
                    System.out.println("Company registered with ID: " + id);
                    return c;
                } else {
                    System.out.println("Failed to retrieve generated keys.");
                }
            } else {
                System.out.println("Insert returned 0 rows.");
            }
        } catch (SQLException e) {
            System.out.println("Error Registrating the Company: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Employee> readAllEmployeesFromACompany(Company c) {
        List<Employee> employees = new ArrayList<>();

        String command = "SELECT * FROM employees WHERE company_id=?";
        try {
            Connection connect = LaborTrackDBConnector.connect();
            pstmt = connect.prepareStatement(command);
            pstmt.setInt(1, c.getCompanyID());
            
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                // Extract each employee info
                int empID = resultSet.getInt("employee_id");
                String name = resultSet.getString("name");
                String dob = resultSet.getString("dob");
                String position = resultSet.getString("position");
                String department = resultSet.getString("department");
                double hourly_wage = resultSet.getDouble("hourly_wage");
                String email = resultSet.getString("email");
                String phone_number = resultSet.getString("phone_number");
                String address = resultSet.getString("address");
                String profile_image = resultSet.getString("profile_image");
                
                String[] dobParts = resultSet.getString("dob").split("/");
                int day = Integer.parseInt(dobParts[0]);    
                int month = Integer.parseInt(dobParts[1]);  
                int year = Integer.parseInt(dobParts[2]);  
           
                Person p = new Person(name, day, month, year, profile_image);
                JobDetails j = new JobDetails(position, department, hourly_wage);
                ContactInformation contIn = new ContactInformation(email, phone_number, address);
              
                Employee newEmp = new Employee(p, j, contIn);
                newEmp.setEmployeeID(empID);
                newEmp.setProfilePic(profile_image);

                employees.add(newEmp);
            }   
        } catch (Exception e) {
            System.out.println("Error reading employee: " + e.getMessage());
        }

        return employees;
    }
    
    // Read employee 
    public Employee readEmployeeFromCompany(Company c, Employee e) {
    	String command = "SELECT * FROM employees WHERE employee_id = ? AND company_id = ?";
        
        try {
            Connection connect = LaborTrackDBConnector.connect();
            pstmt = connect.prepareStatement(command);
            pstmt.setInt(1, e.getEmployeeID());
            pstmt.setInt(2, c.getCompanyID());

            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                int empID = resultSet.getInt("employee_id");
                String name = resultSet.getString("name");
                String dob = resultSet.getString("dob");
                String position = resultSet.getString("position");
                String department = resultSet.getString("department");
                double hourly_wage = resultSet.getDouble("hourly_wage");
                String email = resultSet.getString("email");
                String phone_number = resultSet.getString("phone_number");
                String address = resultSet.getString("address");
                String profile_image = resultSet.getString("profile_image");
                int company_id = resultSet.getInt("company_id");

                String[] dobParts = resultSet.getString("dob").split("/");
                int day = Integer.parseInt(dobParts[0]);    
                int month = Integer.parseInt(dobParts[1]);  
                int year = Integer.parseInt(dobParts[2]);  

                Person p = new Person(name, day, month, year, profile_image);
                JobDetails j = new JobDetails(position, department, hourly_wage);
                ContactInformation contIn = new ContactInformation(email, phone_number, address);

                Employee newEmp = new Employee(p, j, contIn);

                newEmp.setEmployeeID(empID);
                newEmp.setProfilePic(profile_image);
                return newEmp;
            }
        } catch (Exception e1) {
            System.out.println("Error reading employee: " + e1.getMessage());
        }
        return null; // Employee not fount
    }
    // Insert Employee to DB(employees)
    public boolean insertEmployee(Company c, Employee e) {
    	try {
            // Check if email already exists in the same company
            String checkSql = "SELECT COUNT(*) FROM employees WHERE email = ? AND company_id = ?";
            Connection connect = LaborTrackDBConnector.connect();
            pstmt = connect.prepareStatement(checkSql);
            pstmt.setString(1, e.getContactInfo().getEmail());
            pstmt.setInt(2, c.getCompanyID());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("❌ Employee with this email already exists.");
                return false;
            }

            // Build DOB from day/month/year
            String dob = e.getDay() + "/" + e.getMonth() + "/" + e.getYear();

            // Prepare INSERT statement without employee_id
            String sql = "INSERT INTO employees (name, dob, position, department,hourly_wage, email, "
            		+ "phone_number, address, profile_image, "
            		+ "company_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = connect.prepareStatement(sql);
            pstmt.setString(1, e.getName());
            pstmt.setString(2, dob);
            pstmt.setString(3, e.getJobDetails().getPosition());
            pstmt.setString(4, e.getJobDetails().getDepartment());
            pstmt.setDouble(5, e.getJobDetails().getHourlyWage());
            pstmt.setString(6, e.getContactInfo().getEmail());
            pstmt.setString(7, e.getContactInfo().getPhoneNumber());
            pstmt.setString(8, e.getContactInfo().getAddress());
            pstmt.setString(9, e.getProfilePic());
            pstmt.setInt(10, c.getCompanyID());

            int rowsInserted = pstmt.executeUpdate();
            System.out.println("✅ Employee inserted: " + (rowsInserted > 0));
            return rowsInserted > 0;

        } catch (SQLException exce) {
            System.out.println("❌ SQL error inserting employee: " + exce.getMessage());
            return false;
        }
    }

    // Delete Employee from DB(employees)
    public Employee deleteEmployee(Employee e) {
        boolean punchHistoryDelete = false;
        boolean employeeDelete = false;
        Employee currentEmployee = e;
        
        String command = "DELETE FROM punch_records WHERE employee_id=?";
        String command2 = "DELETE FROM employees WHERE employee_id=?";
        try {
        	// Delete punching history
            Connection connect = LaborTrackDBConnector.connect();
            pstmt = connect.prepareStatement(command);
            pstmt.setInt(1, e.getEmployeeID());
            punchHistoryDelete = pstmt.executeUpdate() > 0;
        
            // Delete employee from employees
            pstmt = connect.prepareStatement(command2);
            pstmt.setInt(1, e.getEmployeeID());
            employeeDelete = pstmt.executeUpdate() > 0;
            
            if (employeeDelete) {
            	System.out.println("Employee has been delete.");
            	if (punchHistoryDelete) {
            		System.out.println("Employee puching history delete.");
            	}else {
            		System.out.println("Employee has not punching history!");
            	}
                return currentEmployee; // retrieved the delete employee's info
            }else {
            	System.out.println("Employee couldn't be delete");
            }
        } catch (SQLException exce3) {
            System.out.println("Error deleting employee: " + exce3.getMessage());
        }

        return null; // no employee found!
    }
    
    // Insert a punch in record
    public static boolean insertPunchIn(int empId) {
        String sql = 
            "INSERT INTO punch_records(employee_id, punch_in) " +
            "VALUES(?, datetime('now'));";

        try (Connection c = LaborTrackDBConnector.connect();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, empId);
            return p.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("❌ punchIn error: " + e.getMessage());
            return false;
        }
    }

    // Insert a punch out record: Record the end of a shift by updating the most recent NULL punch_out for this employee. Also, fills in duration_minutes.
     public static boolean insertPunchOut(int empId) {
        String sql = 
            "UPDATE punch_records " +
            "   SET punch_out = datetime('now'), " +
            "       duration_minutes = " +
            "         CAST((julianday('now') - julianday(punch_in))*24*60 AS INTEGER) " +
            " WHERE employee_id=? AND punch_out IS NULL;";

        try (Connection c = LaborTrackDBConnector.connect();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, empId);
            return p.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("❌ punchOut error: " + e.getMessage());
            return false;
        }
    }

    // Insert a punching record
    public boolean insertPunchRecord(Employee e, LocalDateTime punchIn, LocalDateTime punchOut) {
        String command = "INSERT INTO punch_records (employee_id, punch_in, punch_out, duration_minutes) VALUES (?, ?, ?, ?)";
        try {
            Connection connect = LaborTrackDBConnector.connect();
            pstmt = connect.prepareStatement(command);
            
            long minutes = java.time.Duration.between(punchIn, punchOut).toMinutes();

            pstmt.setInt(1, e.getEmployeeID());
            pstmt.setString(2, punchIn.toString());
            pstmt.setString(3, punchOut.toString());
            pstmt.setInt(4, (int) minutes);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            System.out.println("❌ Error inserting punch record: " + ex.getMessage());
            return false;
        }
    }
    
    // Read all punching records from an employee
    public List<String> readPunchHistory(Employee e) {
        List<String> records = new ArrayList<>();
        String query = "SELECT * FROM punch_records WHERE employee_id = ?";
        
        try {
            Connection connect = LaborTrackDBConnector.connect();
            pstmt = connect.prepareStatement(query);
            pstmt.setInt(1, e.getEmployeeID());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime in = LocalDateTime.parse(rs.getString("punch_in"));
                    LocalDateTime out = LocalDateTime.parse(rs.getString("punch_out"));
                    Duration duration = Duration.between(in, out);
                    double hours = duration.toMinutes() / 60.0;
                    double salary = e.getJobDetails().getHourlyWage() * hours;

                    String line = String.format(
                        "Punch In: %s,\nPunch Out: %s,\nDuration: %d Minutes,\nSalary: $%.2f,",
                        formatDate(in), formatDate(out), duration.toMinutes(), salary
                    );

                    records.add(line);
                }
            }
        } catch (Exception ex) {
            System.out.println("Error reading punch records: " + ex.getMessage());
        }

        return records;
    }

    // Read open but not close punches:
    public List<String> readOpenPunchTimes(int empId) {
        List<String> punchIns = new ArrayList<>();
        String sql = "SELECT punch_in FROM punch_records WHERE employee_id = ? AND punch_out IS NULL";
        try (Connection conn = LaborTrackDBConnector.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // returns the raw ISO string, e.g. "2025-05-28T18:47:11"
                    punchIns.add(rs.getString("punch_in"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return punchIns;
    }
    

    private String formatDate(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ofPattern("MM/dd/yy 'at' hh:mm a"));
    }

    // Read Single Last Record from employee
    public String readLastRecord(Employee emp) {
		String command = "SELECT * FROM punch_records WHERE employee_id = ? ORDER BY punch_in DESC LIMIT 1";
		
		try {
            Connection connect = LaborTrackDBConnector.connect();
			pstmt = connect.prepareStatement(command);
			pstmt.setInt(1, emp.getEmployeeID());
			ResultSet result_set = pstmt.executeQuery();
			
			if (result_set.next()) {
				LocalDateTime punch_in = LocalDateTime.parse(result_set.getString("punch_in"));
				LocalDateTime punch_out = LocalDateTime.parse(result_set.getString("punch_out"));
				int duration_min = result_set.getInt("duration_minutes");
				double hourly_wage = emp.getJobDetails().getHourlyWage();
				double salary = (hourly_wage*duration_min)/60;
				
				// Format time
				DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
				
				String form_punch_in = String.format("%s at %s", punch_in.toLocalDate().format(dateFormatter), punch_in.toLocalTime().format(timeFormatter));
	            String form_punch_out = String.format("%s at %s", punch_out.toLocalDate().format(dateFormatter), punch_out.toLocalTime().format(timeFormatter));
	            
				String lastWeekWage = String.format("Punch In: %s,\nPunch Out: %s,\nDuration: %d Minutes,\nSalary: $%.2f,", 
						form_punch_in, form_punch_out, duration_min, salary);
				return lastWeekWage;
			}
		}catch (SQLException e) {
			System.out.println("Error reading employee Last week records: " + e.getMessage());
		}
		return "No Punch records found.";
	}
    
    
    public Employee readEmployeeFromID(Employee e) {
    	String query = "SELECT * FROM employees WHERE employee_id = ?";
        try {
            Connection connect = LaborTrackDBConnector.connect();
            pstmt = connect.prepareStatement(query);
            pstmt.setInt(1, e.getEmployeeID());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String[] dob = rs.getString("dob").split("/");
                    int day = Integer.parseInt(dob[0]);
                    int month = Integer.parseInt(dob[1]);
                    int year = Integer.parseInt(dob[2]);
                    String profile = rs.getString("profile_image");

                    String position = rs.getString("position");
                    String department = rs.getString("department");
                    double wage = rs.getDouble("hourly_wage");

                    String email = rs.getString("email");
                    String phone = rs.getString("phone_number");
                    String address = rs.getString("address");

                    Employee emp = new Employee(
                        new Person(name, day, month, year, profile),
                        new JobDetails(position, department, wage),
                        new ContactInformation(email, phone, address)
                    );
                    emp.setEmployeeID(e.getEmployeeID());
                    return emp;
                }
            }
        } catch (Exception ex) {
            System.out.println("Error finding employee by ID: " + ex.getMessage());
        }
        return null;
    }
    
    // Helpers to split "day/month/year" from DOB string
    private int parseDay(String dob) {
        try {
            return Integer.parseInt(dob.split("/")[0]);
        } catch (Exception e) {
            return 1; // fallback
        }
    }

    private int parseMonth(String dob) {
        try {
            return Integer.parseInt(dob.split("/")[1]);
        } catch (Exception e) {
            return 1;
        }
    }

    private int parseYear(String dob) {
        try {
            return Integer.parseInt(dob.split("/")[2]);
        } catch (Exception e) {
            return 2000;
        }
    }
}
