/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import uk.ac.ed.inf.pepa.ctmc.derivation.common.State;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.Transition;

/**
 * Note: it might be possible to re-use pepa.ctmt.derivation.common.Component
 * instead...
 * 
 * @author Giacomo Alzetta
 *
 */
public class Component {

	private ArrayList<State> states;
	
	/** 
	 * The offset at which the representation of this component starts
	 * in the state vector. 
	 * **/
	public int offset;
	
	/**
	 * The number of slots taken by this component. This is just 1 when the
	 * component is a sequential component.
	 */
	public int length;
	
	// TODO: transition map needed!
	/**
	 * All the outgoing transitions of this component.
	 */
	protected ArrayList<Transition> outgoingTransitions;
	
	/**
	 * The set of hidden actions.
	 */
	public BitSet hidingSet;
	
	/**
	 * Name of the component for debugging purposes.
	 */
	private String name = null;
	

	public Component() {
		outgoingTransitions = new ArrayList<>();
		hidingSet = new BitSet();
	}
	
	public Component(String name) {
		super();
		this.name = name;
	}
	
	
	public void addState(short[] state) {
		states.add(new State(state, Arrays.hashCode(state)));
	}
	
	public boolean containsState(short[] state) {
		return states.contains(new State(state, Arrays.hashCode(state)));
	}
	
	public short[] getState(int stateIndex) {
		return states.get(stateIndex).fState;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * @return the number of states in this component.
	 */
	public int size() {
		return states.size();
	}
}
