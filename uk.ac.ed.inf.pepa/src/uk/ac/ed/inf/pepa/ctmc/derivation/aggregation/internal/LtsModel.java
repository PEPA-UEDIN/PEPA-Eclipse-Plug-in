package uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.LabelledTransitionSystem;

public class LtsModel<S> implements LabelledTransitionSystem<S> {

	private ArrayList<S> states;
	private HashMap<S, HashMap<S, double[]>> transitionMap;
	private HashMap<S, ArrayList<S>> preImageMap;
	
	private int numActionIds;
	
	public LtsModel(int numActionIds) {
		this.numActionIds = numActionIds;
		states = new ArrayList<>();
		preImageMap = new HashMap<>();
		transitionMap = new HashMap<>();
	}
	
	@Override
	public int size() {
		return states.size();
	}

	@Override
	public double getApparentRate(S source, S target, short actionId) {
		// FIXME: this may throw a NPE if the LTS is built incorrectly...
		return transitionMap.get(source).get(target)[actionId];
	}

	@Override
	public Iterable<S> getImage(S source) {
		HashMap<S, double[]> targetsMap = transitionMap.get(source);
		return new ArrayList<S>(targetsMap.keySet());
		
	}

	@Override
	public Iterable<S> getPreImage(S target) {
		return preImageMap.get(target);
	}
	
	@Override
	public Iterator<Short> getActions(S source, S target) {
		HashMap<S, double[]> acts = transitionMap.get(source);
		assert acts != null;
		
		double[] actionTypes = acts.get(target);
		if (actionTypes == null) {
			return new Iterator<Short>() {
				public boolean hasNext() { return false; }
				public Short next() { return 0; }
		    	};
		} else {
			return new Iterator<Short>(){
				private final double[] map = actionTypes;
				private short i=0;
				
				public boolean hasNext() {
					if (map == null || i >= map.length) {
						return false;
					}
					for (; i < map.length; ++i) {
						if (map[i] != 0.0d) {
							return true;
						}
					}
					return false;
				}
				
				public Short next() {
					return i++;
				}
			};
		}
	}

	@Override
	public void addState(S state) {
		states.add(state);
		transitionMap.put(state, new HashMap<>());
		preImageMap.put(state, new ArrayList<>());
	}

	@Override
	public void addTransition(S source, S target, double rate, short actionId) {
		double[] targetMap = get(source, target);
		targetMap[actionId] = rate;
		ArrayList<S> preImTarget = preImageMap.get(target);
		if (preImTarget == null) {
			preImTarget = new ArrayList<>();
			preImageMap.put(target, preImTarget);
		}
		preImTarget.add(source);
	}
	
	@Override
	public Iterator<S> iterator() {
		return states.iterator();
	}
	
	
	private double[] get(S source, S target) {
		HashMap<S, double[]> targetsMap = transitionMap.get(source);
		// targetsMap cannot be null. If it is then source is not in the LTS.
		assert targetsMap != null;
		
		double[] map = targetsMap.get(target);
		if (map == null) {
			map = new double[numActionIds];
			//Arrays.fill(map, 0.0d);
			targetsMap.put(target, map);
		}
		
		return map;
	}

	@Override
	public int numberOfTransitions() {
		int total = 0;
		for (HashMap<S, double[]> trans : transitionMap.values()) {
			for (double[] m : trans.values()) {
				for (double d: m) {
					if (d != 0.0d) {
						total += 1;
					}
				}
			}
		}
		
		return total;
	}
	
	@Override
	public int numberOfActionTypes() {
		return numActionIds;
	}
}
