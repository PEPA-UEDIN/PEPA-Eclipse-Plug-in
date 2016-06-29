/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import uk.ac.ed.inf.pepa.DoNothingMonitor;
import uk.ac.ed.inf.pepa.IProgressMonitor;
import uk.ac.ed.inf.pepa.aggregation.internal.LtsModel;
import uk.ac.ed.inf.pepa.ctmc.derivation.DerivationException;
import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpace;
import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpaceBuilder;
import uk.ac.ed.inf.pepa.ctmc.derivation.MeasurementData;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.DoubleArray;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.IStateExplorer;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.ISymbolGenerator;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.IntegerArray;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.MemoryCallback;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.OptimisedHashMap;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.State;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.Transition;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.OptimisedHashMap.InsertionResult;
import uk.ac.ed.inf.pepa.model.NamedRate;
import uk.ac.ed.inf.pepa.model.RateMath;
import uk.ac.ed.inf.pepa.model.internal.NamedRateImpl;
import uk.ac.ed.inf.pepa.parsing.AggregationNode;
import uk.ac.ed.inf.pepa.parsing.ConstantProcessNode;
import uk.ac.ed.inf.pepa.parsing.CooperationNode;
import uk.ac.ed.inf.pepa.parsing.DefaultVisitor;
import uk.ac.ed.inf.pepa.parsing.HidingNode;
import uk.ac.ed.inf.pepa.parsing.ModelNode;
import uk.ac.ed.inf.pepa.parsing.PrefixNode;
import uk.ac.ed.inf.pepa.parsing.ProcessNode;
import uk.ac.ed.inf.pepa.parsing.RateDoubleNode;
import uk.ac.ed.inf.pepa.parsing.WildcardCooperationNode;

/**
 * Computes the aggregated state space.
 * 
 * Note: in order to use this builder you must first create a StateSpaceBuilder
 * and pass the explorer and symbol generator from that builder to this one.
 * @author Giacomo Alzetta
 *
 */
public class AggregationStateSpaceBuilder implements IStateSpaceBuilder {
	
	private final ISymbolGenerator generator;
	private IStateExplorer explorer;
	
	private ProcessNode systemEquation;
	private boolean aggregateArrays;
	
	private final static int REFRESH_MONITOR = 20000;
	

	public AggregationStateSpaceBuilder(
			IStateExplorer explorer,
			ISymbolGenerator generator,
			ModelNode model,
			boolean aggregateArrays) {
		this.explorer = explorer;
		this.generator = generator;
		this.systemEquation = model.getSystemEquation();
		this.aggregateArrays = aggregateArrays;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpaceBuilder#derive(boolean, uk.ac.ed.inf.pepa.IProgressMonitor)
	 */
	@Override
	public IStateSpace derive(boolean allowPassiveRates, IProgressMonitor monitor)
			throws DerivationException {
//		SystemEquationVisitor visitor = new SystemEquationVisitor();
//		systemEquation.accept(visitor);
//		LabelledTransitionSystem<State> original = visitor.result;
//		AggregationAlgorithm algorithm;
//		LabelledTransitionSystem<AggregatedState> lts = algorithm.aggregate(original);
//		return lts.toStateSpace();
		
		if (monitor == null) 
			monitor = new DoNothingMonitor();
		
		
		monitor.beginTask(IProgressMonitor.UNKNOWN);
		MemoryCallback callback = new MemoryCallback();
		ArrayList<State> states = new ArrayList<>(1000);
		
		OptimisedHashMap map = new OptimisedHashMap();
		Queue<State> queue = new LinkedList<State>();
		
		short[] initialState = generator.getInitialState();
		int hashCode = Arrays.hashCode(initialState);
		
		State initState = map.putIfNotPresentUnsync(initialState, hashCode).state;
		queue.add(initState);
		
		int numTransitions = 0;
		Transition[] found;
		
		while (!queue.isEmpty()) {
			if (monitor.isCanceled()) {
				monitor.done();
				return null;
			}
			
			State s = queue.remove();
			
			if (s.stateNumber % REFRESH_MONITOR == 0) {
				monitor.worked(REFRESH_MONITOR);
			}
			
			try {
				found = explorer.exploreState(s.fState);
				
			} catch (DerivationException e) {
				throw createException(s, e.getMessage());
			}
			
			if (found.length == 0) {
				monitor.done();
				throw createException(s, "Deadlock found.");
			}
			
			numTransitions += found.length;
			
			for (Transition t: found) {
				if (t.fRate <= 0) {
					throw createException(s,
							"Incomplete model with respect to action: "
									+ generator.getActionLabel(t.fActionId)
									+ ". ");
				}
				
				hashCode = Arrays.hashCode(t.fTargetProcess);
				// IMPORTANT hashCode is calculated externally and then
				// passed in to avoid calculating twice when a new state is
				// added
				//ticMap = System.nanoTime();
				InsertionResult result = map.putIfNotPresentUnsync(
						t.fTargetProcess,
						hashCode
				);
				//tocAdded += System.nanoTime() - ticMap;
				
				t.fState = result.state;
				
				if (!result.wasPresent) {
					queue.add(result.state);
					//result.state.stateNumber = rowNumber++;
				}
			}
			
			callback.foundDerivatives(s, found);
			states.add(s);
		}
		
		explorer.dispose();
		states.trimToSize();
		callback.done(generator, states);
		
		IntegerArray row = callback.getRow();
		IntegerArray col = callback.getColumn();
		DoubleArray rates = callback.getRates();
		IntegerArray actionIds = callback.getActions();
		
		LtsModel<Integer> lts = new LtsModel<>();
		int i = 0;
		for (State s: states) {
			lts.addState(s.stateNumber);
			
			int rangeStart = row.get(i);
			int rangeEnd = (i == row.size()-1 ? col.size() : row.get(i+1));
			for (int j=rangeStart; j < rangeEnd; j += 2) {
				int colVal = col.get(j);
				int colRangeStart = col.get(j+1);
				int colRangeEnd  = (j < col.size() - 3 ? col.get(j+3): rates.size());
				
				// add transitions by actions.
				for (int k=colRangeStart; k < colRangeEnd; k++) {
					double rate = rates.get(k);
					short actionId = (short)actionIds.get(k);
					// FIXME: the lts shouldn't use strings as labels,
					// but the shorts.
					lts.addTransition(s.stateNumber, colVal, rate, actionId);
				}
			}
			
			
			++i;
		}
		
		// Build the LTS here
		
		// Aggregate the LTS here
		
		// Derive the CTMC here
		IStateSpace result = null;
		monitor.done();
		
		return result;
	}
	
	private DerivationException createException(State state, String message) {
		StringBuilder buf = new StringBuilder();
		buf.append(message);
		buf.append(" State number: ");
		buf.append(String.valueOf(state.stateNumber));
		buf.append(". ");
		buf.append("State: ");
		for (int i = 0; i < state.fState.length; i++) {
			buf.append(generator.getProcessLabel(state.fState[i]));
			if (i != state.fState.length - 1)
				buf.append(",");
		}
		return new DerivationException(buf.toString());
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpaceBuilder#getMeasurementData()
	 */
	@Override
	public MeasurementData getMeasurementData() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public Component constructModelComponent(int componentId, short localInitialState[]) {
		Component comp = new Component();
		comp.offset = componentId;
		Queue<short[]> queue = new LinkedList<short[]>();
		queue.add(localInitialState);
		comp.addState(new short[] {localInitialState[0]});
		
		while (!queue.isEmpty()) {
			short[] state = queue.remove();
			ArrayList<Transition> found = getTransitions(state[0]);
			for (Transition t: found) {
				short s[] = new short[1];
				s[0] = t.fTargetProcess[componentId];
				if (!comp.containsState(s)) {
					comp.addState(s);
					queue.add(s);
				}
			}
		}
		
		// comp.buildTransitions();  // possible optimization.
		for (int i = 0; i < comp.size(); i++) {
			short state[] = comp.getState(i);
			ArrayList<Transition> found = getTransitions(state[0]);

			found.sort(new Comparator<Transition>() {

				@Override
				public int compare(Transition o1, Transition o2) {
					return o1.compareTo(o2);
				}
				
			});
			//Collections.sort(found);
			
			// The transitions found by the StateExplorer could be multiple
			// in our LTS we would to sum rates from the same pair of
			// states and same label. This is what we do here.
			ArrayList<Transition> grouped = new ArrayList<>(found.size());
			
			short[] curTarget = null;
			double curRate = 0.0d;
			short curActionId = -1;
			for (Transition t : found) {
				if (curTarget == null) {
					curTarget = t.fTargetProcess;
					curActionId = t.fActionId;
					curRate = t.fRate;
				} else if (Arrays.equals(curTarget, t.fTargetProcess)) {
					if (curActionId == t.fActionId) {
						curRate += t.fRate;
					} else {
						Transition newT = new Transition();
						newT.fTargetProcess = Arrays.copyOf(curTarget, curTarget.length);
						newT.fActionId = curActionId;
						newT.fRate = curRate;
						grouped.add(newT);
						
						curActionId = t.fActionId;
						curRate = t.fRate;
					}
				}
			}
			Transition newT = new Transition();
			newT.fTargetProcess = Arrays.copyOf(curTarget, curTarget.length);
			newT.fActionId = curActionId;
			newT.fRate = curRate;
			grouped.add(newT);
			
			grouped.trimToSize();
			
			for (Transition t : grouped) {
				short target[] = new short[1];
				target[0] = t.fTargetProcess[componentId];
				comp.addTransition(state, target, t.fActionId, t.fRate);
			}
		}
		
		return comp;
	}
	
	private ArrayList<Transition> getTransitions(short processId) {
		return explorer.getData(processId).fFirstStepDerivative;
	}
	
	private class SystemEquationVisitor extends DefaultVisitor {
		
		int stateSize;
		int multiplicity;
		Component resultComponent;
		
		SystemEquationVisitor() {
			stateSize = 0;
		}
		
		@Override
		public void visitCooperationNode(CooperationNode cooperation) {
			cooperation.getLeft().accept(this);
			AggregatedModelComponent comp = resultComponent;
			cooperation.getRight().accept(this);
			resultComponent = null;
		}
		
		@Override
		public void visitHidingNode(HidingNode hiding) {
			hiding.getProcess().accept(this);
		}
		
		@Override
		public void visitWildcardCooperationNode(WildcardCooperationNode wildcard) {
			wildcard.getLeft().accept(this);
			AggregatedModelComponent comp = resultComponent;
			wildcard.getRight().accept(this);
			// create the cooperation component from comp and resultComponent.
			resultComponent = null;
		}
		
		@Override
		public void visitAggregationNode(AggregationNode aggregation) {
			// Aggregated arrays get de-sugared into wildcard cooperations
			ProcessNode seq = aggregation.getProcessNode();
			aggregation.getCopies().accept(this);
			int copies = multiplicity;
			
			if (multiplicity == 1) {
				seq.accept(this);
			} else {
				WildcardCooperationNode coop = new WildcardCooperationNode();
				WildcardCooperationNode c = coop;
				WildcardCooperationNode c2;
				while (copies > 2) {
					c2 = new WildcardCooperationNode();
					c.setLeft(seq);
					c.setRight(c2);
					c = c2;
					copies--;
				}
				c.setLeft(seq);
				c.setRight(seq);
				coop.accept(this);
			}
		}
		
		@Override
		public void visitConstantProcessNode(ConstantProcessNode constant) {
			short[] initialState = generator.getInitialState();
			int componentId = stateSize++;
			short localInitialState[] = Arrays.copyOfRange(
					initialState, componentId, componentId + 1);
			resultComponent = constructModelComponent(
					componentId, localInitialState);
		}
		
		
		/**
		 * Used to compute the number of copies of Aggregated Arrays.
		 */
		@Override
		public void visitRateDoubleNode(RateDoubleNode node) {
			multiplicity = (int) node.getValue();
		}
	}
	
}
