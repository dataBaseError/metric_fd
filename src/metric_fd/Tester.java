package metric_fd;

import java.util.ArrayList;
import java.util.HashMap;

public class Tester {
	
	public static void main(String[] args) {
		
		String username = "postgres";
		String password = "password";
		if(args != null && args.length >= 2) {
			if(args[0].equalsIgnoreCase("movies")) {
				testMovies(username, password, Integer.parseInt(args[1]));
			}
			else {
				testFlight(username, password, Integer.parseInt(args[1]));
			}
		} 
		else {
			System.out.println("Not enough args");
		}
		
	}
	
	private static void testFlight(String username, String password, Integer delta) {
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
				ArrayList<HashMap<String, Comparable > > rows = db.get_sorted(table_name, attributes, group_by);
				
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
				
				Repair<String> re = new Repair<String>(mfds.get(i)) {
	
					@Override
					public Integer distance(Object left, Object right) {
	
						if(left instanceof Integer && right instanceof Integer) {
							return Math.abs((Integer) left - (Integer) right);
						}
						else if(left instanceof String && right instanceof String) {
							return Levenshtein.distance((String) left, (String) right);
						}
						return null;
					}
					
				};
				
				ArrayList<ArrayList<HashMap<String, Comparable > > > corePatterns = re.createCorePatterns(rows);
				
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
				}
				System.out.println("Finished " + i);
				
				ArrayList<HashMap<String, Comparable > > badResults = new ArrayList<HashMap<String, Comparable > >();
				ArrayList<HashMap<String, Comparable > > result = re.costAnalysis(rows, corePatterns, badResults);
				
				// TODO update datebase with new values
				db.updateRows(table_name, result, attributes.get(0), group_by);
			}
		}
	}

	private static void testMovies(String username, String password, Integer delta) {
		String db_name = "movies";
		DBInterface db = new DBInterface(db_name, username, password);
		
		ArrayList<String> x_attributes = new ArrayList<String>();
		x_attributes.add("name");
		String y_attribute = "duration";
		String table_name = "movie";
		//Integer delta = 5;
		
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("website");
		attributes.add("name");
		attributes.add(y_attribute);
		
		ArrayList<String > group_by = new ArrayList<String>();
		group_by.addAll(x_attributes);
		group_by.add(y_attribute);
		
		db.connect();
		
		if(db.isConnected()) {
			
			ArrayList<HashMap<String, Comparable > > rows = db.get_sorted(table_name, attributes, group_by);
			
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
			
			Repair<String> re = new Repair<String>(mfd) {

				@Override
				public Integer distance(Object left, Object right) {

					if(left instanceof Integer && right instanceof Integer) {
						return Math.abs((Integer) left - (Integer) right);
					}
					else if(left instanceof String && right instanceof String) {
						return Levenshtein.distance((String) left, (String) right);
					}
					return null;
				}
				
			};
			
			ArrayList<ArrayList<HashMap<String, Comparable > > > corePatterns = re.createCorePatterns(rows);
			
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
			}

			ArrayList<HashMap<String, Comparable > > badResults = new ArrayList<HashMap<String, Comparable > >();
			re.costAnalysis(rows, corePatterns, badResults);
		}
	}

}
