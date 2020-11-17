package com.cmtech.web.btdevice;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;
import static com.cmtech.web.MyConstant.*;
import com.cmtech.web.util.Base64;

public class Account implements IDbOperation, IJsonable {
	private static final int HOUR_NEED_LOGIN = 24*30;
	
	public static final int LOGIN_WAY_PASSWORD = 0;
    public static final int LOGIN_WAY_QR_CODE = 1;
    public static final int LOGIN_WAY_QQ = 2;
    public static final int LOGIN_WAY_WECHAT = 3;
    
	private int id = INVALID_ID;
	private String ver = DEFAULT_VER;
	private String userName;
	private String password;
	private String nickName;
	private String note;
	private byte[] iconData;
	private int gender = 0;
    private long birthday = INVALID_TIME;
    private int weight = 0;
    private int height = 0;
    private long lastLoginTime = 0;
	
	public Account(int id) {
		this.id = id;
	}
	
	private Account(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}
	
	public static int login(String userName, String password) {
		int id = getIdFromDb(userName, password);
		
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		PreparedStatement ps = null;
		String sql = "update Account set lastLoginTime=? where id = ?";
		try {
			long lastLoginTime = new Date().getTime();
			ps = conn.prepareStatement(sql);
			ps.setLong(1, lastLoginTime);
			ps.setInt(2, id);
			if(ps.executeUpdate() != 0) {
				return id;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return INVALID_ID;		
	}
	
	public static boolean signUp(String userName, String password) {
		if(Account.exist(userName)) return false;
		
		return new Account(userName, password).insert();
	}
	
	public boolean isNeedWebLogin() {
		long currentTime = new Date().getTime();
		
		return (currentTime - lastLoginTime > HOUR_NEED_LOGIN*60*60*1000L);
	}
	
	@Override
	public void fromJson(JSONObject json) {
		ver = json.getString("ver");
		//userName = json.getString("userName");
		//password = json.getString("password");
		nickName = json.getString("nickName");
		note = json.getString("note");
		String iconStr = json.getString("iconStr");
		if(iconStr.equals(""))
			iconData = new byte[0];
		else
			iconData = Base64.decode(iconStr, Base64.DEFAULT);
		gender = json.getInt("gender");
		birthday = json.getLong("birthday");
		weight = json.getInt("weight");
		height = json.getInt("height");
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("ver", ver);
		//json.put("userName", userName);
		//json.put("password", password);
		json.put("nickName", nickName);
		json.put("note", note);
		if(iconData == null)
			json.put("iconStr", "");
		else
			json.put("iconStr", Base64.encodeToString(iconData, Base64.DEFAULT));
		json.put("gender", gender);
		json.put("birthday", birthday);
		json.put("weight", weight);
		json.put("height", height);
		json.put("needWebLogin", isNeedWebLogin());
	
		return json;
	}
	
	@Override
	public int getId() {
		return id;		
	}
	
	@Override
	public boolean retrieve() {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select ver, userName, password, nickName, note, icon, gender, birthday, weight, height, lastLoginTime "
				+ "from Account where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(rs.next()) {
				getFromResultSet(rs);
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;		
	}
	
	private void getFromResultSet(ResultSet rs) throws SQLException {
		ver = rs.getString("ver");
		userName = rs.getString("userName");
		password = rs.getString("password");
		nickName = rs.getString("nickName");
		note = rs.getString("note");
		Blob b = rs.getBlob("icon");
		if(b == null || b.length() < 1) 
			iconData = null;
		else
			iconData = b.getBytes(1, (int)b.length());
		gender = rs.getInt("gender");
		birthday = rs.getLong("birthday");
		weight = rs.getInt("weight");
		height = rs.getInt("height");
		lastLoginTime = rs.getLong("lastLoginTime");
	}

	@Override
	public boolean insert() {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into Account (ver, userName, password) values (?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, ver);
			ps.setString(2, userName);
			ps.setString(3, password);
			if(ps.executeUpdate() != 0)
				return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
	
	@Override
	public boolean update() {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update Account set nickName=?, note=?, icon=?, gender=?, birthday=?, weight=?, height=? where id = ?";
		try {
			ps = conn.prepareStatement(sql);
			int index = 1;
			ps.setString(index++, nickName);
			ps.setString(index++, note);
			Blob b = conn.createBlob();
			b.setBytes(1, iconData);
			ps.setBlob(index++, b);
			ps.setInt(index++, gender);
			ps.setLong(index++, birthday);
			ps.setInt(index++, weight);
			ps.setInt(index++, height);
			ps.setInt(index++, id);
			if(ps.executeUpdate() != 0) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
	
	@Override
	public boolean delete() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean retrieveBasicInfo() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String toString() {
		return "userName="+userName+",password="+password+",nickName="+nickName+",note="+note+",iconDataLength="+iconData.length;
	}
	
	public static boolean exist(int id) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select 1 from Account where id = ? limit 1";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;		
	}
	
	private static boolean exist(String userName) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select 1 from Account where userName = ? limit 1";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, userName);
			rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;		
	}
	
	private static int getIdFromDb(String userName, String password) {
		Connection conn = DbUtil.connect();		
		if(conn == null) return INVALID_ID;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from Account where userName = ? and password = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, userName);
			ps.setString(2, password);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getInt("id");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return INVALID_ID;		
	}
}
