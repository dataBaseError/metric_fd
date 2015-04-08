package metric_fd;

import java.util.HashMap;

public class Candidate<E, T extends Comparable<T> > {
	
	private HashMap<E, T> row;
	private Integer distance;
	
	public Candidate(HashMap<E, T> row, Integer distance) {
		this.row = row;
		this.distance = distance;
	}

	/**
	 * Get the row
	 * @return the row values
	 */
	public HashMap<E, T> getRow() {
		return row;
	}

	/**
	 * Get the distance
	 * @return the distance
	 */
	public Integer getDistance() {
		return distance;
	}
}
