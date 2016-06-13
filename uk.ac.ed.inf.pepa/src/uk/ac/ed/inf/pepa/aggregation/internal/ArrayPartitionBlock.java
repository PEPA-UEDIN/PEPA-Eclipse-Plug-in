/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.ac.ed.inf.pepa.aggregation.PartitionBlock;
import uk.ac.ed.inf.pepa.aggregation.StateIsMarkedException;
import uk.ac.ed.inf.pepa.aggregation.StateNotFoundException;

/**
 * @author Giacomo Alzetta
 *
 */
public class ArrayPartitionBlock<T, V> implements PartitionBlock<T, V> {

	private ArrayList<T> states;
	private int markIndex = 0;
	
	
	public ArrayPartitionBlock() {
		states = new ArrayList<T>();
	}
	
	public ArrayPartitionBlock(List<T> sts) {
		states.addAll(sts);
	}
	
	@Override
	public void addState(T state) {
		states.add(state);
		
	}
	@Override
	public boolean isEmpty() {
		return states.isEmpty();
	}
	@Override
	public Iterator<T> getStates() {
		return new Iterator<T>() {
			private int i=0;
			
			public boolean hasNext() {
				return i < states.size();
			}
			
			public T next() {
				return states.get(i++);
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public Iterator<T> getMarkedStates() {
		return new Iterator<T>() {
			private int i=0;
			
			public boolean hasNext() {
				return i < markIndex;
			}
			
			public T next() {
				return states.get(i++);
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	@Override
	public PartitionBlock<T, V> splitMarkedStates() {
		ArrayPartitionBlock<T, V> newBlock = new ArrayPartitionBlock<>(states.subList(0, markIndex));
		ArrayList<T> newStates = new ArrayList<>(states.size() - markIndex);
		for (int i=markIndex; i < states.size(); i++) {
			newStates.set(i, states.get(i));
		}
		states = newStates;
		return newBlock;
	}
	
	@Override
	public Iterator<PartitionBlock<T, V>> splitBlock() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void markState(T state) throws StateNotFoundException {
		int i = states.indexOf(state);
		if (i < 0) {
			throw new StateNotFoundException("The state: " + state.toString() + " was not found.");
		}
		// We swap the first non-marked state with the state we want to mark,
		// and increase the markIndex.
		
		T firstNonMarkedState = states.get(markIndex);
		states.set(markIndex, state);
		states.set(i, firstNonMarkedState);
		++markIndex;
	}
	
	@Override
	public boolean isMarked(T state) throws StateNotFoundException {
		for (int i=0; i < states.size(); i++) {
			boolean found = state.equals(states.get(i));
			if (i < markIndex && found) {
				return true;
			} else if (found) {
				return false;
			}
		}
		throw new StateNotFoundException("The state: " + state.toString() + "could not be found.");
	}
	
	
	@Override
	public void setValue(T state, V value) throws StateNotFoundException, StateIsMarkedException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public V getValue(T state) throws StateNotFoundException, StateIsMarkedException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
