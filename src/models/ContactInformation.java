
public class ContactInformation {
	private String email;
	private String phoneNumber;
	private String address;
	
	public ContactInformation() {
		this.email = "UNK";
		this.phoneNumber = "UNK";
		this.address = "UNK";
	}
	public ContactInformation(String email, String phoneNumber, String address) {
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.address = address;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getContactInfo() {
		return String.format("Email: %s,\nPhone Number: %s,\nAddress: %s,", this.email, this.phoneNumber, this.address);
	}
}
