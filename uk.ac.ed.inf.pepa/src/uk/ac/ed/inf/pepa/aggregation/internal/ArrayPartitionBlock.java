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
import uk.ac.ed.inf.pepa.aggregation.StateIsMarkedException;
import uk.ac.ed.inf.pepa.aggregation.StateNotFoundException;
import uk.ac.ed.inf.pepa.model.Rate;
import uk.ac.ed.inf.pepa.model.RateMath;

/**
 * A partition refinement data structure implementing using an array and a mapping.
 * 
 * All the states of the block are stored in an array <code>states</code>.
 * When a state is added to the block it is appended to the array.
 * We also keep track of an index <code>markIndex</code> inside the array.
 * All states at positions <em>before</em> <code>markIndex</code> are marked
 * states. To mark a state we simply swap it with the state at position
 * <code>markIndex</code> and increase <code>markIndex</code> by <code>1</code>.
 * 
 * @author Giacomo Alzetta
 *
 */
public class ArrayPartitionBlock<T> implements PartitionBlock<T> {

	private ArrayList<T> states;
	private int markIndex = 0;
	
	private HashMap<T, Double> mapToValues;
	
	/**
	 * Create an empty partition block.
	 */
	public ArrayPartitionBlock() {
		states = new ArrayList<T>();
		mapToValues = new HashMap<>();
	}
	
	/**
	 * Create a partition block that contains the given states.
	 * 
	 * @param sts
	 */
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
	
	/**
	 * Iterate over all the states in the block.
	 * 
	 * Implementation detail: the states are not ordered but currently
	 * all marked states are returned before the non-marked states.
	 */
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
	public PartitionBlock<T> splitMarkedStates() {
		ArrayPartitionBlock<T> newBlock = new ArrayPartitionBlock<>(states.subList(0, markIndex));
		// TODO: optimization: if markIndex << size we can just swap
		//       the last markIndex states at the beginning, instead of moving
		// 	     all remaining states backwards.
		for (int i=markIndex; i < states.size(); i++) {
			states.set(i-markIndex, states.get(i));
		}
		
		states.trimToSize();
		return newBlock;
	}
	
	@Override
	public Iterator<PartitionBlock<T>> splitBlock() {
		ArrayList<Double> values = new ArrayList<>(mapToValues.values());
		double pmc = PartitioningUtils.pmc(values);
		HashMap<T, Double> mappingNotPmc = new HashMap<>(mapToValues);
		HashMap<T, Double> mappingOfPmc = PartitioningUtils.splitMapOnValue(mappingNotPmc, pmc);
		
		PartitionBlock<T> pmcBlock = new ArrayPartitionBlock<>();
		for (Map.Entry<T, Double> entry: mappingOfPmc.entrySet()) {
			pmcBlock.addState(entry.getKey());
		}
		
		ArrayList<Map.Entry<T, Double>> entries = new ArrayList<>(mappingNotPmc.entrySet());
		entries.sort(new Comparator<Map.Entry<T, Double>>() {
			
			@Override
			public int compare(Map.Entry<T, Double> e1, Map.Entry<T, Double> e2) {
				double v1 = e1.getValue();
				double v2 = e2.getValue();
				
				// TODO: check that this comparison is sound.
				if (v1 == v2) {
					return 0;
				} else {
					// in particular here
					return Math.min(v1, v2) == v1 ? -1 : 1;
				}
			}
		});
		
		HashMap<Double, PartitionBlock<T>> blocks = new HashMap<>();
		blocks.put(pmc, pmcBlock);
		
		for (Map.Entry<T, Double> entry : entries) {
			Double val = entry.getValue();
			if (!blocks.containsKey(val)) {
				blocks.put(val, new ArrayPartitionBlock<>());
			}
			blocks.get(val).addState(entry.getKey());
		}
		
		// TODO: We do not want to allow modifications...
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
	public void setValue(T state, double value) throws StateNotFoundException, StateIsMarkedException {
		checkStateExistNonMarked(state);
		
		mapToValues.put(state, value);
	}
	
	@Override
	public double getValue(T state) throws StateNotFoundException, StateIsMarkedException {
		Double value = mapToValues.get(state);
		if (value == null) {
			if (mapToValues.containsKey(state)) {
				throw new RuntimeException("Impossible happened: null value assigned to state.");
			}
			
			checkStateExistNonMarked(state);
			
		}
		
		return value;
	}
	
	@Override
	public int size() {
		return states.size();
	}
	

	/**
	 * Simple parameter checking function.
	 * 
	 * @param state
	 * @throws StateNotFoundException
	 * @throws StateIsMarkedException
	 */
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
