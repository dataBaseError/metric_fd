package metric_fd;

import java.util.ArrayList;
import java.util.HashMap;

public class Tester {
	
	public static void main(String[] args) {
		
		String username = "postgres";
		String password = "password";
		if(args != null && args.length >= 3) {
			if(args[0].equalsIgnoreCase("movies")) {
				testMovies(username, password, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			}
			else {
				testFlight(username, password, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			}
		} 
		else {
			System.out.println("Not enough args");
		}
		
	}
	
	private static void testFlight(String username, String password, Integer delta, int limit) {
		String db_name = "clean_flight";
		DBInterface db = new DBInterface(db_name, username, password);
		
		ArrayList<String> x_attributes = new ArrayList<String>();
		x_attributes.add("flight_number");
		String table_name = "clean_flight";
		// Vary by 1000 milliseconds
		//Integer delta = 1000;
		
		/*
		flight_number -> ScheduledDeparture
		flight_number -> ActualDeparture
		flight_number -> ScheduledArrival
		flight_number -> ActualArrival 
		 */
		ArrayList<MetricFD<String> > mfds = new ArrayList<MetricFD<String> >();
		mfds.add(new MetricFD<String>(x_attributes, "schedualed_departure", delta));
		mfds.add(new MetricFD<String>(x_attributes, "actual_departure", delta));
		mfds.add(new MetricFD<String>(x_attributes, "schedualed_arrival", delta));
		mfds.add(new MetricFD<String>(x_attributes, "actual_arrival", delta));
		
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("id");
		attributes.addAll(x_attributes);
		attributes.add(mfds.get(0).getY_attribute());
		
		ArrayList<String > group_by = new ArrayList<String>();
		group_by.addAll(x_attributes);
		group_by.add(mfds.get(0).getY_attribute());
		
		db.connect();
		
		if(db.isConnected()) {
			
			for(int i = 0; i < mfds.size(); i++) {
				group_by.set(group_by.size()-1, mfds.get(i).getY_attribute());
				attributes.set(attributes.size()-1, mfds.get(i).getY_attribute());
				ArrayList<HashMap<String, Comparable > > rows = db.get_sorted(table_name, attributes, group_by, limit);
				System.out.println("Number of Tuples: " + rows.size());
				
				HashMap<String, HashMap<String, Comparable> > min_max = db.get_min_max(table_name, group_by);
				/*
				for(int j = 0; j < attributes.size(); j++) {
					System.out.print(attributes.get(j) + ", ");
				}
				System.out.println();
				
				for(int i = 0; i < rows.size(); i++) {
					for(int j = 0; j < attributes.size(); j++) {
						System.out.print(rows.get(i).get(attributes.get(j))+ ", ");
					}
					System.out.println();
				}*/
				
				// TODO move this definition into an actual class (since it is quite general).
				Repair<String, Comparable> re = new BasicRepair<String, Comparable>(mfds.get(i), min_max);
				
				ArrayList<ArrayList<HashMap<String, Comparable > > > corePatterns = re.createCorePatterns(rows);
				
				System.out.println("MFD " + i);
				System.out.println("Clean rate = " + (re.getCleanRate() / (double) rows.size()) * 100);
				System.out.println("Error rate = " + (1.0 - (re.getCleanRate() / (double) rows.size())) * 100);
				
				/*
				for(int k = 0; k < corePatterns.size(); k++) {
					System.out.print("Group " + k);
					System.out.println(" has " + corePatterns.get(k).size() + " elements in this pattern e.g.");
					for(int j = 0; j < attributes.size(); j++) {
						System.out.print(corePatterns.get(k).get(0).get(attributes.get(j)));
						if(j < attributes.size() -1) {
							System.out.print(", ");
						}
					}
					System.out.println();
				}*/
				
				ArrayList<HashMap<String, Comparable > > badResults = new ArrayList<HashMap<String, Comparable > >();
				ArrayList<HashMap<String, Comparable > > result = re.costAnalysis(rows, corePatterns, badResults);
			
				re.resetCleanRatio();
				corePatterns = re.createCorePatterns(result);
				
				System.out.println("Clean rate = " + (re.getCleanRate() / (double) rows.size()) * 100);
				System.out.println("Error rate = " + (1.0 - (re.getCleanRate() / (double) rows.size())) * 100);
				
				System.out.println("Repair Count = " + re.getRepairCount());
				System.out.println("Unrepairable Count = " + badResults.size());
				System.out.println("Finished " + i);
				
				
				if(re.getRepairCount() > 0) {
					db.updateRows(table_name, result, attributes.get(0), group_by);
				}
			}
		}
	}

	private static void testMovies(String username, String password, Integer delta, int limit) {
		String db_name = "movies";
		DBInterface db = new DBInterface(db_name, username, password);
		
		ArrayList<String> x_attributes = new ArrayList<String>();
		x_attributes.add("name");
		String y_attribute = "duration";
		String table_name = "movie";
		//Integer delta = 5;
		
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("id");
		attributes.add("website");
		attributes.add("name");
		attributes.add(y_attribute);
		
		ArrayList<String > group_by = new ArrayList<String>();
		group_by.addAll(x_attributes);
		group_by.add(y_attribute);
		
		db.connect();
		
		if(db.isConnected()) {
			
			ArrayList<HashMap<String, Comparable > > rows = db.get_sorted(table_name, attributes, group_by, limit);
			
			System.out.println("Number of Tuples: " + rows.size());
			
			HashMap<String, HashMap<String, Comparable> > min_max = db.get_min_max(table_name, group_by);
			
			for(int j = 0; j < group_by.size(); j++) {
				System.out.print("Max = " + min_max.get(group_by.get(j)).get("max")+ ", ");
				System.out.print("Min = " + min_max.get(group_by.get(j)).get("min")+ ", ");
				System.out.println();
			}
			
			/*
			for(int j = 0; j < attributes.size(); j++) {
				System.out.print(attributes.get(j) + ", ");
			}
			System.out.println();
			
			for(int i = 0; i < rows.size(); i++) {
				for(int j = 0; j < attributes.size(); j++) {
					System.out.print(rows.get(i).get(attributes.get(j))+ ", ");
				}
				System.out.println();
			}*/
			MetricFD<String> mfd = new MetricFD<String>(x_attributes, y_attribute, delta);
			
			Repair<String, Comparable> re = new BasicRepair<String, Comparable>(mfd, min_max);
			
			ArrayList<ArrayList<HashMap<String, Comparable > > > corePatterns = re.createCorePatterns(rows);
			
			System.out.println("Clean rate = " + (re.getCleanRate() / (double) rows.size()) * 100);
			System.out.println("Error rate = " + (1.0 - (re.getCleanRate() / (double) rows.size())) * 100);
			
			/*
			for(int k = 0; k < corePatterns.size(); k++) {
				System.out.print("Group " + k);
				System.out.println(" has " + corePatterns.get(k).size() + " elements in this pattern e.g.");
				for(int j = 0; j < attributes.size(); j++) {
					System.out.print(corePatterns.get(k).get(0).get(attributes.get(j)));
					if(j < attributes.size() -1) {
						System.out.print(", ");
					}
				}
				System.out.println();
			}*/

			ArrayList<HashMap<String, Comparable > > badResults = new ArrayList<HashMap<String, Comparable > >();
			ArrayList<HashMap<String, Comparable > > result = re.costAnalysis(rows, corePatterns, badResults);
			
			re.resetCleanRatio();
			corePatterns = re.createCorePatterns(result);
			
			System.out.println("Clean rate = " + (re.getCleanRate() / (double) rows.size()) * 100);
			System.out.println("Error rate = " + (1.0 - (re.getCleanRate() / (double) rows.size())) * 100);
			
			System.out.println("Repair Count = " + re.getRepairCount());
			System.out.println("Unrepairable Count = " + badResults.size());
			
			// The attribute that the row is updated by must be UNIQUE!
			if(re.getRepairCount() > 0) {
				db.updateRows(table_name, result, attributes.get(0), group_by);
			}
		}
	}

}
