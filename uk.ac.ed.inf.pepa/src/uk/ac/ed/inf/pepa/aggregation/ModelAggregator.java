/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation;

import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpace;
import uk.ac.ed.inf.pepa.model.Model;

/**
 * @author Giacomo Alzetta
 *
 */
public interface ModelAggregator {

	public IStateSpace aggregate(Model model);
}
