import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

class image
{
	public int imgID;
	public String imgName;
	public String imgDesc;
	
}
public class imageManager {

	int imageID;
	String imageName, ImageDesc;
	
	/*
	 * Input: Username
	 * Output: A class array consisting of a list of Image_ID, Image_Name and Image_Description for the user identified by username
	 */
	public static image[] getData(String username)
	{
		ResultSet rs=null, rs1=null,rs2=null;
		int count=0;
		image image_list[]=null;
		try
		{
		Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
        Statement stat = conn.createStatement(); 
        DatabaseMetaData dbm = conn.getMetaData();
        rs1 = dbm.getTables(null, null, "image", null);
        if (rs1.next())
        {
        	rs2=stat.executeQuery("select COUNT(*) from image where user_name='"+username+"';");
        	 while (rs2.next()){
        		  count = rs2.getInt(1);
        		  }
        	rs = stat.executeQuery("select * from image where user_name='"+username+"';");
        if(rs!=null)
        {
        image_list=new image[count];
        int counter=0;
        while (rs.next()) {
        	image_list[counter]=new image();
        	image_list[counter].imgID=rs.getInt("image_id");
        	image_list[counter].imgName=rs.getString("image_name");
        	image_list[counter].imgDesc=rs.getString("image_desc");
        	counter=counter+1;
        }
        }
        }
        rs1.close();
        conn.close();
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		return image_list;
	}
	
	
	/*
	 * Input: Username, ImageID, ImageName, ImageDesc
	 * Output: Nothing
	 * Stores the details into the Image table
	 */
	public static void storeData(String username, int imageId, String imageName, String imageDesc)
	{
		try
		{
		Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
        Statement stat = conn.createStatement();
        DatabaseMetaData dbm = conn.getMetaData();
        ResultSet rs1 = dbm.getTables(null, null, "image", null);
        if (!rs1.next())
        {
        stat.executeUpdate("create table image (user_name, image_id, image_name, image_desc);");
        }
        PreparedStatement prep = conn.prepareStatement(
            "insert into image values (?, ?, ?, ?);");
        prep.setString(1, username);
        prep.setInt(2,imageId);
        prep.setString(3, imageName);
        prep.setString(4, imageDesc);
        prep.addBatch();
        conn.setAutoCommit(false);
        prep.executeBatch();
        conn.setAutoCommit(true);
        conn.close();
        rs1.close();
        }
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}
}
