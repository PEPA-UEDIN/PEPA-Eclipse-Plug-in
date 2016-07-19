package uk.ac.ed.inf.pepa.ctmc.derivation.aggregation;

import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Interface to represent a PEPA's LTS model.
 * 
 * @author Giacomo Alzetta
 *
 */
public interface LabelledTransitionSystem<S> extends Iterable<S> {
	
	/**
	 * The number of states in the transition system.
	 * @return the number of states in the transition system.
	 */
	public int size();
	default int numberOfStates() {
		return size();
	}
	
	/**
	 * A transition is a triplet: <source-state, target-state, label>
	 * 
	 * @return the number of transitions in the system.
	 */
	public int numberOfTransitions();
	
	/**
	 * @return the number of action types that appear in LTS transitions.
	 */
	public int numberOfActionTypes();
	
	/**
	 * Adds a state to the LTS model.
	 * 
	 * @param state The state to be added.
	 */
	public void addState(S state);
	
	/**
	 * Add a transition to the LTS model.
	 * 
	 * @param source 	The starting state
	 * @param target 	The ending state
	 * @param rate   	The rate of the transition
	 * @param actionId 	The action type of the transition
	 */
	public void addTransition(S source, S target, double rate, short actionId);
	
	/**
	 * Return all the action types that appear in transitions from source
	 * to target.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public Iterable<Short> getActions(S source, S target);
	
	/**
	 * Get the apparent rate of the transitions from source to target
	 * with the given action type.
	 * 
	 * @param source
	 * @param target
	 * @param actionId
	 * @return
	 */
	public double getApparentRate(S source, S target, short actionId);
	default double getApparentRate(S source, List<S> targets, short actionId) {
		double[] rates = new double[targets.size()];
		int i = 0;
		for (S target: targets) {
			double x = getApparentRate(source, target, actionId);
			rates[i++] = x;
		}
		
		double result = rates[0];
		for (i=1; i < rates.length; ++i) {
			result += rates[i];
		}
		
		return result;
	}
	
	/**
	 * Get all the states reachable by transitions from source.
	 * @param source
	 * @return
	 */
	public Iterable<S> getImage(S source);
	
	/**
	 * Get all states that have transition to target.
	 * 
	 * @param target
	 * @return
	 */
	public Iterable<S> getPreImage(S target);
}
