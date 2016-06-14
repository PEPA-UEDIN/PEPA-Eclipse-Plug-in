/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation.internal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.ed.inf.pepa.aggregation.PartitionBlock;
import uk.ac.ed.inf.pepa.aggregation.PartitioningUtils;
import uk.ac.ed.inf.pepa.aggregation.StateIsMarkedException;
import uk.ac.ed.inf.pepa.aggregation.StateNotFoundException;
import uk.ac.ed.inf.pepa.model.Rate;
import uk.ac.ed.inf.pepa.model.RateMath;

/**
 * @author Giacomo Alzetta
 *
 */
public class ArrayPartitionBlock<T, V extends Rate> implements PartitionBlock<T, V> {

	private ArrayList<T> states;
	private int markIndex = 0;
	
	private HashMap<T, V> mapToValues;
	
	
	public ArrayPartitionBlock() {
		states = new ArrayList<T>();
		mapToValues = new HashMap<>();
	}
	
	public ArrayPartitionBlock(List<T> sts) {
		states.addAll(sts);
		mapToValues = new HashMap<>();
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
		ArrayList<V> values = new ArrayList<>(mapToValues.values());
		V pmc = PartitioningUtils.pmc(values);
		HashMap<T, V> mappingNotPmc = new HashMap<>(mapToValues);
		HashMap<T, V> mappingOfPmc = PartitioningUtils.splitMapOnValue(mappingNotPmc, pmc);
		
		PartitionBlock<T, V> pmcBlock = new ArrayPartitionBlock<>();
		for (Map.Entry<T, V> entry: mappingOfPmc.entrySet()) {
			pmcBlock.addState(entry.getKey());
		}
		
		ArrayList<Map.Entry<T, V>> entries = new ArrayList<>(mappingNotPmc.entrySet());
		entries.sort(new Comparator<Map.Entry<T, V>>() {
			
			@Override
			public int compare(Map.Entry<T, V> e1, Map.Entry<T, V> e2) {
				V v1 = e1.getValue();
				V v2 = e2.getValue();
				
				// TODO: check that this comparison is sound.
				if (e1.getValue().equals(e2.getValue())) {
					return 0;
				} else {
					return RateMath.min(v1, v2).equals(v1) ? -1 : 1;
				}
			}
		});
		
		HashMap<V, PartitionBlock<T, V>> blocks = new HashMap<>();
		blocks.put(pmc, pmcBlock);
		
		for (Map.Entry<T, V> entry : entries) {
			V val = entry.getValue();
			if (!blocks.containsKey(val)) {
				blocks.put(val, new ArrayPartitionBlock<>());
			}
			blocks.get(val).addState(entry.getKey());
		}
		
		return blocks.values().iterator();
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
		if (value == null) {
			throw new NullPointerException("Cannot assign a null value!");
		}
		
		checkStateExistNonMarked(state);
		
		mapToValues.put(state, value);
	}
	@Override
	public V getValue(T state) throws StateNotFoundException, StateIsMarkedException {
		V value = mapToValues.get(state);
		if (value == null) {
			if (mapToValues.containsKey(state)) {
				throw new RuntimeException("Impossible happened: null value assigned to state.");
			}
			
			checkStateExistNonMarked(state);
			
		}
		
		return value;
	}
	

	private void checkStateExistNonMarked(T state)
			throws StateNotFoundException, StateIsMarkedException {
		int i = states.indexOf(state);
		if (i < 0) {
			throw new StateNotFoundException("The state: " + state.toString() + " could not be found.");
		} else if (i < markIndex) {
			throw new StateIsMarkedException("The state: " + state.toString() + " is marked.");
		}
	}
}
