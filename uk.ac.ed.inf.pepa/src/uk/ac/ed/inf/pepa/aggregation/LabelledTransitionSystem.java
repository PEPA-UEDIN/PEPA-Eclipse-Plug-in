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
	public boolean addTransition(S source, S target, NamedRate rate);
	
	public NamedRate getApparentRate(S source, S target, String action);
	default NamedRate getApparentRate(S source, S[] targets, String action) {
		NamedRate[] rates = new NamedRate[targets.length];
		int i = 0;
		for (S target: targets) {
			NamedRate x = getApparentRate(source, target, action);
			rates[i++] = x;
		}
		
		NamedRate result = rates[0];
		for (i=1; i < rates.length; ++i) {
			result = (NamedRate) RateMath.sum(result, rates[i]);
		}
		
		return result;
	}
	
	public List<S> getImage(S source);
	
	public List<S> getPreImage(S target);
	
	public IStateSpace toStateSpace();
}