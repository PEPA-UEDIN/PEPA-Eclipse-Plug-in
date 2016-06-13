/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import uk.ac.ed.inf.pepa.aggregation.PartitionBlock;
import uk.ac.ed.inf.pepa.aggregation.StateIsMarkedException;
import uk.ac.ed.inf.pepa.aggregation.StateNotFoundException;

/**
 * @author giacomo
 * @param <T>
 *
 */
public class PartitionBlockImpl<T, V> implements PartitionBlock<T, V> {

	private LinkedList<T> nonMarkedStates;
	private LinkedList<T> markedStates;
	
	private HashMap<T, V> mapToValues;
	
	public PartitionBlockImpl(LinkedList<T> states) {
		nonMarkedStates = states;
	}
	
	@Override
	public void addState(T state) {
		nonMarkedStates.add(state);
		
	}

	@Override
	public boolean isEmpty() {
		return nonMarkedStates.isEmpty() && markedStates.isEmpty();
	}

	@Override
	public Iterator<T> getStates() {
		Iterator<T> iterator = new Iterator<T>() {
			private Iterator<T> it1 = nonMarkedStates.iterator();
			private Iterator<T> it2 = markedStates.iterator();
			
			public boolean hasNext() {
				return it1.hasNext() || it2.hasNext();
			}
			
			public T next() {
				if (it1.hasNext()) {
					return it1.next();
				} else {
					return it2.next();
				}
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		
		return iterator;
		
	}

	@Override
	public Iterator<T> getMarkedStates() {
		return new Iterator<T>() {
			Iterator<T> it = markedStates.iterator();
			
			public boolean hasNext() {
				return it.hasNext();
			}
			
			public T next() {
				return it.next();
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}		
		};
	}

	@Override
	public PartitionBlock<T, V> splitMarkedStates() {
		PartitionBlock<T, V> marked = new PartitionBlockImpl<>(markedStates);
		this.markedStates = new LinkedList<>();
		return marked;
	}

	@Override
	public Iterator<PartitionBlock<T, V>> splitBlock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void markState(T state) throws StateNotFoundException {
		boolean found = false;
		for (int i=0; i < nonMarkedStates.size(); i++) {
			if (nonMarkedStates.get(i).equals(state)) {
				nonMarkedStates.remove(i);
				markedStates.add(state);
				found = true;
				break;
			}
		}
		
		if (!found) {
			throw new StateNotFoundException("Could not find the state: " + state.toString() + " in block.");
		}
		
	}

	@Override
	public void setValue(T state, V value) throws StateNotFoundException, StateIsMarkedException {
		if (value == null) {
			throw new NullPointerException("You cannot assign a null value to a state!");
		}
		
		if (nonMarkedStates.contains(state)) {
			mapToValues.put(state, value);
		} else if (markedStates.contains(state)) {
			throw new StateIsMarkedException("State: " + state.toString() + " is marked.");
		} else {
			throw new StateNotFoundException("State: " + state.toString() + " could not be found in the block.");
		}
	}

	@Override
	public V getValue(T state) throws StateNotFoundException, StateIsMarkedException {
		V val = mapToValues.get(state);
		if (val == null) {
			if (mapToValues.containsKey(state)) {
				throw new RuntimeException("Impossible has happened: a state mapped to null.");
			} else if (nonMarkedStates.contains(state)) {
				throw new StateIsMarkedException("State: " + state.toString() + " is marked.");
			} else {
				throw new StateNotFoundException("State: " + state.toString() + " could not be found in the block.");
			}
		}
		return val;
	}

	@Override
	public boolean isMarked(T state) throws StateNotFoundException {
		for (T s: markedStates) {
			if (state.equals(s)) {
				return true;
			}
		}
		
		for (T s: nonMarkedStates) {
			if (state.equals(s)) {
				return false;
			}
		}
		
		throw new StateNotFoundException("The state: " + state.toString() + " could not be found.");
	}

}
