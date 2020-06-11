package com.chensong.main.jdbc;

import com.chensong.main.entitys.NewConnection;
import com.chensong.main.exception.SQLBadGrammarException;
import com.chensong.main.support.ExucuteSQLTimeoutFunction;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class JDBCConnection {

    private static Map<String,Connection> connectionPool = new HashMap<>();
    /**
     * MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL
      */
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    // MySQL 8.0 以上版本 - JDBC 驱动名
    //static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    static{
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("jdbc驱动加载失败");
        }
    }

    /**
     * 获取连接池中的连接，未获取到直接连接，并将连接放到连接池
     * @param url
     * @param username
     * @param pwd
     * @return
     */
    private static Connection getConnectionFromPool(String url,String username,String pwd){
        Connection c = connectionPool.get(url);
        try {
            if(c == null || c.isClosed()){
                // 打开链接
                Connection conn = DriverManager.getConnection(url,
                        username,pwd);
                connectionPool.put(url,conn);
                c = conn;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return c;
    }

    /**
     * 查看所有表名
     * @param newConnection
     * @return
     */
    public static List<String> showTables(NewConnection newConnection) throws Exception{
        return executeSQLTimeout(i -> {
            List<String> tablesList = new ArrayList<>();
            Connection conn = null;
            Statement stmt = null;
            try{

                // 打开链接
                conn = getConnectionFromPool(newConnection.getUrl(),
                        newConnection.getUsernameTextField().getText(),newConnection.getPwdTextField().getText());
                // 执行查询
                stmt = conn.createStatement();
                String sql;
                sql = "show tables;";
                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()){
                    tablesList.add( rs.getString("Tables_in_"+newConnection.getSchemaTextField().getText()));
                }
                // 完成后关闭
                rs.close();
                stmt.close();
            }catch(SQLException se){
                // 处理 JDBC 错误
                se.printStackTrace();
            }catch(Exception e){
                // 处理 Class.forName 错误
                e.printStackTrace();
            }
            return tablesList;
        },null);

    }

    /**
     * 释放连接池
     */
    public static void releaseConncetions() {
        try{
            for(String url:connectionPool.keySet()){
                Connection con = connectionPool.get(url);
                if(con != null && !con.isClosed()){
                    con.close();
                    System.out.println("成功释放连接："+url);
                }
            }
        }catch(SQLException ex){
            System.out.println("关闭连接池出错");
        }
    }

    /**
     * 查询DDL语句
     * @param currentConnection
     * @param tableName
     * @return
     */
    public static String getShowCreateTable(NewConnection currentConnection, Object tableName) throws Exception{
        return executeSQLTimeout(t -> {
            Connection conn = connectionPool.get(currentConnection.getUrl());
            // 执行查询
            Statement stmt = null;
            ResultSet rs = null;
            String ddl = "";
            try{

                stmt = conn.createStatement();
                String sql;
                sql = "show create table "+t+";";
                rs = stmt.executeQuery(sql);
                while(rs.next()){
                    ddl = rs.getString("Create Table");
                }
                rs.close();
                stmt.close();
            }catch(SQLException ex){
                ex.printStackTrace();
            }finally {
                commonCloseStsAndRs(stmt,rs);

            }
            return ddl;
        },tableName.toString());

    }

    /**
     * 分页查单表记录
     * @param columnNames
     * @param currentConnection
     * @param tableName
     * @param start
     * @param end
     * @return
     */
    public static Object[][] getTableRowsLimit(String[] columnNames,NewConnection currentConnection, Object tableName, int start, int end) {
        Connection conn = connectionPool.get(currentConnection.getUrl());
        // 执行查询
        Statement stmt = null;
        ResultSet rs = null;
        String ddl = "";
        String[][] rows = new String[end][columnNames.length];
        try{
            stmt = conn.createStatement();
            String sqlTemplate;
            sqlTemplate = "select {fileds} from {tableName} limit {start},{end}; ";
            String sql = sqlTemplate.replace("{fileds}",String.join(",",columnNames)).replace("{tableName}",tableName.toString())
                    .replace("{start}",start+"").replace("{end}",end+"");
            rs = stmt.executeQuery(sql);
            int ir = 0;
            while(rs.next()){
                String[] row = new String[columnNames.length];
                int ic = 0;

                for(String column:columnNames){
                    row[ic] = rs.getString(column);
                    ic++;
                }
                rows[ir] = row;
                ir++;
            }
            rs.close();
            stmt.close();
        }catch(SQLException ex){
            ex.printStackTrace();
        }finally {
           commonCloseStsAndRs(stmt,rs);

        }
        return rows;
    }

    /**
     * 执行用户自定义语句的查询语句
     */
    public static Map<String,Object> getTableRowsByCustomer(NewConnection currentConnection, String sql) throws Exception {
       return executeSQLTimeout((t) ->{
            Map<String,Object> result = new HashMap<>();
            Connection conn = getConnectionFromPool(currentConnection.getUrl(),currentConnection.getUsernameTextField().getText(),currentConnection.getPwdTextField().getText());
            // 执行查询
            Statement stmt = null;
            ResultSet rs = null;
            String[][] rows =null;
            try{
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
                rs = stmt.executeQuery(t);
                rs.last();
                //记录总行数
                int totalRows = rs.getRow();
                String[] columnNames = getColumnsFromResutSetByTest(rs);
                result.put("columnNames",columnNames);
                int totalColumns = columnNames.length;
                rs.absolute(0);
                rows = new String[totalRows][totalColumns];
                result.put("rows",rows);
                int ir = 0;

                while(rs.next()){
                    String[] row = new String[totalColumns];
                    int ic = 0;

                    for(int i = 0;i < totalColumns;i++){
                        row[ic] = rs.getString(columnNames[i]);
                        ic++;
                    }
                    rows[ir] = row;
                    ir++;
                }
                rs.close();
                stmt.close();
            }catch(SQLException ex){
                String msg = ex.getMessage();
                throw new SQLBadGrammarException(msg);
            }finally {
               commonCloseStsAndRs(stmt,rs);

            }
            return result;
        },sql);
    }

    /**
     * 获取结果集中包含的列名数组
     * @param rs
     * @return
     */
    private static String[] getColumnsFromResutSetByTest(ResultSet rs) {
        List<String> columns = new ArrayList<>();
        try {
            ResultSetMetaData a =rs.getMetaData();
            int total = a.getColumnCount();
            for(int i = 1;i < total+1;i++){
                String columnName = a.getColumnName(i);
                columns.add(columnName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columns.toArray(new String[columns.size()]);
    }

    /**
     * 执行非查询语句
     * @param currentConnection
     * @param sql
     */
    public static String executeNonDQL(NewConnection currentConnection, String sql)throws Exception {
        return executeSQLTimeout((t) ->{
            Connection conn = getConnectionFromPool(currentConnection.getUrl(),currentConnection.getUsernameTextField().getText(),currentConnection.getPwdTextField().getText());
            // 执行查询
            Statement stmt = null;
            ResultSet rs = null;
            String result = "";
            try{
                stmt = conn.createStatement();
                stmt.execute(t);
                int effectRows = stmt.getUpdateCount();
                result = effectRows+" rows effected.";
                if(effectRows == -1){
                    rs = stmt.getResultSet();
                    rs.last();
                    String r = rs.getString(2);
                    result = r;
                }
                stmt.close();
            }catch(SQLException ex){
                String msg = ex.getMessage();
                throw new SQLBadGrammarException(msg);
            }finally {
                commonCloseStsAndRs(stmt,rs);

            }
            return result;
        },sql);

    }

    private static void commonCloseStsAndRs(Statement stmt, ResultSet rs) {
        try{
            if(stmt != null){
                stmt.close();
            }
            if(rs != null){
                rs.close();
            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }


    /**
     * 测试连接
     * @param dbUrl
     * @param username
     * @param pwd
     * @return
     */
    public  static boolean testConnect(String dbUrl,String username,String pwd) {
        boolean isConnected = false;
        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库"+dbUrl+"");
            conn = DriverManager.getConnection(dbUrl,
                    username,pwd);

            // 执行查询
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT 1 as rst;";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                int rst  = rs.getInt("rst");

                // 输出数据
                System.out.print("rst: " + rst);
                System.out.print("\n");
            }
            isConnected = true;
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null){
                    stmt.close();
                }
                if(conn != null){
                    conn.close();
                }
            }catch(Exception se2){
            }// 什么都不做
        }
        return isConnected;
    }

    /**
     * 超时执行SQL查询
     * @return
     */
    public static <R> R executeSQLTimeout(ExucuteSQLTimeoutFunction<String,R> f, String sql)throws Exception{
            R result = null;
            FutureTask<R> future = null;
            try {
                future = new FutureTask<>(()->f.apply(sql));
                executor.execute(future);
                result = future.get(60, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw e;
            } finally {
            future.cancel(true);
        }
        return result;
    }

    public static int getRowsCount(NewConnection currentConnection, Object tableName) throws Exception{
        return executeSQLTimeout(t -> {
            Connection conn = connectionPool.get(currentConnection.getUrl());
            // 执行查询
            Statement stmt = null;
            ResultSet rs = null;
            int total = 0;
            try{
                stmt = conn.createStatement();
                String sqlTemplate;
                sqlTemplate = "select count(1) from {tableName} ";
                String sql = sqlTemplate.replace("{tableName}",t);
                rs = stmt.executeQuery(sql);
                while(rs.next()){
                    total = Integer.parseInt(rs.getString(1));
                }
                rs.close();
                stmt.close();
            }catch(SQLException ex){
                ex.printStackTrace();
            }finally {
                commonCloseStsAndRs(stmt,rs);

            }
            return total;
        },tableName.toString());

    }
}
