package org.gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;


public class parametersetting extends Dialog {

	protected int result;
	protected Shell shlParameterSetting;
	private Text text;
	private Text text_1;
	private Text text_2;
    private Text text_3;
    private Text text_4;
    
	public int timeWindow;//ʱ�䴰 Ĭ��200000
    public int taskAverageLength;//�����ƽ�����ȣ�20,30,40,50 Ĭ��ֵ30��
    public int dagAverageSize;//dag��ƽ����С��20��30��40��50 Ĭ��ֵ30��
    public int dagLevelFlag;//(1,2,3)����([3,sqrt(N-2)],[sqrt(N-2),sqrt(N-2)+4],[sqrt(N-2),N-2])
    public double deadLineTimes;//deadline�ı���ֵ ��1.1��1.3��1.6��2.0��
    public int processorNumber;//����Ԫ�ĸ�����2,4,8,16��
	
    
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public parametersetting(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public int open() {
		createContents();
		shlParameterSetting.open();
		shlParameterSetting.layout();
		Display display = getParent().getDisplay();
		while (!shlParameterSetting.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlParameterSetting = new Shell(getParent(), getStyle());
		shlParameterSetting.setSize(440, 420);
		shlParameterSetting.setText("Parameter Setting");
		shlParameterSetting.setLayout(null);

		Label lblNewLabel = new Label(shlParameterSetting, SWT.NONE);
		lblNewLabel.setBounds(24, 30, 96, 17);
		lblNewLabel.setText("TimeWindow");
		
		Label lblMaxdeviationrate = new Label(shlParameterSetting, SWT.NONE);
		lblMaxdeviationrate.setText("TaskAverageLength");
		lblMaxdeviationrate.setBounds(24, 71, 113, 17);
		
		Label lblPricingInterval = new Label(shlParameterSetting, SWT.NONE);
		lblPricingInterval.setText("DAGAverageSize");
		lblPricingInterval.setBounds(24, 126, 113, 17);
		
		text_4 = new Text(shlParameterSetting, SWT.BORDER);
		text_4.setText("30");
		text_4.setBounds(190, 126, 73, 23);
		
		text = new Text(shlParameterSetting, SWT.BORDER);
		text.setText("30");
		text.setBounds(190, 71, 73, 23);
		
		text_1 = new Text(shlParameterSetting, SWT.BORDER);
		text_1.setText("20000");
		text_1.setBounds(190, 27, 73, 23);
		
		Label lbldaglength = new Label(shlParameterSetting, SWT.NONE);
		lbldaglength.setText("(optional:20/30/40/50)");
		lbldaglength.setBounds(270, 71, 156, 17);
		
		Label lbldagsize = new Label(shlParameterSetting, SWT.NONE);
		lbldagsize.setText("(optional:20/30/40/50)");
		lbldagsize.setBounds(270, 126, 156, 17);
		
		Label lblRuntimeDistributionType = new Label(shlParameterSetting, SWT.NONE);
		lblRuntimeDistributionType.setText("DAGLevelFlag");
		lblRuntimeDistributionType.setBounds(24, 175, 156, 17);
		
		Composite composite = new Composite(shlParameterSetting, SWT.NONE);
		composite.setBounds(190, 157, 214, 64);
		
		Button btnRadioButton_2 = new Button(composite, SWT.RADIO);
		btnRadioButton_2.setBounds(120, 21, 97, 17);
		btnRadioButton_2.setText("3");
		
		Button btnRadioButton_1 = new Button(composite, SWT.RADIO);
		btnRadioButton_1.setBounds(60, 21, 97, 17);
		btnRadioButton_1.setSelection(true);
		btnRadioButton_1.setText("2");
		
		Button btnRadioButton = new Button(composite, SWT.RADIO);
		btnRadioButton.setBounds(0, 21, 97, 17);
		btnRadioButton.setText("1");
		
		Label lblSystemBandwidth = new Label(shlParameterSetting, SWT.NONE);
		lblSystemBandwidth.setText("DeadlineTimes");
		lblSystemBandwidth.setBounds(24, 234, 109, 17);
		
		text_2 = new Text(shlParameterSetting, SWT.BORDER);
		text_2.setText("1.3");
		text_2.setBounds(190, 231, 73, 23);
		
		Label lbldead = new Label(shlParameterSetting, SWT.NONE);
		lbldead.setText("(optional:1.1/1.3/1.6/2.0)");
		lbldead.setBounds(270, 231, 156, 17);
		
		Button btnNewButton = new Button(shlParameterSetting, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				timeWindow=Integer.valueOf(text_1.getText());
				taskAverageLength=Integer.valueOf(text.getText());
				dagAverageSize=Integer.valueOf(text_4.getText());
				deadLineTimes=Double.valueOf(text_2.getText());
				processorNumber=Integer.valueOf(text_3.getText());
			    
			    if(btnRadioButton_1.getSelection())
			    	dagLevelFlag=2;
			    else if(btnRadioButton.getSelection())
			    	dagLevelFlag=1;
			    else if(btnRadioButton_2.getSelection())
			    	dagLevelFlag=3;
			   
			    result=SWT.OK;
			  
			    shlParameterSetting.close();
			}
		});
		btnNewButton.setSelection(true);
		btnNewButton.setBounds(80, 330, 80, 27);
		btnNewButton.setText("OK");
		
		Button btnNewButton_1 = new Button(shlParameterSetting, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				  result=SWT.CANCEL;
				  shlParameterSetting.close();
			}
		});
		btnNewButton_1.setBounds(220, 330, 80, 27);
		btnNewButton_1.setText("Cancel");
		
		Label lblVmSetupTime = new Label(shlParameterSetting, SWT.NONE);
		lblVmSetupTime.setText("ProcessorNumber");
		lblVmSetupTime.setBounds(24, 273, 113, 17);
		
		text_3 = new Text(shlParameterSetting, SWT.BORDER);
		text_3.setText("8");
		text_3.setBounds(190, 270, 73, 23);

		Label lblSeconds = new Label(shlParameterSetting, SWT.NONE);
		lblSeconds.setText("(optional:2/4/8/16/32)");
		lblSeconds.setBounds(270, 273, 156, 17);
	}
}
