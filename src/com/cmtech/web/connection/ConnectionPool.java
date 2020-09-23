package com.cmtech.web.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ConnectionPool {
	private String jdbcDriver           = ""; // 数据库驱动
	 
    private String dbUrl                = ""; // 数据 URL
 
    private String dbUsername           = ""; // 数据库用户名
 
    private String dbPassword           = ""; // 数据库用户密码
 
    private String testTable            = ""; // 测试连接是否可用的测试表名，默认没有测试表
 
    private int initialConnections      = 10; // 连接池的初始大小
 
    private int incrementalConnections  = 5;// 连接池自动增加的大小
 
    private int maxConnections          = 800; // 连接池最大的大小
 
    private List<PooledConnection> connections            = null; // 存放连接池中数据库连接的列表 , 初始时为 null
 
 
    public ConnectionPool(String jdbcDriver, String dbUrl, String dbUsername, String dbPassword) {
        this.jdbcDriver = jdbcDriver;
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
 
        try {
            createPool();//初始化时创建连接池
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    void setIncrementalConnections(int incrementalConnections) {
        this.incrementalConnections = incrementalConnections;
    }
 
    void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
 
    /**
     * 创建数据库连接池，连接池初始数量由initialConnections确定
     **/
    private synchronized void createPool() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
        if(connections != null){
            return; ///如果已经创建了连接池，返回这个连接池
        }
 
//        Driver driver = (Driver)(Class.forName(this.jdbcDriver).newInstance());
//        DriverManager.registerDriver(driver); ///注册驱动
 
        Class.forName(this.jdbcDriver); //只是在创建连接池时加载一次驱动
        connections = new ArrayList<>();
        //根据initialConnections创建连接数目
        createConnection(this.initialConnections);
    }
 
    /**
     * @param initialConnections 要创建的数据库连接数目
     **/
    private void createConnection(int initialConnections) throws SQLException {
        //根据initialConnections创建连接
        for (int x = 0; x < initialConnections; x++){
            ///如果连接数目已经达到最大，退出不创建连接
            if(this.maxConnections > 0 && this.connections.size() > this.maxConnections){
                break;
            }///end if
            try {
                ///将Connection对象用PooledConnection包装放入List当中
                connections.add(new PooledConnection(newConnection()));
            } catch (SQLException e) {
                System.out.println("创建数据库连接失败！");
                throw new SQLException();
            }
        }
    }
 
    /**
     * 创建一个新的连接，并且返回这个连接
     **/
    private Connection newConnection() throws SQLException {
        ///创建一个数据库连接
        Connection connection = DriverManager.getConnection(dbUrl, dbUsername,dbPassword);
        ///如果是第一次创建数据连接，检查数据库，获取数据库最大连接数目
        if(connections.size() == 0){
            DatabaseMetaData metaData = connection.getMetaData();
            int driverMaxConnections = metaData.getMaxConnections();
 
            //数据库返回0则说明无最大连接数目限制，将最大连接数据设置为数据库返回的数目
            if(driverMaxConnections > 0 && this.maxConnections > driverMaxConnections){
                this.maxConnections = driverMaxConnections;
            }
        }///end if
        return  connection; ///返回新创建的数据库连接
    }
 
    /**
     * 同步获取当前可用的一个数据库连接对象
     **/
     synchronized Connection getConnection() throws SQLException {
        ///确保连接池已经被创建完成
        if(connections == null){
            return null;
        }
        Connection connection = getFreeConnection(); ///获取一个空闲的连接
 
        //如果当前没有可用的连接，等一会再试
        while (connection == null){
            wait(250);
            connection = getFreeConnection();
        }
 
        return connection;
    }
 
    private Connection getFreeConnection() throws SQLException {
        Connection connection = findFreeConnection();
 
        if(connection == null){
            //如果当前连接池没有可用连接了，创建一些连接
            createConnection(incrementalConnections);
            ///重新从池中查找空闭连接
            connection = findFreeConnection();
            if(connection == null)
                return null;///如果还是找不到则返回空
        }
        return connection;
    }
 
    private Connection findFreeConnection() {
        Connection connection = null;
        PooledConnection pConn = null;
        ///获得连接池所有连接
        Iterator<PooledConnection> iterator = connections.iterator();
        ///遍历所有连接，查找可用连接
        while (iterator.hasNext()){
            pConn = (PooledConnection)iterator.next();
            ///如果当前连接是空闲的
            if(!pConn.isBusy()){
                ///当前连接不忙，获取它的连接设当前连接忙碌
                connection = pConn.getConnection();
                pConn.setBusy(true);
                //测试当前连接是否可用
                if(!testConnection(connection)){
                    ///如果不可用，创建一个新的连接并替换原来那个
                    try{
                        connection = newConnection();
                    } catch (SQLException e) {
                        System.out.println("创建数据库失败！" + e.getMessage());
                        return null;
                    }
                    pConn.setConnection(connection);
                }
                break;///已经找到一个可用连接
            }
        }
        return connection;
    }
 
    /**
     * 测试一个连接是否可用，如果不可用,关掉它并且返回false
     * @param connection 需要测试的连接
     **/
    private boolean testConnection(Connection connection) {
        try {
            if(testTable.equals("")){
                connection.setAutoCommit(true);
            }else{
                Statement stmt = connection.createStatement();
                stmt.execute("select count(*) from " + testTable);
            }
        } catch (SQLException e) {
            closeConnection(connection);
            return false;
        }
        ///连接可用
        return true;
    }
    /**
     * 将指定数据库连接对象返回到连接池，下次可以重用
     *@param connection 要返回连接池的连接
     */
    public void returnConnection(Connection connection){
        ///确保连接池存在
        if(connections  == null){
            System.out.println("连接池不存在，无法返回连接！");
            return;
        }
        PooledConnection pConn = null;
        Iterator<PooledConnection> iterator =connections.iterator();
        while (iterator.hasNext()){
            pConn = (PooledConnection)iterator.next();
            if(pConn.getConnection() == connection){
                pConn.setBusy(false); ///将该连接设置为空闲状态
                break;
            }
        }
    }
 
    /**
     * 刷新当前连接池里面的所有连接
     **/
    public synchronized void refreshConnection() throws SQLException {
        ///确保连接池存在
        if(connections  == null){
            System.out.println("连接池不存在，无法返回连接！");
            return;
        }
        Iterator<PooledConnection> iterator = this.connections.iterator();
        PooledConnection pConn = null;
        while (iterator.hasNext()){
            pConn = (PooledConnection)iterator.next();
            if (pConn.isBusy()){
                wait(5000);
            }///当前连接不空闲，等待5秒
            closeConnection(pConn.connection);
            pConn.setConnection(newConnection());
            pConn.setBusy(false);
        }
    }
 
    /**
     * 关闭连接池中所有的连接，并清空连接池。
     */
    public synchronized void closeConnectionPool() throws SQLException {
 
        // 确保连接池存在，如果不存在，返回
        if (connections == null) {
            System.out.println(" 连接池不存在，无法关闭 !");
            return;
        }
 
        for (PooledConnection pConn : this.connections){
            pConn = (PooledConnection)pConn;
            if (pConn.isBusy()){
                wait(5000);
            }///当前连接不空闲，等待5秒
            closeConnection(pConn.getConnection());
        }
        this.connections.clear();
        this.connections = null;
    }
    /**
     * 关闭一个数据库连接
     *
     * @param conn 需要关闭的数据库连接
     */
    private void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println(" 关闭数据库连接出错： " + e.getMessage());
        }
    }
    private void wait(int mills){
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            System.out.println("等待线程中断了~");
            e.printStackTrace();
        }
    }

}
