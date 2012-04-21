public class Program {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// String pass = "alo";
		// String encrpass = PasswordManager.encrypt(pass);
		// String decrpass = PasswordManager.decrypt(encrpass);
		// System.out.print(encrpass);
		// System.out.print(decrpass);
		OBAController controller = OBAController.getInstance();
		OBADBManager DBManager = OBADBManager.getInstance();
		controller.initialize();
		System.exit(0);
	}
}
