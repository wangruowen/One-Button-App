import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
	private Display display;
	private Shell shell;
	private Table table;

	private Table statusTable;
	private TabFolder mainTabFolder;
	private TabItem tbtmStatus;

	private Combo duration_combo;
	private float[] all_possible_durations;
	private HashMap<String, Integer> status_Titles;

	private OBAController controller;
	private Text text_script_path;
	private Text text_dropbox_url;
	private Text desc_text;

	public MainOBAGUI() {
		this.controller = OBAController.getInstance();
	}

	/**
	 * Open the window.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		// center the dialog screen to the monitor
		Rectangle bounds = display.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
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

		mainTabFolder = new TabFolder(shell, SWT.NONE);
		FormData fd_One = new FormData();
		fd_One.right = new FormAttachment(100);
		fd_One.bottom = new FormAttachment(100);
		fd_One.top = new FormAttachment(0);
		fd_One.left = new FormAttachment(0);
		mainTabFolder.setLayoutData(fd_One);

		/**
		 * tab1.
		 */
		TabItem tbtmOne = new TabItem(mainTabFolder, SWT.NONE);
		tbtmOne.setText("Launch OBA");

		Composite compo1 = new Composite(mainTabFolder, SWT.NONE);
		tbtmOne.setControl(compo1);
		compo1.setLayout(new FormLayout());

		table = new Table(compo1, SWT.BORDER | SWT.FULL_SELECTION);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {

			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 3) {
					Menu menu = new Menu(table.getShell(), SWT.POP_UP);
					MenuItem editItem = new MenuItem(menu, SWT.PUSH);
					editItem.setText("Edit");
					MenuItem delItem = new MenuItem(menu, SWT.PUSH);
					delItem.setText("Delete");

					// draws pop up menu:
					Point pt = new Point(e.x, e.y);
					pt = table.toDisplay(pt);
					menu.setLocation(pt.x, pt.y);
					menu.setVisible(true);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (e.button == 1) {
					// When double click one OBA item, start launching it
					start_one_OBA_instance(table.getSelection());
				}
			}
		});
		FormData fd_table = new FormData();
		fd_table.top = new FormAttachment(0, 10);
		fd_table.left = new FormAttachment(0, 10);
		fd_table.right = new FormAttachment(100, -10);
		table.setLayoutData(fd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] titles = { "OBA ID", "Name", "Description" };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);

		}

		loadPreconfiguredOBAItems(table);

		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}

		Button btnOba = new Button(compo1, SWT.NONE);

		// Launch button is clicked
		btnOba.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				start_one_OBA_instance(table.getSelection());
			}

			/**
			 * this method looks into the reservationList of the controller and
			 * find the OBABean corresponding to each input TableItem
			 * 
			 * @param selectedItems
			 *            the list of input TableItem
			 * @return
			 */
			private OBABean[] getOBAof(TableItem[] selectedItems) {
				// TODO Auto-generated method stub

				return null;
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

		duration_combo = new Combo(compo1, SWT.NONE);
		all_possible_durations = new float[] { 0.5f, 0.75f, 1f, 2f, 3f, 4f, };
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
		duration_combo.setItems(durations);
		duration_combo.select(2);

		FormData fd_combo = new FormData();
		fd_combo.bottom = new FormAttachment(btnCheckButton, -6);
		fd_combo.left = new FormAttachment(lblDuration, 8);
		duration_combo.setLayoutData(fd_combo);

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
		TabItem tbtmNew = new TabItem(mainTabFolder, SWT.NONE);
		tbtmNew.setText("Create new OBA");

		Composite compo2 = new Composite(mainTabFolder, SWT.NONE);
		compo2.setLayout(new FormLayout());
		tbtmNew.setControl(compo2);

		Label lblImage = new Label(compo2, SWT.NONE);
		FormData fd_lblImage = new FormData();
		fd_lblImage.top = new FormAttachment(0, 10);
		fd_lblImage.left = new FormAttachment(0, 10);
		lblImage.setLayoutData(fd_lblImage);

		lblImage.setText("Please select the environment you want to use from the list:");

		final Combo combo_choose_image = new Combo(compo2, SWT.NONE);
		FormData fd_combo_1 = new FormData();
		fd_combo_1.right = new FormAttachment(100, -10);
		fd_combo_1.top = new FormAttachment(lblImage, 6);
		fd_combo_1.left = new FormAttachment(0, 10);
		combo_choose_image.setLayoutData(fd_combo_1);

		// Now list all available images in this combo
		new Thread() {
			public void run() {
				LinkedHashMap<Integer, String> image_hash_map = controller.VCLConnector
						.getAvailableImages();
				final ArrayList<String> all_image_string_array = new ArrayList<String>();
				for (Map.Entry<Integer, String> one_image_entry : image_hash_map
						.entrySet()) {
					controller.putReverseOBAentryHashMap(
							one_image_entry.getValue(),
							one_image_entry.getKey());
					all_image_string_array.add(one_image_entry.getValue());
				}

				if (display.isDisposed())
					return;
				display.asyncExec(new Runnable() {
					public void run() {
						combo_choose_image.setItems(all_image_string_array
								.toArray(new String[0]));
						combo_choose_image.select(0);
					}
				});
			}
		}.start();

		Label lblDescription = new Label(compo2, SWT.NONE);
		FormData fd_lblDescription = new FormData();
		fd_lblDescription.top = new FormAttachment(combo_choose_image, 10);
		fd_lblDescription.left = new FormAttachment(lblImage, 0, SWT.LEFT);
		lblDescription.setLayoutData(fd_lblDescription);
		lblDescription.setText("Description:");

		desc_text = new Text(compo2, SWT.BORDER);
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(combo_choose_image, 6);
		fd_text.right = new FormAttachment(100, -10);
		fd_text.left = new FormAttachment(lblDescription, 6);
		desc_text.setLayoutData(fd_text);

		Label lblImage_Duration = new Label(compo2, SWT.NONE);
		FormData fd_lblImage_Duration = new FormData();
		fd_lblImage_Duration.top = new FormAttachment(lblDescription, 10);
		fd_lblImage_Duration.left = new FormAttachment(lblImage, 0, SWT.LEFT);
		lblImage_Duration.setLayoutData(fd_lblImage_Duration);
		lblImage_Duration.setText("Default duration: ");

		Combo combo_1 = new Combo(compo2, SWT.NONE);
		FormData fd_combo_2 = new FormData();
		fd_combo_2.top = new FormAttachment(lblDescription, 6);
		fd_combo_2.left = new FormAttachment(lblImage_Duration, 6);
		combo_1.setLayoutData(fd_combo_2);

		Label lbluploadScript = new Label(compo2, SWT.NONE);
		FormData fd_lbluploadScript = new FormData();
		fd_lbluploadScript.top = new FormAttachment(combo_1, 10);
		fd_lbluploadScript.left = new FormAttachment(0, 10);
		lbluploadScript.setLayoutData(fd_lbluploadScript);
		lbluploadScript.setText("User defined script: ");

		text_script_path = new Text(compo2, SWT.BORDER);
		FormData fd_text_script_path = new FormData();
		fd_text_script_path.right = new FormAttachment(lblImage, 0, SWT.RIGHT);
		fd_text_script_path.top = new FormAttachment(combo_1, 6);
		fd_text_script_path.left = new FormAttachment(lbluploadScript, 6);
		text_script_path.setLayoutData(fd_text_script_path);

		Button btnBrowseButton = new Button(compo2, SWT.NONE);
		FormData fd_btnBrowseButton = new FormData();
		fd_btnBrowseButton.top = new FormAttachment(text_script_path, -2,
				SWT.TOP);
		fd_btnBrowseButton.right = new FormAttachment(combo_choose_image, 0,
				SWT.RIGHT);
		fd_btnBrowseButton.left = new FormAttachment(100, -100);
		fd_text_script_path.right = new FormAttachment(btnBrowseButton, -10);
		btnBrowseButton.setLayoutData(fd_btnBrowseButton);
		btnBrowseButton.setText("Browse");

		Label lblDropboxLabel = new Label(compo2, SWT.NONE);
		FormData fd_lblDropboxLabel = new FormData();
		fd_lblDropboxLabel.top = new FormAttachment(text_script_path, 10);
		fd_lblDropboxLabel.left = new FormAttachment(0, 10);
		lblDropboxLabel.setLayoutData(fd_lblDropboxLabel);
		lblDropboxLabel.setText("Dropbox URL: ");

		text_dropbox_url = new Text(compo2, SWT.BORDER);
		FormData fd_text_dropbox_url = new FormData();
		fd_text_dropbox_url.left = new FormAttachment(lblDropboxLabel, 6);
		fd_text_dropbox_url.right = new FormAttachment(100, -10);
		fd_text_dropbox_url.top = new FormAttachment(text_script_path, 6);
		text_dropbox_url.setLayoutData(fd_text_dropbox_url);

		// Create button is clicked
		Button btnCreate = new Button(compo2, SWT.NONE);
		btnCreate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String image_name = combo_choose_image.getText();
				String image_desc = desc_text.getText();
				int image_id = controller.getImageIDByImageName(image_name);
				controller.createOBAentry(image_id, image_name, image_desc);

				// Now we need to add a new TableItem on the table on the first
				// tab
				TableItem newOBAItem = new TableItem(table, SWT.NONE);
				newOBAItem.setText(0,
						Integer.toString(table.indexOf(newOBAItem) + 1));
				newOBAItem.setText(1, image_name);
				newOBAItem.setText(2, image_desc);
				// Now switch to the first tab
				mainTabFolder.setSelection(0);
			}
		});
		FormData fd_btnCreate = new FormData();
		fd_btnCreate.left = new FormAttachment(btnBrowseButton, 0, SWT.LEFT);
		fd_btnCreate.bottom = new FormAttachment(100, -10);
		fd_btnCreate.right = new FormAttachment(btnBrowseButton, 0, SWT.RIGHT);
		fd_btnCreate.top = new FormAttachment(100, -40);
		btnCreate.setLayoutData(fd_btnCreate);
		btnCreate.setText("Create");

		/**
		 * tab3.
		 */
		tbtmStatus = new TabItem(mainTabFolder, SWT.NONE);
		tbtmStatus.setText("Current OBA Status");

		Composite composite = new Composite(mainTabFolder, SWT.NONE);
		tbtmStatus.setControl(composite);
		composite.setLayout(new FormLayout());

		statusTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
		statusTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 3) {
					TableItem selectItem = statusTable.getSelection()[0];

					Menu menu = new Menu(statusTable.getShell(), SWT.POP_UP);
					MenuItem editItem = new MenuItem(menu, SWT.PUSH);
					editItem.setText("Connect");
					editItem.addSelectionListener(new StatusMenuLisnter(
							selectItem));
					MenuItem delItem = new MenuItem(menu, SWT.PUSH);
					delItem.setText("End");
					delItem.addSelectionListener(new StatusMenuLisnter(
							selectItem));

					// draws pop up menu:
					Point pt = new Point(e.x, e.y);
					pt = table.toDisplay(pt);
					menu.setLocation(pt.x, pt.y);
					menu.setVisible(true);
				}
			}
		});
		FormData fd_table_1 = new FormData();
		fd_table_1.bottom = new FormAttachment(100, -10);
		fd_table_1.right = new FormAttachment(100, -10);
		fd_table_1.top = new FormAttachment(0, 10);
		fd_table_1.left = new FormAttachment(0, 10);
		statusTable.setLayoutData(fd_table_1);
		statusTable.setHeaderVisible(true);
		statusTable.setLinesVisible(true);

		this.status_Titles = new HashMap<String, Integer>();
		String[] tmp_titleString = new String[] { "Image ID", "Name", "Status",
				"Remaining Time", "Auto Time Extend", "IP address", "Username",
				"Password", "Request ID" };
		for (int i = 0; i < tmp_titleString.length; i++) {
			status_Titles.put(tmp_titleString[i], i);

			TableColumn column = new TableColumn(statusTable, SWT.NONE);
			column.setText(tmp_titleString[i]);
			if (tmp_titleString[i].equals("Status")) {
				column.setWidth(1500);
			}
		}
		for (int i = 0; i < tmp_titleString.length; i++) {
			statusTable.getColumn(i).pack();
		}
	}

	private class StatusMenuLisnter extends SelectionAdapter {
		private OBABean obaBean = null;
		private TableItem selectItem;

		public StatusMenuLisnter(TableItem selectItem) {
			this.selectItem = selectItem;
			String request_id_str = selectItem.getText(status_Titles
					.get("Request ID"));
			if (!request_id_str.equals("")) {
				this.obaBean = controller.getOBAByRequestId(Integer
						.parseInt(request_id_str));
			}
		}

		public void widgetSelected(SelectionEvent event) {
			if (obaBean == null) {
				return;
			}

			String menuItemString = ((MenuItem) event.widget).getText();
			if (menuItemString.equals("Connect")) {
				obaBean.directStart();
			} else if (menuItemString.equals("End")) {
				new Thread() {
					public void run() {
						if (controller.VCLConnector.endRequest(obaBean
								.getRequestId())) {
							if (display.isDisposed())
								return;
							display.asyncExec(new Runnable() {
								public void run() {
									// Delete this table item
									statusTable.remove(statusTable
											.getSelectionIndices());

									MessageBox dialog = new MessageBox(shell,
											SWT.ICON_INFORMATION | SWT.OK);
									dialog.setText("The reservation has been successfully ended. ");
									dialog.setMessage("Reservation ends. Request ID: "
											+ obaBean.getRequestId()
											+ ", Name: "
											+ obaBean.getImageName());
									dialog.open();
								}
							});
						} else {
							if (display.isDisposed())
								return;
							display.asyncExec(new Runnable() {
								public void run() {
									MessageBox dialog = new MessageBox(shell,
											SWT.ICON_ERROR | SWT.OK);
									dialog.setText("Failure in ending the reservation. ");
									dialog.setMessage("Fail to end Reservation with Request ID: "
											+ obaBean.getRequestId()
											+ ", Name: "
											+ obaBean.getImageName());
									dialog.open();
								}
							});
						}
					}
				}.start();
			}
		}
	}

	/*
	 * Load all existing OBA configurations into the Table, each item represents
	 * one OBA application.
	 */
	private void loadPreconfiguredOBAItems(Table mainTable) {
		OBAEntry[] entry_list = controller.getPreconfigedOBAEntries();

		for (int i = 0; i < entry_list.length; i++) {
			TableItem one_item = new TableItem(mainTable, SWT.NONE);
			one_item.setText(0, Integer.toString(i + 1));
			one_item.setText(1, entry_list[i].getImageName());
			one_item.setText(2, entry_list[i].getImageDesc());
		}
		//
		// TableItem item = new TableItem(mainTable, SWT.NONE);
		// item.setText(0, "2422");
		// item.setText(1, "VCL2.2.1 SandBox");
		// item.setText(2, "Our testing image1");
		//
		// TableItem item2 = new TableItem(mainTable, SWT.NONE);
		// item2.setText(0, "2813");
		// item2.setText(1, "centos_tunnel_main_campus");
		// item2.setText(2, "Our testing image2");
		//
		// TableItem item3 = new TableItem(mainTable, SWT.NONE);
		// item3.setText(0, "1913");
		// item3.setText(1, "centos_tunnel_mcnc");
		// item3.setText(2, "Our testing image3");
	}

	/**
	 * This method is called when the "One Button Start" button is clicked, or
	 * when the user double-clicks one OBA table item. This method is moved to
	 * the controller This method is moved to the controller
	 * 
	 * @param selectedItems
	 */
	private void start_one_OBA_instance(TableItem[] selectedItems) {
		if (selectedItems.length == 0) {
			// No item has been selected
			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			dialog.setText("No Selection Found");
			dialog.setMessage("Please select one OBA instance to start");
			dialog.open();
		} else {
			// Get all information needed for making a reservation
			int oba_id = Integer.parseInt(selectedItems[0].getText(0));
			final OBAEntry selectedOBAEntry = controller
					.getOBAEntryByTableID(oba_id - 1);

			final int image_id = selectedOBAEntry.getImageID();
			final String name = selectedOBAEntry.getImageName();
			final Calendar startTime = Calendar.getInstance();
			final int duration = (int) (all_possible_durations[duration_combo
					.getSelectionIndex()] * 60);
			// Now switch to the Current active OBA tab
			mainTabFolder.setSelection(2);

			final TableItem one_status_Item = new TableItem(statusTable,
					SWT.NONE);
			one_status_Item.setText(0, Integer.toString(image_id));
			one_status_Item.setText(1, name);

			final int item_index = statusTable.indexOf(one_status_Item);

			final ProgressBar bar = new ProgressBar(statusTable, SWT.NONE);
			final TableEditor editor = new TableEditor(statusTable);
			editor.grabHorizontal = editor.grabVertical = true;
			editor.setEditor(bar, one_status_Item, 2);

			// Add disposelistener to the one_status_item, so that when the
			// table item is deleted,
			// its editor should be deleted too.
			one_status_Item.addDisposeListener(new DisposeListener() {

				@Override
				public void widgetDisposed(DisposeEvent arg0) {
					// TODO Auto-generated method stub
					bar.dispose();
					editor.dispose();
				}
			});

			bar.setMaximum(100);
			bar.setMinimum(0);
			bar.setSelection(0);

			// Prepare a error message dialog
			final MessageBox error_dialog = new MessageBox(shell,
					SWT.ICON_ERROR | SWT.OK);
			error_dialog.setText("Making Reservation Fails!");
			error_dialog.setMessage("Cannot make the reservation.");

			new Thread() {
				public void run() {
					final OBABean new_OBA_bean = controller.launchOBA(
							selectedOBAEntry, startTime, duration);
					if (new_OBA_bean != null) {
						// Now start polling status
						final int[] complete_percent = new int[1];
						complete_percent[0] = 0;
						boolean ready = false;

						while (true) {
							String[] status = controller.VCLConnector
									.getPercentageStatus(new_OBA_bean);

							if (status[0].equals("error")) {
								// Delete the tableitem
								statusTable.remove(item_index);
								error_dialog.open();
								break;
							}

							complete_percent[0] = Integer.parseInt(status[0]);
							final String remain_time_str = status[1];

							complete_percent[0]++;
							if (complete_percent[0] >= 100) {
								if (display.isDisposed())
									return;
								display.asyncExec(new Runnable() {
									public void run() {
										if (bar.isDisposed())
											return;
										bar.setSelection(complete_percent[0]);
										one_status_Item.setText(3,
												remain_time_str);
									}
								});
								ready = true;
								break;
							} else if (complete_percent[0] >= 0) {
								try {
									Thread.sleep(10);
								} catch (Throwable th) {
								}
								if (display.isDisposed())
									return;
								display.asyncExec(new Runnable() {
									public void run() {
										if (bar.isDisposed()) {
											return;
										}
										bar.setSelection(complete_percent[0]);
										one_status_Item.setText(3,
												remain_time_str);
									}
								});
							} else {
								// Error
								if (display.isDisposed())
									return;
								display.asyncExec(new Runnable() {
									public void run() {
										if (bar.isDisposed())
											return;
										// Delete the tableitem
										statusTable.remove(item_index);
									}
								});
								error_dialog.open();
							}
						}

						if (ready) {
							final String[] conn_data = controller.VCLConnector
									.getConnectData(new_OBA_bean.getRequestId());
							new_OBA_bean.setIpAddress(conn_data[0]);
							new_OBA_bean.setUsername(conn_data[1]);
							new_OBA_bean.setPassword(conn_data[2]);

							// Now update the statusTable item to show all
							// available info
							if (display.isDisposed())
								return;
							display.asyncExec(new Runnable() {
								public void run() {
									if (bar.isDisposed())
										return;
									one_status_Item.setText(
											status_Titles.get("IP address"),
											conn_data[0]);
									one_status_Item.setText(
											status_Titles.get("Username"),
											conn_data[1]);
									if (conn_data[2]
											.equals(controller.VCLConnector
													.getPassword())) {
										one_status_Item.setText(
												status_Titles.get("Password"),
												"(use your campus password)");
									} else {
										one_status_Item.setText(
												status_Titles.get("Password"),
												conn_data[2]);
									}
									one_status_Item.setText(status_Titles
											.get("Request ID"), Integer
											.toString(new_OBA_bean
													.getRequestId()));
								}
							});

							new_OBA_bean.start();
						}
					} else {
						// No OBA is created, which means error
						if (display.isDisposed())
							return;
						display.asyncExec(new Runnable() {
							public void run() {
								if (bar.isDisposed())
									return;
								// Delete the tableitem
								statusTable.remove(item_index);
							}
						});
						error_dialog.open();
					}
				}
			}.start();
		}
	}

	/**
	 * TODO : fill the code This method take an ArrayList of OBABean and load it
	 * into a table
	 * 
	 * @param reservationList
	 */
	private void loadReservations(Table mainTable,
			HashMap<Integer, OBABean> reservationList) {

	}
}
