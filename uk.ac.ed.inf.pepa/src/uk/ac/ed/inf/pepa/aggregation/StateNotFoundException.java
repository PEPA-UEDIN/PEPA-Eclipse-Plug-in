/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation;

/**
 * @author Giacomo Alzetta
 *
 */
public class StateNotFoundException extends PartitioningException {

	/**
	 * Auto-generated serial UID number.
	 */
	private static final long serialVersionUID = 8122028244251504693L;

	public StateNotFoundException(String message) {
		super(message);
	}
	
	public StateNotFoundException(Throwable exc) {
		super(exc);
	}
	
	public StateNotFoundException(String message, Throwable exc) {
		super(message, exc);
	}
}
