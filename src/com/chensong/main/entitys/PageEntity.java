package com.chensong.main.entitys;

import com.chensong.main.jdbc.JDBCConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PageEntity {
    private int currPage = 1;
    private int totalPage;
    private int pageSize = 1000;
    private NewConnection newConnection;
    private int totalRows;
    private String tableName;
    private DefaultTableModel defaultTableModel;
    private String pageInfo;
    private JLabel pageInfoField;
    public PageEntity(NewConnection connection,String tableName,int totalRows,DefaultTableModel defaultTableModel){
        this.tableName = tableName;
        this.totalRows = totalRows;
        this.defaultTableModel = defaultTableModel;
        this.newConnection = connection;
        int remain = this.totalRows%this.pageSize;
        this.totalPage = remain == 0?this.totalRows/this.pageSize:this.totalRows/this.pageSize+1;

    }

    public int getCurrPage() {
        return currPage;
    }

    public void setCurrPage(int currPage) {
        this.currPage = currPage;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public NewConnection getNewConnection() {
        return newConnection;
    }

    public void setNewConnection(NewConnection newConnection) {
        this.newConnection = newConnection;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public String getTableName() {
        return tableName;
    }

    public DefaultTableModel getDefaultTableModel() {
        return defaultTableModel;
    }

    public void setDefaultTableModel(DefaultTableModel defaultTableModel) {
        this.defaultTableModel = defaultTableModel;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void prev(){
        if(this.currPage == 1){
            return;
        }
        this.currPage -- ;
        refreshInfo();
    }

    public void next(){
        this.currPage ++ ;
        refreshInfo();
    }

    public void first(){
        this.currPage = 1;
        refreshInfo();
    }

    public void last(){
        this.currPage = this.totalPage;
        refreshInfo();
    }

    public int getOffset(){
        return (this.currPage-1)*this.pageSize;
    }

    public void refresh(String tableName)throws Exception{
        this.totalRows = JDBCConnection.getRowsCount(this.newConnection,tableName);
        int remain = this.totalRows%this.pageSize;
        this.totalPage = remain == 0?this.totalRows/this.pageSize:this.totalRows/this.pageSize+1;
        refreshInfo();
    }

    public String getPageInfo() {
        return this.currPage+"/"+this.totalPage+"  "+this.totalRows;
    }

    public void setPageInfo(String pageInfo) {
        this.pageInfo = pageInfo;
    }

    public JLabel getPageInfoField() {
        return pageInfoField;
    }

    public void setPageInfoField(JLabel pageInfoField) {
        this.pageInfoField = pageInfoField;
    }

    public void refreshInfo(){
        this.getPageInfoField().setText(this.getPageInfo());
    }
}
