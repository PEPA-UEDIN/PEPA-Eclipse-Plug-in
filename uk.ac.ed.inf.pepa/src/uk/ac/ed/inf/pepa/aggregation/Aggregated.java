package uk.ac.ed.inf.pepa.aggregation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Giacomo Alzetta
 *
 * @param <S> The type of single state.
 */
public class Aggregated<S extends Comparable<S>> implements Iterable<S>, Comparable<Aggregated<S>> {

	private Set<S> internalStates = new HashSet<S>();
	
	public S getRepresentative() {
		return internalStates.iterator().next();
	}
	
	public int size() {
		return internalStates.size();
	}
	
	public void add(S state) {
		internalStates.add(state);
	}
	
	public boolean contains(S state) {
		return internalStates.contains(state);
	}
	
	@Override
	public Iterator<S>iterator() {
		return internalStates.iterator();
	}

	@Override
	public int compareTo(Aggregated<S> o) {
		return getRepresentative().compareTo(o.getRepresentative());
	}
}
