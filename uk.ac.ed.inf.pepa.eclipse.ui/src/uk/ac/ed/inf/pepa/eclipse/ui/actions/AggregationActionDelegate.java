/**
 * 
 */
package uk.ac.ed.inf.pepa.eclipse.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorActionDelegate;

/**
 * @author Giacomo Alzetta
 *
 */
public class AggregationActionDelegate
	extends BasicProcessAlgebraModelActionDelegate
	implements IEditorActionDelegate {

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.eclipse.ui.actions.BasicProcessAlgebraModelActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		ActionCommands.aggregation(model);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.pepa.eclipse.ui.actions.BasicProcessAlgebraModelActionDelegate#checkStatus()
	 */
	@Override
	protected void checkStatus() {
		if (this.action != null) {
			action.setEnabled(model.isDerivable());
		}
	}

}
