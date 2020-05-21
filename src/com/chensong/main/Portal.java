package com.chensong.main;


import com.chensong.main.entitys.MessageLocale;
import com.chensong.main.entitys.NewConnection;
import com.chensong.main.entitys.PageEntity;
import com.chensong.main.jdbc.JDBCConnection;
import com.chensong.main.support.SystemLanguage;
import com.chensong.main.uitl.BeanUtils;
import com.mysql.jdbc.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeoutException;
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
    private static final String TOP_CONTAINER_NAME = "NavicatLikeApp";
    /**
     * 顶级容器尺寸
     */
    private static final int JFRAME_SIZE_WIDTH = 1140;
    private static final int JFRAME_SIZE_HEIGHT = 800;

    /**
     * 创建一个顶级容器
     */
    private static JFrame jf = new JFrame(TOP_CONTAINER_NAME);

    /**
     * 连接列表滚动面板
     */
    private static JScrollPane jspLeft;

    /**
     * 放置所有数据表的滚动面板
     */
    private static JScrollPane tablesJScrollPane;

    /**
     * 放置DDL建表语句的滚动面板
     */
    private static JScrollPane infoJScrollPane;

    /**
     * 放置查询选项卡和数据表结果的选项卡面板
     */
    private static JTabbedPane tabbedPane;

    /**
     * 数据库连接列表控件
     */
    private static JTree connectionJTree ;

    /**
     * 存放数据库所有表的列表控件
     */
    private static JList tablesJList = new JList(new String[]{""});

    /**
     * 数据库所有表的数组
     */
    private static String[] tablesArr;

    /**
     * 记录当前查询中选择的连接
     */
    private static NewConnection currentConnection;

    /**
     * 数据库建表语句DDL文本域控件
     */
    private static JTextArea ddlTextArea = new JTextArea(" ");

    private static JDialog newConnectionDialog ;

    /**
     * 菜单栏控件
     */
    private static JMenuBar menuBar;
    private static JMenu fileOneMenu,settingsOneMenu,helpOneMenu;
    private static JMenuItem newConnectionMenuItem,newQueryMenuItem,exitMenuItem;
    private static JMenu languageItem;
    private static JMenuItem aboutItem;

    private static MessageLocale currentMessageLocale  ;


    /**
     * 顶级容器内的分割面板
     */
    private static JSplitPane splitPane = new JSplitPane();

    /**
     *所有的数据库连接列表
     */
    private static List<NewConnection> connections = null;


    /**
     * 支持的语言
     */
    public static final String[] supportLanguages = new String[]{"English","简体中文","繁体中文","日本語"};
    public static final Locale[] supportLocales = new Locale[]{Locale.ENGLISH,Locale.SIMPLIFIED_CHINESE,Locale.TRADITIONAL_CHINESE,Locale.JAPAN};


    /**
     * 系统语言事件
     */
    public static SystemLanguage systemLanguage = new SystemLanguage();

    private static URL firstPageIconURL ,appSourceURL,prevPageResourceURL,
            nextPageResourceURL,lastPageResourceURL,refreshResourceURL;
    //加载资源
    static{
        appSourceURL = Portal.class.getResource("/icon/app.png");
        firstPageIconURL = Portal.class.getResource("/icon/first.png");
        prevPageResourceURL = Portal.class.getResource("/icon/prev.png");
        nextPageResourceURL = Portal.class.getResource("/icon/next.png");
        lastPageResourceURL = Portal.class.getResource("/icon/last.png");
        refreshResourceURL = Portal.class.getResource("/icon/refresh.png");
    }

    public static void main(String[] args) {
        currentMessageLocale = MessageLocale.getMessageLocale(Locale.SIMPLIFIED_CHINESE);
        // 确保一个漂亮的外观风格
        JFrame.setDefaultLookAndFeelDecorated(true);
        jf.addWindowListener(new WindowAdapter() {
            /**
             * 程序关闭事件绑定：释放连接池
             * @param e
             */
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
        // 设置左右两边显示的组件
        //初始化左边连接列表面板
        initConnectionListJScrollPane();
        splitPane.setLeftComponent(jspLeft);
        //初始化右边结果显示面板
        initResultTabbedPane();
        splitPane.setRightComponent(tabbedPane);
        // 分隔条上显示快速 折叠/展开 两边组件的小按钮
        splitPane.setOneTouchExpandable(true);

        // 拖动分隔条时连续重绘组件
        splitPane.setContinuousLayout(true);

        // 设置分隔条的初始位置
        splitPane.setDividerLocation(150);

        initMenuBar();
        jf.setJMenuBar(menuBar);

        //设置面板容器到顶级容器
        jf.setContentPane(splitPane);

        jf.setIconImage(new ImageIcon(appSourceURL).getImage());
        //显示顶级容器
        jf.setVisible(true);
        //添加关闭钩子函数
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                JDBCConnection.releaseConncetions();
            }
        });
    }

    /**
     * 初始化左侧连接列表滚动面板
     */
    private static void initConnectionListJScrollPane(){
         connections = NewConnection.getAllConnections();
        //把连接添加到列表框中
        if(connections == null){
            System.exit(0);
        }
        List<String> connectionNames = connections.stream().map(i -> i.getConnectionNameTextField().getText()).collect(Collectors.toList());
        // 创建根节点
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("连接");
        connectionNames.forEach(name -> {
            DefaultMutableTreeNode connectionNode = new DefaultMutableTreeNode(name,true);
            rootNode.add(connectionNode);
        });
        //将连接名数组添加到连接名list控件中
        connectionJTree = new JTree(rootNode);
        connectionJTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int c = e.getClickCount();
                //连接被双击事件绑定
                TreePath treePath = connectionJTree.getSelectionPath();
                Object[] paths = treePath.getPath();
                DefaultMutableTreeNode selectTreeNode = (DefaultMutableTreeNode) connectionJTree.getLastSelectedPathComponent();

                if(paths.length == 2 && c == 2){
                    //显示二级树
                    showConnectionSecondTree(selectTreeNode);
                }else if(paths.length == 3 && c == 2){
                    int selectIdx = selectTreeNode.getParent().getIndex(selectTreeNode);
                    //显示三级树
                    showConnectionThirdTree(rootNode,selectTreeNode,selectIdx);
                }else if(paths.length == 4){
                   //表名或查询名被点击
                     int idx = selectTreeNode.getParent().getParent().getIndex(selectTreeNode.getParent());
                     String tableName = paths[3].toString();
                     if(idx == 0 ){
                         //双击表 打开表
                         if(c == 2){
                             openTableRows(tableName);
                         }
                         //单击表 打开DDL
                         if(c == 1){
                             showDDL(tableName);
                         }
                     }else if(idx == 1){
                         //打开查询
                        openQuery();
                     }
                }
                if(c == 2 && !selectTreeNode.isLeaf()){
                    //点击节点后，如果该节点不是叶子节点，展开节点
                    connectionJTree.expandPath(treePath);
                }
                }
        });
        jspLeft = new JScrollPane(connectionJTree);
        jspLeft.setSize(new Dimension(200,200));
    }

    private static void openTableRows(String tableName) {
        showTableRows(tableName,0,1000);
    }


    private static void openQuery() {
    }
    /**
     * 在连接树中显示子树表名
     * @param selectTreeNode
     */
    private static void showConnectionSecondTree(DefaultMutableTreeNode selectTreeNode) {

        if(!selectTreeNode.children().hasMoreElements()){
            DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(currentMessageLocale.getTables());
            selectTreeNode.add(tableNode);
            DefaultMutableTreeNode queryNode = new DefaultMutableTreeNode(currentMessageLocale.getQuery());
            selectTreeNode.add(queryNode);
            connectionJTree.repaint();
        }
    }

    /**
     *显示三级树
     * @param rootNode
     * @param selectTreeNode
     * @param selectIdx
     */
    private static void showConnectionThirdTree(DefaultMutableTreeNode rootNode,DefaultMutableTreeNode selectTreeNode,int selectIdx){
        DefaultMutableTreeNode connectionNode = (DefaultMutableTreeNode)selectTreeNode.getParent();
        int connectionIdx = rootNode.getIndex(connectionNode);
        if(selectTreeNode.children().hasMoreElements()){
            //已经有字节点，返回
           return;
        }
        if(connectionIdx != -1 && selectIdx == 0){
            connectDbClicked(selectTreeNode.getParent().getParent().getIndex(selectTreeNode.getParent()));
            //点击二级树中的 表 节点
            currentConnection = connections.get(connectionIdx);
            List<String> tables = null;
            try {
                tables = JDBCConnection.showTables(currentConnection);
                for(String table:tables){
                    DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
                    selectTreeNode.add(tableNode);
                }
                connectionJTree.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(selectIdx == 1){
            //点击了查询，创建查询三级树 undo
        }
    }

    /**
     * 双击连接 显示所有表
     */
    private static void connectDbClicked(int selectedIndex) {
        currentConnection = connections.get(selectedIndex);
        List<String> tables = null;
        try {
            tables = JDBCConnection.showTables(currentConnection);
            tablesArr = tables.toArray(new String[tables.size()]);
            tablesJList.setListData(tablesArr);
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    /**
     * 初始化结果面板：tabbedPane(JSplitPane(JScrollPane(JList)+JScrollPane(JTextArea)))
     * @return
     */
    private static void initResultTabbedPane(){
         tabbedPane = new JTabbedPane();

         //创建分隔面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
         splitPane.setDividerLocation(450);

         //创建滚动面板
         tablesJScrollPane = new JScrollPane(tablesJList);

          //设置显示DDL语句控件的前景色和字体
         ddlTextArea.setForeground(new Color(255,0,0));
        Font x = new Font("Serif",1,15);
        ddlTextArea.setFont(x);
        //创建滚动面板，并将DDL语句控件放入面板
         infoJScrollPane = new JScrollPane(ddlTextArea);

        tablesJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                /**
                 * 绑定数据库表JList控件的双击事件：显示表内容到一个新面板
                 */
                if(tablesJList.getSelectedIndex() != -1 && e.getClickCount() == 2){
                    //双击表，显示表结果
                    showTableRows(tablesJList.getSelectedValue(),0,1000);
                }
            }

        });
        tablesJList.addListSelectionListener(l -> {
            /**
             * 绑定数据库表JList控件的选中事件：显示表DDL语句到JTextArea控件中
             */
            showDDL(tablesJList.getSelectedValue());
        });

        splitPane.setLeftComponent(tablesJScrollPane);
        splitPane.setRightComponent(infoJScrollPane);
        // 创建第 1 个选项卡（选项卡只包含 标题）
        tabbedPane.addTab(currentMessageLocale.getTables(),splitPane );
        // 设置默认选中的选项卡
        tabbedPane.setSelectedIndex(0);
        systemLanguage.addListener(m -> tabbedPane.setTitleAt(0,m.getTables()));
    }

    /**
     *显示表所有数据到新开的表格面板
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
        DefaultTableModel defaultTableModel = new DefaultTableModel();
        //数据源，列名
        defaultTableModel.setDataVector(rowData, columnNames);
        JTable table = new JTable(defaultTableModel);
        FitTableColumns(table);
        // 把 表格 放到 滚动面板 中（表头将自动添加到滚动面板顶部）
        JToolBar jFooterBar = new JToolBar();
        JButton firstPageBtn = new JButton("",new ImageIcon(firstPageIconURL));
        firstPageBtn.setSize(50,50);
        NewConnection rowConnection = new NewConnection();
        BeanUtils.deepCopy(currentConnection,rowConnection);
        try {
            PageEntity pageEntity = new PageEntity(rowConnection,tableName.toString(), JDBCConnection.getRowsCount(currentConnection,tableName),defaultTableModel);
            firstPageBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    firstPage(pageEntity);
                }
            });
            jFooterBar.add(firstPageBtn);
            JButton prevPageBtn = new JButton("",new ImageIcon(prevPageResourceURL));

            prevPageBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    prevPage(pageEntity);
                }
            });
            jFooterBar.add(prevPageBtn);
            JButton nextPageBtn = new JButton("",new ImageIcon(nextPageResourceURL));
            nextPageBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    nextPage(pageEntity);
                }
            });
            jFooterBar.add(nextPageBtn);

            JButton lastPageBtn = new JButton("",new ImageIcon(lastPageResourceURL));
            lastPageBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    lastPage(pageEntity);
                }
            });
            jFooterBar.add(lastPageBtn);
            JButton refreshBtn = new JButton("",new ImageIcon(refreshResourceURL));
            refreshBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        pageEntity.refresh(tableName.toString());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
            jFooterBar.add(refreshBtn);
            JLabel pageInfoField = new JLabel(pageEntity.getPageInfo());
            pageEntity.setPageInfoField(pageInfoField);
            jFooterBar.add(pageInfoField);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JPanel jp = new JPanel(new BorderLayout());
        jp.add(jFooterBar,BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(table));
        splitPane.setRightComponent(jp);
        splitPane.setDividerLocation(680);
        tabbedPane.addTab(currentConnection.getSchemaTextField().getText()+"."+tableName,splitPane);
        //设置当前选项卡为活动状态
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
    }

    /**
     * 下一页
     */
    private static void prevPage(PageEntity pageEntity) {
        if(pageEntity.getCurrPage() == 1){
            return;
        }
        pageEntity.prev();
        // 表头（列名）
        String[] columnNames = parseTableHeads(pageEntity.getTableName());

        // 表格所有行数据
        Object[][] rowData = JDBCConnection.getTableRowsLimit(columnNames, pageEntity.getNewConnection(),pageEntity.getTableName()
                ,pageEntity.getOffset(),pageEntity.getPageSize());
        pageEntity.getDefaultTableModel().setDataVector(rowData,columnNames);

    }

    /**
     * 上一页
     */
    private static void nextPage(PageEntity pageEntity) {
        if(pageEntity.getCurrPage() == pageEntity.getTotalPage() || pageEntity.getTotalPage() == 0){
            return;
        }
        pageEntity.next();
        // 表头（列名）
        String[] columnNames = parseTableHeads(pageEntity.getTableName());

        // 表格所有行数据
        Object[][] rowData = JDBCConnection.getTableRowsLimit(columnNames, pageEntity.getNewConnection(),pageEntity.getTableName()
                ,pageEntity.getOffset(),pageEntity.getPageSize());
        pageEntity.getDefaultTableModel().setDataVector(rowData,columnNames);
    }

    /**
     * 第一页
     */
    private static void firstPage(PageEntity pageEntity) {
        if(pageEntity.getCurrPage() == 1){
            return;
        }
        pageEntity.first();
        // 表头（列名）
        String[] columnNames = parseTableHeads(pageEntity.getTableName());

        // 表格所有行数据
        Object[][] rowData = JDBCConnection.getTableRowsLimit(columnNames, pageEntity.getNewConnection(),pageEntity.getTableName()
                ,pageEntity.getOffset(),pageEntity.getPageSize());
        pageEntity.getDefaultTableModel().setDataVector(rowData,columnNames);
    }

    /**
     * 最后一页
     */
    private static void lastPage(PageEntity pageEntity) {
        if(pageEntity.getCurrPage() == pageEntity.getTotalPage()){
            return;
        }
        if(pageEntity.getTotalPage() == 0){
            return;
        }
        pageEntity.last();
        // 表头（列名）
        String[] columnNames = parseTableHeads(pageEntity.getTableName());

        // 表格所有行数据
        Object[][] rowData = JDBCConnection.getTableRowsLimit(columnNames, pageEntity.getNewConnection(),pageEntity.getTableName()
                ,pageEntity.getOffset(),pageEntity.getPageSize());
        pageEntity.getDefaultTableModel().setDataVector(rowData,columnNames);
    }

    /**
     * 美化表格面板
     * @param myTable
     */
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
     * 查询指定表名的DDL语句并解析所有字段名
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
     * 显示指定下标的连接下的表的DDL语句到JTextArea控件
     * @param selectedValue
     */
    private static void showDDL(Object selectedValue) {
        if(Objects.equals(selectedValue,"")){
            return;
        }
        try{

            String ddl = JDBCConnection.getShowCreateTable(currentConnection,selectedValue);
            ddlTextArea.setText(ddl);
        }catch (TimeoutException ex){
            ddlTextArea.setText("timeout!");
        }catch(Exception ex){
            ddlTextArea.setText("error occured!");
        }
    }

    /**
     * 初始化菜单
     */
    private static void initMenuBar() {
        //设置一个菜单栏
         menuBar = new JMenuBar();
         initFileOneMenu();
        menuBar.add(fileOneMenu);
        initSettingsOneMenu();
        menuBar.add(settingsOneMenu);
        initHelpOneMenu();
        menuBar.add(helpOneMenu);
    }


    /**
     * 初始化一级菜单：帮助
     */
    private static void initHelpOneMenu() {
        helpOneMenu = new JMenu(currentMessageLocale.getHelp());
        systemLanguage.addListener(m -> helpOneMenu.setText(m.getHelp()));
        aboutItem = new JMenuItem(currentMessageLocale.getAbout());
        systemLanguage.addListener(m -> aboutItem.setText(m.getAbout()));
        aboutItem.addActionListener(e -> {
            //新建连接被点击
            showAbout();
        });
        helpOneMenu.add(aboutItem);
    }

    private static void showAbout(){
        //创建一个摸态对话框
        JDialog dialog = new JDialog(jf,currentMessageLocale.getAbout(),false);
        systemLanguage.addListener(m -> dialog.setTitle(m.getAbout()));
        //设置对话框尺寸
        dialog.setSize(JFRAME_SIZE_WIDTH/2,JFRAME_SIZE_HEIGHT/2);
        //设置对话框是否可改变
        dialog.setResizable(false);
        //设置对话框相对位置
        dialog.setLocationRelativeTo(jf);




        JPanel jp = new JPanel(new GridLayout(5,1));
        jp.add(new JLabel("介绍：连接MYSQL的图形化客户端软件"));
        jp.add(new JLabel("作者：peter"));
        jp.add(new JLabel("QQ:327259852"));
        jp.add(new JLabel("wechat:cs327259852"));
        jp.add(new JLabel("版本：0.2"));
        dialog.add(jp);
        //显示对话框
        dialog.setVisible(true);
    }

    /**
     * 初始化一级菜单：设置
     */
    private static void initSettingsOneMenu() {
        settingsOneMenu = new JMenu(currentMessageLocale.getSettings());
        systemLanguage.addListener(m -> settingsOneMenu.setText(m.getSettings()));
        languageItem = new JMenu(currentMessageLocale.getLanguage());
        systemLanguage.addListener(m -> languageItem.setText(m.getLanguage()));
        settingsOneMenu.add(languageItem);
        for(String supportLanguage:supportLanguages){
            JMenuItem item = new JMenuItem(supportLanguage);
            languageItem.add(item);
            item.addActionListener(l ->  updateSystemLanguage(l.getActionCommand()));
        }
    }

    /**
     * 切换系统语言
     * @param languageName
     */
    private static void updateSystemLanguage(String languageName){
        int idx = 0;
        for(String s:supportLanguages){
            if(s.equalsIgnoreCase(languageName)){
                break;
            }
            idx++;
        }
        MessageLocale messageLocal = MessageLocale.getMessageLocale(supportLocales[idx]);
        if(messageLocal != null){
            currentMessageLocale = messageLocal;
            systemLanguage.systemLanguageNotify(currentMessageLocale);

        }

    }

    /**
     * 初始化一级菜单：文件
     */
    private static void initFileOneMenu(){
        /**
         * 创建一级菜单
         */
        fileOneMenu = new JMenu(currentMessageLocale.getFile());
        systemLanguage.addListener((m) -> fileOneMenu.setText(m.getFile()));
        /**
         * 创建文件下面的二级菜单
         */
        newConnectionMenuItem = new JMenuItem(currentMessageLocale.getNewMySqlConnection());
        systemLanguage.addListener(m -> newConnectionMenuItem.setText(m.getNewConnection()));
        newConnectionMenuItem.addActionListener(e -> {
            //新建连接被点击
            drawNewConnectionDialog(jf);
        });
        fileOneMenu.add(newConnectionMenuItem);
        newQueryMenuItem = new JMenuItem(currentMessageLocale.getNewQuery());
        systemLanguage.addListener(m -> newQueryMenuItem.setText(m.getNewQuery()));
        newQueryMenuItem.addActionListener(e -> {
            //新建查询被点击
            newQuery();
        });
        fileOneMenu.add(newQueryMenuItem);

        fileOneMenu.addSeparator();
        exitMenuItem = new JMenuItem(currentMessageLocale.getExit());
        systemLanguage.addListener(m -> exitMenuItem.setText(m.getExit()));
        exitMenuItem.addActionListener(e ->{
                JDBCConnection.releaseConncetions();
                System.exit(0);
        });
        fileOneMenu.add(exitMenuItem);
    }


    /**
     * 创建查询：打开一个选项卡面板
     */
    private static void newQuery() {
        JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JScrollPane downScrollPane = new JScrollPane();
        //查询结果页 表格面板
        JScrollPane upScrollPane = new JScrollPane(getQueryPane(downScrollPane));
        jsp.setLeftComponent(upScrollPane);

        jsp.setRightComponent(downScrollPane);
        jsp.setDividerLocation(450);

        tabbedPane.addTab(currentMessageLocale.getQuery(),jsp);
        int index = tabbedPane.getTabCount()-1;
        systemLanguage.addListener(m -> tabbedPane.setTitleAt(index,m.getQuery()));
        tabbedPane.setSelectedIndex(index);
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
        JButton executeBtn = new JButton(currentMessageLocale.getExecute());
        systemLanguage.addListener(m -> executeBtn.setText(m.getExecute()));
        executeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleQuery(resultJsp,jTextArea.getText(),jCombobox);
            }
        });
        jToolBar.add(executeBtn);
        JButton executeSelectedBtn = new JButton(currentMessageLocale.getExecuteSelected());
        systemLanguage.addListener(m -> executeSelectedBtn.setText(m.getExecuteSelected()));
        executeSelectedBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleQuery(resultJsp,jTextArea.getSelectedText(),jCombobox);
            }
        });
        executeSelectedBtn.setVisible(false);
        jToolBar.add(executeSelectedBtn);
        jTextArea.addCaretListener(e -> {
                String selectedText = jTextArea.getSelectedText();
                boolean isEmpty = StringUtils.isNullOrEmpty(selectedText);
                if(isEmpty ){
                    executeBtn.setVisible(true);
                    executeSelectedBtn.setVisible(false);
                }else{
                    executeBtn.setVisible(false);
                    executeSelectedBtn.setVisible(true);
                }
        });
        fillSchemaSelectList(jCombobox);
        jToolBar.add(jCombobox);
        jp.add(jToolBar,BorderLayout.PAGE_START);
        jp.add(jTextArea,BorderLayout.CENTER);
        return jp;
    }

    /**
     * 处理点击查询/查询选中
     * @param resultJsp
     * @param sql
     */
    private static void handleQuery(JScrollPane resultJsp, String sql,JComboBox jCombobox) {
        if("".equals(sql) || sql == null){
            return;
        }
        //是否是查询语句，查询语句需要在结果面板添加表格面板
        boolean selectQuery = false;
        sql = sql.replaceAll(" +"," ").trim().replaceAll("\n","").replace("\t","");
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
            } catch (Exception e) {
                setErrMsg(e,resultJsp);
            }
        }else{
            //执行非数据库查询语句语句
            try {
                String result = JDBCConnection.executeNonDQL(connections.get(jCombobox.getSelectedIndex()),sql);
                JTextArea jTextArea = new JTextArea(result);
                jTextArea.setFont(x);
                resultJsp.setViewportView(jTextArea);
            } catch (Exception e) {
                setErrMsg(e,resultJsp);
            }
        }
    }

    private static void setErrMsg(Exception e,JScrollPane resultJsp){
        String errMsg = e.getMessage();
        if(e instanceof TimeoutException){
            errMsg = "执行超时！";
        }
        JTextArea errText = new JTextArea(errMsg);
        errText.setForeground(new Color(255,0,0));
        //查询语句语法错误
        resultJsp.setViewportView(errText);

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
        NewConnection newConnectionComponents = new NewConnection(currentMessageLocale,systemLanguage);
        //创建一个摸态对话框
        newConnectionDialog = new JDialog(jf,currentMessageLocale.getNewConnection(),false);
        systemLanguage.addListener(m -> newConnectionDialog.setTitle(m.getNewConnection()));
        //设置对话框尺寸
        newConnectionDialog.setSize(JFRAME_SIZE_WIDTH/2,JFRAME_SIZE_HEIGHT/2);
        //设置对话框是否可改变
        newConnectionDialog.setResizable(false);
        //设置对话框相对位置
        newConnectionDialog.setLocationRelativeTo(jf);

        JButton confirmBtn = new JButton(currentMessageLocale.getSave());
        systemLanguage.addListener(m -> confirmBtn.setText(m.getSave()));
        confirmBtn.addActionListener(e -> {
            saveConnection(newConnectionComponents,newConnectionDialog);
        });
        newConnectionDialog.add(confirmBtn);
        JButton testBtn = new JButton(currentMessageLocale.getTestConnection());
        systemLanguage.addListener(m -> testBtn.setText(m.getTestConnection()));
        //测试连接按钮点击事件绑定
        testBtn.addActionListener(e -> testConnection(newConnectionComponents,newConnectionDialog));

        //创建对话框中的面板
        JPanel jp = new JPanel(new GridLayout(7, 2,10,10));

        newConnectionComponents.setComponents(jp);
        //按钮添加到面板容器
        jp.add(confirmBtn);
        jp.add(testBtn);
        //将面板设置到对话框
        newConnectionDialog.setContentPane(jp);

        //显示对话框
        newConnectionDialog.setVisible(true);

    }

    /**
     * 测试连接
     * @param newConnectionComponents
     */
    private static void testConnection(NewConnection newConnectionComponents,JDialog dialog) {
       if( newConnectionComponents.testConnection()){
           JOptionPane.showMessageDialog(
                   dialog,
                   currentMessageLocale.getConnectSuccessfully(),
                   currentMessageLocale.getTip(),
                   JOptionPane.INFORMATION_MESSAGE
           );
       }else{
           JOptionPane.showMessageDialog(
                   dialog,
                   currentMessageLocale.getConnectFailed(),
                   currentMessageLocale.getTip(),
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
