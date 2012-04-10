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
		OBADBManager DBManager = OBADBManager.getInstance();
		HashMap<String, String> savedUserInfos = DBManager.getRememberPasswd();
		this.reservationsList = new HashMap<Integer, OBABean>();
		this.reverseOBAentryHashMap = new HashMap<String, Integer>();
		if (loginWithSavePasswd(savedUserInfos)) {
			setVCLConnector(username, password);
			showMainOBA();
		} else {
			loginOBA = new LoginDialog();
			loginOBA.show();
		}

	}

	public void showMainOBA() {
		// controller.loadCurrentReservations();
		// HashMap<Integer, String> list_image =
		// VCLConnector.getAvailableImages();
		// System.out.println(list_image);
		// Calendar now = Calendar.getInstance();
		// int requestID = VCLConnector.sendRequestReservation(1008, now , 60);
		// VCLConnector.getConnectData(1776879);
		// VCLConnector.getCurrentReservations();
		// VCLConnector.getConnectData(1776900);
		// VCLConnector.getConnectData(1776943);
		if (mainOBA == null) {
			mainOBA = new MainOBAGUI();
		}
		mainOBA.open();
	}

	/**
	 * Try to login with this couple of username and password
	 * 
	 * @param savedUserInfos
	 * @return if successful initialize the field username and password of
	 *         controller and return true if not delete the store password in
	 *         the database, return false;
	 */
	private boolean loginWithSavePasswd(HashMap<String, String> savedUserInfos) {
		// TODO Auto-generated method stub
		return false;
	}

	public void loadCurrentReservationsVCL() {

	}

	/**
	 * Use the VCLConnector to get the current reservations from the VCL server
	 * and write it down in the reservationsList variable Check the
	 * reservationsList variable and return it
	 * 
	 * @return reservationsList
	 */
	public HashMap<Integer, OBABean> getCurrentReservations() {
		if (reservationsList == null) {
			reservationsList = VCLConnector.getCurrentReservations();
			return reservationsList;
		} else {
			return reservationsList;
		}
	}

	/*
	 * This method returns the existing preconfigured OBA list from the database
	 */
	public OBAEntry[] getPreconfigedOBAEntries() {
		// TODO call the imageManager's getData

		return null;
	}

	/**
	 * Try to launch an list of OBA to login to a reservation take out each OBA,
	 * and call it function OBA.start();
	 * 
	 * @param selectedItems
	 *            from the table of MainOBAGUI
	 * @return true if login success, false if not.
	 */
	public OBABean launchOBA(int image_id, String image_name,
			Calendar start_Time, int duration) {
		OBABean result_Bean = null;

		int request_id = VCLConnector
				.addRequest(image_id, start_Time, duration);
		if (request_id > 0) {
			// The making_reservation_request is successful, we got the
			// request_id. We should create
			// a OBABean for this reservation.
			// At this time, IP address is not available.
			result_Bean = new OBABean(image_id, image_name, username, password,
					request_id, null, Platform.Windows, start_Time, null,
					duration, false);
			reservationsList.put(request_id, result_Bean);
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
		// TODO Auto-generated method stub
		OBADBManager DBManager = OBADBManager.getInstance();
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
		// TODO Auto-generated method stub
		VCLConnector = new OBALogic(username, password);
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

	}

	public void putReverseOBAentryHashMap(String image_name, int image_id) {
		this.reverseOBAentryHashMap.put(image_name, image_id);
	}
}
