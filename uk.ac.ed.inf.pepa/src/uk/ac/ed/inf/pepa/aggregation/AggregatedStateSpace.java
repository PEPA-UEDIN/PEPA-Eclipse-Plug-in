/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation;

import java.util.ArrayList;

import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import uk.ac.ed.inf.pepa.IProgressMonitor;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.ISymbolGenerator;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.State;
import uk.ac.ed.inf.pepa.ctmc.derivation.internal.hbf.AbstractStateSpace;
import uk.ac.ed.inf.pepa.ctmc.solution.internal.simple.Generator;

/**
 * @author Giacomo Alzetta
 *
 */
public class AggregatedStateSpace extends AbstractStateSpace {

	private LabelledTransitionSystem<AggregatedState> ltsModel;
	
	public AggregatedStateSpace(
			ISymbolGenerator generator,
			ArrayList<State> states,
			LabelledTransitionSystem<AggregatedState> ltsModel) {
		super(generator, states, false, generator.getInitialState().length);
		
		this.ltsModel = ltsModel;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpace#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.ctmc.derivation.internal.hbf.AbstractStateSpace#createGeneratorMatrix()
	 */
	@Override
	protected FlexCompRowMatrix createGeneratorMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.ctmc.derivation.internal.hbf.AbstractStateSpace#createSimpleGenerator()
	 */
	@Override
	protected Generator createSimpleGenerator() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.ctmc.derivation.internal.hbf.AbstractStateSpace#doThroughput(uk.ac.ed.inf.pepa.IProgressMonitor)
	 */
	@Override
	protected void doThroughput(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.ctmc.derivation.internal.hbf.AbstractStateSpace#getAction(int, int)
	 */
	@Override
	public String[] getAction(int source, int target) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.ctmc.derivation.internal.hbf.AbstractStateSpace#getIncomingStateIndices(int)
	 */
	@Override
	public int[] getIncomingStateIndices(int stateIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.ctmc.derivation.internal.hbf.AbstractStateSpace#getOutgoingStateIndices(int)
	 */
	@Override
	public int[] getOutgoingStateIndices(int stateIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.ctmc.derivation.internal.hbf.AbstractStateSpace#getRate(int, int)
	 */
	@Override
	public double getRate(int source, int target) {
		// TODO Auto-generated method stub
		return 0;
	}

}
