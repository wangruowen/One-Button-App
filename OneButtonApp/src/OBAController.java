import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class OBAController {
	private static OBAController instance = null;
	private HashMap<Integer, OBABean> reservationsList;
	private HashMap<String, Integer> reverseOBAentryHashMap;

	// public for the test purpose, need to change back to private after that.
	public OBALogic VCLConnector;

	private String username;
	private String password;
	private LoginDialog loginOBA;
	private MainOBAGUI mainOBA;
	private OBADBManager DBManager;
	private ArrayList<OBAEntry> currentConfigedOBAEntryList;
	private ArrayList<OBAEntry> newlyCreatedOBAEntryList;

	protected OBAController() {
		// Exists only to defeat instantiation.
	}

	/**
	 * Singleton pattern If the controller is already created, just return the
	 * instance if not, create it and return the instance
	 * 
	 * @return the unique controller of the program
	 */
	public static OBAController getInstance() {
		if (instance == null) {
			instance = new OBAController();
		}
		return instance;
	}

	/**
	 * Initialize the parameter, nothing right now
	 */
	public void initialize() {
		DBManager = OBADBManager.getInstance();
		DBManager.initialize();

		String[] savedUserInfos = DBManager.getRememberPasswd();
		// HashMap<String, String> savedUserInfos =
		// DBManager.getRememberPasswd();
		this.reservationsList = new HashMap<Integer, OBABean>();
		this.reverseOBAentryHashMap = new HashMap<String, Integer>();

		PasswordCheckingDialog checkPasswordDialog = new PasswordCheckingDialog();
		if (savedUserInfos != null
				&& checkPasswordDialog.checkPassword(savedUserInfos)) {
			username = savedUserInfos[0];
			password = savedUserInfos[1];
			setVCLConnector(username, password);
			showMainOBA();
		} else {
			loginOBA = new LoginDialog();
			loginOBA.show();
		}
	}

	public void showMainOBA() {
		if (mainOBA == null) {
			this.currentConfigedOBAEntryList = DBManager
					.getStoredOBAEntries(username);
			this.newlyCreatedOBAEntryList = new ArrayList<OBAEntry>();
			mainOBA = new MainOBAGUI();
		}
		mainOBA.open();

		// After mainOBA is disposed, and get back to here, everything is done,
		// about to end.
		// We should check whether any new OBAEntry is created and store them
		// back to the database
		beforeQuit();
	}

	public void signInAsDifferentUser() {
		DBManager = OBADBManager.getInstance();
		DBManager.clearLoginTable();
	}

	/**
	 * Try to login with this couple of username and password
	 * 
	 * @param savedUserInfos
	 * @return if successful initialize the field username and password of
	 *         controller and return true if not delete the store password in
	 *         the database, return false;
	 */
	public boolean loginWithSavePasswd(String[] savedUserInfos) {
		OBALogic oba_inst = new OBALogic(savedUserInfos[0], savedUserInfos[1]);
		return oba_inst.loginCheck();
	}

	/**
	 * Use the VCLConnector to get the current reservations from the VCL server
	 * and write it down in the reservationsList variable Check the
	 * reservationsList variable and return it
	 * 
	 * @return reservationsList
	 */
	public HashMap<Integer, OBABean> getCurrentReservations() {
		if (this.reservationsList.size() == 0) {
			reservationsList = VCLConnector.getCurrentReservations();

			// TODO we need to associate currentConfigedOBAEntryList with the
			// current active reservation
			if (currentConfigedOBAEntryList != null) {
				for (OBABean each_active_bean : reservationsList.values()) {
					// go through the OBAEntry List to find the OBAEntry that
					// can match the OBABean
					for (OBAEntry each_entry : currentConfigedOBAEntryList) {
						if (each_active_bean.getImageId() == each_entry
								.getImageID()) {
							each_active_bean.setOwnerEntry(each_entry);
							break;
						}
					}
				}
			}

			return reservationsList;
		} else {
			return this.reservationsList;
		}
	}

	/*
	 * This method returns the existing preconfigured OBA list from the database
	 */
	public OBAEntry[] getPreconfigedOBAEntries() {
		return currentConfigedOBAEntryList.toArray(new OBAEntry[0]);
	}

	/**
	 * Try to launch an list of OBA to login to a reservation take out each OBA,
	 * and call it function OBA.start();
	 * 
	 * @param selectedItems
	 *            from the table of MainOBAGUI
	 * @return true if login success, false if not.
	 */
	public OBABean launchOBA(OBAEntry ownerEntry, Calendar start_Time,
			int duration, boolean auto_extend) {
		OBABean result_Bean = null;

		int image_id = ownerEntry.getImageID();
		String image_name = ownerEntry.getImageName();

		int request_id = VCLConnector
				.addRequest(image_id, start_Time, duration);
		if (request_id > 0) {
			// The making_reservation_request is successful, we got the
			// request_id. We should create
			// a OBABean for this reservation.
			// At this time, IP address is not available.
			result_Bean = new OBABean(image_id, image_name, username, password,
					request_id, null, Platform.Windows, start_Time, null,
					duration, OBABean.UNKNOWN_STATUS, false, ownerEntry,
					auto_extend);
			// reservationsList.put(request_id, result_Bean);
		}

		return result_Bean;
	}

	/**
	 * Call the OBADBManager to save the Password of user in the SQLite database
	 * 
	 * @param username
	 * @param password
	 */
	public void savePasswd(String username, String password) {
		DBManager.storePasswd(username, password);
	}

	/**
	 * This method initialize the VCLConnector of the program It's called after
	 * authentication in the LoginDialog
	 * 
	 * @param username
	 * @param password
	 */
	public void setVCLConnector(String username, String password) {
		VCLConnector = new OBALogic(username, password);

		if (this.username == null) {
			this.username = username;
		}
		if (this.password == null) {
			this.password = password;
		}
	}

	/**
	 * This method look at the reservationList and find out the reservation
	 * corresponding to the input id
	 * 
	 * @param id
	 * @return
	 */
	public OBABean getOBAByRequestId(int id) {
		OBABean resultBean = null;

		if (reservationsList != null) {
			resultBean = reservationsList.get(id);
		}

		return resultBean;
	}

	public void createOBAentry(int image_id, String image_name,
			String image_desc) {
		// TODO Auto-generated method stub
		OBAEntry newObaEntry = new OBAEntry(image_id, image_name, image_desc,
				-1);
		this.newlyCreatedOBAEntryList.add(newObaEntry);

		this.currentConfigedOBAEntryList.add(newObaEntry);
	}

	public void putReverseOBAentryHashMap(String image_name, int image_id) {
		this.reverseOBAentryHashMap.put(image_name, image_id);
	}

	public OBAEntry getOBAEntryByTableID(int table_item_id) {
		return currentConfigedOBAEntryList.get(table_item_id);
	}

	public int getImageIDByImageName(String image_name) {
		return this.reverseOBAentryHashMap.get(image_name);
	}

	public OBABean getOBABean(int selectedOBABeanRequestID) {
		return this.reservationsList.get(selectedOBABeanRequestID);
	}

	public void addOBABean(OBABean aBean) {
		this.reservationsList.put(aBean.getRequestId(), aBean);
	}
	
	public void beforeQuit() {
		if (!newlyCreatedOBAEntryList.isEmpty()) {
			for (OBAEntry each_new_entry : newlyCreatedOBAEntryList) {
				DBManager.storeOBAEntry(username, each_new_entry);
			}
		}
	}
}
