package com.chensong.main.entitys;

import com.chensong.main.Portal;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 国际化信息实体类(单例)
 */
public class MessageLocale {
    private  String tables,newMySqlConnection,exit,query,execute,executeSelected,newConnection,
    save,testConnection,connectSuccessfully,connectFailed,tip,file,settings,language,help,about,connectionName,
    connectionAddress,connectionPort,connectionSchema,connectionUsername,connectionPwd,newQuery;

    private static Map<Locale,MessageLocale> localeMessageLocaleHashMap = new HashMap<>();
    static{
        for(Locale locale: Portal.supportLocales){
            ResourceBundle rb = ResourceBundle.getBundle("Message", locale);

            MessageLocale instance = new MessageLocale();
            Field[] fields = MessageLocale.class.getDeclaredFields();
            for(Field field:fields){
                field.setAccessible(true);
                try {
                    String fieldName = field.getName();
                    if("localeMessageLocaleHashMap".equalsIgnoreCase(fieldName)){
                        continue;
                    }
                    field.set(instance,rb.getString(field.getName()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            localeMessageLocaleHashMap.put(locale,instance);
        }
    }

    /**
     * 读取配置文件并实例化一个单例
     * @return
     */
    public static MessageLocale getMessageLocale(Locale locale){
        return localeMessageLocaleHashMap.get(locale);
    }

    public String getTables() {
        return tables;
    }

    public void setTables(String tables) {
        this.tables = tables;
    }

    public String getNewMySqlConnection() {
        return newMySqlConnection;
    }

    public void setNewMySqlConnection(String newMySqlConnection) {
        this.newMySqlConnection = newMySqlConnection;
    }

    public String getExit() {
        return exit;
    }

    public void setExit(String exit) {
        this.exit = exit;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getExecute() {
        return execute;
    }

    public void setExecute(String execute) {
        this.execute = execute;
    }

    public String getExecuteSelected() {
        return executeSelected;
    }

    public void setExecuteSelected(String executeSelected) {
        this.executeSelected = executeSelected;
    }

    public String getNewConnection() {
        return newConnection;
    }

    public void setNewConnection(String newConnection) {
        this.newConnection = newConnection;
    }

    public String getSave() {
        return save;
    }

    public void setSave(String save) {
        this.save = save;
    }

    public String getTestConnection() {
        return testConnection;
    }

    public void setTestConnection(String testConnection) {
        this.testConnection = testConnection;
    }

    public String getConnectSuccessfully() {
        return connectSuccessfully;
    }

    public void setConnectSuccessfully(String connectSuccessfully) {
        this.connectSuccessfully = connectSuccessfully;
    }

    public String getConnectFailed() {
        return connectFailed;
    }

    public void setConnectFailed(String connectFailed) {
        this.connectFailed = connectFailed;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }


    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getConnectionAddress() {
        return connectionAddress;
    }

    public void setConnectionAddress(String connectionAddress) {
        this.connectionAddress = connectionAddress;
    }

    public String getConnectionPort() {
        return connectionPort;
    }

    public void setConnectionPort(String connectionPort) {
        this.connectionPort = connectionPort;
    }

    public String getConnectionSchema() {
        return connectionSchema;
    }

    public void setConnectionSchema(String connectionSchema) {
        this.connectionSchema = connectionSchema;
    }

    public String getConnectionUsername() {
        return connectionUsername;
    }

    public void setConnectionUsername(String connectionUsername) {
        this.connectionUsername = connectionUsername;
    }

    public String getConnectionPwd() {
        return connectionPwd;
    }

    public void setConnectionPwd(String connectionPwd) {
        this.connectionPwd = connectionPwd;
    }

    public String getNewQuery() {
        return newQuery;
    }

    public void setNewQuery(String newQuery) {
        this.newQuery = newQuery;
    }
}
