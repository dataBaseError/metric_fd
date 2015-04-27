package metric_fd;

import java.util.HashMap;

public class Candidate<E, T extends Comparable<T> > {
	
	private HashMap<E, T> row;
	private Double distance;
	
	public Candidate(HashMap<E, T> row, Double cost) {
		this.row = row;
		this.distance = cost;
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
	public Double getDistance() {
		return distance;
	}
}
