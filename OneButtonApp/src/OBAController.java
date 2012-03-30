import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.widgets.TableItem;


public class OBAController {
	private static OBAController instance = null;
	private ArrayList<OBABean> reservationsList;
	private OBALogic VCLConnector;
	private String username;
	private String password;
	private LoginDialog loginOBA;
	private MainOBAGUI mainOBA;
	protected OBAController() {
		// Exists only to defeat instantiation.
	}
	
	/**
	 * Singleton pattern
	 * If the controller is already created, just return the instance
	 * if not, create it and return the instance
	 * @return	the unique controller of the program
	 */
	public static OBAController getInstance() {
		if(instance == null) {
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
		if (loginWithSavePasswd (savedUserInfos)) {
			setVCLConnector(username, password);
			showMainOBA();
		} else {
			loginOBA = new LoginDialog();
			loginOBA.show();
		}

	}

	public void showMainOBA() {
		//controller.loadCurrentReservations();
		if (mainOBA == null) {
			mainOBA = new MainOBAGUI();
		}
		mainOBA.open();
	}

	/**
	 * Try to login with this couple of username and password
	 * 
	 * @param savedUserInfos
	 * @return	if successful initialize the field username and password of controller and return true
	 * 			if not delete the store password in the database, return false;
	 */
	private boolean loginWithSavePasswd(HashMap<String, String> savedUserInfos) {
		// TODO Auto-generated method stub
		return false;
	}

	public void loadCurrentReservationsVCL() {

	}
	
	/**
	 * Use the VCLConnector to get the current reservations from the VCL server
	 * and write it down in the reservationsList variable
	 * Check the reservationsList variable and return it
	 * @return	reservationsList
	 */
	public ArrayList<OBABean> getCurrentReservations() {
		if (reservationsList != null) {
			reservationsList = VCLConnector.getCurrentReservations();
			return reservationsList;
		} else {
			return reservationsList;
		}
	}
	/**
	 * Get the active reservation from the SQLite Database if exist 
	 * An active reservation is an reservation which will be run default
	 * @return	The OBABean of this active reservation if exist and null if not
	 */
	public OBABean getActiveReservation() {
		return null;
	}
	
	/**
	 * Try to launch an list of OBA to login to a reservation
	 * take out each OBA, and call it function OBA.start();
	 * @param selectedItems	from the table of MainOBAGUI
	 * @return	true if login success, false if not.
	 */
	public boolean launchOBA (TableItem[] selectedItems) {
//			// Get all information needed for making a reservation
//			int image_id = Integer.parseInt(selectedItems[0].getText(0));
//			String name = selectedItems[0].getText(1);
//			String startTime = "now";
//			String duration = "60";
//			mainTabFolder.setSelection(2);
	//
//			OBALogic one_OBA_instance = new OBALogic(image_id, name, username,
//					password, OBALogic.Platform.Windows, startTime, duration);
//			activeOBA.add(one_OBA_instance);
	//
//			updateStatusTable(one_OBA_instance);
			// if (one_OBA_instance.makeReservation()) {
			// // If succeed in making the reservation, we should
			// // switch to the status tab to show the
			// // current status of the reservation.
			// updateStatusTable(one_OBA_instance);
			// } else {
			// // make resevation fail.
			// }
//		}
		return false;
	}

	/**
	 * Call the OBADBManager to save the Password of user in the SQLite database
	 * @param username
	 * @param password
	 */
	public void savePasswd(String username, String password) {
		// TODO Auto-generated method stub
		OBADBManager DBManager = OBADBManager.getInstance();
		DBManager.storePasswd (username, password);
		
	}

	/**
	 * This method initialize the VCLConnector of the program
	 * It's called after authentication in the LoginDialog
	 * @param username
	 * @param password
	 */
	public void setVCLConnector(String username, String password) {
		// TODO Auto-generated method stub
		VCLConnector = new OBALogic(username, password);
	}
	
	/**
	 * This method look at the reservationList and find out the reservation corresponding to the input id
	 * @param id
	 * @return
	 */
	public OBABean getOBAById(int id) {
		return null;
	}
}
