/**
 * 
 */
package uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.LabelledTransitionSystem;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.DoubleArray;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.IntegerArray;

/**
 * @author Giacomo Alzetta
 *
 */
public class ArraysLtsModel implements LabelledTransitionSystem<Integer> {
	
	
	private IntegerArray stateRow;
	private IntegerArray transitionColumn;
	private IntegerArray actionColumn;
	private IntegerArray preStateRow = null;
	private IntegerArray preImageColumn = null;
	private DoubleArray rates;
	private int numActionTypes;

	
	public ArraysLtsModel(int numActionTypes, IntegerArray row, IntegerArray column,
						  IntegerArray actions, DoubleArray rates) {
		stateRow = row;
		transitionColumn = column;
		actionColumn = actions;
		this.rates = rates;
		this.numActionTypes = numActionTypes;
		
		computePreImageColumn();
	}
	
	
	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			private int i=0;
			
			public boolean hasNext() {
				return i < stateRow.size();
			}
			
			public Integer next() {
				return i++;
			}
		};
	}

	@Override
	public int size() {
		return stateRow.size();
	}

	@Override
	public int numberOfActionTypes() {
		return numActionTypes;
	}

	@Override
	public void addState(Integer state) {
		// TODO Auto-generated method stub
		System.err.println("This is a static LTS");
	}

	@Override
	public void addTransition(Integer source, Integer target, double rate, short actionId) {
		// TODO Auto-generated method stub
		System.err.println("This is a static LTS 2");
	}

	@Override
	public Iterable<Short> getActions(Integer source, Integer target) {
		// TODO: check if the transitions are ordered by target,
		// if so this could be optimized quite a bit.
		ArrayList<Short> acts = new ArrayList<>();
		int startCol = stateRow.get(source);
		int endCol = (source == stateRow.size() - 1 ? transitionColumn.size() : stateRow.get(source+1));
		for (int i=startCol; i < endCol; i += 2) {
			int tState = transitionColumn.get(i);
			
			if (tState != target) continue;
			
			int startTrans = transitionColumn.get(i+1);
			int endTrans = i < transitionColumn.size() -3 ? transitionColumn.get(i+3) : actionColumn.size();
			//ArrayList<Short> acts = new ArrayList<>(endTrans - startTrans);
			for (int j=startTrans; j < endTrans; ++j) {
				acts.add((short) actionColumn.get(j));
			}
			
			//return acts;
		}
		
		// FIXME: maybe we should avoid this?
		HashSet<Short> actsUnique = new HashSet<>(acts);
		acts.clear();
		acts.addAll(actsUnique);
		return acts;
		//return new ArrayList<>(0);
	}

	@Override
	public double getApparentRate(Integer source, Integer target, short actionId) {
		// FIXME: we could pre-compute these sums.
		int aId = actionId;
		double rate = 0.0d;
		int startCol = stateRow.get(source);
		int endCol = (source == stateRow.size() - 1 ? transitionColumn.size() : stateRow.get(source+1));
		for (int i=startCol; i < endCol; i += 2) {
			int tState = transitionColumn.get(i);
			
			if (tState != target) continue;
			
			int startTrans = transitionColumn.get(i+1);
			int endTrans = i < transitionColumn.size() -3 ? transitionColumn.get(i+3) : actionColumn.size();
			for (int j=startTrans; j < endTrans; ++j) {
				if (actionColumn.get(j) == aId) {
					rate += rates.get(j);
				}
			}
		}
		
		return rate;
	}

	@Override
	public Iterable<Integer> getImage(Integer source) {
		HashSet<Integer> targets = new HashSet<>();
		int startCol = stateRow.get(source);
		int endCol = (source == stateRow.size() - 1 ? transitionColumn.size() : stateRow.get(source+1));
		for (int i=startCol; i < endCol; i += 2) {
			int tState = transitionColumn.get(i);
			
			targets.add(tState);
		}
		
		return targets;
	}

	@Override
	public Iterable<Integer> getPreImage(Integer target) {
		int rangeStart = preStateRow.get(target);
		int rangeEnd = target == preStateRow.size()-1 ? preImageColumn.size() : preStateRow.get(target);
		
		ArrayList<Integer> preIm = new ArrayList<>(rangeEnd-rangeStart);
		for (int i=rangeStart; i < rangeEnd; ++i) {
			preIm.add(preImageColumn.get(i));
		}
		
		return preIm;
 	}
	
	
	
	private void computePreImageColumn() {
		HashMap<Integer, HashSet<Integer>> pre = new HashMap<>(stateRow.size());
		
		for (int source=0; source < stateRow.size(); ++source) {
			int rangeStart = stateRow.get(source);
			int rangeEnd = source == stateRow.size() -1? transitionColumn.size(): stateRow.get(source+1);
			for (int t=rangeStart; t < rangeEnd; t+= 2) {
				HashSet<Integer> trans = pre.get(t);
				if (trans == null) {
					trans = new HashSet<>();
					pre.put(t, trans);
				}
				
				trans.add(source);
			}
		}

		int total = 0;
		for (int i=0; i < stateRow.size(); i++) {
			HashSet<Integer> ts = pre.get(i);
			int val = ts == null ? 0 : ts.size();
			total += val;
		}
		
		preStateRow = new IntegerArray(stateRow.size());
		preImageColumn = new IntegerArray(total);
		
		int curIndex = 0;
		for (int target=0; target < stateRow.size(); ++target) {
			HashSet<Integer> sources = pre.get(target);
			preStateRow.add(curIndex);
			if (sources == null) continue;
			for (int source: sources) {
				preImageColumn.add(source);
				++curIndex;
			}
		}
	}


	@Override
	public int numberOfTransitions() {
		return rates.size();
	}

}
