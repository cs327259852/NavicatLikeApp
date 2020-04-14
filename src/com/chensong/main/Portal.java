package com.chensong.main;


import com.chensong.main.entitys.NewConnection;
import com.chensong.main.exception.SQLBadGrammarException;
import com.chensong.main.jdbc.JDBCConnection;
import com.sun.deploy.panel.JavaPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 */
public class Portal {

    /**
     *顶级容器名称
     */
    private static final String TOP_CONTAINER_NAME = "Navicat";
    private static final int JFRAME_SIZE_WIDTH = 1140;
    private static final int JFRAME_SIZE_HEIGHT = 800;

    /**
     * 创建一个顶级容器
     */
    private static JFrame jf = new JFrame(TOP_CONTAINER_NAME);
    private static JScrollPane jspLeft,tablesJScrollPane,infoJScrollPane;
    private static JTabbedPane tabbedPane;
    private static JList connectionJList ,tablesJList = new JList(new String[]{""});
    private static String[] tablesArr;
    private static NewConnection currentConnection;
    private static JTextArea ddlTextArea = new JTextArea("信息页");
    private static List<NewConnection> connections = null;
    

    public static void main(String[] args) {
        // 确保一个漂亮的外观风格
        JFrame.setDefaultLookAndFeelDecorated(true);
        jf.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                JDBCConnection.releaseConncetions();
            }
        });

        //设置顶级容器尺寸
        jf.setSize(JFRAME_SIZE_WIDTH,JFRAME_SIZE_HEIGHT);

        //设置顶层容器居中显示
        jf.setLocationRelativeTo(null);

        //设置关闭按钮的动作
        jf.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 创建分隔面板
        JSplitPane splitPane = new JSplitPane();
        // 设置左右两边显示的组件
        //左边面板
        jspLeft = initConnectionListJScrollPane();
        splitPane.setLeftComponent(jspLeft);
//        splitPane.setM
        //右边面板
        tabbedPane = initResultTabbedPane();
        splitPane.setRightComponent(tabbedPane);
        // 分隔条上显示快速 折叠/展开 两边组件的小按钮
        splitPane.setOneTouchExpandable(true);

        // 拖动分隔条时连续重绘组件
        splitPane.setContinuousLayout(true);

        // 设置分隔条的初始位置
        splitPane.setDividerLocation(150);

        //设置中间容器的布局为：流式布局



        JMenuBar mb = initMenuBar();
        jf.setJMenuBar(mb);

        //设置面板容器到顶级容器
        jf.setContentPane(splitPane);


        //显示顶级容器
        jf.setVisible(true);
    }

    /**
     * 初始化左侧连接列表滚动面板
     * @return
     */
    private static JScrollPane initConnectionListJScrollPane(){
         connections = NewConnection.getAllConnections();
        //把经典添加到列表框中
        if(connections == null){
            System.exit(0);
        }
        List<String> connectionNames = connections.stream().map(i -> i.getConnectionNameTextField().getText()).collect(Collectors.toList());
         connectionJList=new JList(connectionNames.toArray(new String[connectionNames.size()]));
        connectionJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(connectionJList.getSelectedIndex() != -1){
                    if(e.getClickCount() == 2){
                        connectDbClicked(connectionJList.getSelectedIndex());
                    }
                }
            }
        });
        JScrollPane jsp1=new JScrollPane(connectionJList);
        jsp1.setSize(new Dimension(200,200));
        jsp1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return jsp1;
    }



    /**
     * 双击连接 显示所有表
     */
    private static void connectDbClicked(int selectedIndex) {
        currentConnection = connections.get(selectedIndex);
        List<String> tables = JDBCConnection.showTables(currentConnection);
        tablesArr = tables.toArray(new String[tables.size()]);
        tablesJList.setListData(tablesArr);

    }

    /**
     * 初始化结果面板
     * @return
     */
    private static JTabbedPane initResultTabbedPane(){   // 创建选项卡面板
         JTabbedPane tabbedPane = new JTabbedPane();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
         splitPane.setDividerLocation(450);
         tablesJScrollPane = new JScrollPane(tablesJList);
        ddlTextArea.setForeground(new Color(255,0,0));
        Font x = new Font("Serif",1,15);
        ddlTextArea.setFont(x);
         infoJScrollPane = new JScrollPane(ddlTextArea);
        tablesJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(tablesJList.getSelectedIndex() != -1 && e.getClickCount() == 2){
                    //双击表，显示表结果
                    showTableRows(tablesJList.getSelectedValue(),0,1000);
                }
            }

        });
        tablesJList.addListSelectionListener(l -> {
                showDDL(tablesJList.getSelectedValue());
        });

        tablesJScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tablesJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        splitPane.setLeftComponent(tablesJScrollPane);
        splitPane.setRightComponent(infoJScrollPane);
        // 创建第 1 个选项卡（选项卡只包含 标题）
        tabbedPane.addTab("表",splitPane );
        // 设置默认选中的选项卡
        tabbedPane.setSelectedIndex(0);
        return tabbedPane;
    }

    /**
     *
     * @param tableName
     */
    private static void showTableRows(Object tableName,int start,int end) {
        if(Objects.equals(tableName,"")){
            return;
        }
        // 表头（列名）
        String[] columnNames = parseTableHeads(tableName.toString());

        // 表格所有行数据
        Object[][] rowData = JDBCConnection.getTableRowsLimit(columnNames,currentConnection,tableName,start,end);

        // 创建一个表格，指定 表头 和 所有行数据
        JTable table = new JTable(rowData, columnNames);
        FitTableColumns(table);
        // 把 表格 放到 滚动面板 中（表头将自动添加到滚动面板顶部）
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tabbedPane.addTab(currentConnection.getSchemaTextField().getText()+"."+tableName,scrollPane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
    }

    private static void FitTableColumns(JTable myTable) {
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();

        Enumeration columns = myTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int) myTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(myTable, column.getIdentifier(), false, false, -1, col)
                    .getPreferredSize().getWidth();
            for (int row = 0; row < rowCount; row++) {
                int preferedWidth = (int) myTable.getCellRenderer(row, col)
                        .getTableCellRendererComponent(myTable, myTable.getValueAt(row, col), false, false, row, col)
                        .getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column);
            column.setWidth(width + myTable.getIntercellSpacing().width + 10);
            myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
    }

    /**
     * 从建表语句中解析所有字段名
     */
    private static String[] parseTableHeads(String tableName) {
        String ddl = ddlTextArea.getText();

        ddl = ddl.replaceAll("\\s*|\t|\r|\n", "");
        String regex = ",`(.+?)`";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ddl);
        List<String> filedList = new ArrayList<>();
        while(matcher.find()) {
            filedList.add(matcher.group(1).trim());
        }
        regex = "`"+tableName+"`\\(`(.+?)`";
         pattern = Pattern.compile(regex);
         matcher = pattern.matcher(ddl);
        while(matcher.find()) {
            filedList.add(0,matcher.group(1).trim());
        }
        if(filedList.size() == 0){
            System.out.println("未解析到字段名列表");
        }
        return filedList.toArray(new String[filedList.size()]);
    }

    /**
     * 显示当前连接下的表的DDL语句
     * @param selectedValue
     */
    private static void showDDL(Object selectedValue) {
        if(Objects.equals(selectedValue,"")){
            return;
        }
        String ddl = JDBCConnection.getShowCreateTable(currentConnection,selectedValue);
        ddlTextArea.setText(ddl);
    }
    /**
     * 初始化菜单
     */
    private static JMenuBar initMenuBar() {
        //设置一个菜单栏
        JMenuBar menuBar = new JMenuBar();

        /**
         * 创建一级菜单
         */
        JMenu fileMenu = new JMenu("文件");
        menuBar.add(fileMenu);

        /**
         * 创建文件下面的二级菜单
         */

        JMenuItem newConnectionMenuItem = new JMenuItem("新建mysql连接");
        newConnectionMenuItem.addActionListener(e -> {
            //新建连接被点击
            drawNewConnectionDialog(jf);
        });
        fileMenu.add(newConnectionMenuItem);
        JMenuItem newQueryMenuItem = new JMenuItem("新建查询");
        newQueryMenuItem.addActionListener(e -> {
            //新建查询被点击
            newQuery(jf);
        });
        fileMenu.add(newQueryMenuItem);

        fileMenu.addSeparator();
        JMenuItem exitMenuItem = new JMenuItem("退出");
        exitMenuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JDBCConnection.releaseConncetions();
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        return menuBar;
    }

    /**
     * 创建查询：打开一个选项卡面板
     * @param jf
     */
    private static void newQuery(JFrame jf) {
        JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JScrollPane downScrollPane = new JScrollPane();
        //查询结果页 表格面板
        JScrollPane upScrollPane = new JScrollPane(getQueryPane(downScrollPane));
        jsp.setLeftComponent(upScrollPane);

        upScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jsp.setRightComponent(downScrollPane);
        downScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setDividerLocation(450);

        tabbedPane.addTab("查询",jsp);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
    }

    /**
     * 获取查询面板
     * @return
     */
    private static JPanel getQueryPane(JScrollPane resultJsp) {
        JComboBox jCombobox = new JComboBox();
        JTextArea jTextArea = new JTextArea(" ");
        JPanel jp = new JPanel(new BorderLayout());
        JToolBar jToolBar = new JToolBar();
        JButton executeBtn = new JButton("执行");
        executeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleQuery(resultJsp,jTextArea.getText(),jCombobox);
            }
        });
        jToolBar.add(executeBtn);
        JButton executeSelectedBtn = new JButton("执行所选择的");
        executeSelectedBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleQuery(resultJsp,jTextArea.getSelectedText(),jCombobox);
            }
        });
        jToolBar.add(executeSelectedBtn);

        fillSchemaSelectList(jCombobox);
        jToolBar.add(jCombobox);
        jp.add(jToolBar,BorderLayout.PAGE_START);
        jp.add(jTextArea,BorderLayout.CENTER);
        return jp;
    }

    /**
     * 处理点击查询
     * @param resultJsp
     * @param sql
     */
    private static void handleQuery(JScrollPane resultJsp, String sql,JComboBox jCombobox) {
        if("".equals(sql)){
            return;
        }
        //是否是查询语句，查询语句需要在结果面板添加表格面板
        boolean selectQuery = false;
        sql = sql.replaceAll(" +"," ").trim();
        String[] sqlsplit = sql.split(" ");
        if(sqlsplit[0].equalsIgnoreCase("select")){
            selectQuery = true;
        }
        Font x = new Font(Font.SERIF,1,15);
        if(selectQuery){
            try {
                Map<String,Object> result = JDBCConnection.getTableRowsByCustomer(connections.get(jCombobox.getSelectedIndex()),sql);
                Object[][] rowData = (Object[][])result.get("rows");
                String[] columnNames = (String[])result.get("columnNames");
                // 创建一个表格，指定 表头 和 所有行数据
                JTable table = new JTable(rowData, columnNames);
                FitTableColumns(table);
                table.setFont(x);
                resultJsp.setViewportView(table);
            } catch (SQLBadGrammarException e1) {
                JTextArea errText = new JTextArea(e1.getMessage());
                errText.setForeground(new Color(255,0,0));
                //查询语句语法错误
                resultJsp.setViewportView(errText);
            }
        }else{
            //执行非数据库查询语句语句
            try {
                String result = JDBCConnection.executeNonDQL(connections.get(jCombobox.getSelectedIndex()),sql);
                JTextArea jTextArea = new JTextArea(result);
                jTextArea.setFont(x);
                resultJsp.setViewportView(jTextArea);
            } catch (SQLBadGrammarException e) {
                JTextArea errText = new JTextArea(e.getMessage());
                errText.setForeground(new Color(255,0,0));
                //查询语句语法错误
                resultJsp.setViewportView(errText);
            }
        }
    }

    /**
     * 填充schema列表
     * @param jCombobox
     */
    private static void fillSchemaSelectList(JComboBox jCombobox) {
        if(connections == null){
            return;
        }
        connections.forEach(c -> {
            jCombobox.addItem(c.getConnectionNameTextField().getText());
        });
    }

    /**
     * 新建连接被点击事件
     * @param jf
     */
    private static void drawNewConnectionDialog(JFrame jf){
        NewConnection newConnectionComponents = new NewConnection();
        //创建一个摸态对话框
        final JDialog dialog = new JDialog(jf,"新建连接",false);
        //设置对话框尺寸
        dialog.setSize(JFRAME_SIZE_WIDTH/2,JFRAME_SIZE_HEIGHT/2);
        //设置对话框是否可改变
        dialog.setResizable(false);
        //设置对话框相对位置
        dialog.setLocationRelativeTo(jf);

        JButton confirmBtn = new JButton("保存");
        confirmBtn.addActionListener(e -> {
            saveConnection(newConnectionComponents,dialog);
        });
        dialog.add(confirmBtn);
        JButton testBtn = new JButton("测试连接");
        //测试连接按钮点击事件绑定
        testBtn.addActionListener(e -> testConnection(newConnectionComponents,dialog));

        //创建对话框中的面板
        JPanel jp = new JPanel(new GridLayout(7, 2,10,10));

        newConnectionComponents.setComponents(jp);
        //按钮添加到面板容器
        jp.add(confirmBtn);
        jp.add(testBtn);
        //将面板设置到对话框
        dialog.setContentPane(jp);

        //显示对话框
        dialog.setVisible(true);

    }

    /**
     * 测试连接
     * @param newConnectionComponents
     */
    private static void testConnection(NewConnection newConnectionComponents,JDialog dialog) {
       if( newConnectionComponents.testConnection()){
           JOptionPane.showMessageDialog(
                   dialog,
                   "连接成功",
                   "提示",
                   JOptionPane.INFORMATION_MESSAGE
           );
       }else{
           JOptionPane.showMessageDialog(
                   dialog,
                   "连接失败",
                   "提示",
                   JOptionPane.WARNING_MESSAGE
           );
       }

    }

    /**
     * 保存连接
     * @param newConnectionComponents
     */
    private static void saveConnection(NewConnection newConnectionComponents,Dialog dialog) {
        boolean success = newConnectionComponents.saveConnecton();
        dialog.dispose();
    }


}
