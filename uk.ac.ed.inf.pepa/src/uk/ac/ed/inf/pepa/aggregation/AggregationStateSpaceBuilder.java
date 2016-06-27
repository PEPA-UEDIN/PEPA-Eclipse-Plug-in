/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import uk.ac.ed.inf.pepa.IProgressMonitor;
import uk.ac.ed.inf.pepa.aggregation.internal.LtsModel;
import uk.ac.ed.inf.pepa.ctmc.derivation.DerivationException;
import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpace;
import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpaceBuilder;
import uk.ac.ed.inf.pepa.ctmc.derivation.MeasurementData;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.IStateExplorer;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.ISymbolGenerator;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.State;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.Transition;
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
 * @author Giacomo Alzetta
 *
 */
public class AggregationStateSpaceBuilder implements IStateSpaceBuilder {
	
	private final ISymbolGenerator generator;
	
	private ProcessNode systemEquation;
	private boolean aggregateArrays;
	

	public AggregationStateSpaceBuilder(
			ISymbolGenerator generator,
			ModelNode model,
			boolean aggregateArrays) {
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
		SystemEquationVisitor visitor = new SystemEquationVisitor();
		systemEquation.accept(visitor);
		LabelledTransitionSystem<State> original = visitor.result;
		AggregationAlgorithm algorithm;
		LabelledTransitionSystem<AggregatedState> lts = algorithm.aggregate(original);
		return lts.toStateSpace();
		
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
		
		comp.buildTransitions();
		for (int i = 0; i < comp.size(); i++) {
			short state[] = comp.getState(i);
			ArrayList<Transition> found = getTransitions(state[0]);

			for (Transition t : found) {
				short target[] = new short[1];
				target[0] = t.fTargetProcess[componentId];
				comp.addTransition(state, target, t.fRate, t.fActionId);
			}
		}
		
		return comp;
	}
	
	private ArrayList<Transition> getTransitions(short processId) {
		
		return new ArrayList<>();
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
			// Aggregated arrays get desugared into wildcard cooperations
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
			int componentId = stateSize;
			short localInitialState[] = Arrays.copyOfRange(
					initialState, componentId, componentId + 1);
			resultComponent = constructModelComponent(
					componentId, localInitialState);
			
			stateSize++;
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
