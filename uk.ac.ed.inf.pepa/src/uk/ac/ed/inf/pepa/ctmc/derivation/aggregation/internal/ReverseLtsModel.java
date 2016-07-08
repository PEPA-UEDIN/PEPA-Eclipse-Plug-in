/**
 * 
 */
package uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.LabelledTransitionSystem;

/**
 * @author giacomo
 *
 */
public class ReverseLtsModel<S> implements LabelledTransitionSystem<S> {

	
	LabelledTransitionSystem<S> lts;
	
	public ReverseLtsModel(LabelledTransitionSystem<S> lts) {
		this.lts = lts;
	}
	
	// Check if it is correct!
	@Override
	public double getApparentRate(S source, S target, short actionId) {
		return lts.getApparentRate(target, source, actionId);
	}
	
	@Override
	public List<S> getImage(S source) {
		return lts.getPreImage(source);
	}
	
	@Override
	public List<S> getPreImage(S target) {
		return lts.getImage(target);
	}
	
	@Override
	public Set<Short> getActions(S source, S target) {
		return lts.getActions(target, source);
	}

	@Override
	public Iterator<S> iterator() {
		return lts.iterator();
	}

	@Override
	public boolean isValid() {
		return lts.isValid();
	}

	@Override
	public int size() {
		return lts.size();
	}

	@Override
	public int numberOfTransitions() {
		return lts.numberOfTransitions();
	}

	@Override
	public boolean addState(S state) {
		return lts.addState(state);
	}

	@Override
	public boolean addTransition(S source, S target, double rate, short actionId) {
		return lts.addTransition(source, target, rate, actionId);
	}
	
}
