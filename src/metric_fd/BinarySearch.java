package metric_fd;

import java.util.ArrayList;
import java.util.HashMap;

public class BinarySearch {
	
	/**
	 * Simplified interface method for calling binary search.
	 * @param patterns The data structure to search through for the target value
	 * @param target The target value to look for.
	 * @param attribute The attribute key that the target value is set under.
	 * @return The index that the value is located at. Null is returned if the value is not found
	 */
	public static <E, T extends Comparable<T> > Integer binarySearch(ArrayList<HashMap<E, T > > patterns, T target, E attribute) {
		
		return binarySearch(patterns, target, attribute, 0, patterns.size()-1);
	}
	
	/**
	 * The recursive method that allows used to split the data set into relatively even parts
	 * and locate the target value if it exists in the data set.
	 * @param patterns The data structure to search through for the target value
	 * @param target The target value to look for.
	 * @param attribute The attribute key that the target value is set under.
	 * @param start The starting index for the search, used for recursive calls.
	 * @param end The ending index for the search, used for recursive calls.
	 * @return The index that the value is located at. Null is returned if the value is not found
	 */
	private static <E, T extends Comparable<T> > Integer binarySearch(ArrayList<HashMap<E, T > > patterns, T target, E attribute, int start, int end) {
		
		// Not found
		if(start > end) {
			return null;
		}
		
		// One element left lets check that element
		if(start == end) {
			if(patterns.get(start).get(attribute).compareTo(target) == 0) {
				// The last value is the one we are looking for
				return start;
			}
			// The value is not here.
			return null;
		}
		
		// Integer division
		int middle = (start + end) / 2;
		int result = patterns.get(middle).get(attribute).compareTo(target);
		if (result > 0) {
			// We can use the tested element+1 till the end since the target value was no the middle element
			return binarySearch(patterns, target, attribute, middle+1, end);
		}
		else if(result < 0) {
			// We can use the start till the tested element-1 since the target value was no the middle element
			return binarySearch(patterns, target, attribute, start, middle-1);
		}
		return middle;
	}
}
