package uk.ac.ed.inf.pepa.aggregation;

import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpace;
import uk.ac.ed.inf.pepa.model.NamedRate;
import uk.ac.ed.inf.pepa.model.RateMath;
import java.util.List;


/**
 * Interface to represent a PEPA's LTS semantics.
 * 
 * @author Giacomo Alzetta
 *
 */
public interface LabelledTransitionSystem<S> {

	public boolean isValid();
	
	public int size();
	
	public boolean addState(S state);
	public boolean addTransition(S source, S target, double rate, short actionId);
	
	public double getApparentRate(S source, S target, short actionId);
	default double getApparentRate(S source, S[] targets, short actionId) {
		double[] rates = new double[targets.length];
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
	
	public List<S> getImage(S source);
	
	public List<S> getPreImage(S target);
}
