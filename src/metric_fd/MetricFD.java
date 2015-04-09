package metric_fd;

import java.util.ArrayList;

public class MetricFD<E> {
	
	private ArrayList<E> x_attributes;
	private E y_attribute;
	private Integer delta;
	
	public MetricFD(ArrayList<E> x_attributes, E y_attribute, Integer delta) {
		this.x_attributes = x_attributes;
		this.y_attribute = y_attribute;
		this.delta = delta;
	}

	/**
	 * Get the LHS attributes for the MFD
	 * @return the x_attributes
	 */
	public ArrayList<E> getX_attributes() {
		return x_attributes;
	}

	/**
	 * Set the LHS attributes for the MFD
	 * @param x_attributes the x_attributes to set
	 */
	public void setX_attributes(ArrayList<E> x_attributes) {
		this.x_attributes = x_attributes;
	}

	/**
	 * Get the RHS attribute
	 * @return the y_attribute
	 */
	public E getY_attribute() {
		return y_attribute;
	}

	/**
	 * Set the RHS attribute
	 * @param y_attribute the y_attribute to set
	 */
	public void setY_attribute(E y_attribute) {
		this.y_attribute = y_attribute;
	}

	/**
	 * Get the delta constant
	 * @return the delta
	 */
	public Integer getDelta() {
		return delta;
	}
}
