/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.ed.inf.pepa.ctmc.derivation.common.State;

/**
 * @author Giacomo Alzetta
 *
 */
public class AggregatedState implements Iterable<State> {

	private Set<State> internalStates = new HashSet<State>();
	private short[] repr = null;
	
	public AggregatedState() {}
	
	public AggregatedState(short[] state) {
		add(state);
		repr = state;
	}
	
	public int size() {
		return internalStates.size();
	}
	
	public boolean add(short[] state) {
		if (repr == null) {
			repr = state;
		}
		return internalStates.add(new State(state, Arrays.hashCode(state)));
	}
	
	public boolean contains(State state) {
		return internalStates.contains(state);
	}
	
	public short[] getRepresentative() {
		return repr;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof AggregatedState)) {
			return false;
		}
		
		return internalStates.equals(((AggregatedState) obj).internalStates);
	}
	
	@Override
	public Iterator<State> iterator() {
		return internalStates.iterator();
	}
	
	
}
