package metric_fd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Repair<E, H extends Comparable> {
	
	// TODO create a class that encapsulates the MFD
	/*
	private E[] x_attributes;
	private E y_attribute;
	private Integer delta;
	*/
	/*
	public Repair(E[] x_attributes, E y_attribute, Integer delta) {
		this.x_attributes = x_attributes;
		this.y_attribute = y_attribute;
		this.delta = delta;
	}
	*/
	
	public final static String MIN = "min";
	public final static String MAX = "max";
	
	private MetricFD<E> mfd;
	private int clean_rate;
	private int repair_count;
	private int num_rows;
	protected HashMap<E, HashMap<String, H > > boundaries;
	
	public Repair(MetricFD<E> mfd, HashMap<E, HashMap<String, H > > boundaries) {
		this.mfd = mfd;
		this.clean_rate = 0;
		this.repair_count = 0;
		this.num_rows = 0;
		this.boundaries = boundaries;
	}
	
	public int getCleanCount() {
		return clean_rate;
	}
	
	public int getRepairCount() {
		return this.repair_count;
	}
	
	public void resetCleanRatio() {
		clean_rate = 0;
	}

	public void setMaxMin(HashMap<E, HashMap<String, H > > boundaries) {
		this.boundaries = boundaries;
	}
	
	public int getRows() {
		return this.num_rows;
	}
	
	public double getCleanRate() {
		return this.clean_rate / ((double) this.num_rows);
	}
	
	/**
	 * TODO complete doc
	 * Basically finds the largest groups of tuples (rows) that satisfies the MFD)
	 * @param <T>
	 * @param rows
	 * @return
	 */
	public <T> ArrayList<ArrayList<HashMap<E, T> > > createCorePatterns(ArrayList<HashMap<E, T> > rows) {
		ArrayList<ArrayList<HashMap<E, T> > > core_patterns = new ArrayList<ArrayList<HashMap<E, T> > >();
		ArrayList<HashMap<E, T> > best_pattern = new ArrayList<HashMap<E, T> >();
		ArrayList<HashMap<E, T> > current_pattern = new ArrayList<HashMap<E, T> >();
		HashMap<E, T> min_row = null;
		boolean invalid = false;
		
		clean_rate = 0;
		num_rows = rows.size();
		
		for(int i = 0; i < rows.size(); i++) {
			if (min_row != null && min_row.get(this.mfd.getY_attribute()) != null) {
				if(!compareArrayElements(rows.get(i-1), rows.get(i), this.mfd.getX_attributes())) {
					// The X values do not match
					
					// Add the best pattern to the core pattern
					if(best_pattern.size() < current_pattern.size()) {
						core_patterns.add(new ArrayList<HashMap<E, T> >(current_pattern));
					}
					else {
						core_patterns.add(new ArrayList<HashMap<E, T> >(best_pattern));
					}
					
					clean_rate += core_patterns.get(core_patterns.size()-1).size();
					
					current_pattern.clear();
					best_pattern.clear();
					min_row = rows.get(i);					
				}
				else if(rows.get(i).get(this.mfd.getY_attribute()) == null) {
					// This row should be marked for repair
					invalid = true;
				}
				else if(!satisftyMFD(this.mfd.getY_attribute(), min_row.get(this.mfd.getY_attribute()), rows.get(i).get(this.mfd.getY_attribute()))) {
					// The Y value does not satisfy the MFD
					if(best_pattern.size() < current_pattern.size()) {
						best_pattern = new ArrayList<HashMap<E, T> >(current_pattern);
					}

					current_pattern.clear();
					min_row = rows.get(i);
				}
				if(!invalid) {
					current_pattern.add(rows.get(i));
				}
				else {
					invalid = false;					
				}
			}
			else {
				current_pattern.clear();
				min_row = rows.get(i);
				current_pattern.add(rows.get(i));				
			}
		}
		
		// Add the last best_pattern (or current_pattern if current is better than best) to the core_patterns
		if(best_pattern.size() < current_pattern.size()) {
			best_pattern = current_pattern;
		}
		core_patterns.add(best_pattern);
		
		return core_patterns;
	}
	
	/**
	 * TODO document
	 * @param arrayList
	 * @param hashMap
	 * @param y_attribute2
	 * @param delta
	 * @return
	 */
	public static <E, T extends Comparable<T> > T getTarget(ArrayList<HashMap<E, T>> arrayList, HashMap<E, T> hashMap, E y_attribute2, Long delta) {
		
		ArrayList<T> g_attributes = new ArrayList<T>(arrayList.size());
		
		for(int i = 0; i < arrayList.size(); i ++) { 
			g_attributes.add(arrayList.get(i).get(y_attribute2));
		}
		
		//Instead of lowest cost (where values can be changed to elements outside the active domain) we can use lowest cost in active domain
		// It is still possible to use lowest cost and add values from outside the active domain however this is really only possible for numerical
		// data types (since other data types would need to define arithmetic operations addition, subtraction, distance. All of which must be directly 
		// related to the data type's comparison operations. E.g. Strings do not work so well for this since a distance function for strings does not
		// relate to the comparison operations used. 
		/* 
		if((Integer) arrayList.get(0).get(y_attribute2) > (Integer) hashMap.get(y_attribute2)) {
			return ((Integer) max(g_attributes) - delta);
		}
		return ((Integer) min(g_attributes) - delta);*/
		
		// This lowest cost will convert the value to lowest cost value within the active domain (of that corePattern
		if(hashMap.get(y_attribute2) == null || arrayList.get(0).get(y_attribute2).compareTo(hashMap.get(y_attribute2)) > 0) {
			return min(g_attributes);
		}
		return max(g_attributes);
	}
	
	/**
	 * Right hand side only
	 * @param rows
	 * @param corePatterns
	 */
	public <T extends Comparable<T> > void costAnalysisRHS(ArrayList<HashMap<E, T> > rows, ArrayList<ArrayList<HashMap<E, T> > > corePatterns) {
		
		// Note rows should be stored by x followed by y
		// The resulting core patterns will be ordered by X (similar to the rows)
		
		int j = 0;
		for(int i = 0; i < rows.size(); i++) {
			if(!compareArrayElements(corePatterns.get(j).get(0), rows.get(i), this.mfd.getX_attributes())) {
				j++;
			}
			
			if(!corePatterns.get(j).contains(rows.get(i))) {
				
				T result = getTarget(corePatterns.get(j), rows.get(i), this.mfd.getY_attribute(), this.mfd.getDelta());
				
				// If we are using values that cannot be subtracted we can incur a larger cost and only use values from within the active domain.
				//Integer cost = distance(rows.get(i).get(this.mfd.getY_attribute()), result);

				// Repair to the right hand side
				rows.get(i).put(this.mfd.getY_attribute(), result);
			}
		}
	}
	
	/**
	 * Left hand side only
	 * @param rows
	 * @param corePatterns
	 */
	public <T extends Comparable<T> > void costAnaylsisLHS(ArrayList<HashMap<E, T> > rows, ArrayList<ArrayList<HashMap<E, T> > > corePatterns) {
		// Do it only based on y == y
		
		// Note rows should be stored by y followed by x
		// The core patterns should alternatively be stored by X
		
		Candidate<E, T> closest;
		
		int j = 0;
		for(int i = 0; i < rows.size(); i++) {
			
			if(!compareArrayElements(corePatterns.get(j).get(0), rows.get(i), this.mfd.getX_attributes())) {
				j++;
			}
			
			/*if(corePatterns.get(j).get(0).get(this.mfd.getY_attribute()).equals(rows.get(i).get(this.mfd.getY_attribute()))) {
				j++;
			}*/
			
			if(!corePatterns.get(j).contains(rows.get(i))) {
				// Determine the which one has a greater cost
				closest = findClosest(rows.get(i), corePatterns, j);
			
				// Repair the left hand side.
				setArray(rows.get(i), closest.getRow(), this.mfd.getX_attributes());
			}			
		}
	}
	
	/**
	 * The general cost analysis function. This method employs both left and right hand side repairs based
	 * on which one has the lowest cost.
	 * @param rows The list of tuples from the database.
	 * @param corePatterns The set of corePatterns created from the createCorePatterns method.
	 */
	public <T extends Comparable<T> > ArrayList<HashMap<E, T> > costAnalysis(ArrayList<HashMap<E, T> > rows, ArrayList<ArrayList<HashMap<E, T> > > corePatterns, ArrayList<HashMap<E, T > > badTuples) {
		
		int j = 0;
		Candidate<E, T> closest = null;
		boolean skip = false;
		T result;
		for(int i = 0; i < rows.size(); i++) {

			// Check if the tuple matches to the current CoreTuples or if the deviant tuple is unrepairable
			for(int k = 0; k < 2 ; k++) {
				if(!compareArrayElements(corePatterns.get(j).get(0), rows.get(i), this.mfd.getX_attributes())) {
					
					if(k == 1) {
						// The deviant tuple does not match to Core Tuple at j+1 or j+2.
						// There are two possible reasons for this:
						// 1. Core Tuples j+1, j+2 do not have any related deviant tuples.
							// We can tell if it is this case by testing whether the X_attributes for the Core Tuple j+1 is less than the deviant tuple's X attributes.
								// 1. they are less than those CoreTuples do not have any values keep checking. Currently every CoreTuple will have tuples in rows (cause they are never removed).
								// 2. Else the deviant pattern is unrepairable.
						// 2. Deviant tuple is part of unrepairable class (e.g. all deviants have null as their Y attribute) no valid value to repair them to).
						if(!compareArray(corePatterns.get(j).get(0), rows.get(i), this.mfd.getX_attributes())) {
							// Bad tuples ignore them!
							badTuples.add(rows.get(i));
							rows.remove(i);
							
							// Check next element (since current was removed.
							i--;
							
							j--;
							
							skip = true;
							break;
						}
					}
					j++;
					
				}
				else {
					if(k == 1)
					{
						// A deviant tuple for the next core tuple set is the current tuple
					}
					else {
						// Current Core tuple set relates to deviant tuple
					}
					break;
				}
			}
			
			if(!skip) {
			
				// Check if the row is already within the corePattern set (aka already repaired)
				if(!corePatterns.get(j).contains(rows.get(i))) {
					result = getTarget(corePatterns.get(j), rows.get(i), this.mfd.getY_attribute(), this.mfd.getDelta());
					
					// If the right side is null then we must repair it to the LHS it is apart of.
					Double cost = null;
					if(rows.get(i).get(this.mfd.getY_attribute()) != null) {
						// If we are using values that cannot be subtracted we can incur a larger cost and only use values from within the active domain.
						cost = norm_distance(this.mfd.getY_attribute(), rows.get(i).get(this.mfd.getY_attribute()), result);
					}
					
					if(cost != null) {
						closest = findClosest(rows.get(i), corePatterns, j);
					}
					
					if(closest == null || cost == null || cost <= closest.getDistance()) {
						// Repair to the right hand side cost
						rows.get(i).put(this.mfd.getY_attribute(), result);
					}
					else {
						// Repair the left hand side.
						setArray(rows.get(i), closest.getRow(), this.mfd.getX_attributes());
					}
					repair_count++;
				}
			}
			else {
				skip = false;
			}
			
		}
		return rows;
	}
	
	/**
	 * TODO move to another class
	 * Set the values of row map for each value in the attributes list to the value in the targets list
	 * aka. row[attributes] = target[attributes]
	 * @param row The HashMap to be updated to the target values
	 * @param target The HashMap containing the target values
	 * @param attributes The attributes to update in the rows HashMap.
	 */
	private static <E, T extends Comparable<T> > void setArray(HashMap<E, T> row, HashMap<E, T> target, ArrayList<E> attributes) {
		for(int i = 0; i < attributes.size(); i++) {
			row.put(attributes.get(i), target.get(attributes.get(i)));
		}
	}
	
	/**
	 * TODO document this
	 * @param row
	 * @param corePatterns
	 * @param index
	 * @return
	 */
	private <T extends Comparable<T>> Candidate<E, T> findClosest(HashMap<E, T> row, ArrayList<ArrayList<HashMap<E, T> > > corePatterns, int index) {
		// TODO fix this;
		// This may not find the closest since the sorting will only find the closest in terms of the X[1] then X[2] ... then X[N]
		// Might have to go through and each core pattern and calculate the distance if they are similar
		// Also in the case of the directly above and below CorePatterns containing no element with the same Y attribute value no closest value will be reported.
		// Use binary search
		
		Candidate<E, T> best_cost = null;
		double cost = 0;
		for(int i = 0; i < corePatterns.size(); i++) {
			if(index != i) {				
				// Search the core patterns for a match between the Y attribute values
				if(BinarySearch.binarySearch(corePatterns.get(i), row.get(this.mfd.getY_attribute()), this.mfd.getY_attribute()) != null) {
					// The Y attribute values match identify the cost of converting
					// All corePatterns will have the same X attributes so any will do.
					cost = arrayDistance(row, corePatterns.get(i).get(0), this.mfd.getX_attributes());
					if(best_cost == null || best_cost.getDistance() > cost) {
						// Set the first cost to the min_cost
						best_cost = new Candidate<E, T>(corePatterns.get(i).get(0), cost);
					}
				}
			}
		}
		return best_cost;
		
		// TODO remove the following
		/*HashMap<String, Object> above = null;
		HashMap<String, Object> below = null;
		
		if(index < corePatterns.size()) {
			// Check above
			above = findSimilar(row, corePatterns.get(index+1));			
		}
		
		
		if(index > 0) {
			// Check below
			below = findSimilar(row, corePatterns.get(index-1));
		}
		
		if(above != null && below != null) {
			Integer below_distance = arrayDistance(row, below, this.x_attributes);
			Integer above_distance = arrayDistance(row, above, this.x_attributes);
			if(below_distance > above_distance) {
				return new Candidate(above, above_distance);
			}
			return new Candidate(below, below_distance);
		}
		else if(above != null) {
			Integer above_distance = arrayDistance(row, above, this.x_attributes);
			return new Candidate(above, above_distance);
		}
		else if(below != null) {
			Integer below_distance = arrayDistance(row, below, this.x_attributes);
			return new Candidate(below, below_distance);
		}
		return null;*/
	}
	
	/* TODO remove
	private HashMap<String, Object> findSimilar(HashMap<String, Object> row, ArrayList<HashMap<String, Object> > corePattern) {
		for(int i = 0; i < corePattern.size(); i++) {
			if(corePattern.get(i).get(this.mfd.getY_attribute()).equals(row.get(this.mfd.getY_attribute()))) {
				// A potential match
				return corePattern.get(i);
			}
		}
		return null;
	}*/
	
	/**
	 * TODO document
	 * @param left
	 * @param right
	 * @param attributes
	 * @return
	 */
	private <T extends Comparable<T> > double arrayDistance(HashMap<E, T> left, HashMap<E, T> right, ArrayList<E> attributes) {
		
		Double cost = 0.0;
		for(int i = 0; i < attributes.size(); i++) {
			// Get the distance of each attribute to the base.
			cost += norm_distance(attributes.get(i), left.get(attributes.get(i)), right.get(attributes.get(i)));
		}
		// Since the distance is normalized we will divide the summation of normalized distances by the number of normalized distances added.
		// This maintains the range of the values (between 0-1)
		return cost/attributes.size();
	}

	/**
	 * TODO move to another class
	 * Compare a array of attributes found in both hash maps. Returns true if all of the attribute's values match
	 * @param element1
	 * @param element2
	 * @param attributes
	 * @return
	 */
	private static <E, F> boolean compareArrayElements(HashMap<E, F> element1, HashMap<E, F> element2, ArrayList<E> attributes) {
		for(int i = 0; i < attributes.size(); i++) {
			// Use the base Object's equals method.
			if (!element1.get(attributes.get(i)).equals(element2.get(attributes.get(i)))) {
				return false;
			}
		}		
		return true;
	}
	
	/**
	 * Compare arrays using compare to method.
	 * @param element1
	 * @param element2
	 * @param attributes
	 * @return
	 */
	private static <E, F extends Comparable<F>> boolean compareArray(HashMap<E, F> element1, HashMap<E, F> element2, ArrayList<E> attributes) {
		for(int i = 0; i < attributes.size(); i++) {
			// Check if the first element is ever greater than (in terms of order) the second element.
			if (element1.get(attributes.get(i)).compareTo(element2.get(attributes.get(i))) > 0) {
				return false;
			}
		}		
		return true;
	}
	
	/**
	 * Check whether the MFD is satisfied.
	 * @param left
	 * @param right
	 * @return
	 */
	private <T> boolean satisftyMFD(E attribute, T left, T right) {
		return distance(left, right) <= this.mfd.getDelta();
	}
	
	/**
	 * Allows for different implementations to be created
	 * Note the return is a positive integer (only magnitude)
	 * TODO string distance
	 * TODO integer/float distance
	 * @param left
	 * @param right
	 * @return
	 */
	public abstract <T> Integer distance(T left, T right);
	
	public <T> Double norm_distance(E attribute, T left, T right) {
		return distance(left, right) / (double) distance(this.boundaries.get(attribute).get(MAX), this.boundaries.get(attribute).get(MIN));
	}
	
	/*public static Integer subtract(Integer first, Integer second) {
		return first - second;
	}

	public static Integer addition(Integer first, Integer second) {
		return first + second;		
	}*/
	
	/**
	 * Get the maximum value in the list
	 * @param list The list of values that is sorted from smallest to largest.
	 * @return The maximum value (which for a sorted list is trivial, the last value in the list).
	 */
	private static <T> T max(ArrayList<T> list) {
		// Since the list is sorted that we can just get the very last element
		return list.get(list.size()-1);
	}
	
	/**
	 * Get the minimum value in the list
	 * @param list The list of values that is sorted from smallest to largest.
	 * @return The minimum value (which for a sorted list is trivial, the first value in the list).
	 */
	private static <T> T min(ArrayList<T> list) {
		// Since the list is sorted that we can just get the very first element
		return list.get(0);
	}
	
	public static int[] orderRepairs(ArrayList<Repair<String, Comparable> > repairs, boolean random) {
		
		int[] list = new int[repairs.size()];
		
		if(random) {
			// Not really random but for the sake of testing this is more ideal since random does not taking into account the repair criteria this is a possible result.
			// While random may find a more ideal result using a normal distributed random order is just as likely to return this result.
			for(int i = 0; i < repairs.size(); i++) {
				list[i] = i;
			}
		}
		else {
			HashMap<Double, ArrayList<Integer > > orderer = new HashMap<Double, ArrayList<Integer > >();
			Double rate = 0.0;
			
			// Handles the case where two MFDs have the same clean rate
			for(int i = 0; i < repairs.size(); i ++) {
				rate = repairs.get(i).getCleanRate();
				if(!orderer.containsKey(rate)) {
					orderer.put(rate, new ArrayList<Integer>());
				}
				orderer.get(rate).add(i);
			}
			List<Double> order = Merge.mergeSort(new ArrayList<Double>(orderer.keySet()));
			for(int i = 0; i < order.size(); i++) {
				for(int j = 0; j < orderer.get(order.get(i)).size(); j++) {
					list[i] = orderer.get(order.get(i)).get(j);
				}
			}
		}
		
		return list;
	}
}