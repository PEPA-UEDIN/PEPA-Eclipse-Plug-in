/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation;

import uk.ac.ed.inf.pepa.IProgressMonitor;
import uk.ac.ed.inf.pepa.ctmc.derivation.DerivationException;
import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpace;
import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpaceBuilder;
import uk.ac.ed.inf.pepa.ctmc.derivation.MeasurementData;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.IStateExplorer;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.ISymbolGenerator;
import uk.ac.ed.inf.pepa.parsing.DefaultVisitor;
import uk.ac.ed.inf.pepa.parsing.ModelNode;
import uk.ac.ed.inf.pepa.parsing.ProcessNode;

/**
 * @author Giacomo Alzetta
 *
 */
public class AggregationStateSpaceBuilder implements IStateSpaceBuilder {
	
	private final ISymbolGenerator generator;
	private IStateExplorer explorer;
	
	private ProcessNode systemEquation;
	private boolean aggregateArrays;
	

	public AggregationStateSpaceBuilder(IStateExplorer explorer,
			ISymbolGenerator generator, ModelNode model,
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
		SystemEquationVisitor visitor = new SystemEquationVisitor();
		systemEquation.accept(visitor);
		final AggregatedModelComponent original = visitor.result;
		
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpaceBuilder#getMeasurementData()
	 */
	@Override
	public MeasurementData getMeasurementData() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	private class SystemEquationVisitor extends DefaultVisitor {
		
		
	}
	
}
