/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.inf.pepa.model.Rate;

/**
 * @author Giacomo Alzetta
 * @param <T>
 * @param <V>
 *
 */
public abstract class AggregatedModelComponent<T, V extends Rate, A extends AggregationAlgorithm<T, V>> {

	protected ArrayList<AggregatedState> componentStates;
	protected TransitionMap transitionMap;
	protected Set<Short> sharedActions = new HashSet<Short>();

	abstract protected void buildStateSpace();
	
	abstract protected void buildTransitions();
	
	final protected void reduceStateSpace() {
		// TODO: call the appropriate lumping algorithm.
	}
	
}
