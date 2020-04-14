package com.chensong.main.entitys;


import com.chensong.main.jdbc.JDBCConnection;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装新建连接的所有组件
 */
public class NewConnection {
    private static String filePath = "resources/connections.conf";
    private static String seperator = ":";

    private JLabel connectionNameLabel;
    private JLabel addressLabel;
    private JLabel portLabel;
    private JLabel schemaLabel;
    private JLabel usernameLabel;
    private JLabel pwdLabel;
    private JTextField connectionNameTextField;
    private JTextField addressTextField;
    private JTextField portTextField;
    private JTextField schemaTextField;
    private JTextField usernameTextField;
    private JTextField pwdTextField;

    public NewConnection(){
        connectionNameLabel = new JLabel("数据库名称");
        addressLabel = new JLabel("数据库地址");
        portLabel = new JLabel("端口");
        schemaLabel = new JLabel("schema");
        usernameLabel = new JLabel("用户名");
        pwdLabel = new JLabel("密码");

        connectionNameTextField = new JTextField(null,null,20);
        addressTextField = new JTextField();
        portTextField = new JTextField();
        schemaTextField = new JTextField();
        usernameTextField = new JTextField();
        pwdTextField = new JPasswordField();
    }

    /**
     * 获取配置中所有的连接名
     * @return
     */
    public static List<NewConnection> getAllConnections() {
        BufferedReader fr = null;
        try{
            List<NewConnection> list = new ArrayList();
             fr = new BufferedReader(new FileReader(filePath));
            String line;
            while((line = fr.readLine()) != null){
                if(line.trim().length() == 0){
                    continue;
                }
                list.add(parseNewConnection(line));
            }
           return list;
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("获取所有连接失败，文件读取错误！");
        }finally {
            if(fr != null){

                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public JLabel getConnectionNameLabel() {
        return connectionNameLabel;
    }

    public void setConnectionNameLabel(JLabel connectionNameLabel) {
        this.connectionNameLabel = connectionNameLabel;
    }

    public JLabel getAddressLabel() {
        return addressLabel;
    }

    public void setAddressLabel(JLabel addressLabel) {
        this.addressLabel = addressLabel;
    }

    public JLabel getPortLabel() {
        return portLabel;
    }

    public void setPortLabel(JLabel portLabel) {
        this.portLabel = portLabel;
    }

    public JLabel getSchemaLabel() {
        return schemaLabel;
    }

    public void setSchemaLabel(JLabel schemaLabel) {
        this.schemaLabel = schemaLabel;
    }

    public JLabel getUsernameLabel() {
        return usernameLabel;
    }

    public void setUsernameLabel(JLabel usernameLabel) {
        this.usernameLabel = usernameLabel;
    }

    public JLabel getPwdLabel() {
        return pwdLabel;
    }

    public void setPwdLable(JLabel pwdLable) {
        this.pwdLabel = pwdLable;
    }

    public JTextField getConnectionNameTextField() {
        return connectionNameTextField;
    }

    public void setConnectionNameTextField(JTextField connectionNameTextField) {
        this.connectionNameTextField = connectionNameTextField;
    }

    public JTextField getAddressTextField() {
        return addressTextField;
    }

    public void setAddressTextField(JTextField addressTextField) {
        this.addressTextField = addressTextField;
    }

    public JTextField getPortTextField() {
        return portTextField;
    }

    public void setPortTextField(JTextField portTextField) {
        this.portTextField = portTextField;
    }

    public JTextField getSchemaTextField() {
        return schemaTextField;
    }

    public void setSchemaTextField(JTextField schemaTextField) {
        this.schemaTextField = schemaTextField;
    }

    public JTextField getUsernameTextField() {
        return usernameTextField;
    }

    public void setUsernameTextField(JTextField usernameTextField) {
        this.usernameTextField = usernameTextField;
    }

    public JTextField getPwdTextField() {
        return pwdTextField;
    }

    public void setPwdTextField(JTextField pwdTextField) {
        this.pwdTextField = pwdTextField;
    }

    public void setComponents(JPanel jp) {
        jp.add(connectionNameLabel);
        jp.add(connectionNameTextField);
        jp.add(addressLabel);
        jp.add(addressTextField);
        jp.add(schemaLabel);
        jp.add(schemaTextField);
        jp.add(portLabel);
        jp.add(portTextField);
        jp.add(usernameLabel);
        jp.add(usernameTextField);
        jp.add(pwdLabel);
        jp.add(pwdTextField);
    }

    /**
     * 测试连接
     * @return
     */
    public boolean testConnection() {
        String dbUrl = getUrl();

        JDBCConnection jdbcConnection = new JDBCConnection();
        return jdbcConnection.testConnect(dbUrl,this.usernameTextField.getText(),this.pwdTextField.getText());
    }

    public boolean saveConnecton() {

        try{
            File file = new File(filePath);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fos = new FileWriter(file,true);
            fos.append("\n");
            fos.append(formatClass());
            fos.close();
        }catch(IOException ex){
            ex.printStackTrace();
            System.out.println("文件创建失败");
        }
        return true;
    }

    private String formatClass() {
        return this.connectionNameTextField.getText()+seperator+this.addressTextField.getText()+seperator
                +this.portTextField.getText()+seperator+this.schemaTextField.getText()+seperator
                +this.usernameTextField.getText()+seperator+this.pwdTextField.getText();

    }

    private static NewConnection parseNewConnection(String line) {
        String[] info = line.split(seperator);
        NewConnection rst = new NewConnection();
        rst.setConnectionNameTextField(new JTextField(info[0]));
        rst.setAddressTextField(new JTextField(info[1]));
        rst.setPortTextField(new JTextField(info[2]));
        rst.setSchemaTextField(new JTextField(info[3]));
        rst.setUsernameTextField(new JTextField(info[4]));
        rst.setPwdTextField(new JPasswordField(info[5]));
        return rst;
    }

    @Override
    public String toString() {
        return "NewConnection{"+
                "connectionNameTextField=" + connectionNameTextField.getText() +
                ", addressTextField=" + addressTextField.getText() +
                ", portTextField=" + portTextField.getText() +
                ", schemaTextField=" + schemaTextField.getText() +
                ", usernameTextField=" + usernameTextField.getText() +
                ", pwdTextField=" + pwdTextField.getText() +
                '}';
    }

    public String getUrl() {
        return "jdbc:mysql://"+this.addressTextField.getText()+":"
                +this.portTextField.getText()+"/"+this.schemaTextField.getText()+"?useSSL=false";
    }
}
