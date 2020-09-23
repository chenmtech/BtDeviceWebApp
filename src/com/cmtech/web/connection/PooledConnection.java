package com.cmtech.web.connection;

import java.sql.Connection;

public class PooledConnection {
	Connection connection = null; ///数据库连接
    boolean busy = false; ///该连接是否正使用
 
    //根据一个Connection构造一个PooledConnection
    public PooledConnection(Connection connection) {
        this.connection = connection;
    }
 
    /**
     * 获取该连接对象
    **/
    public Connection getConnection() {
        return connection;
    }
 
    /**
     * 设置该连接
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
 
    /**
     * 该连接是否空闲
     **/
    public boolean isBusy() {
        return busy;
    }
 
    /**
     * 设置该连接是否空闲
     **/
    public void setBusy(boolean busy) {
        this.busy = busy;
    }

}
