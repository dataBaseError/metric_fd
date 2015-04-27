package metric_fd;

import java.util.HashMap;

public class BasicRepair <E, H extends Comparable > extends Repair<E, H> {
	
	public BasicRepair(MetricFD<E> mfd, HashMap<E, HashMap<String, H> > boundaries) {
		super(mfd, boundaries);
	}

	@Override
	public <T> Integer distance(T left, T right) {

		if(left instanceof Integer && right instanceof Integer) {

			// Normalized distance is the distance divided by max distance 
			return Math.abs((Integer) left - (Integer) right);
		}
		else if(left instanceof String && right instanceof String) {
			// Normalized distance by dividing the distance by the max distance (range)
			// TODO try one of the string distance algorithms from the paper.
			return Levenshtein.distance((String) left, (String) right);
		}
		return null;
	}
}
