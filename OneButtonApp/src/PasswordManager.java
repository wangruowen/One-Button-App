import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class PasswordManager {

	static String name = "";
	static String pwd = "";

	public static void getData() {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager
					.getConnection("jdbc:sqlite:test.db");
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select * from login;");
			while (rs.next()) {
				name = rs.getString("name");
				pwd = rs.getString("pwd");
			}
			rs.close();
			conn.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static String getUserName() {
		return name;
	}

	public static String getPwd() {
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
			prep.setString(2, pwd);
			prep.addBatch();
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.setAutoCommit(true);
			conn.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
