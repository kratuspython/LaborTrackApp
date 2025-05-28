package models;
import java.time.Duration;

public class JobDetails {
	private String position;
	private String department;
	private double hourlyWage;
	
	public JobDetails() {
		this.position = "UNK";
		this.department = "UNK";
		this.hourlyWage = 7.25; // Federal Minimum Wage in the United States
	}
	public JobDetails(String position, String department, double hourlyWage) {
		this.position = position;
		this.department = department;
		this.hourlyWage = hourlyWage;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public double getHourlyWage() {
		return hourlyWage;
	}
	public void setHourlyWage(double hourlyWage) {
		this.hourlyWage = hourlyWage;
	}
	public String getJobDetails() {
		return String.format("Position: %s,\nDepartment: %s,\nHourly Wage: %.2f,", this.position, this.department, this.hourlyWage);
	}
	public double calculateWage(Duration duration) {
	    return (duration.toMinutes() / 60.0) * hourlyWage;
	}
}
