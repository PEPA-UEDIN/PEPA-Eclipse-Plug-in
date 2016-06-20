/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation.internal;

import uk.ac.ed.inf.pepa.DoNothingMonitor;
import uk.ac.ed.inf.pepa.IProgressMonitor;
import uk.ac.ed.inf.pepa.IResourceManager;
import uk.ac.ed.inf.pepa.aggregation.ModelAggregator;
import uk.ac.ed.inf.pepa.aggregation.PartitionBlock;
import uk.ac.ed.inf.pepa.ctmc.derivation.DerivationException;
import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpace;
import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpaceBuilder;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.StateSpaceBuilderFactory;
import uk.ac.ed.inf.pepa.ctmc.solution.OptionMap;
import uk.ac.ed.inf.pepa.model.Model;

/**
 * @author Giacomo Alzetta
 *
 */
public class ModelAggregatorImpl<P extends PartitionBlock<?, ?>> implements ModelAggregator {

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.aggregation.ModelAggregator#aggregate(uk.ac.ed.inf.pepa.model.Model)
	 */
	@Override
	public IStateSpace aggregate(Model model) {
		OptionMap map = new OptionMap();
		IResourceManager manager = IResourceManager.TEMP;
		
		IStateSpaceBuilder builder = StateSpaceBuilderFactory.createStateSpaceBuilder(
				model.getASTModel(), 
				map, 
				manager
		);
		
		IProgressMonitor monitor = new DoNothingMonitor();
		try {
			IStateSpace res = builder.derive(false, monitor);
			return res;
		} catch (DerivationException e) {
			e.printStackTrace();
			
			return null;
		}
	}

}
