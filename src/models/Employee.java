package models;
import java.util.ArrayList;

public class Employee extends Person {
	
	private static int ID = 0;
	private int employeeID;
	
	private Timing currentPunch;
	
	private final ArrayList<Timing> timeWorkHistory;
	private final JobDetails jobDetails;
	private final ContactInformation contactInfo;
	private final WagesHistory wagesHistory;
	
	public Employee() {
		super();
		this.jobDetails = new JobDetails(); 
	    this.contactInfo = new ContactInformation(); 
	    this.wagesHistory = new WagesHistory(); 
	    this.timeWorkHistory = new ArrayList<>();
	}
	public Employee(Person p, JobDetails j, ContactInformation c) throws Exception {
		super(p.getName(), p.getBirthDate()[0], p.getBirthDate()[1], p.getBirthDate()[2], p.getProfilePic());
		this.jobDetails = new JobDetails();
		this.contactInfo = new ContactInformation();
		this.wagesHistory = new WagesHistory();
		this.setJobDetails(j.getPosition(), j.getDepartment(), j.getHourlyWage());
		this.setContactInfo(c.getEmail(), c.getPhoneNumber(), c.getAddress());
		this.employeeID = ++ID;
		this.currentPunch = new Timing();
		this.timeWorkHistory = new ArrayList<>();
	}

	
	public JobDetails getJobDetails() {
		return jobDetails;
	}
	public String getJobDetailsFormated() {
		return jobDetails.getJobDetails();
	}

	public void setJobDetails(String position, String department, double hourlyWage) {
		this.jobDetails.setPosition(position);
		this.jobDetails.setDepartment(department);
		this.jobDetails.setHourlyWage(hourlyWage);
	}


	public ContactInformation getContactInfo() {
		return contactInfo;
	}
	public String getContactInfoFormated() {
		return contactInfo.getContactInfo();
	}

	public void setContactInfo(String email, String phoneNumber, String address) {
		this.contactInfo.setEmail(email);
		this.contactInfo.setPhoneNumber(phoneNumber);
		this.contactInfo.setAddress(address);
	}


	public void punchIn() throws Exception {
		this.currentPunch = new Timing();
		this.currentPunch.startPunch();
	}
	public void punchOut() throws Exception {
		this.currentPunch.endPunch();
		this.timeWorkHistory.add(currentPunch);
		this.wagesHistory.setWagesHistory(timeWorkHistory);
	}
	public int getEmployeeID() {
		return employeeID;
	}
	
	public String getTimeWorkFormated() {
		return this.currentPunch.getTimeFormated();
	}
	public Timing getRawTimeWork() {
		return this.currentPunch;
	}
	
	public String lastWeekEarning() {
		return this.wagesHistory.displayLastWeekEarnings(jobDetails.getHourlyWage());
	}
	public String lastWeekHours() {
		return this.wagesHistory.displayLastWeekHours();
	}
	public String displayWagesHistory() {
		return this.wagesHistory.displayWeeklyWages(jobDetails.getHourlyWage());
	}
	public void setEmployeeID(int id) {
	   this.employeeID = id;
	}
	public Person getPerson() {
	    return this.getPerson();
	}
	public String toString() {
	    return String.format(
	            "Employee ID: %d\n%s\n%s\n%s",
	            this.getEmployeeID(),
	            super.toString(),  // prints name and age
	            jobDetails.getJobDetails(),
	            contactInfo.getContactInfo()
	    );
    }
}
