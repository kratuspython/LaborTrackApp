import java.time.LocalDateTime;

public class Person {
	private String name;
	private int day;
	private int month;
	private int year;
	private int[] dateBirth = new int [3];
	private int age;
	private String profilePic;
	private LocalDateTime currentTime = LocalDateTime.now();
	
	public Person() {
		try {
	        setName("Anonymous"); 
	        this.day = 1;
	        this.month = 1;
	        this.year = 2000;
	        this.profilePic = "default.jpg";
	    } catch (Exception e) {
	        e.printStackTrace(); 
	    }
	}
	public Person(String name, int day, int month, int year, String profilePic) throws Exception {
		this.setName(name);
		this.setDay(day);
		this.setMonth(month);
		this.setYear(year);
		if (currentTime.getYear() - getYear() >= 18) {
			this.age = currentTime.getYear() - getYear();
		}else {
			throw new Exception("You are still a minor. Invalid age!");
		}
		this.dateBirth = new int[]{day, month, year};
		this.setProfilePic(profilePic);
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) throws Exception {
		if (month > 0 && month <= 12) {
			this.month = month;
		}
		else {
			throw new Exception("Month is not recoginezed, please type a number 1-12.");
		}
	}
	public int[] getBirthDate() {
		return dateBirth;
	}
	public String getBirthDateFormated() {
	    if (dateBirth == null) {
	        return day + "/" + month + "/" + year;
	    }
	    return dateBirth[0] + "/" + dateBirth[1] + "/" + dateBirth[2];
	}
	public void setName(String name) throws Exception {
		if (name == "") {
			throw new Exception("Name can't be empty");
		}
		this.name = name;
	}
	public void setDay(int day) throws Exception {
		if (day > 0 && day <= 31) {
			this.day = day;
		}
		else {
			throw new Exception("Day range is invalid. Enter (1-31)");
		}
	}
	public void setYear(int year) throws Exception {
		if (currentTime.getYear()-year >= 18) {
			this.year = year;
		}
		else {
			throw new Exception("You are still a minor. Invalid age!");
		}
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getName() {
		return name;
	}
	public int getAge() {
		return age;
	}
	public int getDay() {
		return day;
	}
	public int getYear() {
		return year;
	}
	
	public String toString() {
		String f = String.format("Name: %s,\nAge: %d,", this.name, this.age);
		return f;
	}

	public String getProfilePic() {
		return this.profilePic;
	}
	public void setProfilePic(String url) {
		this.profilePic = url;
	}
	
}
