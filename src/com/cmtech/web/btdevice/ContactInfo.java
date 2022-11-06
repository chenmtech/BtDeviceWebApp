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

public class ContactInfo implements IJsonable {
    public static final int WAITING = 0;
    public static final int AGREE = 1;

    //-----------------------------------------实例变量
    private int fromId;

    private int toId;

    private int status;

    private long time;
    

	public static boolean insert(int fromId, int toId, long time) {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into ContactInfo (fromId, toId, status, time) values (?, ?, ?, ?)";
		try {
			ps = conn.prepareStatement(sql);
			int begin = 1;
			ps.setInt(begin++, fromId);
			ps.setInt(begin++, toId);
			ps.setInt(begin++, WAITING);
			ps.setLong(begin++, time);
			if(ps.executeUpdate() != 0)
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
	
	public static boolean delete(int accountId, int contactId) {
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		// 删除数据库中的记录
		PreparedStatement ps = null;
		String sql = "delete from ContactInfo where ((fromId = ? and toId = ?) or (fromId = ? and toId = ?)) and status = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, accountId);
			ps.setInt(2, contactId);
			ps.setInt(3, contactId);
			ps.setInt(4, accountId);
			ps.setInt(5, AGREE);
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
		String sql = "select id from ContactInfo where fromId = ? and toId = ?";
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
    
    public static boolean agree(int fromId, int toId) {
    	Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update ContactInfo set status = ? where fromId = ? toId = ? and status = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, AGREE);
			ps.setInt(2, fromId);
			ps.setInt(3, toId);
			ps.setInt(4, WAITING);
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
    
    public static List<ContactInfo> retrieveContactInfo(int accountId) {
    	Connection conn = DbUtil.connect();		
		if(conn == null) return null;		
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select * from ContactInfo where fromId = ? or toId = ?";			
			ps = conn.prepareStatement(sql);
			ps.setInt(1, accountId);
			ps.setInt(2, accountId);
			rs = ps.executeQuery();

			List<ContactInfo> found = new ArrayList<>();
			while(rs.next()) {
				int fromId = rs.getInt("fromId");
				int toId = rs.getInt("toId");
				int status = rs.getInt("status");
				long time = rs.getLong("time");
				// 如果是账户发起的申请，且还在等待对方审核，就不返回了
				if(fromId == accountId && status==WAITING)
					continue;
				found.add(new ContactInfo(fromId, toId, status, time));
			}			
			return found;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
    }
    
    public ContactInfo(int fromId, int toId, int status, long time) {
        this.fromId = fromId;
        this.toId = toId;
        this.status = status;
        this.time = time;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }    


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException {
    	fromId = json.getInt("fromId");
    	toId = json.getInt("toId");
		status = json.getInt("status");
		time = json.getLong("time");
    }

    @Override
    public JSONObject toJson() throws JSONException {
    	JSONObject json = new JSONObject();
		json.put("fromId", fromId);
		json.put("toId", toId);
		json.put("status", status);
		json.put("time", time);
	
		return json;
    }

}
