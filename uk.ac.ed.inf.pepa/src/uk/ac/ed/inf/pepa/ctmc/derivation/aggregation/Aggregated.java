package uk.ac.ed.inf.pepa.ctmc.derivation.aggregation;

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
	private S representative = null;
	private int hash = -1;
	
	
	public Aggregated(Iterable<S> states) {
		internalStates = new HashSet<>();
		representative = null;
		for (S state: states) {
			add(state);
		}
	}
	public S getRepresentative() {
		return representative;
	}
	
	public int size() {
		return internalStates.size();
	}
	
	public void add(S state) {
		hash = -1;
		if (representative == null || state.compareTo(representative) < 0) {
			representative = state;
		}
		internalStates.add(state);
	}
	
	public boolean contains(S state) {
		return internalStates.contains(state);
	}
	
	@Override
	public Iterator<S> iterator() {
		return internalStates.iterator();
	}

	@Override
	public int compareTo(Aggregated<S> o) {
		// FIXME: is this correct? 
		return getRepresentative().compareTo(o.getRepresentative());
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Aggregated)) {
			return false;
		}
		
		Aggregated<S> oo = (Aggregated<S>) o;
		return internalStates.equals(oo.internalStates);
	}
	
	@Override
	public int hashCode() {
		if (hash == -1) {
			hash = internalStates.hashCode();
		}
		return hash;
	}
	
	@Override
	public String toString() {
		return "Aggregated(" + internalStates.toString() + ")";
	}
}
