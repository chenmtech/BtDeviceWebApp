package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.cmtech.web.util.DbUtil;

public class ShareInfo implements IJsonable {
    public static final int DENY = 0;
    public static final int WAITING = 1;
    public static final int AGREE = 2;

    //-----------------------------------------实例变量
    private int fromId;

    private int toId;

    private String fromUserName;

    private String toUserName;

    private int status;
    

	public static boolean insert(int fromId, int toId) {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into ShareInfo (fromId, toId, status) values (?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			int begin = 1;
			ps.setInt(begin++, fromId);
			ps.setInt(begin++, toId);
			ps.setInt(begin++, WAITING);
			if(ps.executeUpdate() != 0)
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
	
    public static int getId(int fromId, int toId) {
    	Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from ShareInfo where fromId = ? and toId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, fromId);
			ps.setInt(2, toId);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return INVALID_ID;	
    }
    
    public static boolean changeStatus(int fromId, int toId, int status) {
    	Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update ShareInfo set status = ? where fromId = ? and toId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, status);
			ps.setInt(2, fromId);
			ps.setInt(3, toId);
			if(ps.executeUpdate() != 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
    }
    
    public static List<ShareInfo> retrieveShareInfo(int accountId) {
    	Connection conn = DbUtil.connect();		
		if(conn == null) return null;		
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select * from ShareInfo where fromId = ? or toId = ?";			
			ps = conn.prepareStatement(sql);
			ps.setInt(1, accountId);
			ps.setInt(2, accountId);
			rs = ps.executeQuery();

			List<ShareInfo> found = new ArrayList<>();
			while(rs.next()) {
				int fromId = rs.getInt("fromId");
				int toId = rs.getInt("toId");
				String fromUserName = rs.getString("fromUserName");
				String toUserName = rs.getString("toUserName");
				int status = rs.getInt("status");
				found.add(new ShareInfo(fromId, toId, fromUserName, toUserName, status));
			}			
			return found;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
    }
    
    public ShareInfo(int fromId, int toId, int status) {
    	this(fromId, toId, "", "", status);
    }

    public ShareInfo(int fromId, int toId, String fromUserName, String toUserName, int status) {
        this.fromId = fromId;
        this.toId = toId;
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
        this.status = status;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException {
    	fromId = json.getInt("fromId");
    	toId = json.getInt("toId");
		fromUserName = json.getString("fromUserName");
		toUserName = json.getString("toUserName");
		status = json.getInt("status");
    }

    @Override
    public JSONObject toJson() throws JSONException {
    	JSONObject json = new JSONObject();
		json.put("fromId", fromId);
		json.put("toId", toId);
		json.put("fromUserName", fromUserName);
		json.put("toUserName", toUserName);
		json.put("status", status);
	
		return json;
    }

}
