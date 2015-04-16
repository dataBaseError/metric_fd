package metric_fd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class DBInterface {
	
	private String LOCALHOST = "127.0.0.1";
	private String PORT = "5432";
	private String user;
	private String password;
	private String db_name;
	
	private Connection conn;
	
	public DBInterface(String db_name, String name, String password) {
		this.db_name = db_name;
		this.user = name;
		this.password = password;
		this.conn = null;
	}
	
	public void connect() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		conn = null;
		
		try {
			String url = "jdbc:postgresql://" + LOCALHOST + ":" + PORT + "/" + this.db_name;
			Properties props = new Properties();
			props.setProperty("user", this.user);
			props.setProperty("password", this.password);
			//props.setProperty("ssl","true");
			conn = DriverManager.getConnection(url, props);
			//conn = DriverManager.getConnection(
			//		"jdbc:postgresql://" + LOCALHOST + ":" + PORT + "/" + DB_NAME, this.user,
			//		this.password);
		} catch (SQLException e) {
			this.conn = null;
			e.printStackTrace();
		}
		
		//return isConnected();
	}
	
	public boolean isConnected() {
		return this.conn != null;
	}
	
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap<String, Comparable > > get_sorted(String table_name, ArrayList<String > attributes, ArrayList<String > group_by, int limit) {
		Statement st;
		ArrayList<HashMap<String, Comparable > > result = new ArrayList<HashMap<String, Comparable > >();
		try {
			st = conn.createStatement();
			
			String attributes_list = linearize(attributes);
			String group_list = linearize(group_by);
			
			String limit_string = "";
			if(limit <= 0) {
				limit_string = " LIMIT " + limit;
			}
		
			ResultSet rs = st.executeQuery("SELECT " + attributes_list + " FROM " + table_name + " ORDER BY " + group_list + limit_string);
			ResultSetMetaData rsmd = rs.getMetaData();
			String type = null;
			Comparable val = null;
			while (rs.next())
			{
				result.add(new HashMap<String, Comparable >());
				for(int i = 0; i < attributes.size(); i++) {
					type = rsmd.getColumnTypeName(i+1);
					
					// TODO catch other types of integer values.
					if(type.equalsIgnoreCase("int4") || type.equalsIgnoreCase("int8")) {
						if(rs.getString(i+1) == null) {
							val = null;
						} else {
							val = (Comparable) rs.getInt(i+1);
						}
					}
					else if (type.equalsIgnoreCase("serial")) {
						val = (Comparable) rs.getLong(i+1);
					}
					// TODO catch other types of floating point values
					else if(type.equalsIgnoreCase("double")) {
						val = (Comparable) rs.getDouble(i+1);
					}
					else if(type.equalsIgnoreCase("char") || type.equalsIgnoreCase("varchar") || type.equalsIgnoreCase("text")) {
						val = (Comparable) rs.getString(i+1);
					}
					result.get(result.size() - 1).put(attributes.get(i), val) ;
				}
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void updateRows(String table, ArrayList<HashMap<String, Comparable > > values, String identifier, ArrayList<String > attributes) {
		Statement st;
		PreparedStatement ps = null;
		int k = 0;
		
		try {
			//st = conn.createStatement();
			//PreparedStatement ps;
			//conn.setAutoCommit(false);
			if(values.size() > 0) {
				String update_list = linearize_with_value(attributes, values.get(0));
				ps = conn.prepareStatement("UPDATE " + table + " SET " + update_list + " WHERE " + identifier + " = ?");
			}
			for(int i = 0; i < values.size(); i++) {
				for(k = 0; k < attributes.size(); k++) {
					//ps.setString(k+1, "" + attributes.get(k));
					
					if(values.get(i).get(attributes.get(k)) != null)
					{
						if(values.get(i).get(attributes.get(k)) instanceof Integer) {
							ps.setInt(k+1, (int) values.get(i).get(attributes.get(k)));
						}
						else {
							ps.setString(k+1, ""+values.get(i).get(attributes.get(k)));
						}
					}
					else {
						if(values.get(i).get(attributes.get(k)) instanceof Integer) {
							ps.setNull(k+1, java.sql.Types.INTEGER);
						}
						else {
							ps.setNull(k+1, java.sql.Types.NVARCHAR);
						}
					}
				}
				ps.setLong(k+1, (Long) values.get(i).get(identifier));
				//ps.
				
				//st.addBatch();
				ps.addBatch();
			}
			
			if(ps != null) {
				//int[] arr = 
				ps.executeBatch();
				
				ps.close();
			}
			//st.executeBatch();
			//conn.commit();
			//st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Exception: " + e.getNextException());
		}
	}
	
	private String linearize_with_value(ArrayList<String> attributes, HashMap<String, Comparable > values) {
		String attributes_list = "";
		String tempValue = "";
		for(int i = 0; i < attributes.size(); i++) {
			
			/*
			if(values.get(attributes.get(i)) != null)
			{ 
				tempValue = "'" + clean(""+ values.get(attributes.get(i))) + "'";
			}
			else {
				tempValue = "null";
			}*/
			tempValue = "?";
			
			attributes_list += attributes.get(i) + " = " + tempValue;
			if (i + 1 != attributes.size()) {
				attributes_list += ", ";
			}
		}
		return attributes_list;
	}

	private String linearize(ArrayList<String > attributes) {
		String attributes_list = "";
		for(int i = 0; i < attributes.size(); i++) {
			attributes_list += attributes.get(i);
			if (i + 1 != attributes.size()) {
				attributes_list += ", ";
			}
		}
		return attributes_list;
	}
	
}
