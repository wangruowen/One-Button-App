import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

public class PasswordManager {

	static String name = "";
	static String pwd = "";
	static Crypto crypto;

	public PasswordManager() {
		try {
			String as = getMACAddress();
			as = as.substring(0, 22);
			as = as + "aldez2@";
			crypto = new Crypto(as);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void getData() {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager
					.getConnection("jdbc:sqlite:test.db");
			Statement stat = conn.createStatement();
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet rs = dbm.getTables(null, null, "login", null);
			if (rs.next()) {
				ResultSet rs1 = stat.executeQuery("select * from login;");
				while (rs1.next()) {
					name = rs1.getString("name");
					String encpwd = rs1.getString("pwd");
					// System.out.print(encpwd);
					pwd = crypto.decrypt(encpwd);
					// System.out.print(pwd);
				}
				rs1.close();
			}
			rs.close();
			conn.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public String getUserName() {
		return name;
	}

	public String getPwd() {
		return pwd;
	}

	public static void storeData(String uname, String upwd) {
		name = uname;
		pwd = upwd;
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager
					.getConnection("jdbc:sqlite:test.db");
			Statement stat = conn.createStatement();
			stat.executeUpdate("drop table if exists login;");
			stat.executeUpdate("create table login (name, pwd);");
			PreparedStatement prep = conn
					.prepareStatement("insert into login values (?, ?);");

			prep.setString(1, name);
			String encPwd = crypto.encrypt(pwd);
			prep.setString(2, encPwd);
			System.out.print(encPwd);
			prep.addBatch();
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.setAutoCommit(true);
			conn.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public String getMACAddress() {
		try {
			String ret = new String();
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface nif = interfaces.nextElement();
				byte[] lBytes = nif.getHardwareAddress();
				StringBuffer lStringBuffer = new StringBuffer();

				if (lBytes != null) {
					for (byte b : lBytes) {
						lStringBuffer.append(String.format("%1$02X ", new Byte(
								b)));
					}
				}

				// System.out.println(lStringBuffer);
				ret = ret + lStringBuffer.toString();
			}
			// System.out.print("\n" + ret + "\n");
			return ret;
		} catch (SocketException e) {

			e.printStackTrace();
		}
		return null;
	}

	public static void deleteTable() {
		// System.out.println("PasswordManager called");
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager
					.getConnection("jdbc:sqlite:test.db");
			Statement stat = conn.createStatement();
			stat.executeUpdate("drop table if exists login;");
			conn.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
