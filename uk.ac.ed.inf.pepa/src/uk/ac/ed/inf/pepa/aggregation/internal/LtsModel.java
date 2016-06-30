package uk.ac.ed.inf.pepa.aggregation.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

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
	public boolean addState(S state) {
		boolean wasPresent =  states.add(state);
		transitionMap.put(state, new HashMap<>());
		preImageMap.put(state, new ArrayList<>());
		
		return wasPresent;
	}

	@Override
	public boolean addTransition(S source, S target, double rate, short actionId) {
		HashMap<S, HashMap<Short,Double>> map = transitionMap.get(source);
		if (map == null) {
			map = new HashMap<>();
			transitionMap.put(source, map);
		}
		HashMap<Short,Double> targetMap = map.get(target);
		if (targetMap == null) {
			targetMap = new HashMap<>();
			map.put(target, targetMap);
		}
		return targetMap.put(actionId, rate) == null;
	}
}
