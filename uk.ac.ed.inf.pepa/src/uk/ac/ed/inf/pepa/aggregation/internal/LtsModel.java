package uk.ac.ed.inf.pepa.aggregation.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;

import uk.ac.ed.inf.pepa.aggregation.LabelledTransitionSystem;
import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpace;
import uk.ac.ed.inf.pepa.model.Action;
import uk.ac.ed.inf.pepa.model.Activity;
import uk.ac.ed.inf.pepa.model.Model;
import uk.ac.ed.inf.pepa.model.NamedAction;
import uk.ac.ed.inf.pepa.model.NamedRate;
import uk.ac.ed.inf.pepa.model.internal.ActivityImpl;
import uk.ac.ed.inf.pepa.model.internal.NamedActionImpl;
import uk.ac.ed.inf.pepa.model.FiniteRate;

public class LtsModel<S> implements LabelledTransitionSystem<S> {

	private ArrayList<S> states;
	private HashMap<S, HashMap<S, HashMap<Short, Double>>> transitionMap;
	private HashMap<S, ArrayList<S>> preImageMap;
	
	public LtsModel() {
		states = new ArrayList<>();
		preImageMap = new HashMap<>();
		transitionMap = new HashMap<>();
	}
	
	@Override
	public boolean isValid() {
		return false;
	}
	
	@Override
	public int size() {
		return states.size();
	}

	@Override
	public double getApparentRate(S source, S target, short actionId) {
		return transitionMap.get(source).get(target).get(actionId);
	}

	@Override
	public List<S> getImage(S source) {
		HashMap<S, HashMap<Short, Double>> targetsMap = transitionMap.get(source);
		ArrayList<S> targets = new ArrayList<>(targetsMap.size());
		for (S key: targetsMap.keySet()) {
			targets.add(key);
		}
		return targets;
		
	}

	@Override
	public List<S> getPreImage(S target) {
		return preImageMap.get(target);
	}
	
	@Override
	public Set<Short> getActions(S source, S target) {
		HashMap<Short, Double> map = get(source, target);
		return map.keySet();
	}

	@Override
	public boolean addState(S state) {
		boolean wasPresent =  states.add(state);
		transitionMap.put(state, new HashMap<>());
		preImageMap.put(state, new ArrayList<>());
		
		return wasPresent;
	}

	@Override
	public boolean addTransition(S source, S target, double rate, short actionId) {
		HashMap<Short,Double> targetMap = get(source, target);
		return targetMap.put(actionId, rate) == null;
	}
	
	@Override
	public Iterator<S> iterator() {
		return states.iterator();
	}
	
	
	private HashMap<Short, Double>get(S source, S target) {
		HashMap<S, HashMap<Short,Double>> targetsMap = transitionMap.get(source);
		// targetsMap cannot be null. If it is then source is not in the LTS.
		assert targetsMap != null;
		
		HashMap<Short, Double> map = targetsMap.get(target);
		if (map == null) {
			map = new HashMap<>();
			targetsMap.put(target, map);
		}
		
		return map;
	}

	@Override
	public int numberOfTransitions() {
		int total = 0;
		for (HashMap<S, HashMap<Short, Double>> trans : transitionMap.values()) {
			for (HashMap<Short, Double> m : trans.values()) {
				total += m.size();
			}
		}
		
		return total;
	}
}
