/**
 * 
 */
package uk.ac.ed.inf.pepa.eclipse.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import uk.ac.ed.inf.pepa.ctmc.solution.OptionMap;
import uk.ac.ed.inf.pepa.eclipse.core.IOptionHandler;

/**
 * @author Giacomo Alzetta
 *
 */
public class AggregationWizard extends Wizard {

	private final static String SETTINGS_PAGE_NAME = "settings_page!";
	
	SettingsPage setPage = null;


	private OptionMap options;
	private IOptionHandler handler;
	
	public AggregationWizard(IOptionHandler handler) {
		super();
		this.options = handler.getOptionMap();
		this.handler = handler;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		if (setPage.isPageComplete()) {
			setPage.applySettings();
		}
		
		handler.setOptionMap(options);
		
		return true;
	}
	
	@Override
	public void addPages() {
		setPage = new SettingsPage(SETTINGS_PAGE_NAME);
		addPage(setPage);
	}

	/**
	 * @author Giacomo Alzetta
	 *
	 */
    private class SettingsPage extends WizardPage {
    	
    	// private static final String SECTION_NAME = "aggregation.settings";
    	
    	private Button aggregationEnabled;
    	
    	// private static final String AGGREGATION_ENABLED_BUTTON = "aggregation.settings.enabled";
    	
    	private Button aggregateArrays;
    	
    	// private static final String AGGREGATE_ARRAYS = "aggregation.settings.aggregate_arrays";
    	
    	private Combo aggregationAlgorithm;
    	
    	// private static final String AGGREGATION_ALGORITHM = "aggregation.settings.algorithm";
    	
    	private static final String AGGREGATION_NONE = "No aggregation";
    	private static final String AGGREGATION_CONTEXTUAL_LUMPABILITY = "Contextual Lumpability";
    	private static final String AGGREGATION_EXACT_EQUIVALENCE = "Exact Equivalence";
    	
    	private int aggregationChosen = OptionMap.AGGREGATION_NONE;

    	protected SettingsPage(String pageName) {
			super(pageName);
			this.setTitle("Aggregation Settings");
			this.setDescription("Choose aggregation options.");
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		public void createControl(Composite parent) {			
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(1, true));
			setControl(composite);
			
			aggregationEnabled = new Button(composite, SWT.CHECK);
			aggregationEnabled.setText("Enable aggregation");
			aggregationEnabled.setLayoutData(createDefaultGridData());
			aggregationEnabled.addListener(SWT.Selection,
					new Listener() {
						public void handleEvent(Event event) {
							boolean enabled = aggregationEnabled.getSelection();
							aggregateArrays.setEnabled(enabled);
							aggregationAlgorithm.setEnabled(enabled);
							validate();
						}
			});
			
			aggregateArrays = new Button(composite, SWT.CHECK);
			aggregateArrays.setText("Use aggregate arrays");
			aggregateArrays.setLayoutData(createDefaultGridData());
			aggregateArrays.addListener(SWT.Selection, 
					new Listener() {
						public void handleEvent(Event event) {
							validate();
						}
			});
			
			aggregationAlgorithm = new Combo(composite, SWT.READ_ONLY);
			aggregationAlgorithm.setLayoutData(createDefaultGridData());
			
			String[] items = {
					AGGREGATION_NONE,
					AGGREGATION_CONTEXTUAL_LUMPABILITY,
					AGGREGATION_EXACT_EQUIVALENCE
			};
			aggregationAlgorithm.setItems(items);
			aggregationAlgorithm.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String text = aggregationAlgorithm.getText();
					if (text == null || text.equals(AGGREGATION_NONE)) {
						aggregationChosen = OptionMap.AGGREGATION_NONE;
					} else if (text.equals(AGGREGATION_CONTEXTUAL_LUMPABILITY)) {
						aggregationChosen = OptionMap.AGGREGATION_CONTEXTUAL_LUMPABILITY;
					} else if (text.equals(AGGREGATION_EXACT_EQUIVALENCE)) {
						aggregationChosen = OptionMap.AGGREGATION_EXACT_EQUIVALENCE;
					}
				}
			});
			
			initContents();
			validate();
		}
		
		private void initContents() {
			/* Should init from default settings */

			boolean enabled = (boolean)options.get(OptionMap.AGGREGATION_ENABLED);
			aggregationEnabled.setSelection(enabled);
			
			aggregateArrays.setSelection(
					(boolean)options.get(OptionMap.AGGREGATE_ARRAYS));
			aggregateArrays.setEnabled(enabled);
			
			int algIndex = (int)options.get(OptionMap.AGGREGATION);
			aggregationAlgorithm.select(algIndex);
			aggregationAlgorithm.setEnabled(enabled);
		}
		
		private boolean isAggregationEnabled() {
			return aggregationEnabled.getSelection();
		}
		
		private boolean areAggregateArraysEnabled() {
			return aggregateArrays.getSelection();
		}
		
		protected void applySettings() {
			
			options.put(OptionMap.AGGREGATION_ENABLED, isAggregationEnabled());
			options.put(OptionMap.AGGREGATION, aggregationChosen);
			options.put(OptionMap.AGGREGATE_ARRAYS, areAggregateArraysEnabled());
		}
		
		private GridData createDefaultGridData() {
			/* ...with grabbing horizontal space */
			return new GridData(SWT.FILL, SWT.CENTER, true, false);
		}
	
		public void validate() {
			// If we will ever need to add error validation,
			// do so here.
			
			setPageComplete(true);
		}
    }

}
