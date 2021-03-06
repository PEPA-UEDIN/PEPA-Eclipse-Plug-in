package uk.ac.ed.inf.pepa.eclipse.ui.wizards.cpt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import uk.ac.ed.inf.pepa.cpt.config.Config;
import uk.ac.ed.inf.pepa.cpt.config.control.Control;
import uk.ac.ed.inf.pepa.eclipse.ui.dialogs.IValidationCallback;

public class KeyDoubleValueWidgetForWeightAndTarget extends CapacityPlanningWidget {
	
	private String key, value1, value2;
	private final Text text1, text2;
	private boolean isTar;
	
	private class MyCallBack implements IValidationCallback {
		
		public void setTar() {
			setWidgetTar();
		}
		
		public void setWei() {
			setWidgetWei();
		}

		@Override
		public void validate() {
			cb.validate();
			
		}
		
	}
	
	protected final MyCallBack myCallBack = new MyCallBack();

	public KeyDoubleValueWidgetForWeightAndTarget(final IValidationCallback cb, 
			Composite container, String key, String value1, String value2, Control control) {
		super(cb, container, control);
	
		this.key = key;
		this.value1 = value1;
		this.value2 = value2;
		this.isTar = true;
			
		//pad
		Label label = new Label(container, SWT.FILL);
		label.setText("");
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		label.setLayoutData(data);
		
		label = new Label(container, SWT.SINGLE | SWT.LEFT);
		label.setText(this.key);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		label.setLayoutData(data);
		
		text1 = new Text(container, SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
		text1.setText("" + this.value1);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		text1.setLayoutData(data);
		
		text1.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				myCallBack.setTar();
				myCallBack.validate();
				
			}
			
		});
		
		text2 = new Text(container, SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
		text2.setText("" + this.value2);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		text2.setLayoutData(data);
		
		text2.addListener(SWT.Modify, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				myCallBack.setWei();
				myCallBack.validate();
				
			}
			
		});
		
		label = new Label(container, SWT.FILL);
		label.setText("");
		data = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		label.setLayoutData(data);
		
	}

	@Override
	public Response isValid() {
		
		if(isTar){
			Response response = new Response(control.setValue(this.key, Config.LABTAR, text1.getText()));
			
			if(!response.valid){
				response.setComplaint("Invalid entry: " + this.key + " " + text1.getText());
			}
			return response;
			
		} else {
			Response response = new Response(control.setValue(this.key, Config.LABWEI, text2.getText()));
			
			if(!response.valid){
				response.setComplaint("Invalid entry: " + this.key + " " + text2.getText());
			}
			return response;
		}
		
	}
	
	public void setWidgetTar(){
		this.isTar = true;
	}
	
	public void setWidgetWei(){
		this.isTar = false;
	}

}
