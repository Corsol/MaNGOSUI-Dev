/*
 * Copyright (C) 2015 Boni Simone <simo.boni@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package mangosui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author Boni Simone <simo.boni@gmail.com>
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
    private LinkedList<JButton> btnWorkList = new LinkedList<>();
    private JButton btnInvoker;
    private static final Logger LOG = Logger.getLogger(MainWindow.class.getName());

    /**
     * Default constructor that creates new form with all components
     */
    public MainWindow() {
        cmdManager = new CommandManager();
        if (confLoader.isConfLoaded()) {
            cmdManager.setDebugLevel(confLoader.getDebugLevel());
        }
        this.initComponents();
        this.lblNextStep.setVisible(false);
        this.disableComponentCascade(this.pnlDatabase);
        if (cmdManager.getCURR_OS() == cmdManager.WINDOWS) {
            this.pnlSetupDeps.setVisible(false);
            this.pnlDownloadDeps.setEnabled(true);
        } else {
            this.pnlDownloadDeps.setVisible(false);
            this.pnlSetupDeps.setEnabled(true);
        }
    }

    /**
     * Execute all checks for missing dependencies like git, mysql, cmake
     */
    private void doAllChecks() {
        Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        this.setCursor(cursor);
        if (!confLoader.isConfLoaded()) {
            console.updateGUIConsole(this.txpConsole, "ERROR: Configuration file NOT loaded. Check it.", ConsoleManager.TEXT_RED);
        } else {
            HashMap<String, String> mangosVers = confLoader.getMaNGOSVersions();
            ArrayList<String> keysMap = new ArrayList<>(mangosVers.keySet());
            Collections.sort(keysMap);
            for (String keyMap : keysMap) {
                this.cmbCores.addItem(mangosVers.get(keyMap) + " - MaNGOS " + keyMap.substring(keyMap.lastIndexOf('.') + 1));
            }
            this.loadCmbCores();
        }

        if (cmdManager.getCURR_OS() > 0) {
            this.btnSetupMissingDeps.setEnabled(false);
            this.lblDownloadGit.setEnabled(false);
            this.chkSetupGit.setEnabled(false);
            this.chkSetupGit.setSelected(false);
            this.lblDownloadMySQL.setEnabled(false);
            this.chkSetupMySQL.setEnabled(false);
            this.chkSetupMySQL.setSelected(false);
            this.lblDownloadCMake.setEnabled(false);
            this.chkSetupCMake.setEnabled(false);
            this.chkSetupCMake.setSelected(false);
            this.lblDownloadOpenSSL.setEnabled(false);
            this.chkSetupOpenSSL.setEnabled(false);
            this.chkSetupOpenSSL.setSelected(false);
            this.prbSetupProgress.setMaximum(0);
            this.prbSetupProgress.setValue(0);

            console.updateGUIConsole(this.txpConsole, "INFO: Your OS is: " + cmdManager.getOsName() + " version " + cmdManager.getOsVersion() + " with java architecture: " + cmdManager.getOsArch(), ConsoleManager.TEXT_BLUE);

            gitOk = checkGit(confLoader.getWinGitHubPath(), confLoader.getWinGitExtPath(), cmdManager, this.txpConsole);
            if (!gitOk) {
                this.lblDownloadGit.setEnabled(true);
                this.chkSetupGit.setEnabled(true);
                this.chkSetupGit.setSelected(true);
                this.btnSetupMissingDeps.setEnabled(true);
                this.disableComponentCascade(this.pnlDownload);
            } else {
                this.enableComponentCascade(this.pnlDownload);
                this.btnDatabaseDownload.setEnabled(true);
                this.btnServerDownload.setEnabled(true);
                this.btnLUADownload.setEnabled(true);
            }
            this.checkGitConf();

            mysqlOk = checkMySQL(this.databaseFolder, confLoader.getPathToMySQL(), cmdManager, this.txpConsole);
            if (!mysqlOk) {
                this.lblDownloadMySQL.setEnabled(true);
                this.chkSetupMySQL.setEnabled(true);
                this.chkSetupMySQL.setSelected(true);
                this.btnSetupMissingDeps.setEnabled(true);
                this.disableComponentCascade(this.pnlDatabase);
            } else {
                this.pnlDatabase.setEnabled(true);
                this.enableComponentCascade(this.pnlDatabaseConfig);
                this.checkMySQLConf(confLoader.getDatabaseServer(), confLoader.getDatabasePort(), confLoader.getDatabaseAdmin(), confLoader.getDatabaseAdminPass(),
                        confLoader.getDatabaseUser(), confLoader.getDatabaseUserPass(), confLoader.getWorldDBName(), confLoader.getCharDBName(), confLoader.getRealmDBName());
            }

            cmakeOk = checkCMake(confLoader.getWin32PathCMake(), confLoader.getWin64PathCMake(), cmdManager, this.txpConsole);
            if (!cmakeOk) {
                this.lblDownloadCMake.setEnabled(true);
                this.chkSetupCMake.setEnabled(true);
                this.chkSetupCMake.setSelected(true);
                this.btnSetupMissingDeps.setEnabled(true);
                this.disableComponentCascade(this.pnlBuildInstall);
            } else {
                this.enableComponentCascade(this.pnlBuildInstall);
                this.checkCMakeConf(confLoader.getCMakeBuildFolder());
            }

            if (!checkOpenSSL(cmdManager, this.txpConsole)) {
                this.lblDownloadOpenSSL.setEnabled(true);
                this.chkSetupOpenSSL.setEnabled(true);
                this.chkSetupOpenSSL.setSelected(true);
                this.btnSetupMissingDeps.setEnabled(true);
            }

        } else {
            console.updateGUIConsole(this.txpConsole, "CRITICAL: Operatig system not supported.", ConsoleManager.TEXT_RED);
            this.disableComponentCascade(this.tabOperations);
        }
        cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        this.setCursor(cursor);
    }

    private void checkGitConf(/*String serverFolder, String databaseFolder, String elunaFolder*/) {
        this.serverFolder = setGitFolder(this.txtFolderServer.getText(), this.txtGitServer.getText());

        this.rdbGitServerWipe.setEnabled(false);
        this.rdbGitServerUpdate.setEnabled(false);
        this.rdbGitServerNew.setEnabled(false);
        if (cmdManager.checkFolder(this.serverFolder)) {
            //optGitSrvWipe = true;
            this.rdbGitServerWipe.setEnabled(true);
            // Check server version for update
            if (!cmdManager.isRepoUpToDate(this.serverFolder)) {
                this.rdbGitServerUpdate.setEnabled(true);
            }
        } else {
            this.rdbGitServerNew.setEnabled(true);
        }

        this.databaseFolder = setGitFolder(this.txtFolderDatabase.getText(), this.txtGitDatabase.getText());

        this.rdbGitDatabaseWipe.setEnabled(false);
        this.rdbGitDatabaseUpdate.setEnabled(false);
        this.rdbGitDatabaseNew.setEnabled(false);
        if (cmdManager.checkFolder(this.databaseFolder)) {
            //optGitSrvWipe = true;
            this.rdbGitDatabaseWipe.setEnabled(true);
            // Check server version for update
            if (!cmdManager.isRepoUpToDate(this.databaseFolder)) {
                this.rdbGitDatabaseUpdate.setEnabled(true);
            }
        } else {
            this.rdbGitDatabaseNew.setEnabled(true);
        }

        this.elunaFolder = setGitFolder(this.txtFolderLUA.getText(), this.txtGitLUA.getText());

        this.rdbGitLUAWipe.setEnabled(false);
        this.rdbGitLUAUpdate.setEnabled(false);
        this.rdbGitLUAUpdate.setEnabled(false);
        if (cmdManager.checkFolder(this.elunaFolder)) {
            //optGitSrvWipe = true;
            this.rdbGitLUAWipe.setEnabled(true);
            // Check server version for update
            if (!cmdManager.isRepoUpToDate(this.elunaFolder)) {
                this.rdbGitLUAUpdate.setEnabled(true);
            }
        } else {
            this.rdbGitLUANew.setEnabled(true);
        }
    }

    private void checkMySQLConf(String dbServer, String dbPort, String dbAdmin, String dbAdminPass, String dbUser, String dbUserPass, String dbWorld, String dbCharacter, String dbRealm) {
        if (!checkDBExistance(dbServer, dbPort, dbAdmin, dbAdminPass, dbUser, dbUserPass, dbWorld, dbCharacter, dbRealm, cmdManager, this.txpConsole)) {
            this.enableComponentCascade(this.pnlDBFirstInstall);
        } else {
            this.enableComponentCascade(this.pnlDBWorld);
            this.enableComponentCascade(this.pnlDBCharacter);
            this.enableComponentCascade(this.pnlDBRealm);
            this.disableComponentCascade(this.pnlDBFirstInstall);
        }
    }

    private void checkCMakeConf(String buildFolder) {
        buildFolder += File.separator + "CMakeFiles";
        if (!cmdManager.checkFolder(buildFolder)) {
            this.btnInstall.setEnabled(false);
        } else {
            this.btnInstall.setEnabled(true);
        }
    }

    private void disableComponentCascade(Component component) {
        try {
            if (component != null) {
                component.setEnabled(false);
                if (component instanceof Container) {
                    Container componentAsContainer = (Container) component;
                    for (Component c : componentAsContainer.getComponents()) {
                        this.disableComponentCascade(c);
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
                        this.enableComponentCascade(c);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    private void loadCmbCores() {
        String input = "0";
        try {
            input = this.cmbCores.getSelectedItem().toString().substring(0, 1);
        } catch (Exception ex) {
        }
        confLoader.getGitURLServer(input);
        confLoader.getGitURLDatabase(input);
        confLoader.getGitURLEluna(input);
        confLoader.getGitBranchServer(input);
        confLoader.getGitBranchDatabase(input);
        confLoader.getGitBranchEluna(input);
        this.applyConfLoaded();
        this.checkGitConf();
    }

    private void applyConfLoaded() {
        this.lblDownloadGit.setText("<html>Git: <a href=\"" + confLoader.getURLGit() + "\">" + confLoader.getURLGit() + "</a></html>");
        this.lblDownloadMySQL.setText("<html>MySQL: <a href=\"" + confLoader.getURLMySQL() + "\">" + confLoader.getURLMySQL() + "</a></html>");
        this.lblDownloadMySQL.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        this.lblDownloadCMake.setText("<html>CMake: <a href=\"" + confLoader.getURLCMake() + "\">" + confLoader.getURLCMake() + "</a></html>");
        this.lblDownloadOpenSSL.setText("<html>OpenSSL: <a href=\"" + confLoader.getURLOpenSSL() + "\">" + confLoader.getURLOpenSSL() + "</a></html>");

        this.txtGitServer.setText(confLoader.getGitURLServer());
        this.txtFolderServer.setText(confLoader.getGitFolderServer());
        this.txtBranchServer.setText(confLoader.getGitBranchServer());

        this.txtGitDatabase.setText(confLoader.getGitURLDatabase());
        this.txtFolderDatabase.setText(confLoader.getGitFolderDatabase());
        this.txtBranchDatabase.setText(confLoader.getGitBranchDatabase());

        this.txtGitLUA.setText(confLoader.getGitURLEluna());
        this.txtFolderLUA.setText(confLoader.getGitFolderEluna());
        this.txtBranchLUA.setText(confLoader.getGitBranchEluna());

        this.txtDBConfServer.setText(confLoader.getDatabaseServer());
        this.txtDBConfPort.setText(confLoader.getDatabasePort());
        this.txtDBConfAdmin.setText(confLoader.getDatabaseAdmin());
        this.txtDBConfAdminPwd.setText(confLoader.getDatabaseAdminPass());
        this.txtDBConfUser.setText(confLoader.getDatabaseUser());
        this.txtDBConfUserPwd.setText(confLoader.getDatabaseUserPass());

        this.txtWorldDBName.setText(confLoader.getWorldDBName());
        this.txtWorldFolder.setText(confLoader.getWorldFolder());
        this.txtWorldFullDB.setText(confLoader.getWorldFullDB());
        this.txtWorldLoadDB.setText(confLoader.getWorldLoadDBName());
        ArrayList<String> updFolders = new ArrayList<String>();
        for (String updFolder : confLoader.getWorldUpdRel().values()) {
            updFolders.add(updFolder);
        }
        Collections.sort(updFolders);
        this.lstWorldUpdFolders.setListData(updFolders.toArray(new String[updFolders.size()]));

        this.txtCharDBName.setText(confLoader.getCharDBName());
        this.txtCharFolder.setText(confLoader.getCharFolder());
        this.txtCharLoadDB.setText(confLoader.getCharLoadDBName());
        updFolders = new ArrayList<String>();
        for (String updFolder : confLoader.getCharUpdRel().values()) {
            updFolders.add(updFolder);
        }
        Collections.sort(updFolders);
        this.lstCharUpdFolders.setListData(updFolders.toArray(new String[updFolders.size()]));

        this.txtRealmDBName.setText(confLoader.getRealmDBName());
        this.txtRealmFolder.setText(confLoader.getRealmFolder());
        this.txtRealmLoadDB.setText(confLoader.getRealmLoadDBName());
        updFolders = new ArrayList<String>();
        for (String updFolder : confLoader.getRealmUpdRel().values()) {
            updFolders.add(updFolder);
        }
        Collections.sort(updFolders);
        this.lstRealmUpdFolders.setListData(updFolders.toArray(new String[updFolders.size()]));

        this.txtBuildFolder.setText(confLoader.getCMakeBuildFolder());
        ArrayList<String> cmakeOptions = new ArrayList<String>();
        for (String optKey : confLoader.getCmakeOptions().keySet()) {
            String lstItem = optKey.substring(optKey.indexOf(".") + 1);
            lstItem += "=" + confLoader.getCmakeOptions().get(optKey);
            cmakeOptions.add(lstItem);
        }
        Collections.sort(cmakeOptions);
        this.lstCMakeOptions.setListData(cmakeOptions.toArray(new String[cmakeOptions.size()]));

        String unixRun = "";
        if (cmdManager.getCURR_OS() == cmdManager.UNIX) {
            unixRun = "bin" + File.separator;
        }
        this.txtMapTools.setText(this.getRunFolder() + File.separator + unixRun + "tools");
        this.txtMapServer.setText(this.getRunFolder() + File.separator + "data");

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
        String newUpdFolder = JOptionPane.showInputDialog(null, "Insert new update folder that can be found inside 'Updates' folder:", "New update folder", JOptionPane.OK_CANCEL_OPTION);
        if (!newUpdFolder.isEmpty()) {
            ArrayList<String> currFolders = this.getListItems(jList);
            currFolders.add(newUpdFolder);
            Collections.sort(currFolders);
            jList.setListData(currFolders.toArray(new String[currFolders.size()]));
        }
    }

    private void remUpdFolder(JList<String> jList) {
        if (jList.getSelectedIndex() >= 0) {
            ArrayList<String> currFolders = this.getListItems(jList);
            currFolders.remove(jList.getSelectedIndex());
            Collections.sort(currFolders);
            jList.setListData(currFolders.toArray(new String[currFolders.size()]));
        }
    }

    private JButton dbWorkerSetup(final String dbFolder, final String loadScript, final String midUpdFolder, final String dbName) {
        final JButton btnWorker = new JButton("DB Worker");
        PropertyChangeListener propChangeCreation = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                DefaultCaret caret = (DefaultCaret) txpConsole.getCaret();
                caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

                if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
                    SwingWorker<Object, Object> mySqlWorker = new SwingWorker<Object, Object>() {

                        @Override
                        protected Object doInBackground() throws Exception {
                            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                            //cmdManager.setPrbCurrWork(prbDBCurrWork);
                            return mysqlLoadDB(txtDBConfServer.getText(), txtDBConfPort.getText(), txtDBConfAdmin.getText(), txtDBConfAdminPwd.getText(), databaseFolder, dbFolder, loadScript, dbName, midUpdFolder, cmdManager, txpConsole, prbDBCurrWork);

                        }

                        @Override
                        public void done() {
                            prbDBOverall.setValue(prbDBOverall.getValue() + 1);
                        }
                    };
                    mySqlWorker.execute();
                }
            }
        };
        btnWorker.addPropertyChangeListener(propChangeCreation);
        return btnWorker;
    }

    private JButton dbWorkersUpdate(final String dbFolder, final ArrayList<String> updSubFolders, final String midUpdFolder, final String dbName) {
        final JButton btnWorker = new JButton("DB Worker");
        PropertyChangeListener propChangeCreation = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                DefaultCaret caret = (DefaultCaret) txpConsole.getCaret();
                caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

                if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
                    //if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                    //cmdManager.setBtnInvoker(nextButton);
                    //cmdManager.setPrbCurrWork(prbDBCurrWork);
                    SwingWorker<Object, Object> mySqlWorker = new SwingWorker<Object, Object>() {

                        @Override
                        protected Object doInBackground() throws Exception {
                            //cmdManager.setPrbCurrWork(prbDBCurrWork);
                            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                            return mysqlUpdateDB(txtDBConfServer.getText(), txtDBConfPort.getText(), txtDBConfAdmin.getText(), txtDBConfAdminPwd.getText(), databaseFolder, dbFolder, updSubFolders, null, dbName, midUpdFolder,
                                    cmdManager, txpConsole, prbDBCurrWork);

                        }

                        @Override
                        public void done() {
                            prbDBOverall.setValue(prbDBOverall.getValue() + 1);
                        }
                    };
                    mySqlWorker.execute();
                    /*mysqlUpdateDB(txtDBConfServer.getText(), txtDBConfPort.getText(), txtDBConfAdmin.getText(), txtDBConfAdminPwd.getText(),
                     databaseFolder, dbFolder, updSubFolders, null, dbName, midUpdFolder,
                     cmdManager, console, txpGitConsole);
                     */ //console.updateGUIConsole(txpGitConsole, "Done", ConsoleManager.TEXT_BLUE);
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

    private void dbSetup(JList jList, String dbFolder, String dbName, String loadScript, String fullDB) {
        this.btnWorkList = new LinkedList<>();

        JButton btnSetup = this.dbWorkerSetup(dbFolder, loadScript, confLoader.getDatabaseSetupFolder(), dbName);
        this.btnWorkList.add(btnSetup);

        if (fullDB != null && !fullDB.isEmpty()) {
            ArrayList<String> updFolders = new ArrayList<>();
            updFolders.add(fullDB);
            JButton btnLoad = this.dbWorkersUpdate(dbFolder, updFolders, confLoader.getDatabaseSetupFolder(), dbName);
            this.btnWorkList.add(btnLoad);
        }
        this.dbUpdate(jList, dbFolder, dbName);

    }

    private void dbUpdate(JList jList, String dbFolder, String dbName) {
        if (this.btnWorkList == null || this.btnWorkList.isEmpty()) {
            this.btnWorkList = new LinkedList<>();
        }
        //prbDBCurrWork.setValue();
        //prbDBOverall.setMaximum(getListItems(lstWorldUpdFolders).size()+1);
        for (String curSubFolder : this.getListItems(jList)) {
            ArrayList<String> updFolders = new ArrayList<>();
            updFolders.add(curSubFolder);
            JButton btnWorker = this.dbWorkersUpdate(dbFolder, updFolders, confLoader.getDatabaseUpdateFolder(), dbName);
            this.btnWorkList.add(btnWorker);
            //prbDBCurrWork.setValue(prbDBCurrWork.getValue()+5);
        }
        this.prbDBOverall.setMaximum(this.btnWorkList.size());
        //cmdManager.setPrbCurrWork(prbDBCurrWork);
        this.btnWorkList.removeFirst().setText("Run");

    }

    private String getRunFolder() {
        ArrayList<String> currOptions = this.getListItems(this.lstCMakeOptions);
        for (String option : currOptions) {
            if (option.contains("CMAKE_INSTALL_PREFIX")) {
                return option.split("=")[1].replace("\"", "");
            }
        }
        return "";
    }

    private void openURL(final String url) {
        SwingWorker<Object, Object> browserOpener = new SwingWorker<Object, Object>() {

            @Override
            protected Object doInBackground() throws Exception {
                if (Desktop.isDesktopSupported()) {
                    try {
                        // Windows
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (URISyntaxException | IOException ex) {
                        Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    // Ubuntu
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        runtime.exec("/usr/bin/firefox -new-window " + url);
                    } catch (IOException ex) {
                        Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return "";
            }

            @Override
            public void done() {
                //prbDBOverall.setValue(prbDBOverall.getValue() + 1);
            }
        };
        browserOpener.execute();
    }

    private JButton depsSetup(final int option) {
        this.prbSetupProgress.setMaximum(this.prbSetupProgress.getMaximum() + 1);
        final JButton btnSetupWorker = new JButton("Setup Worker");
        PropertyChangeListener propChangeCreation;
        propChangeCreation = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
                    SwingWorker<Object, Object> depsWorker = new SwingWorker<Object, Object>() {

                        @Override
                        protected Object doInBackground() throws Exception {
                            boolean retval = false;
                            switch (option) {
                                case 1:
                                    retval = cmdManager.setupGit(txpConsole, prbSetupProgress);
                                    break;
                                case 2:
                                    retval = cmdManager.setupMySQL(txpConsole, prbSetupProgress);
                                    break;
                                case 3:
                                    retval = cmdManager.setupCMake(txpConsole, prbSetupProgress);
                                    break;
                                case 4:
                                    retval = cmdManager.setupOpenSSL(txpConsole, prbSetupProgress);
                                    break;
                            }
                            return retval;
                        }

                        @Override
                        public void done() {
                            try {
                                if ((boolean) this.get()) {
                                    //btnDBCheckMouseClicked(null);
                                    console.updateGUIConsole(txpConsole, "Done", ConsoleManager.TEXT_BLUE);
                                } else {
                                    console.updateGUIConsole(txpConsole, "ERROR: Check console output and redo setup.", ConsoleManager.TEXT_RED);
                                }
                                //btnSetupMissingDeps.setEnabled(true);
                            } catch (InterruptedException | ExecutionException ex) {
                                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                                //console.updateGUIConsole(txpConsole, "ERROR: .", ConsoleManager.TEXT_RED);
                            }
                            prbSetupProgress.setValue(prbSetupProgress.getValue() + 1);
                            if (!btnWorkList.isEmpty()) {
                                btnWorkList.removeFirst().setText("Run");
                            }
                        }
                    };
                    depsWorker.execute();
                }
            }
        };
        btnSetupWorker.addPropertyChangeListener(propChangeCreation);
        return btnSetupWorker;
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
        pnlSysDeps = new javax.swing.JPanel();
        pnlSetupDeps = new javax.swing.JPanel();
        chkSetupGit = new javax.swing.JCheckBox();
        chkSetupMySQL = new javax.swing.JCheckBox();
        chkSetupCMake = new javax.swing.JCheckBox();
        chkSetupOpenSSL = new javax.swing.JCheckBox();
        btnSetupMissingDeps = new javax.swing.JButton();
        btnCheckDeps = new javax.swing.JButton();
        prbSetupProgress = new javax.swing.JProgressBar();
        pnlDownloadDeps = new javax.swing.JPanel();
        lblDownloadGit = new javax.swing.JLabel();
        lblDownloadMySQL = new javax.swing.JLabel();
        lblDownloadCMake = new javax.swing.JLabel();
        lblDownloadOpenSSL = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
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
        lblDBCurrentJob = new javax.swing.JLabel();
        pnlDBStatus = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        prbDBCurrWork = new javax.swing.JProgressBar();
        jLabel32 = new javax.swing.JLabel();
        prbDBOverall = new javax.swing.JProgressBar();
        pnlBuildInstall = new javax.swing.JPanel();
        pnlBuild = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        lstCMakeOptions = new javax.swing.JList();
        jLabel34 = new javax.swing.JLabel();
        btnCMakeOptAdd = new javax.swing.JButton();
        btnCMakeOptDel = new javax.swing.JButton();
        btnCMakeOptEdit = new javax.swing.JButton();
        jLabel35 = new javax.swing.JLabel();
        txtBuildFolder = new javax.swing.JTextField();
        btnBuild = new javax.swing.JButton();
        btnInstall = new javax.swing.JButton();
        btnSetupLuaScripts = new javax.swing.JButton();
        lblNextStep = new javax.swing.JLabel();
        pnlMapExtraction = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        txtMapTools = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        txtMapClient = new javax.swing.JTextField();
        jLabel40 = new javax.swing.JLabel();
        txtMapServer = new javax.swing.JTextField();
        btnMapExtractor = new javax.swing.JButton();
        pnlExtractionResult = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        prbMapExtraction = new javax.swing.JProgressBar();
        chkMapExtracted = new javax.swing.JCheckBox();
        chkVMapExtracted = new javax.swing.JCheckBox();
        chkVMapAssmbled = new javax.swing.JCheckBox();
        chkMMapGenerated = new javax.swing.JCheckBox();
        chkMapCleaning = new javax.swing.JCheckBox();
        pnlDownloadConsole = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txpConsole = new javax.swing.JTextPane();

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

        pnlSysDeps.setEnabled(false);

        pnlSetupDeps.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Needed dependecies", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        chkSetupGit.setText("Git");
        chkSetupGit.setEnabled(false);

        chkSetupMySQL.setText("MySQL");
        chkSetupMySQL.setEnabled(false);

        chkSetupCMake.setText("CMake");
        chkSetupCMake.setEnabled(false);

        chkSetupOpenSSL.setText("OpenSSL");
        chkSetupOpenSSL.setEnabled(false);

        btnSetupMissingDeps.setText("Install missing");
        btnSetupMissingDeps.setEnabled(false);
        btnSetupMissingDeps.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSetupMissingDepsMouseClicked(evt);
            }
        });
        btnSetupMissingDeps.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnSetupMissingDepsPropertyChange(evt);
            }
        });
        btnSetupMissingDeps.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnSetupMissingDepsKeyPressed(evt);
            }
        });

        btnCheckDeps.setText("Check deps");
        btnCheckDeps.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCheckDepsMouseClicked(evt);
            }
        });
        btnCheckDeps.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCheckDepsKeyPressed(evt);
            }
        });

        prbSetupProgress.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                prbSetupProgressStateChanged(evt);
            }
        });

        javax.swing.GroupLayout pnlSetupDepsLayout = new javax.swing.GroupLayout(pnlSetupDeps);
        pnlSetupDeps.setLayout(pnlSetupDepsLayout);
        pnlSetupDepsLayout.setHorizontalGroup(
            pnlSetupDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSetupDepsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSetupDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(prbSetupProgress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlSetupDepsLayout.createSequentialGroup()
                        .addGroup(pnlSetupDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkSetupGit)
                            .addComponent(chkSetupMySQL)
                            .addComponent(chkSetupCMake))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addGroup(pnlSetupDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnSetupMissingDeps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCheckDeps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlSetupDepsLayout.createSequentialGroup()
                        .addComponent(chkSetupOpenSSL)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlSetupDepsLayout.setVerticalGroup(
            pnlSetupDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSetupDepsLayout.createSequentialGroup()
                .addGroup(pnlSetupDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSetupDepsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(chkSetupGit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkSetupMySQL)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkSetupCMake)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkSetupOpenSSL))
                    .addGroup(pnlSetupDepsLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(btnSetupMissingDeps)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCheckDeps)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(prbSetupProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlDownloadDeps.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Download dependencies", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        lblDownloadGit.setText("<html>Git: <a href=\"http://sourceforge.net/projects/gitextensions/files/latest/download\">http://sourceforge.net/projects/gitextensions/files/latest/download</a></html>");
        lblDownloadGit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblDownloadGit.setEnabled(false);
        lblDownloadGit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDownloadGitMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblDownloadGitMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblDownloadGitMouseExited(evt);
            }
        });

        lblDownloadMySQL.setText("<html>MySQL: <a href=\"https://dev.mysql.com/downloads/windows/installer/5.6.html\">https://dev.mysql.com/downloads/windows/installer/5.6.html</a></html>");
        lblDownloadMySQL.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblDownloadMySQL.setEnabled(false);
        lblDownloadMySQL.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDownloadMySQLMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblDownloadMySQLMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblDownloadMySQLMouseExited(evt);
            }
        });

        lblDownloadCMake.setText("<html>CMake: <a href=\"http://www.cmake.org/files/v3.3/cmake-3.3.1-win32-x86.exe\">http://www.cmake.org/files/v3.3/cmake-3.3.1-win32-x86.exe</a></html>");
        lblDownloadCMake.setToolTipText("");
        lblDownloadCMake.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblDownloadCMake.setEnabled(false);
        lblDownloadCMake.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDownloadCMakeMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblDownloadCMakeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblDownloadCMakeMouseExited(evt);
            }
        });

        lblDownloadOpenSSL.setText("<html>OpenSSL: <a href=\"http://slproweb.com/download/Win32OpenSSL-1_0_2d.exe\">http://slproweb.com/download/Win32OpenSSL-1_0_2d.exe</a></html>");
        lblDownloadOpenSSL.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblDownloadOpenSSL.setEnabled(false);
        lblDownloadOpenSSL.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDownloadOpenSSLMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblDownloadOpenSSLMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblDownloadOpenSSLMouseExited(evt);
            }
        });

        jLabel36.setText("Download and install follow enabled links to anable all MaNGOS UI features...");

        javax.swing.GroupLayout pnlDownloadDepsLayout = new javax.swing.GroupLayout(pnlDownloadDeps);
        pnlDownloadDeps.setLayout(pnlDownloadDepsLayout);
        pnlDownloadDepsLayout.setHorizontalGroup(
            pnlDownloadDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDownloadDepsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDownloadDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDownloadGit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDownloadMySQL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDownloadCMake, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDownloadOpenSSL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlDownloadDepsLayout.setVerticalGroup(
            pnlDownloadDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDownloadDepsLayout.createSequentialGroup()
                .addComponent(jLabel36)
                .addGap(9, 9, 9)
                .addComponent(lblDownloadGit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDownloadMySQL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDownloadCMake, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDownloadOpenSSL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlSysDepsLayout = new javax.swing.GroupLayout(pnlSysDeps);
        pnlSysDeps.setLayout(pnlSysDepsLayout);
        pnlSysDepsLayout.setHorizontalGroup(
            pnlSysDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSysDepsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlSetupDeps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 355, Short.MAX_VALUE)
                .addComponent(pnlDownloadDeps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlSysDepsLayout.setVerticalGroup(
            pnlSysDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSysDepsLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(pnlSysDepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlDownloadDeps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlSetupDeps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(230, Short.MAX_VALUE))
        );

        tabOperations.addTab("System deps", pnlSysDeps);

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

        chkProxy.setText("Use http proxy");
        chkProxy.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        chkProxy.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkProxyItemStateChanged(evt);
            }
        });

        jLabel11.setText("Port");

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
                .addGap(0, 21, Short.MAX_VALUE))
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
                .addComponent(pnlGitRepos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlDownloadLayout.setVerticalGroup(
            pnlDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDownloadLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlGitRepos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        btnDBCharSetup.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDBCharSetupMouseClicked(evt);
            }
        });
        btnDBCharSetup.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnDBCharSetupPropertyChange(evt);
            }
        });
        btnDBCharSetup.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnDBCharSetupKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout pnlDBCharacterLayout = new javax.swing.GroupLayout(pnlDBCharacter);
        pnlDBCharacter.setLayout(pnlDBCharacterLayout);
        pnlDBCharacterLayout.setHorizontalGroup(
            pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                        .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel24)
                            .addComponent(txtCharDBName, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25)
                            .addComponent(txtCharFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCharAddUpdFolder))
                        .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnCharDelUpdFolder))
                                .addGap(0, 10, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDBCharacterLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel28)
                                .addGap(38, 38, 38))))
                    .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                        .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                                .addComponent(rdbDBCharWipe)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rdbDBCharUpdate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnDBCharSetup)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(pnlDBCharacterLayout.createSequentialGroup()
                                .addComponent(jLabel27)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtCharLoadDB, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
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
                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCharDelUpdFolder)
                    .addComponent(btnCharAddUpdFolder))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(txtCharLoadDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDBCharacterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbDBCharWipe)
                    .addComponent(rdbDBCharUpdate)
                    .addComponent(btnDBCharSetup))
                .addContainerGap())
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
        btnDBRealmSetup.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDBRealmSetupMouseClicked(evt);
            }
        });
        btnDBRealmSetup.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnDBRealmSetupPropertyChange(evt);
            }
        });
        btnDBRealmSetup.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnDBRealmSetupKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout pnlDBRealmLayout = new javax.swing.GroupLayout(pnlDBRealm);
        pnlDBRealm.setLayout(pnlDBRealmLayout);
        pnlDBRealmLayout.setHorizontalGroup(
            pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDBRealmLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDBRealmLayout.createSequentialGroup()
                        .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel26)
                            .addComponent(txtRealmDBName, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel29)
                            .addComponent(txtRealmFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDBRealmLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 11, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDBRealmLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel31)
                                .addGap(37, 37, 37))))
                    .addGroup(pnlDBRealmLayout.createSequentialGroup()
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtRealmLoadDB, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(pnlDBRealmLayout.createSequentialGroup()
                        .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDBRealmLayout.createSequentialGroup()
                                .addComponent(btnRealmAddUpdFolder)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRealmDelUpdFolder))
                            .addGroup(pnlDBRealmLayout.createSequentialGroup()
                                .addComponent(rdbDBRealmWipe)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rdbDBRealmUpdate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnDBRealmSetup)))
                        .addGap(0, 0, Short.MAX_VALUE))))
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
                .addGap(6, 6, 6)
                .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRealmAddUpdFolder)
                    .addComponent(btnRealmDelUpdFolder))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(txtRealmLoadDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDBRealmLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdbDBRealmWipe)
                    .addComponent(rdbDBRealmUpdate)
                    .addComponent(btnDBRealmSetup))
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

        jLabel33.setText("Current job:");

        prbDBCurrWork.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                prbDBCurrWorkStateChanged(evt);
            }
        });

        jLabel32.setText("Overall progress:");

        prbDBOverall.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                prbDBOverallStateChanged(evt);
            }
        });

        javax.swing.GroupLayout pnlDBStatusLayout = new javax.swing.GroupLayout(pnlDBStatus);
        pnlDBStatus.setLayout(pnlDBStatusLayout);
        pnlDBStatusLayout.setHorizontalGroup(
            pnlDBStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDBStatusLayout.createSequentialGroup()
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(prbDBCurrWork, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel32)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(prbDBOverall, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlDBStatusLayout.setVerticalGroup(
            pnlDBStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDBStatusLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlDBStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel32)
                    .addComponent(prbDBOverall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlDBStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel33)
                        .addComponent(prbDBCurrWork, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                        .addComponent(pnlDatabaseConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlDBFirstInstall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(113, Short.MAX_VALUE))
                    .addGroup(pnlDatabaseLayout.createSequentialGroup()
                        .addGroup(pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlDBWorld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlDatabaseLayout.createSequentialGroup()
                                .addGap(65, 65, 65)
                                .addComponent(lblDBCurrentJob)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlDatabaseLayout.createSequentialGroup()
                                .addComponent(pnlDBCharacter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pnlDBRealm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(pnlDBStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        pnlDatabaseLayout.setVerticalGroup(
            pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatabaseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlDatabaseConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDBFirstInstall, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlDatabaseLayout.createSequentialGroup()
                        .addGroup(pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pnlDBWorld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pnlDBStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblDBCurrentJob)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlDatabaseLayout.createSequentialGroup()
                        .addGroup(pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlDBCharacter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pnlDBRealm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        tabOperations.addTab("Database setup", pnlDatabase);

        pnlBuild.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Build parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        lstCMakeOptions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane5.setViewportView(lstCMakeOptions);

        jLabel34.setText("Build options (leave no value to use defaults)");

        btnCMakeOptAdd.setText("Add option");
        btnCMakeOptAdd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCMakeOptAddMouseClicked(evt);
            }
        });
        btnCMakeOptAdd.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnCMakeOptAddPropertyChange(evt);
            }
        });
        btnCMakeOptAdd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCMakeOptAddKeyPressed(evt);
            }
        });

        btnCMakeOptDel.setText("Delete option");
        btnCMakeOptDel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCMakeOptDelMouseClicked(evt);
            }
        });
        btnCMakeOptDel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnCMakeOptDelPropertyChange(evt);
            }
        });
        btnCMakeOptDel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCMakeOptDelKeyPressed(evt);
            }
        });

        btnCMakeOptEdit.setText("Edit option");
        btnCMakeOptEdit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCMakeOptEditMouseClicked(evt);
            }
        });
        btnCMakeOptEdit.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnCMakeOptEditPropertyChange(evt);
            }
        });
        btnCMakeOptEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCMakeOptEditKeyPressed(evt);
            }
        });

        jLabel35.setText("Build folder");

        txtBuildFolder.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtBuildFolderFocusLost(evt);
            }
        });

        btnBuild.setText("Build");
        btnBuild.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnBuildMouseClicked(evt);
            }
        });
        btnBuild.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnBuildPropertyChange(evt);
            }
        });
        btnBuild.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnBuildKeyPressed(evt);
            }
        });

        btnInstall.setText("Install");
        btnInstall.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnInstallMouseClicked(evt);
            }
        });
        btnInstall.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnInstallPropertyChange(evt);
            }
        });
        btnInstall.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnInstallKeyPressed(evt);
            }
        });

        btnSetupLuaScripts.setText("Install LUA Scripts");
        btnSetupLuaScripts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSetupLuaScriptsMouseClicked(evt);
            }
        });
        btnSetupLuaScripts.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnSetupLuaScriptsPropertyChange(evt);
            }
        });
        btnSetupLuaScripts.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnSetupLuaScriptsKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout pnlBuildLayout = new javax.swing.GroupLayout(pnlBuild);
        pnlBuild.setLayout(pnlBuildLayout);
        pnlBuildLayout.setHorizontalGroup(
            pnlBuildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBuildLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBuildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBuildLayout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlBuildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnCMakeOptDel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCMakeOptEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCMakeOptAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlBuildLayout.createSequentialGroup()
                        .addGroup(pnlBuildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel34)
                            .addComponent(jLabel35))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlBuildLayout.createSequentialGroup()
                        .addComponent(txtBuildFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnBuild)
                        .addGap(18, 18, 18)
                        .addComponent(btnInstall)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSetupLuaScripts)))
                .addContainerGap())
        );
        pnlBuildLayout.setVerticalGroup(
            pnlBuildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBuildLayout.createSequentialGroup()
                .addComponent(jLabel34)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBuildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlBuildLayout.createSequentialGroup()
                        .addComponent(btnCMakeOptAdd)
                        .addGap(18, 18, 18)
                        .addComponent(btnCMakeOptEdit)
                        .addGap(18, 18, 18)
                        .addComponent(btnCMakeOptDel))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel35)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBuildLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuildFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuild)
                    .addComponent(btnInstall)
                    .addComponent(btnSetupLuaScripts))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        lblNextStep.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblNextStep.setForeground(new java.awt.Color(255, 0, 0));
        lblNextStep.setText("<html>MaNGOS server is now installed. To complete the installation you need:<br/>\n1. Add a new row inside \"realmlist\" table with new MaNGOS installed information.<br/>\n2. Extract game data from a WoW client (use next tab to do this).<br/>\n3. Configure \".conf\" file.<br/>\n4. Run \"realmd\" and \"mangosd\" deamons from intall folder.<br/>\n<br/> <br/>\nEnjoy!\n</html>");

        javax.swing.GroupLayout pnlBuildInstallLayout = new javax.swing.GroupLayout(pnlBuildInstall);
        pnlBuildInstall.setLayout(pnlBuildInstallLayout);
        pnlBuildInstallLayout.setHorizontalGroup(
            pnlBuildInstallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBuildInstallLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlBuild, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblNextStep, javax.swing.GroupLayout.PREFERRED_SIZE, 364, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlBuildInstallLayout.setVerticalGroup(
            pnlBuildInstallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBuildInstallLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBuildInstallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlBuild, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblNextStep))
                .addContainerGap())
        );

        tabOperations.addTab("Build and Install", pnlBuildInstall);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Wow client information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        jLabel38.setText("Path to tools forlder");

        jLabel39.setText("Path to WoW client folder");

        jLabel40.setText("Path to MaNGOS runnable folder");

        btnMapExtractor.setText("Run extractions tools");
        btnMapExtractor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnMapExtractorMouseClicked(evt);
            }
        });
        btnMapExtractor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                btnMapExtractorPropertyChange(evt);
            }
        });
        btnMapExtractor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnMapExtractorKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMapTools)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel38)
                            .addComponent(jLabel39)
                            .addComponent(jLabel40))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(txtMapClient)
                    .addComponent(txtMapServer))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(240, 240, 240)
                .addComponent(btnMapExtractor)
                .addContainerGap(237, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel38)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMapTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel39)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMapClient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel40)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMapServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnMapExtractor)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        pnlExtractionResult.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Extraction results", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP));

        jLabel41.setText("Extraction progress");

        prbMapExtraction.setMaximum(5);
        prbMapExtraction.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                prbMapExtractionStateChanged(evt);
            }
        });

        chkMapExtracted.setText("Map extraction");
        chkMapExtracted.setToolTipText("");

        chkVMapExtracted.setText("VMap extraction");

        chkVMapAssmbled.setText("VMap assembled");

        chkMMapGenerated.setText("MMap generated");

        chkMapCleaning.setText("Clean unused folder");

        javax.swing.GroupLayout pnlExtractionResultLayout = new javax.swing.GroupLayout(pnlExtractionResult);
        pnlExtractionResult.setLayout(pnlExtractionResultLayout);
        pnlExtractionResultLayout.setHorizontalGroup(
            pnlExtractionResultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExtractionResultLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlExtractionResultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(prbMapExtraction, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                    .addGroup(pnlExtractionResultLayout.createSequentialGroup()
                        .addGroup(pnlExtractionResultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkMapCleaning)
                            .addComponent(chkMMapGenerated)
                            .addComponent(chkVMapAssmbled)
                            .addComponent(chkVMapExtracted)
                            .addComponent(chkMapExtracted)
                            .addComponent(jLabel41))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlExtractionResultLayout.setVerticalGroup(
            pnlExtractionResultLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExtractionResultLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel41)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(prbMapExtraction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(chkMapExtracted)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkVMapExtracted)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkVMapAssmbled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMMapGenerated)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMapCleaning)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlMapExtractionLayout = new javax.swing.GroupLayout(pnlMapExtraction);
        pnlMapExtraction.setLayout(pnlMapExtractionLayout);
        pnlMapExtractionLayout.setHorizontalGroup(
            pnlMapExtractionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMapExtractionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(pnlExtractionResult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlMapExtractionLayout.setVerticalGroup(
            pnlMapExtractionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMapExtractionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMapExtractionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlExtractionResult, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(152, Short.MAX_VALUE))
        );

        tabOperations.addTab("Map-VMap-MMap extraction", pnlMapExtraction);

        pnlDownloadConsole.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Console", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));

        txpConsole.setEditable(false);
        txpConsole.setText("Welcome to MaNGOS Universal Installer.\nCredit to: Antz (for MySQL and OpenSSL checks) and Faded (for Linux dependecy setup)\nInitializing...");
        txpConsole.setAutoscrolls(false);
        jScrollPane2.setViewportView(txpConsole);

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
                    .addComponent(pnlDownloadConsole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
        if (this.chkProxy.isSelected()) {
            // Enable components
            this.txtProxyServer.setText(confLoader.getProxyServer());
            this.txtProxyServer.setEnabled(true);
            this.txtProxyPort.setText(confLoader.getProxyPort());
            this.txtProxyPort.setEnabled(true);
        } else {
            // Disable components
            this.txtProxyServer.setText("");
            this.txtProxyServer.setEnabled(false);
            this.txtProxyPort.setText("");
            this.txtProxyPort.setEnabled(false);
        }
    }//GEN-LAST:event_chkProxyItemStateChanged

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        this.doAllChecks();
    }//GEN-LAST:event_formWindowOpened

    private void cmbCoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCoresActionPerformed
        this.loadCmbCores();
    }//GEN-LAST:event_cmbCoresActionPerformed

    private void btnServerDownloadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnServerDownloadKeyPressed
        this.btnServerDownloadMouseClicked(null);
    }//GEN-LAST:event_btnServerDownloadKeyPressed

    private void btnServerDownloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnServerDownloadMouseClicked
        if (this.btnGrpGitServer.getSelection() != null) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            if (this.chkProxy.isSelected()) {
                cmdManager.setGitProxy(this.txtProxyServer.getText(), this.txtProxyPort.getText(), this.txpConsole);
            }
            this.btnServerDownload.setEnabled(false);
            this.btnDatabaseDownload.setEnabled(false);
            this.btnLUADownload.setEnabled(false);
            cmdManager.setBtnInvoker(this.btnServerDownload);
            this.serverFolder = setGitFolder(this.txtFolderServer.getText(), this.txtGitServer.getText());
            gitDownload(this.btnGrpGitServer.getSelection().getActionCommand(), this.txtGitServer.getText(), this.serverFolder, this.txtBranchServer.getText(), "", "", cmdManager, this.txpConsole);
        }
    }//GEN-LAST:event_btnServerDownloadMouseClicked

    private void btnDatabaseDownloadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDatabaseDownloadKeyPressed
        this.btnDatabaseDownloadMouseClicked(null);
    }//GEN-LAST:event_btnDatabaseDownloadKeyPressed

    private void btnDatabaseDownloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDatabaseDownloadMouseClicked
        if (this.btnGrpGitDatabase.getSelection() != null) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            if (this.chkProxy.isSelected()) {
                cmdManager.setGitProxy(this.txtProxyServer.getText(), this.txtProxyPort.getText(), this.txpConsole);
            }
            this.btnServerDownload.setEnabled(false);
            this.btnDatabaseDownload.setEnabled(false);
            this.btnLUADownload.setEnabled(false);
            cmdManager.setBtnInvoker(this.btnDatabaseDownload);
            this.databaseFolder = setGitFolder(this.txtFolderDatabase.getText(), this.txtGitDatabase.getText());
            gitDownload(this.btnGrpGitDatabase.getSelection().getActionCommand(), this.txtGitDatabase.getText(), this.databaseFolder, this.txtBranchDatabase.getText(), "", "", cmdManager, this.txpConsole);
        }
    }//GEN-LAST:event_btnDatabaseDownloadMouseClicked

    private void btnLUADownloadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnLUADownloadKeyPressed
        this.btnLUADownloadMouseClicked(null);
    }//GEN-LAST:event_btnLUADownloadKeyPressed

    private void btnLUADownloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLUADownloadMouseClicked
        if (this.btnGrpGitLUA.getSelection() != null) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            if (this.chkProxy.isSelected()) {
                cmdManager.setGitProxy(this.txtProxyServer.getText(), this.txtProxyPort.getText(), this.txpConsole);
            }
            this.btnServerDownload.setEnabled(false);
            this.btnDatabaseDownload.setEnabled(false);
            this.btnLUADownload.setEnabled(false);
            cmdManager.setBtnInvoker(this.btnLUADownload);
            this.elunaFolder = setGitFolder(this.txtFolderLUA.getText(), this.txtGitLUA.getText());
            gitDownload(this.btnGrpGitLUA.getSelection().getActionCommand(), this.txtGitLUA.getText(), this.elunaFolder, this.txtBranchLUA.getText(), "", "", cmdManager, this.txpConsole);
        }
    }//GEN-LAST:event_btnLUADownloadMouseClicked

    private void btnServerDownloadPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnServerDownloadPropertyChange
        if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(this.txpConsole, "Done", ConsoleManager.TEXT_BLUE);
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(this.txpConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
            }
            this.btnDatabaseDownload.setEnabled(true);
            //btnServerDownload.setEnabled(true);
            this.btnLUADownload.setEnabled(true);
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_btnServerDownloadPropertyChange

    private void btnDatabaseDownloadPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnDatabaseDownloadPropertyChange
        if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                mysqlOk = checkMySQL(this.databaseFolder, confLoader.getPathToMySQL(), cmdManager, this.txpConsole);
                if (!mysqlOk) {
                    this.disableComponentCascade(this.pnlDatabase);
                } else {
                    this.pnlDatabase.setEnabled(true);
                    this.enableComponentCascade(this.pnlDatabaseConfig);
                }
                console.updateGUIConsole(this.txpConsole, "Done", ConsoleManager.TEXT_BLUE);
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(this.txpConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
            }
            //btnDatabaseDownload.setEnabled(true);
            this.btnServerDownload.setEnabled(true);
            this.btnLUADownload.setEnabled(true);
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_btnDatabaseDownloadPropertyChange

    private void btnLUADownloadPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnLUADownloadPropertyChange
        if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(this.txpConsole, "Done", ConsoleManager.TEXT_BLUE);
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(this.txpConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
            }
            this.btnDatabaseDownload.setEnabled(true);
            this.btnServerDownload.setEnabled(true);
            //btnLUADownload.setEnabled(true);
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_btnLUADownloadPropertyChange

    private void btnDBCheckMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDBCheckMouseClicked
        //cmdManager.setBtnInvoker(btnDBCheck);
        if (evt == null || evt.getComponent().isEnabled()) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            this.checkMySQLConf(this.txtDBConfServer.getText(), this.txtDBConfPort.getText(), this.txtDBConfAdmin.getText(), this.txtDBConfAdminPwd.getText(),
                    this.txtDBConfUser.getText(), this.txtDBConfUserPwd.getText(), confLoader.getWorldDBName(), confLoader.getCharDBName(), confLoader.getRealmDBName());
            cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_btnDBCheckMouseClicked

    private void btnDBCheckKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDBCheckKeyPressed
        this.btnDBCheckMouseClicked(null);
    }//GEN-LAST:event_btnDBCheckKeyPressed

    private void btnDBFirstInstallKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDBFirstInstallKeyPressed
        this.btnDBFirstInstallMouseClicked(null);
    }//GEN-LAST:event_btnDBFirstInstallKeyPressed

    private void btnDBFirstInstallMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDBFirstInstallMouseClicked
        if (evt.getComponent().isEnabled()) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            cmdManager.setBtnInvoker(this.btnDBFirstInstall);
            mysqlCreateDB(this.txtDBConfServer.getText(), this.txtDBConfPort.getText(), this.txtDBConfAdmin.getText(), this.txtDBConfAdminPwd.getText(),
                    this.txtDBConfUser.getText(), this.txtDBConfUserPwd.getText(), this.txtWorldDBName.getText(), this.txtCharDBName.getText(), this.txtRealmDBName.getText(),
                    cmdManager, this.txpConsole);
        }
    }//GEN-LAST:event_btnDBFirstInstallMouseClicked

    private void btnDBFirstInstallPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnDBFirstInstallPropertyChange
        if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                this.btnDBCheckMouseClicked(null);
                //console.updateGUIConsole(txpGitConsole, "Done", ConsoleManager.TEXT_BLUE);
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                //console.updateGUIConsole(txpGitConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
            }
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_btnDBFirstInstallPropertyChange

    private void btnWorldAddUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnWorldAddUpdFolderKeyPressed
        this.btnWorldAddUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnWorldAddUpdFolderKeyPressed

    private void btnWorldAddUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnWorldAddUpdFolderMouseClicked
        if (evt.getComponent().isEnabled()) {
            this.addUpdFolder(this.lstWorldUpdFolders);
        }
    }//GEN-LAST:event_btnWorldAddUpdFolderMouseClicked

    private void btnWorldDelUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnWorldDelUpdFolderKeyPressed
        this.btnWorldDelUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnWorldDelUpdFolderKeyPressed

    private void btnWorldDelUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnWorldDelUpdFolderMouseClicked
        if (evt.getComponent().isEnabled()) {
            this.remUpdFolder(this.lstWorldUpdFolders);
        }
    }//GEN-LAST:event_btnWorldDelUpdFolderMouseClicked

    private void btnCharAddUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCharAddUpdFolderKeyPressed
        this.btnCharAddUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnCharAddUpdFolderKeyPressed

    private void btnCharAddUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCharAddUpdFolderMouseClicked
        if (evt.getComponent().isEnabled()) {
            this.addUpdFolder(this.lstCharUpdFolders);
        }
    }//GEN-LAST:event_btnCharAddUpdFolderMouseClicked

    private void btnCharDelUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCharDelUpdFolderKeyPressed
        this.btnCharDelUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnCharDelUpdFolderKeyPressed

    private void btnCharDelUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCharDelUpdFolderMouseClicked
        this.remUpdFolder(this.lstCharUpdFolders);
    }//GEN-LAST:event_btnCharDelUpdFolderMouseClicked

    private void btnRealmAddUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnRealmAddUpdFolderKeyPressed
        this.btnRealmAddUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnRealmAddUpdFolderKeyPressed

    private void btnRealmAddUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRealmAddUpdFolderMouseClicked
        if (evt.getComponent().isEnabled()) {
            this.addUpdFolder(this.lstRealmUpdFolders);
        }
    }//GEN-LAST:event_btnRealmAddUpdFolderMouseClicked

    private void btnRealmDelUpdFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnRealmDelUpdFolderKeyPressed
        this.btnRealmDelUpdFolderMouseClicked(null);
    }//GEN-LAST:event_btnRealmDelUpdFolderKeyPressed

    private void btnRealmDelUpdFolderMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRealmDelUpdFolderMouseClicked
        this.remUpdFolder(this.lstRealmUpdFolders);
    }//GEN-LAST:event_btnRealmDelUpdFolderMouseClicked

    private void btnDBWorldSetupKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDBWorldSetupKeyPressed
        this.btnDBWorldSetupMouseClicked(null);
    }//GEN-LAST:event_btnDBWorldSetupKeyPressed

    private void btnDBWorldSetupMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDBWorldSetupMouseClicked
        if (this.btnGrpDBWorld.getSelection() != null) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            this.btnInvoker = this.btnDBWorldSetup;
            //cmdManager.setPrbCurrWork(prbDBCurrWork);
            this.disableComponentCascade(this.pnlDBWorld);
            this.disableComponentCascade(this.pnlDBCharacter);
            this.disableComponentCascade(this.pnlDBRealm);
            this.disableComponentCascade(this.pnlDatabaseConfig);
            this.enableComponentCascade(this.pnlDBStatus);
            this.prbDBCurrWork.setValue(0);
            this.prbDBOverall.setValue(0);

            if ("W".equalsIgnoreCase(this.btnGrpDBWorld.getSelection().getActionCommand())) {
                this.dbSetup(this.lstWorldUpdFolders, this.txtWorldFolder.getText(), this.txtWorldDBName.getText(), this.txtWorldLoadDB.getText(), this.txtWorldFullDB.getText());
            } else if ("U".equalsIgnoreCase(this.btnGrpDBWorld.getSelection().getActionCommand())) {
                this.dbUpdate(this.lstWorldUpdFolders, this.txtWorldFolder.getText(), this.txtWorldDBName.getText());
            }
        }
    }//GEN-LAST:event_btnDBWorldSetupMouseClicked

    private void btnDBWorldSetupPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnDBWorldSetupPropertyChange

    }//GEN-LAST:event_btnDBWorldSetupPropertyChange

    private void btnDBCharSetupKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDBCharSetupKeyPressed
        this.btnDBCharSetupMouseClicked(null);
    }//GEN-LAST:event_btnDBCharSetupKeyPressed

    private void btnDBCharSetupMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDBCharSetupMouseClicked
        if (this.btnGrpDBCharacter.getSelection() != null) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            this.btnInvoker = this.btnDBCharSetup;
            //cmdManager.setPrbCurrWork(prbDBCurrWork);
            this.disableComponentCascade(this.pnlDBWorld);
            this.disableComponentCascade(this.pnlDBCharacter);
            this.disableComponentCascade(this.pnlDBRealm);
            this.disableComponentCascade(this.pnlDatabaseConfig);
            this.enableComponentCascade(this.pnlDBStatus);
            this.prbDBCurrWork.setValue(0);
            this.prbDBOverall.setValue(0);

            if ("W".equalsIgnoreCase(this.btnGrpDBCharacter.getSelection().getActionCommand())) {
                this.dbSetup(this.lstCharUpdFolders, this.txtCharFolder.getText(), this.txtCharDBName.getText(), this.txtCharLoadDB.getText(), null);
            } else if ("U".equalsIgnoreCase(this.btnGrpDBCharacter.getSelection().getActionCommand())) {
                this.dbUpdate(this.lstCharUpdFolders, this.txtCharFolder.getText(), this.txtCharDBName.getText());
            }
        }
    }//GEN-LAST:event_btnDBCharSetupMouseClicked

    private void btnDBCharSetupPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnDBCharSetupPropertyChange

    }//GEN-LAST:event_btnDBCharSetupPropertyChange

    private void btnDBRealmSetupKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnDBRealmSetupKeyPressed
        this.btnDBRealmSetupMouseClicked(null);
    }//GEN-LAST:event_btnDBRealmSetupKeyPressed

    private void btnDBRealmSetupMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDBRealmSetupMouseClicked
        if (this.btnGrpDBRealm.getSelection() != null) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            this.btnInvoker = this.btnDBRealmSetup;
            //cmdManager.setPrbCurrWork(prbDBCurrWork);
            this.disableComponentCascade(this.pnlDBWorld);
            this.disableComponentCascade(this.pnlDBCharacter);
            this.disableComponentCascade(this.pnlDBRealm);
            this.disableComponentCascade(this.pnlDatabaseConfig);
            this.enableComponentCascade(this.pnlDBStatus);
            this.prbDBCurrWork.setValue(0);
            this.prbDBOverall.setValue(0);

            if ("W".equalsIgnoreCase(this.btnGrpDBRealm.getSelection().getActionCommand())) {
                this.dbSetup(this.lstRealmUpdFolders, this.txtRealmFolder.getText(), this.txtRealmDBName.getText(), this.txtRealmLoadDB.getText(), null);
            } else if ("U".equalsIgnoreCase(this.btnGrpDBRealm.getSelection().getActionCommand())) {
                this.dbUpdate(this.lstRealmUpdFolders, this.txtRealmFolder.getText(), this.txtRealmDBName.getText());
            }
        }
    }//GEN-LAST:event_btnDBRealmSetupMouseClicked

    private void btnDBRealmSetupPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnDBRealmSetupPropertyChange

    }//GEN-LAST:event_btnDBRealmSetupPropertyChange

    private void prbDBCurrWorkStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_prbDBCurrWorkStateChanged
        if (this.prbDBCurrWork.getValue() >= this.prbDBCurrWork.getMaximum()) {
            this.prbDBCurrWork.setValue(0);
            console.updateGUIConsole(this.txpConsole, "\nCurrent job done.", ConsoleManager.TEXT_BLUE);
            if (!this.btnWorkList.isEmpty()) {
                this.btnWorkList.removeFirst().setText("Run");
            }
        }
    }//GEN-LAST:event_prbDBCurrWorkStateChanged

    private void prbDBOverallStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_prbDBOverallStateChanged
        if (this.prbDBOverall.getValue() >= this.prbDBOverall.getMaximum()) {
            //cmdManager.setPrbCurrWork(null);
            //prbDBCurrWork.setValue(0);
            //prbDBOverall.setValue(0);
            console.updateGUIConsole(this.txpConsole, "\nAll processes done.", ConsoleManager.TEXT_BLUE);
            this.enableComponentCascade(this.pnlDBWorld);
            this.enableComponentCascade(this.pnlDBCharacter);
            this.enableComponentCascade(this.pnlDBRealm);
            this.enableComponentCascade(this.pnlDatabaseConfig);
            this.disableComponentCascade(this.pnlDBStatus);
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_prbDBOverallStateChanged

    private void btnCMakeOptAddKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCMakeOptAddKeyPressed
        this.btnCMakeOptAddMouseClicked(null);
    }//GEN-LAST:event_btnCMakeOptAddKeyPressed

    private void btnCMakeOptAddMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCMakeOptAddMouseClicked
        if (evt.getComponent().isEnabled()) {
            String newOption = JOptionPane.showInputDialog(null, "Insert new option for CMake process with format 'OptionName=Value':", "New CMake ption", JOptionPane.OK_CANCEL_OPTION);
            if (newOption != null && !newOption.isEmpty()) {
                ArrayList<String> currOptions = this.getListItems(this.lstCMakeOptions);
                currOptions.add(newOption);
                Collections.sort(currOptions);
                this.lstCMakeOptions.setListData(currOptions.toArray(new String[currOptions.size()]));
            }
        }
    }//GEN-LAST:event_btnCMakeOptAddMouseClicked

    private void btnCMakeOptAddPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnCMakeOptAddPropertyChange

    }//GEN-LAST:event_btnCMakeOptAddPropertyChange

    private void btnCMakeOptEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCMakeOptEditKeyPressed
        this.btnCMakeOptEditMouseClicked(null);
    }//GEN-LAST:event_btnCMakeOptEditKeyPressed

    private void btnCMakeOptEditMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCMakeOptEditMouseClicked
        if (evt.getComponent().isEnabled() && this.lstCMakeOptions.getSelectedIndex() >= 0) {
            ArrayList<String> currOptions = this.getListItems(this.lstCMakeOptions);
//            String lstItem = currOptions.get(lstCMakeOptions.getSelectedIndex());
            String newlstItem = (String) JOptionPane.showInputDialog(null, "Edit selected option for CMake process with format 'OptionName=Value':", "Edit CMake option", JOptionPane.OK_CANCEL_OPTION, null, null, currOptions.get(this.lstCMakeOptions.getSelectedIndex()));
            if (newlstItem != null && !newlstItem.isEmpty()) {
                currOptions.remove(this.lstCMakeOptions.getSelectedIndex());
                currOptions.add(newlstItem);
                Collections.sort(currOptions);
                this.lstCMakeOptions.setListData(currOptions.toArray(new String[currOptions.size()]));
            }
        }
    }//GEN-LAST:event_btnCMakeOptEditMouseClicked

    private void btnCMakeOptEditPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnCMakeOptEditPropertyChange

    }//GEN-LAST:event_btnCMakeOptEditPropertyChange

    private void btnCMakeOptDelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCMakeOptDelKeyPressed
        this.btnCMakeOptDelMouseClicked(null);
    }//GEN-LAST:event_btnCMakeOptDelKeyPressed

    private void btnCMakeOptDelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCMakeOptDelMouseClicked
        if (evt.getComponent().isEnabled() && this.lstCMakeOptions.getSelectedIndex() >= 0) {
            ArrayList<String> currOptions = this.getListItems(this.lstCMakeOptions);
            currOptions.remove(this.lstCMakeOptions.getSelectedIndex());
            Collections.sort(currOptions);
            this.lstCMakeOptions.setListData(currOptions.toArray(new String[currOptions.size()]));
        }
    }//GEN-LAST:event_btnCMakeOptDelMouseClicked

    private void btnCMakeOptDelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnCMakeOptDelPropertyChange

    }//GEN-LAST:event_btnCMakeOptDelPropertyChange

    private void txtBuildFolderFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtBuildFolderFocusLost
        this.checkCMakeConf(this.txtBuildFolder.getText());
    }//GEN-LAST:event_txtBuildFolderFocusLost

    private void btnBuildKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnBuildKeyPressed
        this.btnBuildMouseClicked(null);
    }//GEN-LAST:event_btnBuildKeyPressed

    private void btnBuildMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuildMouseClicked
        if (evt.getComponent().isEnabled() && JOptionPane.showConfirmDialog(null, "WARNING: This operation may overwrite already built project. Do you want to continue?", "Build confirmation", JOptionPane.OK_CANCEL_OPTION) == 0) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            ArrayList<String> currOptions = this.getListItems(this.lstCMakeOptions);
            HashMap<String, String> cmakeOptions = new HashMap<>();
            for (String item : currOptions) {
                String[] option = item.split("=");
                if (option.length > 1) {
                    cmakeOptions.put(option[0], option[1]);
                }
            }
            this.disableComponentCascade(this.pnlBuildInstall);
            cmdManager.setBtnInvoker(this.btnBuild);
            cmdManager.cmakeConfig(this.serverFolder, this.txtBuildFolder.getText(), cmakeOptions, this.txpConsole);
        }
    }//GEN-LAST:event_btnBuildMouseClicked

    private void btnBuildPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnBuildPropertyChange
        if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                //btnDBCheckMouseClicked(null);
                console.updateGUIConsole(this.txpConsole, "Done", ConsoleManager.TEXT_BLUE);
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                //console.updateGUIConsole(txpGitConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
            }
            this.enableComponentCascade(this.pnlBuildInstall);
            this.btnInstall.setEnabled(true);
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_btnBuildPropertyChange

    private void btnInstallKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnInstallKeyPressed
        this.btnInstallMouseClicked(null);
    }//GEN-LAST:event_btnInstallKeyPressed

    private void btnInstallMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnInstallMouseClicked
        if (evt.getComponent().isEnabled()) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            this.disableComponentCascade(this.pnlBuildInstall);
            cmdManager.setBtnInvoker(this.btnInstall);
            cmdManager.cmakeInstall(this.txtBuildFolder.getText(), this.getRunFolder(), confLoader.getCMakeBuildType(), this.txpConsole);
        }
    }//GEN-LAST:event_btnInstallMouseClicked

    private void btnInstallPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnInstallPropertyChange
        if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                //btnDBCheckMouseClicked(null);
                if (cmdManager.getCURR_OS() == cmdManager.UNIX || (cmdManager.copyFolder(this.txtBuildFolder.getText() + File.separator + "bin" + File.separator + "Debug", this.getRunFolder(), this.txpConsole))) {
                    console.updateGUIConsole(this.txpConsole, "Done", ConsoleManager.TEXT_BLUE);
                    this.lblNextStep.setVisible(true);
                } else {
                    console.updateGUIConsole(this.txpConsole, "ERROR: check console output and redo process.", ConsoleManager.TEXT_RED);
                }
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(this.txpConsole, "ERROR: check console output and redo process.", ConsoleManager.TEXT_RED);
            }
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(cursor);
            this.enableComponentCascade(this.pnlBuildInstall);
        }
    }//GEN-LAST:event_btnInstallPropertyChange

    private void btnSetupLuaScriptsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnSetupLuaScriptsKeyPressed
        this.btnSetupLuaScriptsMouseClicked(null);
    }//GEN-LAST:event_btnSetupLuaScriptsKeyPressed

    private void btnSetupLuaScriptsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSetupLuaScriptsMouseClicked
        if (evt.getComponent().isEnabled()) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            String runFolder = this.getRunFolder();
            if (cmdManager.checkFolder(this.elunaFolder) && mysqlOk && !runFolder.isEmpty() && cmdManager.checkFolder(runFolder)) {
                this.disableComponentCascade(this.pnlBuildInstall);

                SwingWorker<Object, Object> mySqlWorker;
                mySqlWorker = new SwingWorker<Object, Object>() {

                    @Override
                    protected Object doInBackground() throws Exception {
                        String setupPath = elunaFolder + File.separator + "sql";
                        //cmdManager.setBtnInvoker(btnSetupLuaScripts);
                        return mysqlUpdateDB(txtDBConfServer.getText(), txtDBConfPort.getText(), txtDBConfAdmin.getText(), txtDBConfAdminPwd.getText(), databaseFolder, null, null, setupPath, txtWorldDBName.getText(), null, cmdManager, txpConsole, prbDBCurrWork);
                    }

                    @Override
                    public void done() {
                        try {
                            //prbDBOverall.setValue(prbDBOverall.getValue() + 1);
                            if (true == (boolean) this.get()) {
                                btnSetupLuaScripts.setText("DONE");
                                //btnInvoker.setActionCommand("DONE");
                            } else {
                                //btnInvoker.setActionCommand("ERROR");
                                btnSetupLuaScripts.setText("ERROR");
                            }
                        } catch (InterruptedException | ExecutionException ex) {
                            //Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                            btnSetupLuaScripts.setText("ERROR");
                        }
                    }
                };
                mySqlWorker.execute();
            }
        }
    }//GEN-LAST:event_btnSetupLuaScriptsMouseClicked

    private void btnSetupLuaScriptsPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnSetupLuaScriptsPropertyChange
        if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                //btnDBCheckMouseClicked(null);
                String luaSrc = (this.txtFolderLUA.getText().isEmpty() ? this.elunaFolder : this.txtFolderLUA.getText().replace("\"", "")) + File.separator + "lua_scripts";
                String luaDst = this.getRunFolder() + File.separator + "lua_scripts";
                console.updateGUIConsole(this.txpConsole, "\nInstalling binaries into destination folder...", ConsoleManager.TEXT_BLUE);
                boolean cpRet = cmdManager.copyFolder(luaSrc, luaDst, this.txpConsole);
                if (cpRet) {
                    console.updateGUIConsole(this.txpConsole, "Done", ConsoleManager.TEXT_BLUE);
                } else {
                    console.updateGUIConsole(this.txpConsole, "ERROR: check console output and redo process.", ConsoleManager.TEXT_RED);
                }
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                console.updateGUIConsole(this.txpConsole, "ERROR: Check console output and redo process.", ConsoleManager.TEXT_RED);
            }
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(cursor);
            this.enableComponentCascade(this.pnlBuildInstall);
        }
    }//GEN-LAST:event_btnSetupLuaScriptsPropertyChange

    private void btnSetupMissingDepsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnSetupMissingDepsKeyPressed
        this.btnSetupMissingDepsMouseClicked(null);
    }//GEN-LAST:event_btnSetupMissingDepsKeyPressed

    private void btnSetupMissingDepsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSetupMissingDepsMouseClicked
        if (evt.getComponent().isEnabled()
                && (this.chkSetupCMake.isSelected() || this.chkSetupGit.isSelected() || this.chkSetupMySQL.isSelected() || this.chkSetupOpenSSL.isSelected())) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.setCursor(cursor);
            this.btnSetupMissingDeps.setEnabled(false);
            this.btnWorkList = new LinkedList<>();
            this.prbSetupProgress.setValue(0);
            this.prbSetupProgress.setMaximum(0);
            if (cmdManager.checkRootConsole(null)) {
                if (this.chkSetupGit.isEnabled()) {
                    this.btnWorkList.add(this.depsSetup(1));
                }
                if (this.chkSetupMySQL.isEnabled()) {
                    this.btnWorkList.add(this.depsSetup(2));
                }
                if (this.chkSetupCMake.isEnabled()) {
                    this.btnWorkList.add(this.depsSetup(3));
                }
                if (this.chkSetupOpenSSL.isEnabled()) {
                    this.btnWorkList.add(this.depsSetup(4));
                }
                if (!this.btnWorkList.isEmpty()) {
                    this.btnWorkList.removeFirst().setText("Run");
                }
            } else {
                console.updateGUIConsole(this.txpConsole, "\nERROR: Cannot install missing dependecies because MaNGOS UI was not launched from ROOT console. Try it again with ROOT privileges.", WIDTH);
            }
        }
    }//GEN-LAST:event_btnSetupMissingDepsMouseClicked

    private void btnSetupMissingDepsPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnSetupMissingDepsPropertyChange
        if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                //btnDBCheckMouseClicked(null);
                console.updateGUIConsole(this.txpConsole, "Done", ConsoleManager.TEXT_BLUE);
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                //console.updateGUIConsole(txpGitConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
            }
            this.btnSetupMissingDeps.setEnabled(true);
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_btnSetupMissingDepsPropertyChange

    private void lblDownloadGitMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadGitMouseClicked
        this.openURL(confLoader.getURLGit());
    }//GEN-LAST:event_lblDownloadGitMouseClicked

    private void lblDownloadMySQLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadMySQLMouseClicked
        this.openURL(confLoader.getURLMySQL());
    }//GEN-LAST:event_lblDownloadMySQLMouseClicked

    private void lblDownloadCMakeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadCMakeMouseClicked
        this.openURL(confLoader.getURLCMake());
    }//GEN-LAST:event_lblDownloadCMakeMouseClicked

    private void lblDownloadOpenSSLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadOpenSSLMouseClicked
        this.openURL(confLoader.getURLOpenSSL());
    }//GEN-LAST:event_lblDownloadOpenSSLMouseClicked

    private void btnMapExtractorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnMapExtractorKeyPressed
        this.btnMapExtractorMouseClicked(null);
    }//GEN-LAST:event_btnMapExtractorKeyPressed

    private void btnMapExtractorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMapExtractorMouseClicked
        Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        this.setCursor(cursor);
        this.disableComponentCascade(this.pnlMapExtraction);
        this.btnWorkList = new LinkedList<>();
        this.chkMapExtracted.setSelected(false);
        this.chkVMapExtracted.setSelected(false);
        this.chkVMapAssmbled.setSelected(false);
        this.chkMMapGenerated.setSelected(false);
        this.chkMapCleaning.setSelected(false);
        this.prbMapExtraction.setValue(0);

        final JButton btnClean = new JButton("Clean");
        PropertyChangeListener btnCleanPropertyChange = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
                    if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                        chkMapCleaning.setSelected(true);
                    }
                    prbMapExtraction.setValue(prbMapExtraction.getValue() + 1);
                    btnMapExtractor.setText((String) evt.getNewValue());
                }
            }
        };
        btnClean.addPropertyChangeListener(btnCleanPropertyChange);

        final JButton btnMMapGenerator = new JButton("Generate MMap");
        PropertyChangeListener btnMMapGeneratorPropertyChange = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
                    if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                        chkMMapGenerated.setSelected(true);
                    }
                    prbMapExtraction.setValue(prbMapExtraction.getValue() + 1);
                    if (cmdManager.checkFolder(txtMapServer.getText() + File.separator + "Buildings")) {
                        cmdManager.deleteFolder(txtMapServer.getText() + File.separator + "Buildings");
                    }
                    btnClean.setText((String) evt.getNewValue());
                }
            }
        };
        btnMMapGenerator.addPropertyChangeListener(btnMMapGeneratorPropertyChange);

        final JButton btnVMapAssembler = new JButton("Assemble VMap");
        PropertyChangeListener btnVMapAssemblerPropertyChange = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
                    if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                        chkVMapAssmbled.setSelected(true);
                    }
                    prbMapExtraction.setValue(prbMapExtraction.getValue() + 1);
                    cmdManager.setBtnInvoker(btnMMapGenerator);
                    cmdManager.mapExtraction(txtMapTools.getText(), txtMapClient.getText(), txtMapServer.getText(), 4, txpConsole, null);
                }
            }
        };
        btnVMapAssembler.addPropertyChangeListener(btnVMapAssemblerPropertyChange);

        final JButton btnVMapExtractor = new JButton("Extract VMap");
        PropertyChangeListener btnVMapExtractorPropertyChange = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
                    if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                        chkVMapExtracted.setSelected(true);
                    }
                    prbMapExtraction.setValue(prbMapExtraction.getValue() + 1);
                    cmdManager.setBtnInvoker(btnVMapAssembler);
                    cmdManager.mapExtraction(txtMapTools.getText(), txtMapClient.getText(), txtMapServer.getText(), 3, txpConsole, null);
                }
            }
        };
        btnVMapExtractor.addPropertyChangeListener(btnVMapExtractorPropertyChange);

        final JButton btnMapExtract = new JButton("Extract Map");
        PropertyChangeListener btnMapExtractorPropertyChange = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
                    chkMapExtracted.setSelected(true);
                    prbMapExtraction.setValue(prbMapExtraction.getValue() + 1);
                    cmdManager.setBtnInvoker(btnVMapExtractor);
                    cmdManager.mapExtraction(txtMapTools.getText(), txtMapClient.getText(), txtMapServer.getText(), 2, txpConsole, null);
                }
            }
        };
        btnMapExtract.addPropertyChangeListener(btnMapExtractorPropertyChange);

        cmdManager.setBtnInvoker(btnMapExtract);
        cmdManager.mapExtraction(this.txtMapTools.getText(), this.txtMapClient.getText(), this.txtMapServer.getText(), 1, this.txpConsole, null);

    }//GEN-LAST:event_btnMapExtractorMouseClicked

    private void btnMapExtractorPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_btnMapExtractorPropertyChange
        if ("Text".equalsIgnoreCase(evt.getPropertyName())) {
            if ("DONE".equalsIgnoreCase((String) evt.getNewValue())) {
                //btnDBCheckMouseClicked(null);
                console.updateGUIConsole(this.txpConsole, "Done", ConsoleManager.TEXT_BLUE);
            } else if ("ERROR".equalsIgnoreCase((String) evt.getNewValue())) {
                //console.updateGUIConsole(txpGitConsole, "ERROR: Check console output and redo process with W (wipe) option.", ConsoleManager.TEXT_RED);
            }
            this.enableComponentCascade(this.pnlMapExtraction);
            this.btnInstall.setEnabled(true);
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_btnMapExtractorPropertyChange

    private void prbMapExtractionStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_prbMapExtractionStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_prbMapExtractionStateChanged

    private void lblDownloadMySQLMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadMySQLMouseEntered
        if (evt.getComponent().isEnabled()) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_lblDownloadMySQLMouseEntered

    private void lblDownloadMySQLMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadMySQLMouseExited
        Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        this.setCursor(cursor);
    }//GEN-LAST:event_lblDownloadMySQLMouseExited

    private void lblDownloadGitMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadGitMouseEntered
        if (evt.getComponent().isEnabled()) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_lblDownloadGitMouseEntered

    private void lblDownloadGitMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadGitMouseExited
        Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        this.setCursor(cursor);
    }//GEN-LAST:event_lblDownloadGitMouseExited

    private void lblDownloadCMakeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadCMakeMouseExited
        Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        this.setCursor(cursor);
    }//GEN-LAST:event_lblDownloadCMakeMouseExited

    private void lblDownloadCMakeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadCMakeMouseEntered
        if (evt.getComponent().isEnabled()) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_lblDownloadCMakeMouseEntered

    private void lblDownloadOpenSSLMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadOpenSSLMouseEntered
        if (evt.getComponent().isEnabled()) {
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            this.setCursor(cursor);
        }
    }//GEN-LAST:event_lblDownloadOpenSSLMouseEntered

    private void lblDownloadOpenSSLMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadOpenSSLMouseExited
        Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        this.setCursor(cursor);
    }//GEN-LAST:event_lblDownloadOpenSSLMouseExited

    private void btnCheckDepsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCheckDepsKeyPressed
        this.btnCheckDepsMouseClicked(null);
    }//GEN-LAST:event_btnCheckDepsKeyPressed

    private void btnCheckDepsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCheckDepsMouseClicked
        this.doAllChecks();
    }//GEN-LAST:event_btnCheckDepsMouseClicked

    private void prbSetupProgressStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_prbSetupProgressStateChanged
        /*if (!btnWorkList.isEmpty()) {
         btnWorkList.removeFirst().setText("Run");
         }*/

    }//GEN-LAST:event_prbSetupProgressStateChanged

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
    private javax.swing.JButton btnBuild;
    private javax.swing.JButton btnCMakeOptAdd;
    private javax.swing.JButton btnCMakeOptDel;
    private javax.swing.JButton btnCMakeOptEdit;
    private javax.swing.JButton btnCharAddUpdFolder;
    private javax.swing.JButton btnCharDelUpdFolder;
    private javax.swing.JButton btnCheckDeps;
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
    private javax.swing.JButton btnInstall;
    private javax.swing.JButton btnLUADownload;
    private javax.swing.JButton btnMapExtractor;
    private javax.swing.JButton btnRealmAddUpdFolder;
    private javax.swing.JButton btnRealmDelUpdFolder;
    private javax.swing.JButton btnServerDownload;
    private javax.swing.JButton btnSetupLuaScripts;
    private javax.swing.JButton btnSetupMissingDeps;
    private javax.swing.JButton btnWorldAddUpdFolder;
    private javax.swing.JButton btnWorldDelUpdFolder;
    private javax.swing.JCheckBox chkMMapGenerated;
    private javax.swing.JCheckBox chkMapCleaning;
    private javax.swing.JCheckBox chkMapExtracted;
    private javax.swing.JCheckBox chkProxy;
    private javax.swing.JCheckBox chkSetupCMake;
    private javax.swing.JCheckBox chkSetupGit;
    private javax.swing.JCheckBox chkSetupMySQL;
    private javax.swing.JCheckBox chkSetupOpenSSL;
    private javax.swing.JCheckBox chkVMapAssmbled;
    private javax.swing.JCheckBox chkVMapExtracted;
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
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JLabel lblDBCurrentJob;
    private javax.swing.JLabel lblDownloadCMake;
    private javax.swing.JLabel lblDownloadGit;
    private javax.swing.JLabel lblDownloadMySQL;
    private javax.swing.JLabel lblDownloadOpenSSL;
    private javax.swing.JLabel lblNextStep;
    private javax.swing.JList lstCMakeOptions;
    private javax.swing.JList lstCharUpdFolders;
    private javax.swing.JList lstRealmUpdFolders;
    private javax.swing.JList lstWorldUpdFolders;
    private javax.swing.JPanel pnlBuild;
    private javax.swing.JPanel pnlBuildInstall;
    private javax.swing.JPanel pnlDBCharacter;
    private javax.swing.JPanel pnlDBFirstInstall;
    private javax.swing.JPanel pnlDBRealm;
    private javax.swing.JPanel pnlDBStatus;
    private javax.swing.JPanel pnlDBWorld;
    private javax.swing.JPanel pnlDatabase;
    private javax.swing.JPanel pnlDatabaseConfig;
    private javax.swing.JPanel pnlDownload;
    private javax.swing.JPanel pnlDownloadConsole;
    private javax.swing.JPanel pnlDownloadDeps;
    private javax.swing.JPanel pnlExtractionResult;
    private javax.swing.JPanel pnlGitDatabase;
    private javax.swing.JPanel pnlGitLUA;
    private javax.swing.JPanel pnlGitRepos;
    private javax.swing.JPanel pnlGitServer;
    private javax.swing.JPanel pnlMapExtraction;
    private javax.swing.JPanel pnlProxy;
    private javax.swing.JPanel pnlSetupDeps;
    private javax.swing.JPanel pnlSysDeps;
    private javax.swing.JProgressBar prbDBCurrWork;
    private javax.swing.JProgressBar prbDBOverall;
    private javax.swing.JProgressBar prbMapExtraction;
    private javax.swing.JProgressBar prbSetupProgress;
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
    private javax.swing.JTextPane txpConsole;
    private javax.swing.JTextField txtBranchDatabase;
    private javax.swing.JTextField txtBranchLUA;
    private javax.swing.JTextField txtBranchServer;
    private javax.swing.JTextField txtBuildFolder;
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
    private javax.swing.JTextField txtMapClient;
    private javax.swing.JTextField txtMapServer;
    private javax.swing.JTextField txtMapTools;
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
