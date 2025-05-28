package models;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WagesHistory {
	private Map<String, ArrayList<Timing>> weeklyPunches;
	
	public WagesHistory() {
		weeklyPunches = new HashMap<>();
	}
	
	public void setWagesHistory(ArrayList<Timing> timeWorkHistory) throws Exception {
		
		for (int i=0;i<timeWorkHistory.size();i++) {
			LocalDateTime punchIn = timeWorkHistory.get(i).getPunchIn();
			int year = punchIn.getYear();
			int week = punchIn.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
			String key = year + "-W" + week;	
			weeklyPunches.computeIfAbsent(key, k -> new ArrayList<>()).add(timeWorkHistory.get(i));
		}
	}
	public String displayLastWeekEarnings(double hourlyRates) {
		if (this.weeklyPunches.isEmpty()) {
			return null;
		}
		ArrayList<String> keysList = new ArrayList<>(this.weeklyPunches.keySet());
		String lastWeek = keysList.get(keysList.size()-1);
		
		ArrayList<ArrayList<Timing>> valueList = new ArrayList<>(this.weeklyPunches.values());
		ArrayList<Timing> lastWeekEarnings = valueList.get(valueList.size()-1);
		
		Duration weekWorkDuration = Duration.ZERO;
		
		for (int i=0;i<lastWeekEarnings.size();i++) {
			weekWorkDuration = weekWorkDuration.plus(lastWeekEarnings.get(i).getTimeWork());
		}
		
		double hours = weekWorkDuration.toMinutes()/60.0;
		return String.format("Last Week: %s,\nEarning: $%.2f",lastWeek, hours*hourlyRates);
	}
	public String displayLastWeekHours() {
		if (this.weeklyPunches.isEmpty()) {
			return null;
		}
		ArrayList<ArrayList<Timing>> valuesList = new ArrayList<>(this.weeklyPunches.values());
		ArrayList<Timing> lastWeek = valuesList.get(valuesList.size()-1);
		
		Duration weekWorkDuration = Duration.ZERO;
		
		for (int i=0;i<lastWeek.size();i++) {
			weekWorkDuration = weekWorkDuration.plus(lastWeek.get(i).getTimeWork());
		}
		
		long hours = weekWorkDuration.toHours();
		long minutes = weekWorkDuration.toMinutes() % 60;
		
		return String.format("Last Week Time Work: %d hours and %d minutes", hours, minutes);
	}
	
	public String displayWeeklyWages(double hourlyWages) {
		ArrayList<String> keyList = new ArrayList<>(this.weeklyPunches.keySet());
		ArrayList<ArrayList<Timing>> valueList = new ArrayList<>(this.weeklyPunches.values());
		int size = keyList.size();
		String historyWages = "";
		for (int i=0;i<size;i++) {
			double totalMinutes = 0;
			for (int j=0;j<valueList.get(i).size();j++) {
				totalMinutes += valueList.get(i).get(j).getTimeWork().toMinutes();
			}
			
			double weeklyWage = (totalMinutes/60.0)*hourlyWages;
	            
			historyWages += String.format("Week: %s\n", keyList.get(i));
			historyWages += String.format("Total Hours Worked: %.2f hours\n", totalMinutes/60.0);
			historyWages += String.format("Weekly Wage: $%.2f\n", weeklyWage);
			historyWages += String.format("--------------------------\n");
	        
		}
		return historyWages;
	}
}
