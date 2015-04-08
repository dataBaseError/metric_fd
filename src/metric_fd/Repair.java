package metric_fd;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Repair<E> {
	
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
	
	private MetricFD<E> mfd;
	
	public Repair(MetricFD<E> mfd) {
		this.mfd = mfd;
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
		
		for(int i = 0; i < rows.size(); i++) {
			if (i > 0) {
				if(!compareArray(rows.get(i-1), rows.get(i), this.mfd.getX_attributes())) {
					// The X values do not match
					
					// Add the best pattern to the core pattern
					if(best_pattern.size() < current_pattern.size()) {
						core_patterns.add(new ArrayList<HashMap<E, T> >(current_pattern));
					}
					else {
						core_patterns.add(new ArrayList<HashMap<E, T> >(best_pattern));
					}					
					
					current_pattern.clear();
					best_pattern.clear();
					min_row = rows.get(i);					
				}
				else if(!satisftyMFD(min_row.get(this.mfd.getY_attribute()), rows.get(i).get(this.mfd.getY_attribute()))) {
					// The Y value does not satisfy the MFD
					if(best_pattern.size() < current_pattern.size()) {
						best_pattern = new ArrayList<HashMap<E, T> >(current_pattern);
					}					
					current_pattern.clear();
					min_row = rows.get(i);
				}
				current_pattern.add(rows.get(i));
			}
			else {
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
	public static <E, T extends Comparable<T> > T getTarget(ArrayList<HashMap<E, T>> arrayList, HashMap<E, T> hashMap, E y_attribute2, Integer delta) {
		
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
		if(arrayList.get(0).get(y_attribute2).compareTo(hashMap.get(y_attribute2)) > 0) {
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
			if(!compareArray(corePatterns.get(j).get(0), rows.get(i), this.mfd.getX_attributes())) {
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
			
			if(!compareArray(corePatterns.get(j).get(0), rows.get(i), this.mfd.getX_attributes())) {
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
	public <T extends Comparable<T> > void costAnalysis(ArrayList<HashMap<E, T> > rows, ArrayList<ArrayList<HashMap<E, T> > > corePatterns) {
		
		int j = 0;
		Candidate<E, T> closest;
		T result;
		for(int i = 0; i < rows.size(); i++) {
			if(!compareArray(corePatterns.get(j).get(0), rows.get(i), this.mfd.getX_attributes())) {
				j++;
			}
			
			// Check if the row is already within the corePattern set (aka already repaired)
			if(!corePatterns.get(j).contains(rows.get(i))) {
				result = getTarget(corePatterns.get(j), rows.get(i), this.mfd.getY_attribute(), this.mfd.getDelta());
				
				// If we are using values that cannot be subtracted we can incur a larger cost and only use values from within the active domain.
				Integer cost = distance(rows.get(i).get(this.mfd.getY_attribute()), result);
				
				closest = findClosest(rows.get(i), corePatterns, j);
				
				if(closest == null || cost <= closest.getDistance()) {
					// Repair to the right hand side cost
					rows.get(i).put(this.mfd.getY_attribute(), result);
				}
				else {
					// Repair the left hand side.
					setArray(rows.get(i), closest.getRow(), this.mfd.getX_attributes());
				}
			}
		}
	}
	
	/**
	 * TODO move to another class
	 * Set the values of row map for each value in the attributes list to the value in the targets list
	 * aka. row[attributes] = target[attributes]
	 * @param row The HashMap to be updated to the target values
	 * @param target The HashMap containing the target values
	 * @param attributes The attributes to update in the rows HashMap.
	 */
	private static <E, T extends Comparable<T> > void setArray(HashMap<E, T> row, HashMap<E, T> target, E[] attributes) {
		for(int i = 0; i < attributes.length; i++) {
			row.put(attributes[i], target.get(attributes[i]));
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
		Integer cost = 0;
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
	private <T extends Comparable<T> > Integer arrayDistance(HashMap<E, T> left, HashMap<E, T> right, E[] attributes) {
		
		Integer cost = 0;
		for(int i = 0; i < attributes.length; i++) {
			// Get the distance of each attribute to the base.
			cost += distance(left.get(attributes[i]), right.get(attributes[i]));
		}
		
		return cost;
	}

	/**
	 * TODO move to another class
	 * Compare a array of attributes found in both hash maps. Returns true if all of the attribute's values match
	 * @param element1
	 * @param element2
	 * @param attributes
	 * @return
	 */
	private static <E, F> boolean compareArray(HashMap<E, F> element1, HashMap<E, F> element2, E[] attributes) {
		for(int i = 0; i < attributes.length; i++) {
			// Use the base Object's equals method.
			if (!element1.get(attributes[i]).equals(element2.get(attributes[i]))) {
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
	private <T> boolean satisftyMFD(T left, T right) {
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
}