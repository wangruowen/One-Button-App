
public class Program {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OBAController controller = OBAController.getInstance();
		OBADBManager DBManager = OBADBManager.getInstance();
		controller.initialize();
	}
}
