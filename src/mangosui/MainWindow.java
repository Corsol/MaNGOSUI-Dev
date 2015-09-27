/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.text.DefaultCaret;
import static mangosui.WorkExecutor.mysqlUpdateDB;

/**
 *
 * @author Simone
 */
public class MainWindow extends WorkExecutor {

    private static ConfLoader confLoader;
    private static ConsoleManager console;
    private static CommandManager cmdManager;
    private static boolean gitOk = false;
    private static boolean mysqlOk = false;
    private static boolean cmakeOk = false;
    private static final long serialVersionUID = 1L;
    private String serverFolder = "";
    private String databaseFolder = "";
    private String elunaFolder = "";

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        cmdManager = new CommandManager();

        initComponents();
        disableComponentCascade(pnlDatabase);

        DefaultCaret caret = (DefaultCaret) txpGitConsole.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    private void doAllChecks() {
        if (!confLoader.isConfLoaded()) {
            console.updateGUIConsole(txpGitConsole, "ERROR: Configuration file NOT loaded. Check it.", ConsoleManager.TEXT_RED);
        } else {
            HashMap<String, String> mangosVers = confLoader.getMaNGOSVersions();
            ArrayList<String> keysMap = new ArrayList<String>(mangosVers.keySet());
            Collections.sort(keysMap);
            for (String keyMap : keysMap) {
                cmbCores.addItem(mangosVers.get(keyMap) + " - MaNGOS " + keyMap.substring(keyMap.lastIndexOf(".") + 1));
            }
            loadCmbCores();
        }

        if (cmdManager.getCURR_OS() > 0) {
            console.updateGUIConsole(txpGitConsole, "INFO: Your OS is: " + cmdManager.getOsName() + " version " + cmdManager.getOsVersion() + " with java architecture: " + cmdManager.getOsArch(), ConsoleManager.TEXT_BLUE);

            gitOk = checkGit(confLoader.getWinGitPath(), cmdManager, console, txpGitConsole);
            if (!gitOk) {
                disableComponentCascade(pnlDownload);
            } else {
                enableComponentCascade(pnlDownload);
                btnDatabaseDownload.setEnabled(true);
                btnServerDownload.setEnabled(true);
                btnLUADownload.setEnabled(true);
            }
            checkGitConf(serverFolder, databaseFolder, elunaFolder);
            /*if (cmdManager.getCURR_OS() == cmdManager.WINDOWS && !cmdManager.isPSScriptEnabled()) {
             console.updateGUIConsole(txpGitConsole, "WARNING: PowerShell script execution is not ebabled. To enable it run PS (x86) as Administrator and use \"Set-ExecutionPolicy Unrestricted\" command.", console.TEXT_ORANGE);
             }
             cmdManager.setWinGitPath(confLoader.getWinPathGit());

             if (!cmdManager.checkGit(null)) {
             btnGitDownload.setEnabled(false);
             console.updateGUIConsole(txpGitConsole, "ERROR: Git commands not found on system. Check Git installation or manually download repositories.", console.TEXT_RED);
             } else {
             console.updateGUIConsole(txpGitConsole, "INFO: Founded Git commands.", console.TEXT_BLUE);
             }*/
            mysqlOk = checkMySQL(databaseFolder, confLoader.getPathToMySQL(), cmdManager, console, txpGitConsole);
            if (!mysqlOk) {
                disableComponentCascade(pnlDatabase);
            } else {
                pnlDatabase.setEnabled(true);
                enableComponentCascade(pnlDatabaseConfig);
                if (!checkDBExistance(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                        confLoader.getDatabaseUser(), confLoader.getDatabaseUserPass(), confLoader.getWorldDBName(), confLoader.getCharDBName(), confLoader.getRealmDBName(),
                        cmdManager, console, txpGitConsole)) {
                    enableComponentCascade(pnlDBFirstInstall);
                } else {
                    enableComponentCascade(pnlDBWorld);
                    enableComponentCascade(pnlDBCharacter);
                    enableComponentCascade(pnlDBRealm);
                }
            }
            /*if (!cmdManager.checkMySQL(null)) {
             console.updateGUIConsole(txpGitConsole, "INFO: MySQL is not locally installed... checking for mysql.exe tool.", ConsoleManager.TEXT_BLUE);

             String mysqlToolPath = "database" + File.separator + confLoader.getPathToMySQL();
             if (!txtFolderDatabase.getText().isEmpty()) {
             mysqlToolPath = txtFolderDatabase.getText() + File.separator + confLoader.getPathToMySQL();
             }
             if (!cmdManager.checkMySQL(mysqlToolPath, null)) {
             //.setEnabled(false);
             console.updateGUIConsole(txpGitConsole, "WARNING: mysql.exe command not found on system. Check mysql installation or path '" + mysqlToolPath + "' to mysql.exe into database folder.", ConsoleManager.TEXT_RED);
             } else {
             console.updateGUIConsole(txpGitConsole, "INFO: Founded MySQL commands.", console.TEXT_BLUE);
             }
             } else {
             console.updateGUIConsole(txpGitConsole, "INFO: Founded MySQL commands.", console.TEXT_BLUE);
             }*/

            if (!cmdManager.checkCMAKE(null)) {
                //.setEnabled(false);
                console.updateGUIConsole(txpGitConsole, "INFO: CMAKE is not installed into PATH/shell environment... checking for cmake.exe installation folder.", ConsoleManager.TEXT_BLUE);
                String cmake32Path = confLoader.getWin32PathCMake();
                String cmake64Path = confLoader.getWin64PathCMake();
                if (cmdManager.checkCMAKE(cmake32Path, null)) {
                    // cmakeOk = true;
                    System.out.println("INFO: Founded CMAKE commands on x86 system.");
                } else if (cmdManager.checkCMAKE(cmake64Path, null)) {
                    // cmakeOk = true;
                    System.out.println("INFO: Founded CMAKE commands on x64 system.");
                } else {
                    console.updateGUIConsole(txpGitConsole, "ERROR: CMAKE commands not found on system. Check CMAKE installation!.", ConsoleManager.TEXT_RED);
                }
                console.updateGUIConsole(txpGitConsole, "ERROR: CMAKE commands not found on system. Check CMAKE installation!.", ConsoleManager.TEXT_RED);
            } else {
                // cmakePath =
                console.updateGUIConsole(txpGitConsole, "INFO: Founded CMAKE commands.", ConsoleManager.TEXT_BLUE);
            }

        } else {
            console.updateGUIConsole(txpGitConsole, "CRITICAL: Operatig system not supported.", ConsoleManager.TEXT_RED);
            disableComponentCascade(tabOperations);
        }
    }

    private void checkGitConf(String serverFolder, String databaseFolder, String elunaFolder) {
        this.serverFolder = setGitFolder(txtFolderServer.getText(), txtGitServer.getText());

        rdbGitServerWipe.setEnabled(false);
        rdbGitServerUpdate.setEnabled(false);
        rdbGitServerNew.setEnabled(false);
        if (cmdManager.checkFolder(serverFolder)) {
            //optGitSrvWipe = true;
            rdbGitServerWipe.setEnabled(true);
            // Check server version for update
            if (!cmdManager.isRepoUpToDate(serverFolder)) {
                rdbGitServerUpdate.setEnabled(true);
            }
        } else {
            rdbGitServerNew.setEnabled(true);
        }

        this.databaseFolder = setGitFolder(txtFolderDatabase.getText(), txtGitDatabase.getText());

        rdbGitDatabaseWipe.setEnabled(false);
        rdbGitDatabaseUpdate.setEnabled(false);
        rdbGitDatabaseNew.setEnabled(false);
        if (cmdManager.checkFolder(databaseFolder)) {
            //optGitSrvWipe = true;
            rdbGitDatabaseWipe.setEnabled(true);
            // Check server version for update
            if (!cmdManager.isRepoUpToDate(databaseFolder)) {
                rdbGitDatabaseUpdate.setEnabled(true);
            }
        } else {
            rdbGitDatabaseNew.setEnabled(true);
        }

        this.elunaFolder = setGitFolder(txtFolderLUA.getText(), txtGitLUA.getText());

        rdbGitLUAWipe.setEnabled(false);
        rdbGitLUAUpdate.setEnabled(false);
        rdbGitLUAUpdate.setEnabled(false);
        if (cmdManager.checkFolder(elunaFolder)) {
            //optGitSrvWipe = true;
            rdbGitLUAWipe.setEnabled(true);
            // Check server version for update
            if (!cmdManager.isRepoUpToDate(elunaFolder)) {
                rdbGitLUAUpdate.setEnabled(true);
            }
        } else {
            rdbGitLUANew.setEnabled(true);
        }
    }

    private void checkMySQLConf() {
    }

    private void disableComponentCascade(Component component) {
        try {
            if (component != null) {
                component.setEnabled(false);
                if (component instanceof Container) {
                    Container componentAsContainer = (Container) component;
                    for (Component c : componentAsContainer.getComponents()) {
                        disableComponentCascade(c);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    private void enableComponentCascade(Component component) {
        try {
            if (component != null) {
                component.setEnabled(true);
                if (component instanceof Container) {
                    Container componentAsContainer = (Container) component;
                    for (Component c : componentAsContainer.getComponents()) {
                        enableComponentCascade(c);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    private void loadCmbCores() {
        String input = "0";
        try {
            input = cmbCores.getSelectedItem().toString().substring(0, 1);
        } catch (Exception ex) {
        }
        confLoader.getGitURLServer(input);
        confLoader.getGitURLDatabase(input);
        confLoader.getGitURLEluna(input);
        confLoader.getGitBranchServer(input);
        confLoader.getGitBranchDatabase(input);
        confLoader.getGitBranchEluna(input);
        applyConfLoaded();
        checkGitConf(serverFolder, databaseFolder, elunaFolder);
    }

    private void applyConfLoaded() {
        txtGitServer.setText(confLoader.getGitURLServer());
        txtFolderServer.setText(confLoader.getGitFolderServer());
        txtBranchServer.setText(confLoader.getGitBranchServer());

        txtGitDatabase.setText(confLoader.getGitURLDatabase());
        txtFolderDatabase.setText(confLoader.getGitFolderDatabase());
        txtBranchDatabase.setText(confLoader.getGitBranchDatabase());

        txtGitLUA.setText(confLoader.getGitURLEluna());
        txtFolderLUA.setText(confLoader.getGitFolderEluna());
        txtBranchLUA.setText(confLoader.getGitBranchEluna());

        txtDBConfServer.setText(confLoader.getDatabaseServer());
        txtDBConfPort.setText(confLoader.getDatabasePort());
        txtDBConfAdmin.setText(confLoader.getDatabaseAdmin());
        txtDBConfAdminPwd.setText(confLoader.getDatabaseAdminPass());
        txtDBConfUser.setText(confLoader.getDatabaseUser());
        txtDBConfUserPwd.setText(confLoader.getDatabaseUserPass());

        txtWorldDBName.setText(confLoader.getWorldDBName());
        txtWorldFolder.setText(confLoader.getWorldFolder());
        txtWorldFullDB.setText(confLoader.getWorldFullDB());
        txtWorldLoadDB.setText(confLoader.getWorldLoadDBName());
        ArrayList<String> updFolders = new ArrayList<String>();
        for (String updFolder : confLoader.getWorldUpdRel().values()) {
            updFolders.add(updFolder);
        }
        Collections.sort(updFolders);
        lstWorldUpdFolders.setListData(updFolders.toArray(new String[updFolders.size()]));

        txtCharDBName.setText(confLoader.getCharDBName());
        txtCharFolder.setText(confLoader.getCharFolder());
        txtCharLoadDB.setText(confLoader.getCharLoadDBName());
        updFolders = new ArrayList<String>();
        for (String updFolder : confLoader.getCharUpdRel().values()) {
            updFolders.add(updFolder);
        }
        Collections.sort(updFolders);
        lstCharUpdFolders.setListData(updFolders.toArray(new String[updFolders.size()]));

        txtRealmDBName.setText(confLoader.getRealmDBName());
        txtRealmFolder.setText(confLoader.getRealmFolder());
        txtRealmLoadDB.setText(confLoader.getRealmLoadDBName());
        updFolders = new ArrayList<String>();
        for (String updFolder : confLoader.getRealmUpdRel().values()) {
            updFolders.add(updFolder);
        }
        Collections.sort(updFolders);
        lstRealmUpdFolders.setListData(updFolders.toArray(new String[updFolders.size()]));
    }

    private ArrayList<String> getListItems(JList jList) {
        ListModel listModel = jList.getModel();
        ArrayList<String> listItems = new ArrayList<String>();
        for (int i = 0; i < listModel.getSize(); i++) {
            listItems.add((String) listModel.getElementAt(i));
        }
        Collections.sort(listItems);
        return listItems;
    }

    private void addUpdFolder(JList<String> jList) {
        String newUpdFolder = JOptionPane.showInputDialog(null, "Insert new update folder that can be found inside 'Updates' folder:");
        if (!newUpdFolder.isEmpty()) {
            ArrayList<String> currFolders = getListItems(jList);
            currFolders.add(newUpdFolder);
            Collections.sort(currFolders);
            jList.setListData(currFolders.toArray(new String[currFolders.size()]));
        }
    }

    private void remUpdFolder(JList<String> jList) {
        if (jList.getSelectedIndex() >= 0) {
            ArrayList<String> currFolders = getListItems(jList);
            currFolders.remove(jList.getSelectedIndex());
            Collections.sort(currFolders);
            jList.setListData(currFolders.toArray(new String[currFolders.size()]));
        }
    }

    private JButton jButtonWorker(final JButton nextButton, final String dbFolder, final ArrayList<String> updSubFolders, final String midUpdFolder, final String dbName) {
        final JButton btnWorker = new JButton("DB Worker");
        PropertyChangeListener propChangeCreation = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                if (evt.getPropertyName().equalsIgnoreCase("Text")) {
                    //if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                        cmdManager.setBtnInvoker(nextButton);

                        mysqlUpdateDB(txtDBConfServer.getText(), txtDBConfPort.getText(), txtDBConfAdmin.getText(), txtDBConfAdminPwd.getText(),
                                databaseFolder, dbFolder, updSubFolders, null, dbName, midUpdFolder,
                                cmdManager, console, txpGitConsole);

                        //console.updateGUIConsole(txpGitConsole, "Done", ConsoleManager.TEXT_BLUE);
                    //} else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                        //console.updateGUIConsole(txpGitConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
                    //}
                }
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        btnWorker.addPropertyChangeListener(propChangeCreation);
        return btnWorker;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGrpGitServer = new javax.swing.ButtonGroup();
        btnGrpGitDatabase = new javax.swing.ButtonGroup();
        btnGrpGitLUA = new javax.swing.ButtonGroup();
        btnGrpDBWorld = new javax.swing.ButtonGroup();
        btnGrpDBCharacter = new javax.swing.ButtonGroup();
        btnGrpDBRealm = new javax.swing.ButtonGroup();
        tabOperations = new javax.swing.JTabbedPane();
        pnlDownload = new javax.swing.JPanel();
        pnlGitRepos = new javax.swing.JPanel();
        pnlGitServer = new javax.swing.JPanel();
        txtGitServer = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtFolderServer = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtBranchServer = new javax.swing.JTextField();
        rdbGitServerNew = new javax.swing.JRadioButton();
        rdbGitServerWipe = new javax.swing.JRadioButton();
        rdbGitServerUpdate = new javax.swing.JRadioButton();
        btnServerDownload = new javax.swing.JButton();
        pnlGitDatabase = new javax.swing.JPanel();
        txtGitDatabase = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtFolderDatabase = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtBranchDatabase = new javax.swing.JTextField();
        rdbGitDatabaseNew = new javax.swing.JRadioButton();
        rdbGitDatabaseWipe = new javax.swing.JRadioButton();
        rdbGitDatabaseUpdate = new javax.swing.JRadioButton();
        btnDatabaseDownload = new javax.swing.JButton();
        pnlGitLUA = new javax.swing.JPanel();
        txtGitLUA = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtFolderLUA = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtBranchLUA = new javax.swing.JTextField();
        rdbGitLUANew = new javax.swing.JRadioButton();
        rdbGitLUAWipe = new javax.swing.JRadioButton();
        rdbGitLUAUpdate = new javax.swing.JRadioButton();
        btnLUADownload = new javax.swing.JButton();
        pnlProxy = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        txtProxyServer = new javax.swing.JTextField();
        chkProxy = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        txtProxyPort = new javax.swing.JTextField();
        cmbCores = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        pnlDatabase = new javax.swing.JPanel();
        pnlDatabaseConfig = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtDBConfServer = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtDBConfPort = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtDBConfAdmin = new javax.swing.JTextField();
        txtDBConfAdminPwd = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        txtDBConfUserPwd = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtDBConfUser = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        btnDBCheck = new javax.swing.JButton();
        pnlDBWorld = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        txtWorldDBName = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        txtWorldFolder = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        txtWorldFullDB = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        txtWorldLoadDB = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstWorldUpdFolders = new javax.swing.JList();
        btnWorldAddUpdFolder = new javax.swing.JButton();
        btnWorldDelUpdFolder = new javax.swing.JButton();
        rdbDBWorldWipe = new javax.swing.JRadioButton();
        rdbDBWorldUpdate = new javax.swing.JRadioButton();
        btnDBWorldSetup = new javax.swing.JButton();
        pnlDBCharacter = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        txtCharDBName = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        txtCharFolder = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        txtCharLoadDB = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstCharUpdFolders = new javax.swing.JList();
        btnCharAddUpdFolder = new javax.swing.JButton();
        btnCharDelUpdFolder = new javax.swing.JButton();
        rdbDBCharWipe = new javax.swing.JRadioButton();
        rdbDBCharUpdate = new javax.swing.JRadioButton();
        btnDBCharSetup = new javax.swing.JButton();
        pnlDBRealm = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        txtRealmDBName = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        txtRealmFolder = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        txtRealmLoadDB = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        lstRealmUpdFolders = new javax.swing.JList();
        btnRealmAddUpdFolder = new javax.swing.JButton();
        btnRealmDelUpdFolder = new javax.swing.JButton();
        rdbDBRealmWipe = new javax.swing.JRadioButton();
        rdbDBRealmUpdate = new javax.swing.JRadioButton();
        btnDBRealmSetup = new javax.swing.JButton();
        pnlDBFirstInstall = new javax.swing.JPanel();
        btnDBFirstInstall = new javax.swing.JButton();
        pnlCompile = new javax.swing.JPanel();
        pnlDownloadConsole = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txpGitConsole = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MaNGOS Universal Installer");
        setLocation(new java.awt.Point(10, 10));
        setResizable(false);
        setSize(new java.awt.Dimension(1000, 750));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        tabOperations.setName(""); // NOI18N

        pnlGitRepos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Git Repositories", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));

        pnlGitServer.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Server", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));
        pnlGitServer.setPreferredSize(new java.awt.Dimension(292, 166));

        txtGitServer.setToolTipText("Insert here the https url to your git server repository");

        jLabel1.setText("Git URL");

        jLabel2.setText("Destination folder");

        txtFolderServer.setToolTipText("(Optional) Specify the destination folder for repository download");

        jLabel3.setText("Git Brach");

        txtBranchServer.setToolTipText("(Optional) Specify a different branch from master");

        btnGrpGitServer.add(rdbGitServerNew);
        rdbGitServerNew.setText("New");
        rdbGitServerNew.setActionCommand("N");
        rdbGitServerNew.setEnabled(false);

        btnGrpGitServer.add(rdbGitServerWipe);
        rdbGitServerWipe.setText("Wipe and Install");
        rdbGitServerWipe.setActionCommand("W");
        rdbGitServerWipe.setEnabled(false);

        btnGrpGitServer.add(rdbGitServerUpdate);
        rdbGitServerUpdate.setText("Update");
        rdbGitServerUpdate.setActionCommand("U");
        rdbGitServerUpdate.setEnabled(false);

        btnServerDownload.setText("Download");
        btnServerDownload.setEnabled(false);
        btnServerDownload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnServerDownloadMouseClicked(evt);
            }
        });
        btnServerDownload.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnServerDownloadPropertyChange(evt);
            }
        });
        btnServerDownload.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnServerDownloadKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout pnlGitServerLayout = new javax.swing.GroupLayout(pnlGitServer);
        pnlGitServer.setLayout(pnlGitServerLayout);
        pnlGitServerLayout.setHorizontalGroup(
            pnlGitServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitServerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGitServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGitServerLayout.createSequentialGroup()
                        .addGroup(pnlGitServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtGitServer)
                            .addComponent(txtFolderServer)
                            .addGroup(pnlGitServerLayout.createSequentialGroup()
                                .addComponent(rdbGitServerNew)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                                .addComponent(rdbGitServerWipe)
                                .addGap(27, 27, 27)
                                .addComponent(rdbGitServerUpdate))
                            .addGroup(pnlGitServerLayout.createSequentialGroup()
                                .addGroup(pnlGitServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(pnlGitServerLayout.createSequentialGroup()
                        .addComponent(txtBranchServer, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnServerDownload)
                        .addGap(32, 32, 32))))
        );
        pnlGitServerLayout.setVerticalGroup(
            pnlGitServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlGitServerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGitServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbGitServerNew)
                    .addComponent(rdbGitServerWipe)
                    .addComponent(rdbGitServerUpdate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtGitServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFolderServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlGitServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBranchServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnServerDownload))
                .addContainerGap(46, Short.MAX_VALUE))
        );

        pnlGitDatabase.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Database", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));
        pnlGitDatabase.setPreferredSize(new java.awt.Dimension(292, 166));

        txtGitDatabase.setToolTipText("Insert here the https url to your git database repository");

        jLabel7.setText("Git URL");

        jLabel8.setText("Destination folder");

        txtFolderDatabase.setToolTipText("(Optional) Specify the destination folder for repository download");

        jLabel9.setText("Git Brach");

        txtBranchDatabase.setToolTipText("(Optional) Specify a different branch from master");

        btnGrpGitDatabase.add(rdbGitDatabaseNew);
        rdbGitDatabaseNew.setText("New");
        rdbGitDatabaseNew.setActionCommand("N");
        rdbGitDatabaseNew.setEnabled(false);

        btnGrpGitDatabase.add(rdbGitDatabaseWipe);
        rdbGitDatabaseWipe.setText("Wipe and Install");
        rdbGitDatabaseWipe.setActionCommand("W");
        rdbGitDatabaseWipe.setEnabled(false);

        btnGrpGitDatabase.add(rdbGitDatabaseUpdate);
        rdbGitDatabaseUpdate.setText("Update");
        rdbGitDatabaseUpdate.setActionCommand("U");
        rdbGitDatabaseUpdate.setEnabled(false);

        btnDatabaseDownload.setText("Download");
        btnDatabaseDownload.setEnabled(false);
        btnDatabaseDownload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDatabaseDownloadMouseClicked(evt);
            }
        });
        btnDatabaseDownload.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnDatabaseDownloadPropertyChange(evt);
            }
        });
        btnDatabaseDownload.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnDatabaseDownloadKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout pnlGitDatabaseLayout = new javax.swing.GroupLayout(pnlGitDatabase);
        pnlGitDatabase.setLayout(pnlGitDatabaseLayout);
        pnlGitDatabaseLayout.setHorizontalGroup(
            pnlGitDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitDatabaseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGitDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGitDatabaseLayout.createSequentialGroup()
                        .addGroup(pnlGitDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtGitDatabase)
                            .addComponent(txtFolderDatabase)
                            .addGroup(pnlGitDatabaseLayout.createSequentialGroup()
                                .addComponent(rdbGitDatabaseNew)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                                .addComponent(rdbGitDatabaseWipe)
                                .addGap(27, 27, 27)
                                .addComponent(rdbGitDatabaseUpdate))
                            .addGroup(pnlGitDatabaseLayout.createSequentialGroup()
                                .addGroup(pnlGitDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel9))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(pnlGitDatabaseLayout.createSequentialGroup()
                        .addComponent(txtBranchDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDatabaseDownload)
                        .addGap(29, 29, 29))))
        );
        pnlGitDatabaseLayout.setVerticalGroup(
            pnlGitDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitDatabaseLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(pnlGitDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbGitDatabaseNew)
                    .addComponent(rdbGitDatabaseWipe)
                    .addComponent(rdbGitDatabaseUpdate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtGitDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFolderDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlGitDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBranchDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDatabaseDownload))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlGitLUA.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "LUA Script", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));
        pnlGitLUA.setPreferredSize(new java.awt.Dimension(292, 166));

        txtGitLUA.setToolTipText("Insert here the https url to your git server repository");

        jLabel12.setText("Git URL");

        jLabel13.setText("Destination folder");

        txtFolderLUA.setToolTipText("(Optional) Specify the destination folder for repository download");

        jLabel14.setText("Git Brach");

        txtBranchLUA.setToolTipText("(Optional) Specify a different branch from master");

        btnGrpGitLUA.add(rdbGitLUANew);
        rdbGitLUANew.setText("New");
        rdbGitLUANew.setActionCommand("N");
        rdbGitLUANew.setEnabled(false);

        btnGrpGitLUA.add(rdbGitLUAWipe);
        rdbGitLUAWipe.setText("Wipe and Install");
        rdbGitLUAWipe.setActionCommand("W");
        rdbGitLUAWipe.setEnabled(false);

        btnGrpGitLUA.add(rdbGitLUAUpdate);
        rdbGitLUAUpdate.setText("Update");
        rdbGitLUAUpdate.setActionCommand("U");
        rdbGitLUAUpdate.setEnabled(false);

        btnLUADownload.setText("Download");
        btnLUADownload.setEnabled(false);
        btnLUADownload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnLUADownloadMouseClicked(evt);
            }
        });
        btnLUADownload.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnLUADownloadPropertyChange(evt);
            }
        });
        btnLUADownload.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnLUADownloadKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout pnlGitLUALayout = new javax.swing.GroupLayout(pnlGitLUA);
        pnlGitLUA.setLayout(pnlGitLUALayout);
        pnlGitLUALayout.setHorizontalGroup(
            pnlGitLUALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitLUALayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGitLUALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGitLUALayout.createSequentialGroup()
                        .addGroup(pnlGitLUALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlGitLUALayout.createSequentialGroup()
                                .addComponent(rdbGitLUANew)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                                .addComponent(rdbGitLUAWipe)
                                .addGap(27, 27, 27)
                                .addComponent(rdbGitLUAUpdate))
                            .addComponent(txtGitLUA)
                            .addComponent(txtFolderLUA)
                            .addGroup(pnlGitLUALayout.createSequentialGroup()
                                .addGroup(pnlGitLUALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel14))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(pnlGitLUALayout.createSequentialGroup()
                        .addComponent(txtBranchLUA, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnLUADownload)
                        .addGap(29, 29, 29))))
        );
        pnlGitLUALayout.setVerticalGroup(
            pnlGitLUALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitLUALayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGitLUALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbGitLUANew)
                    .addComponent(rdbGitLUAWipe)
                    .addComponent(rdbGitLUAUpdate))
                .addGap(1, 1, 1)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtGitLUA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFolderLUA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlGitLUALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBranchLUA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLUADownload))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlProxy.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Proxy", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));

        jLabel10.setText("Server name");

        txtProxyServer.setEditable(false);

        chkProxy.setText("Use http proxy");
        chkProxy.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        chkProxy.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkProxyItemStateChanged(evt);
            }
        });

        jLabel11.setText("Port");

        txtProxyPort.setEditable(false);

        javax.swing.GroupLayout pnlProxyLayout = new javax.swing.GroupLayout(pnlProxy);
        pnlProxy.setLayout(pnlProxyLayout);
        pnlProxyLayout.setHorizontalGroup(
            pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProxyLayout.createSequentialGroup()
                .addComponent(chkProxy)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(txtProxyServer, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(txtProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        pnlProxyLayout.setVerticalGroup(
            pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProxyLayout.createSequentialGroup()
                .addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(pnlProxyLayout.createSequentialGroup()
                            .addComponent(jLabel11)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlProxyLayout.createSequentialGroup()
                            .addComponent(jLabel10)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtProxyServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlProxyLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(chkProxy)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cmbCores.setToolTipText("Select the MaNGOS core to configure and install");
        cmbCores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCoresActionPerformed(evt);
            }
        });

        jLabel15.setText("MaNGOS Core");

        javax.swing.GroupLayout pnlGitReposLayout = new javax.swing.GroupLayout(pnlGitRepos);
        pnlGitRepos.setLayout(pnlGitReposLayout);
        pnlGitReposLayout.setHorizontalGroup(
            pnlGitReposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitReposLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlGitReposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGitReposLayout.createSequentialGroup()
                        .addComponent(pnlGitServer, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlGitDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlGitLUA, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlGitReposLayout.createSequentialGroup()
                        .addComponent(pnlProxy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlGitReposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(cmbCores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(24, 24, 24))
        );
        pnlGitReposLayout.setVerticalGroup(
            pnlGitReposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitReposLayout.createSequentialGroup()
                .addGroup(pnlGitReposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlGitDatabase, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                    .addComponent(pnlGitLUA, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                    .addComponent(pnlGitServer, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(pnlGitReposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGitReposLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbCores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlProxy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlDownloadLayout = new javax.swing.GroupLayout(pnlDownload);
        pnlDownload.setLayout(pnlDownloadLayout);
        pnlDownloadLayout.setHorizontalGroup(
            pnlDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDownloadLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlGitRepos, javax.swing.GroupLayout.PREFERRED_SIZE, 967, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlDownloadLayout.setVerticalGroup(
            pnlDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDownloadLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlGitRepos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        tabOperations.addTab("Download", pnlDownload);

        pnlDatabaseConfig.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Database parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));
        pnlDatabaseConfig.setAutoscrolls(true);

        jLabel4.setText("Server name or ip");

        jLabel5.setText("Server port");

        jLabel6.setText("Admin user");

        jLabel16.setText("Admin password");

        jLabel17.setText("User password");

        jLabel18.setText("Default user");

        btnDBCheck.setText("Check parameters");
        btnDBCheck.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDBCheckMouseClicked(evt);
            }
        });
        btnDBCheck.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnDBCheckKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout pnlDatabaseConfigLayout = new javax.swing.GroupLayout(pnlDatabaseConfig);
        pnlDatabaseConfig.setLayout(pnlDatabaseConfigLayout);
        pnlDatabaseConfigLayout.setHorizontalGroup(
            pnlDatabaseConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatabaseConfigLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(pnlDatabaseConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDBConfServer, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatabaseConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtDBConfPort))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatabaseConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDBConfAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatabaseConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDBConfAdminPwd, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatabaseConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDBConfUser, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatabaseConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDBConfUserPwd, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDBCheck)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlDatabaseConfigLayout.setVerticalGroup(
            pnlDatabaseConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatabaseConfigLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatabaseConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatabaseConfigLayout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDBConfAdminPwd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatabaseConfigLayout.createSequentialGroup()
                        .addGroup(pnlDatabaseConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDatabaseConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDBConfServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDBConfPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDBConfAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatabaseConfigLayout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDBConfUserPwd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatabaseConfigLayout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDBConfUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatabaseConfigLayout.createSequentialGroup()
                        .addComponent(btnDBCheck)
                        .addContainerGap())))
        );

        pnlDBWorld.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "World DB", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        jLabel19.setText("DB name");
        jLabel19.setToolTipText("");

        jLabel20.setText("Base folder");
        jLabel20.setToolTipText("");

        jLabel21.setText("FullDB folder");

        jLabel22.setText("LoadDB script file");

        jLabel23.setText("Update folders");

        jScrollPane1.setViewportView(lstWorldUpdFolders);

        btnWorldAddUpdFolder.setText("Add Update folder");
        btnWorldAddUpdFolder.setAlignmentY(0.0F);
        btnWorldAddUpdFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnWorldAddUpdFolderMouseClicked(evt);
            }
        });
        btnWorldAddUpdFolder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnWorldAddUpdFolderKeyPressed(evt);
            }
        });

        btnWorldDelUpdFolder.setText("Del Update folder");
        btnWorldDelUpdFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnWorldDelUpdFolderMouseClicked(evt);
            }
        });
        btnWorldDelUpdFolder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnWorldDelUpdFolderKeyPressed(evt);
            }
        });

        btnGrpDBWorld.add(rdbDBWorldWipe);
        rdbDBWorldWipe.setText("(Wipe and) Install");
        rdbDBWorldWipe.setActionCommand("W");
        rdbDBWorldWipe.setEnabled(false);

        btnGrpDBWorld.add(rdbDBWorldUpdate);
        rdbDBWorldUpdate.setText("Update");
        rdbDBWorldUpdate.setActionCommand("U");
        rdbDBWorldUpdate.setEnabled(false);

        btnDBWorldSetup.setText("Setup");
        btnDBWorldSetup.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDBWorldSetupMouseClicked(evt);
            }
        });
        btnDBWorldSetup.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnDBWorldSetupPropertyChange(evt);
            }
        });
        btnDBWorldSetup.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnDBWorldSetupKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout pnlDBWorldLayout = new javax.swing.GroupLayout(pnlDBWorld);
        pnlDBWorld.setLayout(pnlDBWorldLayout);
        pnlDBWorldLayout.setHorizontalGroup(
            pnlDBWorldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDBWorldLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDBWorldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDBWorldLayout.createSequentialGroup()
                        .addComponent(rdbDBWorldWipe)
                        .addGap(67, 67, 67)
                        .addComponent(rdbDBWorldUpdate)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlDBWorldLayout.createSequentialGroup()
                        .addComponent(txtWorldLoadDB, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDBWorldSetup)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlDBWorldLayout.createSequentialGroup()
                        .addGroup(pnlDBWorldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20)
                            .addComponent(jLabel21)
                            .addComponent(txtWorldFullDB, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22)
                            .addComponent(txtWorldDBName, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtWorldFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlDBWorldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDBWorldLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlDBWorldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnWorldAddUpdFolder)
                                    .addComponent(btnWorldDelUpdFolder))
                                .addGap(0, 11, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDBWorldLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel23)
                                .addGap(36, 36, 36))))))
        );
        pnlDBWorldLayout.setVerticalGroup(
            pnlDBWorldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDBWorldLayout.createSequentialGroup()
                .addGroup(pnlDBWorldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDBWorldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlDBWorldLayout.createSequentialGroup()
                        .addComponent(txtWorldDBName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtWorldFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDBWorldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDBWorldLayout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtWorldFullDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel22))
                    .addGroup(pnlDBWorldLayout.createSequentialGroup()
                        .addComponent(btnWorldAddUpdFolder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnWorldDelUpdFolder)))
                .addGap(8, 8, 8)
                .addGroup(pnlDBWorldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtWorldLoadDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDBWorldSetup))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDBWorldLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbDBWorldWipe)
                    .addComponent(rdbDBWorldUpdate))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlDBCharacter.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Character DB", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        jLabel24.setText("DB name");

        jLabel25.setText("Base folder");

        jLabel27.setText("LoadDB script file");

        jLabel28.setText("Update folders");

        jScrollPane3.setViewportView(lstCharUpdFolders);

        btnCharAddUpdFolder.setText("Add Update folder");
        btnCharAddUpdFolder.setAlignmentY(0.0F);
        btnCharAddUpdFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCharAddUpdFolderMouseClicked(evt);
            }
        });
        btnCharAddUpdFolder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCharAddUpdFolderKeyPressed(evt);
            }
        });

        btnCharDelUpdFolder.setText("Del Update folder");
        btnCharDelUpdFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCharDelUpdFolderMouseClicked(evt);
            }
        });
        btnCharDelUpdFolder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCharDelUpdFolderKeyPressed(evt);
            }
        });

        btnGrpDBCharacter.add(rdbDBCharWipe);
        rdbDBCharWipe.setText("(Wipe and) Install");
        rdbDBCharWipe.setActionCommand("W");
        rdbDBCharWipe.setEnabled(false);

        btnGrpDBCharacter.add(rdbDBCharUpdate);
        rdbDBCharUpdate.setText("Update");
        rdbDBCharUpdate.setActionCommand("U");
        rdbDBCharUpdate.setEnabled(false);

        btnDBCharSetup.setText("Setup");

        javax.swing.GroupLayout pnlDBCharacterLayout = new javax.swing.GroupLayout(pnlDBCharacter);
        pnlDBCharacter.setLayout(pnlDBCharacterLayout);
        pnlDBCharacterLayout.setHorizontalGroup(
            pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                        .addComponent(rdbDBCharWipe)
                        .addGap(67, 67, 67)
                        .addComponent(rdbDBCharUpdate)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                        .addComponent(txtCharLoadDB, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDBCharSetup)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                        .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel24)
                            .addComponent(txtCharDBName, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25)
                            .addComponent(txtCharFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel27))
                        .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnCharAddUpdFolder)
                                    .addComponent(btnCharDelUpdFolder))
                                .addGap(0, 11, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDBCharacterLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel28)
                                .addGap(38, 38, 38))))))
        );
        pnlDBCharacterLayout.setVerticalGroup(
            pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jLabel28))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                        .addComponent(txtCharDBName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel25)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCharFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addComponent(jLabel27))
                    .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                        .addComponent(btnCharAddUpdFolder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCharDelUpdFolder)))
                .addGap(8, 8, 8)
                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCharLoadDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDBCharSetup))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbDBCharWipe)
                    .addComponent(rdbDBCharUpdate))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlDBRealm.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Realm DB", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        jLabel26.setText("DB name");

        jLabel29.setText("Base folder");

        jLabel30.setText("LoadDB script file");

        jLabel31.setText("Update folders");

        jScrollPane4.setViewportView(lstRealmUpdFolders);

        btnRealmAddUpdFolder.setText("Add Update folder");
        btnRealmAddUpdFolder.setAlignmentY(0.0F);
        btnRealmAddUpdFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnRealmAddUpdFolderMouseClicked(evt);
            }
        });
        btnRealmAddUpdFolder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnRealmAddUpdFolderKeyPressed(evt);
            }
        });

        btnRealmDelUpdFolder.setText("Del Update folder");
        btnRealmDelUpdFolder.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnRealmDelUpdFolderMouseClicked(evt);
            }
        });
        btnRealmDelUpdFolder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnRealmDelUpdFolderKeyPressed(evt);
            }
        });

        btnGrpDBRealm.add(rdbDBRealmWipe);
        rdbDBRealmWipe.setText("(Wipe and) Install");
        rdbDBRealmWipe.setActionCommand("W");
        rdbDBRealmWipe.setEnabled(false);

        btnGrpDBRealm.add(rdbDBRealmUpdate);
        rdbDBRealmUpdate.setText("Update");
        rdbDBRealmUpdate.setActionCommand("U");
        rdbDBRealmUpdate.setEnabled(false);

        btnDBRealmSetup.setText("Setup");

        javax.swing.GroupLayout pnlDBRealmLayout = new javax.swing.GroupLayout(pnlDBRealm);
        pnlDBRealm.setLayout(pnlDBRealmLayout);
        pnlDBRealmLayout.setHorizontalGroup(
            pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDBRealmLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDBRealmLayout.createSequentialGroup()
                        .addComponent(rdbDBRealmWipe)
                        .addGap(67, 67, 67)
                        .addComponent(rdbDBRealmUpdate)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlDBRealmLayout.createSequentialGroup()
                        .addComponent(txtRealmLoadDB, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDBRealmSetup)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlDBRealmLayout.createSequentialGroup()
                        .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel26)
                            .addComponent(txtRealmDBName, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel29)
                            .addComponent(txtRealmFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel30))
                        .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDBRealmLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnRealmAddUpdFolder)
                                    .addComponent(btnRealmDelUpdFolder))
                                .addGap(0, 11, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDBRealmLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel31)
                                .addGap(37, 37, 37))))))
        );
        pnlDBRealmLayout.setVerticalGroup(
            pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDBRealmLayout.createSequentialGroup()
                .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(jLabel31))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlDBRealmLayout.createSequentialGroup()
                        .addComponent(txtRealmDBName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtRealmFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDBRealmLayout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addComponent(jLabel30))
                    .addGroup(pnlDBRealmLayout.createSequentialGroup()
                        .addComponent(btnRealmAddUpdFolder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRealmDelUpdFolder)))
                .addGap(8, 8, 8)
                .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRealmLoadDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDBRealmSetup))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbDBRealmWipe)
                    .addComponent(rdbDBRealmUpdate))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlDBFirstInstall.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "First install", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        btnDBFirstInstall.setText("Start structure creation");
        btnDBFirstInstall.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDBFirstInstallMouseClicked(evt);
            }
        });
        btnDBFirstInstall.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnDBFirstInstallPropertyChange(evt);
            }
        });
        btnDBFirstInstall.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnDBFirstInstallKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout pnlDBFirstInstallLayout = new javax.swing.GroupLayout(pnlDBFirstInstall);
        pnlDBFirstInstall.setLayout(pnlDBFirstInstallLayout);
        pnlDBFirstInstallLayout.setHorizontalGroup(
            pnlDBFirstInstallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDBFirstInstallLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnDBFirstInstall)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlDBFirstInstallLayout.setVerticalGroup(
            pnlDBFirstInstallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDBFirstInstallLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDBFirstInstall)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlDatabaseLayout = new javax.swing.GroupLayout(pnlDatabase);
        pnlDatabase.setLayout(pnlDatabaseLayout);
        pnlDatabaseLayout.setHorizontalGroup(
            pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatabaseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDatabaseLayout.createSequentialGroup()
                        .addComponent(pnlDBWorld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlDBCharacter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlDBRealm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlDatabaseLayout.createSequentialGroup()
                        .addComponent(pnlDatabaseConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pnlDBFirstInstall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(120, 120, 120))
        );
        pnlDatabaseLayout.setVerticalGroup(
            pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatabaseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlDatabaseConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDBFirstInstall, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pnlDBCharacter, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDBWorld, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDBRealm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(63, Short.MAX_VALUE))
        );

        tabOperations.addTab("Database setup", pnlDatabase);

        javax.swing.GroupLayout pnlCompileLayout = new javax.swing.GroupLayout(pnlCompile);
        pnlCompile.setLayout(pnlCompileLayout);
        pnlCompileLayout.setHorizontalGroup(
            pnlCompileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 987, Short.MAX_VALUE)
        );
        pnlCompileLayout.setVerticalGroup(
            pnlCompileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 397, Short.MAX_VALUE)
        );

        tabOperations.addTab("Build and Install", pnlCompile);

        pnlDownloadConsole.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Console", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));

        txpGitConsole.setEditable(false);
        txpGitConsole.setText("Welcome to MaNGOS Universal Installer. Initializing...");
        txpGitConsole.setAutoscrolls(false);
        jScrollPane2.setViewportView(txpGitConsole);

        javax.swing.GroupLayout pnlDownloadConsoleLayout = new javax.swing.GroupLayout(pnlDownloadConsole);
        pnlDownloadConsole.setLayout(pnlDownloadConsoleLayout);
        pnlDownloadConsoleLayout.setHorizontalGroup(
            pnlDownloadConsoleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDownloadConsoleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2))
        );
        pnlDownloadConsoleLayout.setVerticalGroup(
            pnlDownloadConsoleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabOperations)
                    .addComponent(pnlDownloadConsole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabOperations, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDownloadConsole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabOperations.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void chkProxyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkProxyItemStateChanged
        // TODO add your handling code here:
        if (chkProxy.isSelected()) {
            // Ensable components
            txtProxyServer.setText(confLoader.getProxyServer());
            txtProxyServer.setEnabled(true);
            txtProxyPort.setText(confLoader.getProxyPort());
            txtProxyPort.setEnabled(true);
        } else {
            // Disable components
            txtProxyServer.setText("");
            txtProxyServer.setEnabled(false);
            txtProxyPort.setText("");
            txtProxyPort.setEnabled(false);
        }
    }//GEN-LAST:event_chkProxyItemStateChanged

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        doAllChecks();
    }//GEN-LAST:event_formWindowOpened

    private void cmbCoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCoresActionPerformed
        loadCmbCores();
    }//GEN-LAST:event_cmbCoresActionPerformed

    private void btnServerDownloadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnServerDownloadKeyPressed
        btnServerDownloadMouseClicked(null);
    }//GEN-LAST:event_btnServerDownloadKeyPressed

    private void btnServerDownloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnServerDownloadMouseClicked
        if (btnGrpGitServer.getSelection() != null) {
            btnServerDownload.setEnabled(false);
            btnDatabaseDownload.setEnabled(false);
            btnLUADownload.setEnabled(false);
            cmdManager.setBtnInvoker(btnServerDownload);
            serverFolder = setGitFolder(txtFolderServer.getText(), txtGitServer.getText());
            gitDownload(btnGrpGitServer.getSelection().getActionCommand(), txtGitServer.getText(), serverFolder, txtBranchServer.getText(), txtProxyServer.getText(), txtProxyPort.getText(), cmdManager, console, txpGitConsole);
        }

    }//GEN-LAST:event_btnServerDownloadMouseClicked

    private void btnDatabaseDownloadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDatabaseDownloadKeyPressed
        btnDatabaseDownloadMouseClicked(null);
    }//GEN-LAST:event_btnDatabaseDownloadKeyPressed

    private void btnDatabaseDownloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDatabaseDownloadMouseClicked
        if (btnGrpGitDatabase.getSelection() != null) {
            btnServerDownload.setEnabled(false);
            btnDatabaseDownload.setEnabled(false);
            btnLUADownload.setEnabled(false);
            cmdManager.setBtnInvoker(btnDatabaseDownload);
            databaseFolder = setGitFolder(txtFolderDatabase.getText(), txtGitDatabase.getText());
            gitDownload(btnGrpGitDatabase.getSelection().getActionCommand(), txtGitDatabase.getText(), databaseFolder, txtBranchDatabase.getText(), txtProxyServer.getText(), txtProxyPort.getText(), cmdManager, console, txpGitConsole);
        }
    }//GEN-LAST:event_btnDatabaseDownloadMouseClicked

    private void btnLUADownloadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnLUADownloadKeyPressed
        btnLUADownloadMouseClicked(null);
    }//GEN-LAST:event_btnLUADownloadKeyPressed

    private void btnLUADownloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLUADownloadMouseClicked
        if (btnGrpGitLUA.getSelection() != null) {
            btnServerDownload.setEnabled(false);
            btnDatabaseDownload.setEnabled(false);
            btnLUADownload.setEnabled(false);
            cmdManager.setBtnInvoker(btnLUADownload);
            elunaFolder = setGitFolder(txtFolderLUA.getText(), txtGitLUA.getText());
            gitDownload(btnGrpGitLUA.getSelection().getActionCommand(), txtGitLUA.getText(), elunaFolder, txtBranchLUA.getText(), txtProxyServer.getText(), txtProxyPort.getText(), cmdManager, console, txpGitConsole);
        }
    }//GEN-LAST:event_btnLUADownloadMouseClicked

    private void btnServerDownloadPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnServerDownloadPropertyChange
        if (evt.getPropertyName().equalsIgnoreCase("Text")) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(txpGitConsole, "Done", ConsoleManager.TEXT_BLUE);
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(txpGitConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
            }
            btnDatabaseDownload.setEnabled(true);
            //btnServerDownload.setEnabled(true);
            btnLUADownload.setEnabled(true);
        }
    }//GEN-LAST:event_btnServerDownloadPropertyChange

    private void btnDatabaseDownloadPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnDatabaseDownloadPropertyChange
        if (evt.getPropertyName().equalsIgnoreCase("Text")) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                mysqlOk = checkMySQL(databaseFolder, confLoader.getPathToMySQL(), cmdManager, console, txpGitConsole);
                if (!mysqlOk) {
                    disableComponentCascade(pnlDatabase);
                } else {
                    pnlDatabase.setEnabled(true);
                    enableComponentCascade(pnlDatabaseConfig);
                }
                console.updateGUIConsole(txpGitConsole, "Done", ConsoleManager.TEXT_BLUE);
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(txpGitConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
            }
            //btnDatabaseDownload.setEnabled(true);
            btnServerDownload.setEnabled(true);
            btnLUADownload.setEnabled(true);
        }
    }//GEN-LAST:event_btnDatabaseDownloadPropertyChange

    private void btnLUADownloadPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnLUADownloadPropertyChange
        if (evt.getPropertyName().equalsIgnoreCase("Text")) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(txpGitConsole, "Done", ConsoleManager.TEXT_BLUE);
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(txpGitConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
            }
            btnDatabaseDownload.setEnabled(true);
            btnServerDownload.setEnabled(true);
            //btnLUADownload.setEnabled(true);
        }
    }//GEN-LAST:event_btnLUADownloadPropertyChange

    private void btnDBCheckMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDBCheckMouseClicked
        //cmdManager.setBtnInvoker(btnDBCheck);
        if (checkDBExistance(txtDBConfServer.getText(), txtDBConfPort.getText(), txtDBConfAdmin.getText(), txtDBConfAdminPwd.getText(),
                txtDBConfUser.getText(), txtDBConfUserPwd.getText(), confLoader.getWorldDBName(), confLoader.getCharDBName(), confLoader.getRealmDBName(),
                cmdManager, console, txpGitConsole)) {
            enableComponentCascade(pnlDBWorld);
            enableComponentCascade(pnlDBCharacter);
            enableComponentCascade(pnlDBRealm);
            disableComponentCascade(pnlDBFirstInstall);
            btnDBCheck.setText("Check parameters");
            //console.updateGUIConsole(txpGitConsole, "Done", ConsoleManager.TEXT_BLUE);
        } else {
            enableComponentCascade(pnlDBFirstInstall);
            btnDBCheck.setText("Check parameters");
            //console.updateGUIConsole(txpGitConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
        }
    }//GEN-LAST:event_btnDBCheckMouseClicked

    private void btnDBCheckKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDBCheckKeyPressed
        btnDBCheckMouseClicked(null);
    }//GEN-LAST:event_btnDBCheckKeyPressed

    private void btnDBFirstInstallKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDBFirstInstallKeyPressed
        btnDBFirstInstallMouseClicked(null);
    }//GEN-LAST:event_btnDBFirstInstallKeyPressed

    private void btnDBFirstInstallMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDBFirstInstallMouseClicked
        mysqlCreateDB(txtDBConfServer.getText(), txtDBConfPort.getText(), txtDBConfAdmin.getText(), txtDBConfAdminPwd.getText(),
                txtDBConfUser.getText(), txtDBConfUserPwd.getText(), txtWorldDBName.getText(), txtCharDBName.getText(), txtRealmDBName.getText(),
                cmdManager, console, txpGitConsole);
    }//GEN-LAST:event_btnDBFirstInstallMouseClicked

    private void btnDBFirstInstallPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnDBFirstInstallPropertyChange
        if (evt.getPropertyName().equalsIgnoreCase("Text")) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                btnDBCheckMouseClicked(null);
                //console.updateGUIConsole(txpGitConsole, "Done", ConsoleManager.TEXT_BLUE);
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                //console.updateGUIConsole(txpGitConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
            }
        }
    }//GEN-LAST:event_btnDBFirstInstallPropertyChange

    private void btnWorldAddUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnWorldAddUpdFolderKeyPressed
        btnWorldAddUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnWorldAddUpdFolderKeyPressed

    private void btnWorldAddUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnWorldAddUpdFolderMouseClicked
        addUpdFolder(lstWorldUpdFolders);
    }//GEN-LAST:event_btnWorldAddUpdFolderMouseClicked

    private void btnWorldDelUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnWorldDelUpdFolderKeyPressed
        btnWorldDelUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnWorldDelUpdFolderKeyPressed

    private void btnWorldDelUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnWorldDelUpdFolderMouseClicked
        remUpdFolder(lstWorldUpdFolders);
    }//GEN-LAST:event_btnWorldDelUpdFolderMouseClicked

    private void btnCharAddUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCharAddUpdFolderKeyPressed
        btnCharAddUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnCharAddUpdFolderKeyPressed

    private void btnCharAddUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCharAddUpdFolderMouseClicked
        addUpdFolder(lstCharUpdFolders);
    }//GEN-LAST:event_btnCharAddUpdFolderMouseClicked

    private void btnRealmAddUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnRealmAddUpdFolderKeyPressed
        btnRealmAddUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnRealmAddUpdFolderKeyPressed

    private void btnRealmAddUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRealmAddUpdFolderMouseClicked
        addUpdFolder(lstRealmUpdFolders);
    }//GEN-LAST:event_btnRealmAddUpdFolderMouseClicked

    private void btnCharDelUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCharDelUpdFolderKeyPressed
        btnCharDelUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnCharDelUpdFolderKeyPressed

    private void btnCharDelUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCharDelUpdFolderMouseClicked
        remUpdFolder(lstCharUpdFolders);
    }//GEN-LAST:event_btnCharDelUpdFolderMouseClicked

    private void btnRealmDelUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnRealmDelUpdFolderKeyPressed
        btnRealmDelUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnRealmDelUpdFolderKeyPressed

    private void btnRealmDelUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRealmDelUpdFolderMouseClicked
        remUpdFolder(lstRealmUpdFolders);
    }//GEN-LAST:event_btnRealmDelUpdFolderMouseClicked

    private void btnDBWorldSetupKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDBWorldSetupKeyPressed
        btnDBWorldSetupMouseClicked(null);
    }//GEN-LAST:event_btnDBWorldSetupKeyPressed

    private void btnDBWorldSetupMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDBWorldSetupMouseClicked
        if (btnGrpDBWorld.getSelection() != null) {
            disableComponentCascade(pnlDBWorld);
            disableComponentCascade(pnlDBCharacter);
            disableComponentCascade(pnlDBRealm);
            /*final JButton btnCreate = new JButton("DB Creation");
             final JButton btnLoad = new JButton("DB Load");
             final JButton btnUpdate = new JButton("DB Update");
             PropertyChangeListener propChangeCreation = new PropertyChangeListener() {

             @Override
             public void propertyChange(PropertyChangeEvent evt) {

             if (evt.getPropertyName().equalsIgnoreCase("Text")) {
             if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
             cmdManager.setBtnInvoker(btnLoad);
             //console.updateGUIConsole(txpGitConsole, "Done", ConsoleManager.TEXT_BLUE);
             } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
             //console.updateGUIConsole(txpGitConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
             }
             }
             //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }
             };
             PropertyChangeListener propChangeLoad = new PropertyChangeListener() {

             @Override
             public void propertyChange(PropertyChangeEvent evt) {
             cmdManager.setBtnInvoker(btnUpdate);
             //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }
             };
             PropertyChangeListener proChangeUpdate = new PropertyChangeListener() {

             @Override
             public void propertyChange(PropertyChangeEvent evt) {
             cmdManager.setBtnInvoker(btnDBWorldSetup);
             //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
             }
             };
             btnCreate.addPropertyChangeListener(propChangeCreation);
             btnLoad.addPropertyChangeListener(propChangeLoad);
             btnUpdate.addPropertyChangeListener(proChangeUpdate);*/

            if ("W".equalsIgnoreCase(btnGrpDBWorld.getSelection().getActionCommand())) {
                ArrayList<String> updFolders = new ArrayList<>();
                updFolders.add(txtWorldFullDB.getText());
                JButton btnLoad = jButtonWorker(btnDBWorldSetup, txtWorldFolder.getText(), getListItems(lstWorldUpdFolders), confLoader.getDatabaseUpdateFolder(), txtWorldDBName.getText());
                JButton btnCreate = jButtonWorker(btnLoad, txtWorldFolder.getText(), updFolders, txtWorldFullDB.getText(), txtWorldDBName.getText());
                cmdManager.setBtnInvoker(btnCreate);
                mysqlLoadDB(txtDBConfServer.getText(), txtDBConfPort.getText(), txtDBConfAdmin.getText(), txtDBConfAdminPwd.getText(),
                        databaseFolder, txtWorldFolder.getText(), txtWorldLoadDB.getText(), txtWorldDBName.getText(), confLoader.getDatabaseSetupFolder(),
                        cmdManager, console, txpGitConsole);
            } else if ("U".equalsIgnoreCase(btnGrpDBWorld.getSelection().getActionCommand())) {
                ArrayList<String> updFolders = new ArrayList<>();
                updFolders.add("Rel21");
                JButton btnCreate = jButtonWorker(btnDBWorldSetup, txtWorldFolder.getText(), updFolders/*getListItems(lstWorldUpdFolders)*/, txtWorldFullDB.getText(), txtWorldDBName.getText());
                cmdManager.setBtnInvoker(btnCreate);
                ArrayList<String> updSubFolders = new ArrayList<>();
                updSubFolders.add("Rel20");
                mysqlUpdateDB(txtDBConfServer.getText(), txtDBConfPort.getText(), txtDBConfAdmin.getText(), txtDBConfAdminPwd.getText(),
                        databaseFolder, txtWorldFolder.getText(), updSubFolders/*getListItems(lstWorldUpdFolders)*/, null, txtWorldDBName.getText(),
                        confLoader.getDatabaseUpdateFolder(), cmdManager, console, txpGitConsole);
                //serverFolder = setGitFolder(txtFolderServer.getText(), txtGitServer.getText());
                //gitDownload(btnGrpGitServer.getSelection().getActionCommand(), txtGitServer.getText(), serverFolder, txtBranchServer.getText(), txtProxyServer.getText(), txtProxyPort.getText(), cmdManager, console, txpGitConsole);
            }
        }
    }//GEN-LAST:event_btnDBWorldSetupMouseClicked

    private void btnDBWorldSetupPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnDBWorldSetupPropertyChange
        if (evt.getPropertyName().equalsIgnoreCase("Text")) {
            enableComponentCascade(pnlDBWorld);
            enableComponentCascade(pnlDBCharacter);
            enableComponentCascade(pnlDBRealm);
        }
    }//GEN-LAST:event_btnDBWorldSetupPropertyChange

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        confLoader = new ConfLoader();
        console = new ConsoleManager();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCharAddUpdFolder;
    private javax.swing.JButton btnCharDelUpdFolder;
    private javax.swing.JButton btnDBCharSetup;
    private javax.swing.JButton btnDBCheck;
    private javax.swing.JButton btnDBFirstInstall;
    private javax.swing.JButton btnDBRealmSetup;
    private javax.swing.JButton btnDBWorldSetup;
    private javax.swing.JButton btnDatabaseDownload;
    private javax.swing.ButtonGroup btnGrpDBCharacter;
    private javax.swing.ButtonGroup btnGrpDBRealm;
    private javax.swing.ButtonGroup btnGrpDBWorld;
    private javax.swing.ButtonGroup btnGrpGitDatabase;
    private javax.swing.ButtonGroup btnGrpGitLUA;
    private javax.swing.ButtonGroup btnGrpGitServer;
    private javax.swing.JButton btnLUADownload;
    private javax.swing.JButton btnRealmAddUpdFolder;
    private javax.swing.JButton btnRealmDelUpdFolder;
    private javax.swing.JButton btnServerDownload;
    private javax.swing.JButton btnWorldAddUpdFolder;
    private javax.swing.JButton btnWorldDelUpdFolder;
    private javax.swing.JCheckBox chkProxy;
    private javax.swing.JComboBox cmbCores;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JList lstCharUpdFolders;
    private javax.swing.JList lstRealmUpdFolders;
    private javax.swing.JList lstWorldUpdFolders;
    private javax.swing.JPanel pnlCompile;
    private javax.swing.JPanel pnlDBCharacter;
    private javax.swing.JPanel pnlDBFirstInstall;
    private javax.swing.JPanel pnlDBRealm;
    private javax.swing.JPanel pnlDBWorld;
    private javax.swing.JPanel pnlDatabase;
    private javax.swing.JPanel pnlDatabaseConfig;
    private javax.swing.JPanel pnlDownload;
    private javax.swing.JPanel pnlDownloadConsole;
    private javax.swing.JPanel pnlGitDatabase;
    private javax.swing.JPanel pnlGitLUA;
    private javax.swing.JPanel pnlGitRepos;
    private javax.swing.JPanel pnlGitServer;
    private javax.swing.JPanel pnlProxy;
    private javax.swing.JRadioButton rdbDBCharUpdate;
    private javax.swing.JRadioButton rdbDBCharWipe;
    private javax.swing.JRadioButton rdbDBRealmUpdate;
    private javax.swing.JRadioButton rdbDBRealmWipe;
    private javax.swing.JRadioButton rdbDBWorldUpdate;
    private javax.swing.JRadioButton rdbDBWorldWipe;
    private javax.swing.JRadioButton rdbGitDatabaseNew;
    private javax.swing.JRadioButton rdbGitDatabaseUpdate;
    private javax.swing.JRadioButton rdbGitDatabaseWipe;
    private javax.swing.JRadioButton rdbGitLUANew;
    private javax.swing.JRadioButton rdbGitLUAUpdate;
    private javax.swing.JRadioButton rdbGitLUAWipe;
    private javax.swing.JRadioButton rdbGitServerNew;
    private javax.swing.JRadioButton rdbGitServerUpdate;
    private javax.swing.JRadioButton rdbGitServerWipe;
    private javax.swing.JTabbedPane tabOperations;
    private javax.swing.JTextPane txpGitConsole;
    private javax.swing.JTextField txtBranchDatabase;
    private javax.swing.JTextField txtBranchLUA;
    private javax.swing.JTextField txtBranchServer;
    private javax.swing.JTextField txtCharDBName;
    private javax.swing.JTextField txtCharFolder;
    private javax.swing.JTextField txtCharLoadDB;
    private javax.swing.JTextField txtDBConfAdmin;
    private javax.swing.JTextField txtDBConfAdminPwd;
    private javax.swing.JTextField txtDBConfPort;
    private javax.swing.JTextField txtDBConfServer;
    private javax.swing.JTextField txtDBConfUser;
    private javax.swing.JTextField txtDBConfUserPwd;
    private javax.swing.JTextField txtFolderDatabase;
    private javax.swing.JTextField txtFolderLUA;
    private javax.swing.JTextField txtFolderServer;
    private javax.swing.JTextField txtGitDatabase;
    private javax.swing.JTextField txtGitLUA;
    private javax.swing.JTextField txtGitServer;
    private javax.swing.JTextField txtProxyPort;
    private javax.swing.JTextField txtProxyServer;
    private javax.swing.JTextField txtRealmDBName;
    private javax.swing.JTextField txtRealmFolder;
    private javax.swing.JTextField txtRealmLoadDB;
    private javax.swing.JTextField txtWorldDBName;
    private javax.swing.JTextField txtWorldFolder;
    private javax.swing.JTextField txtWorldFullDB;
    private javax.swing.JTextField txtWorldLoadDB;
    // End of variables declaration//GEN-END:variables
}
