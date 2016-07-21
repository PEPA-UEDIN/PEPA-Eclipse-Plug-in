package uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.LabelledTransitionSystem;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.ISymbolGenerator;

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
	
	private LtsModel(LtsModel<S> lts) {
		this.numActionIds = lts.numActionIds;
		
		this.states = lts.states;
		
		preImageMap = new HashMap<>();
		transitionMap = new HashMap<>();
		
		for (Map.Entry<S, ArrayList<S>> entry: lts.preImageMap.entrySet()) {
			S state = entry.getKey();
			ArrayList<S> val = new ArrayList<S>(entry.getValue());
			if (!val.contains(state)) val.add(state);
			preImageMap.put(state, val);
		}
		
		for (S state: states) {
			HashMap<S, double[]> map = new HashMap<>(lts.transitionMap.get(state));
			double rate = 0.0d;
			for (S t: map.keySet()) {
				if (!t.equals(state)) {
					rate += map.get(t)[0];
				}
			}
			
			double[] rates = map.get(state);
			if (rates == null) {
				rates = new double[numActionIds];
				map.put(state, rates);
			}
			rates[0] = -rate;
			transitionMap.put(state, map);
		}
	}
	
	@Override
	public int size() {
		return states.size();
	}

	@Override
	public double getApparentRate(S source, S target, short actionId) {
		if (actionId == ISymbolGenerator.TAU_ACTION && source.equals(target)) {
			System.err.println("found tau action! (self loop)");
		}
		/*
		if (source.equals(target) && actionId == ISymbolGenerator.TAU_ACTION) {
			// FIXME: is this needed? Note that actionId = -1 ...
			double result = 0.0d;
			for (S t: getImage(source)) {
				if (source.equals(t)) continue;
				double[] rates = transitionMap.get(source).get(t);
				if (rates != null) result += rates[actionId];
			}
			return -result;
		} */

		// FIXME: this may throw a NPE if the LTS is built incorrectly...
		double[] rates = transitionMap.get(source).get(target);
		if (rates == null) return 0.0d;
		return rates[actionId];
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
	public Iterable<Short> getActions(S source, S target) {
		HashMap<S, double[]> acts = transitionMap.get(source);
		assert acts != null;
		
		double[] actionTypes = acts.get(target);
		ArrayList<Short> actTypes = new ArrayList<>();
		if (actionTypes == null) {
			assert false :  "found null action types!";
			return actTypes;
		}
		
		for (short i=0; i < actionTypes.length; ++i) {
			if (actionTypes[i] != 0.0d) {
				actTypes.add(i);
			}
		}
		return actTypes;
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
	
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LTS:\n");
		builder.append("States are:\n");
		for (S state: this) {
			builder.append("State: " + state + "\n");
			
		}
		for (S source: this) {
			builder.append("State: " + source + " to:\n");
			builder.append("Transitions are:\n");
			for (S target: getImage(source)) {
				builder.append("\tTarget " + target + "\n");
				for (short action: getActions(source, target)) {
					builder.append("\t\tLabel " + action + " rate "
								   + getApparentRate(source, target, action) + "\n");
				}
		    }
		}
		
		return builder.toString();
		
	}

	@Override
	public LabelledTransitionSystem<S> variantView() {
		return new VariantView<>(this);
	}
	
}
