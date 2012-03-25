import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class MainOBAGUI {

	protected Shell shell;
	private Text text;
	private Text txtImg;
	private Text txtImg_1;
	private Table table;

	private String username;
	private String password;

	public MainOBAGUI(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Open the window.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(553, 368);
		shell.setMinimumSize(553, 368);
		shell.setText("VCL One Button Application");

		/**
		 * Create tabs in the window.
		 */
		shell.setLayout(new FormLayout());

		TabFolder One = new TabFolder(shell, SWT.NONE);
		FormData fd_One = new FormData();
		fd_One.right = new FormAttachment(100);
		fd_One.bottom = new FormAttachment(100);
		fd_One.top = new FormAttachment(0);
		fd_One.left = new FormAttachment(0);
		One.setLayoutData(fd_One);

		/**
		 * tab1.
		 */
		TabItem tbtmOne = new TabItem(One, SWT.NONE);
		tbtmOne.setText("Launch OBA");

		Composite compo1 = new Composite(One, SWT.NONE);
		tbtmOne.setControl(compo1);
		compo1.setLayout(new FormLayout());

		table = new Table(compo1, SWT.BORDER | SWT.FULL_SELECTION);
		FormData fd_table = new FormData();
		fd_table.top = new FormAttachment(0, 10);
		fd_table.left = new FormAttachment(0, 10);
		fd_table.right = new FormAttachment(100, -10);
		table.setLayoutData(fd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] titles = { "Image ID", "Name", "Description" };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
		}
		loadPreconfiguredOBAItems(table);
		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}

		Button btnOba = new Button(compo1, SWT.NONE);
		btnOba.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selectedItems = table.getSelection();
				if (selectedItems.length == 0) {
					// No item has been selected
					MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR
							| SWT.OK);
					dialog.setText("No Selection Found");
					dialog.setMessage("Please select one OBA instance to start");
					dialog.open();
					return;
				} else {
					// Get all information needed for making a reservation
					int image_id = Integer.parseInt(selectedItems[0].getText(0));
					String startTime = "now";
					String duration = "60";

					TestOBA one_OBA_instance = new TestOBA(image_id, username,
							password, TestOBA.Platform.Windows, startTime,
							duration);
					one_OBA_instance.start();
				}
			}
		});

		FormData fd_btnOba = new FormData();
		fd_btnOba.left = new FormAttachment(100, -152);
		fd_btnOba.bottom = new FormAttachment(100, -10);
		fd_btnOba.right = new FormAttachment(100, -10);
		btnOba.setLayoutData(fd_btnOba);
		btnOba.setText("One Button Start");

		Button btnCheckButton = new Button(compo1, SWT.CHECK);
		FormData fd_btnCheckButton = new FormData();
		fd_btnCheckButton.bottom = new FormAttachment(100, -10);
		fd_btnCheckButton.left = new FormAttachment(0, 10);
		btnCheckButton.setLayoutData(fd_btnCheckButton);
		btnCheckButton.setText("Enable Automatic Time Extending");

		Label lblDuration = new Label(compo1, SWT.NONE);
		lblDuration.setText("Duration: ");
		FormData fd_lblDuration = new FormData();
		fd_lblDuration.left = new FormAttachment(0, 10);
		fd_lblDuration.bottom = new FormAttachment(btnCheckButton, -10);
		lblDuration.setLayoutData(fd_lblDuration);

		Combo combo = new Combo(compo1, SWT.NONE);
		float[] all_possible_durations = new float[] { 0.5f, 0.75f, 1f, 2f, 3f,
				4f, };
		String[] durations = new String[all_possible_durations.length];
		for (int i = 0; i < all_possible_durations.length; i++) {
			String show_hours = null;
			if (all_possible_durations[i] < 1.0) {
				show_hours = Integer
						.toString((int) (all_possible_durations[i] * 60));
				show_hours += " minutes";
			} else {
				show_hours = Integer.toString((int) all_possible_durations[i]);
				if ((int) all_possible_durations[i] == 1) {
					show_hours += " hour";
				} else {
					show_hours += " hours";
				}
			}
			durations[i] = show_hours;
		}
		combo.setItems(durations);
		combo.select(0);

		FormData fd_combo = new FormData();
		fd_combo.bottom = new FormAttachment(btnCheckButton, -6);
		fd_combo.left = new FormAttachment(lblDuration, 8);
		combo.setLayoutData(fd_combo);

		Label lblStartTime = new Label(compo1, SWT.NONE);
		fd_table.bottom = new FormAttachment(lblStartTime, -10);
		lblStartTime.setText("Start Time: ");
		FormData fd_lblStartTime = new FormData();
		fd_lblStartTime.left = new FormAttachment(0, 10);
		fd_lblStartTime.bottom = new FormAttachment(lblDuration, -10);
		lblStartTime.setLayoutData(fd_lblStartTime);

		Button btnRadioNow = new Button(compo1, SWT.RADIO);
		btnRadioNow.setSelection(true);
		FormData fd_btnRadioNow = new FormData();
		fd_btnRadioNow.bottom = new FormAttachment(lblDuration, -10);
		fd_btnRadioNow.left = new FormAttachment(lblStartTime, 6);
		btnRadioNow.setLayoutData(fd_btnRadioNow);
		btnRadioNow.setText("Now");

		Button btnRadioLater = new Button(compo1, SWT.RADIO);
		btnRadioLater.setText("Later");
		FormData fd_btnLater_1 = new FormData();
		fd_btnLater_1.bottom = new FormAttachment(lblDuration, -10);
		fd_btnLater_1.left = new FormAttachment(btnRadioNow, 6);
		btnRadioLater.setLayoutData(fd_btnLater_1);

		Combo combo_day = new Combo(compo1, SWT.NONE);
		combo_day.setItems(new String[] { "Sunday", "Monday", "Tuesday",
				"Wednesday", "Thursday", "Friday", "Saturday" });
		FormData fd_combo_day = new FormData();
		fd_combo_day.bottom = new FormAttachment(lblDuration, -6);
		fd_combo_day.left = new FormAttachment(btnRadioLater, 6);
		combo_day.setLayoutData(fd_combo_day);

		Label lblAt = new Label(compo1, SWT.NONE);
		FormData fd_lblAt = new FormData();
		fd_lblAt.bottom = new FormAttachment(lblDuration, -10);
		fd_lblAt.left = new FormAttachment(combo_day, 6);
		lblAt.setLayoutData(fd_lblAt);
		lblAt.setText("at");

		Combo combo_hour = new Combo(compo1, SWT.NONE);
		combo_hour.setItems(new String[] { "1", "2", "3", "4", "5", "6", "7",
				"8", "9", "10", "11", "12" });
		FormData fd_combo_hour = new FormData();
		fd_combo_hour.right = new FormAttachment(lblAt, 65);
		fd_combo_hour.bottom = new FormAttachment(lblDuration, -6);
		fd_combo_hour.left = new FormAttachment(lblAt, 6);
		combo_hour.setLayoutData(fd_combo_hour);

		Combo combo_minute = new Combo(compo1, SWT.NONE);
		combo_minute.setItems(new String[] { "00", "15", "30", "45" });
		FormData fd_combo_minute = new FormData();
		fd_combo_minute.right = new FormAttachment(combo_hour, 105);
		fd_combo_minute.left = new FormAttachment(combo_hour, 6);
		fd_combo_minute.bottom = new FormAttachment(lblDuration, -6);
		combo_minute.setLayoutData(fd_combo_minute);

		Combo combo_ampm = new Combo(compo1, SWT.NONE);
		combo_ampm.setItems(new String[] { "a.m.", "p.m." });
		FormData fd_combo_ampm = new FormData();
		fd_combo_ampm.right = new FormAttachment(combo_minute, 115);
		fd_combo_ampm.left = new FormAttachment(combo_minute, 6);
		fd_combo_ampm.bottom = new FormAttachment(lblDuration, -6);
		combo_ampm.setLayoutData(fd_combo_ampm);

		/**
		 * tab2.
		 */

		TabItem tbtmNew = new TabItem(One, SWT.NONE);
		tbtmNew.setText("Create new OBA");

		Composite compo2 = new Composite(One, SWT.NONE);
		// compo2.setLayout(new FormLayout());
		tbtmNew.setControl(compo2);

		Label lblImage = new Label(compo2, SWT.NONE);
		lblImage.setLayoutData(new FormData());
		lblImage.setBounds(10, 10, 55, 28);
		lblImage.setText("Image");

		Combo combo_2 = new Combo(compo2, SWT.NONE);
		combo_2.setLayoutData(new FormData());
		combo_2.setBounds(95, 28, 91, 23);

		Button btnNow = new Button(compo2, SWT.RADIO);
		btnNow.setLayoutData(new FormData());
		btnNow.setBounds(10, 57, 90, 16);
		btnNow.setText("Now");

		Button btnLater = new Button(compo2, SWT.RADIO);
		btnLater.setLayoutData(new FormData());
		btnLater.setBounds(10, 79, 75, 16);
		btnLater.setText("Later");

		DateTime dateTime = new DateTime(compo2, SWT.BORDER);
		dateTime.setLayoutData(new FormData());
		dateTime.setBounds(95, 79, 80, 24);

		Button btnCreate = new Button(compo2, SWT.NONE);
		btnCreate.setLayoutData(new FormData());
		btnCreate.setBounds(10, 116, 75, 25);
		btnCreate.setText("Create");

		Label lblUploadScript = new Label(compo2, SWT.NONE);
		lblUploadScript.setLayoutData(new FormData());
		lblUploadScript.setBounds(10, 143, 75, 15);
		lblUploadScript.setText("Upload Script");

		text = new Text(compo2, SWT.BORDER);
		text.setLayoutData(new FormData());
		text.setBounds(95, 137, 76, 21);

		Button btnBrowse = new Button(compo2, SWT.NONE);
		btnBrowse.setLayoutData(new FormData());
		btnBrowse.setBounds(254, 133, 75, 25);
		btnBrowse.setText("Browse");

		/**
		 * tab3.
		 */

		TabItem tbtmStatus = new TabItem(One, SWT.NONE);
		tbtmStatus.setText("Current OBA Status");

		Group group_2 = new Group(One, SWT.NONE);
		tbtmStatus.setControl(group_2);

		txtImg = new Text(group_2, SWT.BORDER);
		txtImg.setText("img1");
		txtImg.setBounds(10, 27, 76, 21);

		ProgressBar progressBar = new ProgressBar(group_2, SWT.NONE);
		progressBar.setBounds(117, 27, 170, 17);

		txtImg_1 = new Text(group_2, SWT.BORDER);
		txtImg_1.setText("img2");
		txtImg_1.setBounds(10, 70, 76, 21);

		ProgressBar progressBar_1 = new ProgressBar(group_2, SWT.NONE);
		progressBar_1.setBounds(117, 70, 170, 17);

	}

	/*
	 * Load all existing OBA configurations into the Table, each item represents
	 * one OBA application.
	 */
	private void loadPreconfiguredOBAItems(Table mainTable) {
		// TODO Auto-generated method stub
		TableItem item = new TableItem(mainTable, SWT.NONE);
		item.setText(0, "2422");
		item.setText(1, "VCL2.2.1 SandBox");
		item.setText(2, "Our testing image1");

		TableItem item2 = new TableItem(mainTable, SWT.NONE);
		item2.setText(0, "2813");
		item2.setText(1, "centos_tunnel_main_campus");
		item2.setText(2, "Our testing image2");
	}
}
