package uk.ac.ed.inf.pepa.ctmc.derivation.aggregation;

import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Interface to represent a PEPA's LTS semantics.
 * 
 * @author Giacomo Alzetta
 *
 */
public interface LabelledTransitionSystem<S> extends Iterable<S> {
	
	public int size();
	default int numberOfStates() {
		return size();
	}
	
	public int numberOfTransitions();
	public int numberOfActionTypes();
	
	public void addState(S state);
	public void addTransition(S source, S target, double rate, short actionId);
	
	public Iterable<Short> getActions(S source, S target);
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
	
	public Iterable<S> getImage(S source);
	
	public Iterable<S> getPreImage(S target);
}
