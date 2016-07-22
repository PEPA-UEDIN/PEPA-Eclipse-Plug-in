/**
 * 
 */
package uk.ac.ed.inf.pepa.eclipse.ui.wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import uk.ac.ed.inf.pepa.ctmc.solution.OptionMap;
import uk.ac.ed.inf.pepa.eclipse.core.IOptionHandler;
import uk.ac.ed.inf.pepa.eclipse.ui.Activator;

/**
 * @author Giacomo Alzetta
 *
 */
public class AggregationWizard extends Wizard {

	private final static String SETTINGS_PAGE_NAME = "settings_page!";


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
		SettingsPage page = (SettingsPage) getPage(SETTINGS_PAGE_NAME);
		if (page.isPageComplete()) {
			page.applySettings();
		}
		
		handler.setOptionMap(options);
		
		return true;
	}
	
	@Override
	public void addPages() {
		SettingsPage page = new SettingsPage(SETTINGS_PAGE_NAME);
		addPage(page);
	}

	/**
	 * @author Giacomo Alzetta
	 *
	 */
    private class SettingsPage extends WizardPage {
    	
    	private static final String SECTION_NAME = "aggregation.settings";
    	
    	private Button aggregationEnabled;
    	
    	private static final String AGGREGATION_ENABLED_BUTTON = "aggregation.settings.enabled";
    	
    	private Button aggregateArrays;
    	
    	private static final String AGGREGATE_ARRAYS = "aggregation.settings.aggregate_arrays";
    	
    	private Combo aggregationAlgorithm;
    	
    	private static final String AGGREGATION_ALGORITHM = "aggregation.settings.algorithm";
    	
    	private static final String AGGREGATION_NONE = "No aggregation";
    	private static final String AGGREGATION_CONTEXTUAL_LUMPABILITY = "Contextual Lumpability";
    	private static final String AGGREGATION_EXACT_EQUIVALENCE = "Exact Equivalence";
    	
    	private int aggregationChosen = OptionMap.AGGREGATION_NONE;
    	
    	private IDialogSettings settings;

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
			composite.setLayout(new GridLayout(2, false));
			setControl(composite);
			
			aggregationEnabled = new Button(composite, SWT.CHECK);
			aggregationEnabled.setText("Enable aggregation");
			aggregationEnabled.setLayoutData(createDefaultGridData());
			aggregationEnabled.addListener(SWT.Selection,
					new Listener() {
						public void handleEvent(Event event) {
							boolean enabled = aggregationEnabled.isEnabled();
							aggregateArrays.setEnabled(enabled);
							aggregationAlgorithm.setEnabled(enabled);
							validate();
						}
			});
			
			// Empty cell
			new Label(composite, SWT.NONE);
			
			aggregateArrays = new Button(composite, SWT.CHECK);
			aggregateArrays.setText("Use aggregate arrays");
			aggregateArrays.setLayoutData(createDefaultGridData());
			aggregateArrays.addListener(SWT.Selection, 
					new Listener() {
						public void handleEvent(Event event) {
							validate();
						}
			});
			
			// Empty cell
			new Label(composite, SWT.NONE);
			
			aggregationAlgorithm = new Combo(composite, SWT.READ_ONLY);
			aggregationAlgorithm.setLayoutData(createDefaultGridData());
			
			String[] items = {
					AGGREGATION_NONE,
					AGGREGATION_CONTEXTUAL_LUMPABILITY,
					AGGREGATION_EXACT_EQUIVALENCE,
			};
			aggregationAlgorithm.setItems(items);
			aggregationAlgorithm.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String text = aggregationAlgorithm.getText();
					if (text.equals(AGGREGATION_NONE)) {
						aggregationChosen = OptionMap.AGGREGATION_NONE;
					} else if (text.equals(AGGREGATION_CONTEXTUAL_LUMPABILITY)) {
						aggregationChosen = OptionMap.AGGREGATION_CONTEXTUAL_LUMPABILITY;
					} else if (text.equals(AGGREGATION_EXACT_EQUIVALENCE)) {
						aggregationChosen = OptionMap.AGGREGATION_EXACT_EQUIVALENCE;
					}
				}
			});
			
			// Empty cell
			new Label(composite, SWT.NONE);
			
			// Empty cell
			new Label(composite, SWT.NONE);
			
			Button apply = new Button(composite, SWT.PUSH);
			apply.setText("Apply");
			apply.addListener(SWT.Selection, 
					new Listener() {
						public void handleEvent(Event e) {
							applySettings();
						}
			});
			
			initContents();
			validate();
		}
		
		private void initContents() {
			/* Should init from default settings */
			IDialogSettings uiSettings = Activator.getDefault().getDialogSettings();
			settings = uiSettings.getSection(SECTION_NAME);
			if (settings == null)
				settings = uiSettings.addNewSection(SECTION_NAME);

			aggregationEnabled.setSelection(
					settings.getBoolean(AGGREGATION_ENABLED_BUTTON));
			aggregateArrays.setSelection(
					settings.getBoolean(AGGREGATE_ARRAYS));
			
			String text = settings.get(AGGREGATION_ALGORITHM);
			int algIndex = 0;
			if (text.equals(AGGREGATION_NONE)) {
				algIndex = 0;
			} else if (text.equals(AGGREGATION_CONTEXTUAL_LUMPABILITY)) {
				algIndex = 1;
			} else if (text.equals(AGGREGATION_EXACT_EQUIVALENCE)) {
				algIndex = 2;
			} else {
				algIndex = -1;
			}
			aggregationAlgorithm.select(algIndex);
		}
		
		private boolean isAggregationEnabled() {
			return aggregationEnabled.isEnabled();
		}
		
		private boolean areAggregateArraysEnabled() {
			return aggregateArrays.isEnabled();
		}
		
		protected void applySettings() {
			if (isAggregationEnabled()) {
				options.put(OptionMap.AGGREGATION, aggregationChosen);
				options.put(OptionMap.AGGREGATE_ARRAYS, areAggregateArraysEnabled());
			} else {
				options.put(OptionMap.AGGREGATION, AGGREGATION_NONE);
				options.put(OptionMap.AGGREGATE_ARRAYS, false);
			}
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
