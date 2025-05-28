package models;
import java.time.Duration;
import java.time.LocalDateTime;


public class Timing {
	private LocalDateTime punchIn;
	private LocalDateTime punchOut;
	private Duration timeWorked;
	
	public Timing() {
		this.punchIn = null;
		this.punchOut = null;
	}
	public Timing(LocalDateTime punchin, LocalDateTime punchOut) {
		this.punchIn = punchin;
		this.punchOut = punchOut;
		this.timeWorked = Duration.ZERO;
	}
	public void startPunch() {
		this.punchIn = LocalDateTime.now();
		System.out.println("Punch In: " + punchIn);
	}
	public void endPunch() {
		this.punchOut = LocalDateTime.now();
		System.out.println("Punch Out: " + punchOut);
		this.setTime();
	}
	
	public LocalDateTime getPunchIn() throws Exception {
		if (this.punchIn == null) {
			throw new Exception("Employee has not punchIn yet!");
		}
		return this.punchIn;
	}
	public LocalDateTime getPunchOut() throws Exception {
		if (this.punchIn == null) {
			throw new Exception("Employee has not started its shift yet!");
		}else if (this.punchIn != null && this.punchOut == null) {
			throw new Exception("Employee has not end it shift yet!");
		}
		return this.punchOut;
	}
	public void setTime() {
		this.timeWorked = Duration.between(punchIn, punchOut);
		System.out.println("Time Work: " + this.timeWorked.toMinutes() + " Minutes");
	}
	public String getTimeFormated() {
		long hours = this.timeWorked.toHours();
		long minutes = this.timeWorked.toMinutes() % 60;
		return String.format("Time Worked: %d hours and %d minutes", hours, minutes);
	}
	public Duration getTimeWork() {
		if (punchIn != null && punchOut != null) {
			return Duration.between(punchIn, punchOut);
		}
		return Duration.ZERO;
	}
	
	
}
