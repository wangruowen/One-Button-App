import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class PasswordCheckingDialog {
	private OBAController controller;
	private Shell shlObaLogin;
	private Display display;
	private ProgressBar bar;

	public PasswordCheckingDialog() {
		display = new Display();
		shlObaLogin = new Shell(display);
		shlObaLogin.setText("OBA Login");
		shlObaLogin.setSize(300, 150);

		bar = new ProgressBar(shlObaLogin, SWT.SMOOTH | SWT.INDETERMINATE);
		bar.setBounds(10, 51, 262, 32);

		Label lblLoginUsingSaved = new Label(shlObaLogin, SWT.NONE);
		lblLoginUsingSaved.setAlignment(SWT.CENTER);
		lblLoginUsingSaved.setBounds(10, 10, 262, 20);
		lblLoginUsingSaved.setText("Login using saved password...");
	}

	public boolean checkPassword(String[] savedUserInfos) {
		// center the dialog screen to the monitor
		Rectangle bounds = display.getBounds();
		Rectangle rect = shlObaLogin.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shlObaLogin.setLocation(x, y);
		shlObaLogin.open();
		for (int i = 0; i < 100; i++) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bar.setSelection(i);
		}

		controller = OBAController.getInstance();
		boolean result = controller.loginWithSavePasswd(savedUserInfos);
		display.dispose();
		return result;
	}
}
