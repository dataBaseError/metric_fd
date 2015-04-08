package metric_fd;

import java.util.ArrayList;
import java.util.HashMap;

public class Tester {
	
	public static void main(String[] args) {
		
		String db_name = "movies";
		DBInterface db = new DBInterface(db_name, "postgres", "password");
		
		String[] x_attributes = new String[1];
		x_attributes[0] = "name";
		String y_attribute = "duration";
		String table_name = "movie";
		Integer delta = 5;
		
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("website");
		attributes.add("name");
		attributes.add(y_attribute);
		
		ArrayList<String > group_by = new ArrayList<String>();
		group_by.add(x_attributes[0]);
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
			
			Repair<String> re = new Repair<String>(x_attributes, y_attribute, delta) {

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
				/*for(int i = 0; i < corePatterns.get(k).size(); i++) {
					for(int j = 0; j < attributes.size(); j++) {
						System.out.print(corePatterns.get(k).get(i).get(attributes.get(j)));
						if(j < attributes.size() -1) {
							System.out.print(", ");
						}
					}
					System.out.println();
				}*/
			}
			
			re.costAnalysis(rows, corePatterns);
			
			/*for(int j = 0; j < attributes.size(); j++) {
				System.out.print(attributes.get(j) + ", ");
			}
			System.out.println();*/
			
		}
	}

}
