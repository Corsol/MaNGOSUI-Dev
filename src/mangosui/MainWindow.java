/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.File;

/**
 *
 * @author Simone
 */
public class MainWindow extends javax.swing.JFrame {

    private static ConfLoader confLoader;
    private static ConsoleManager console;
    private static CommandManager cmdManager;
    private static String gitPath = "";
    private static String mySQLPath = "";
    private static String cmakePath = "";
    private static final long serialVersionUID = 1L;

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        cmdManager = new CommandManager();

        initComponents();

        if (!confLoader.isConfLoaded()) {
            console.updateGUIConsole(txpGitConsole, "ERROR: Configuration file NOT loaded. Check it.", console.TEXT_RED);
        } else {
            applyConfLoaded();
        }
        if (cmdManager.getCURR_OS() > 0) {
            console.updateGUIConsole(txpGitConsole, "INFO: Your OS is: " + cmdManager.getOsName() + " version " + cmdManager.getOsVersion() + " with java architecture: " + cmdManager.getOsArch(), console.TEXT_BLUE);

            if (cmdManager.getCURR_OS() == cmdManager.WINDOWS && !cmdManager.isPSScriptEnabled()) {
                console.updateGUIConsole(txpGitConsole, "WARNING: PowerShell script execution is not ebabled. To enable it run PS (x86) as Administrator and use \"Set-ExecutionPolicy Unrestricted\" command.", console.TEXT_ORANGE);
            }
            cmdManager.setWinGitPath(confLoader.getWinPathGit());

            if (!cmdManager.checkGit(null)) {
                btnGitDownload.setEnabled(false);
                console.updateGUIConsole(txpGitConsole, "ERROR: Git commands not found on system. Check Git installation or manually download repositories.", console.TEXT_RED);
            } else {
                gitPath = confLoader.getWinPathGit();
                console.updateGUIConsole(txpGitConsole, "INFO: Founded Git commands.", console.TEXT_BLUE);
            }

            if (!cmdManager.checkMySQL(null)) {
                console.updateGUIConsole(txpGitConsole, "INFO: MySQL is not locally installed... checking for mysql.exe tool.", console.TEXT_BLUE);

                String mysqlToolPath = "database" + File.separator + confLoader.getPathToMySQL();
                if (!txtFolderDatabase.getText().isEmpty()) {
                    mysqlToolPath = txtFolderDatabase.getText() + File.separator + confLoader.getPathToMySQL();
                }
                if (!cmdManager.checkMySQL(mysqlToolPath, null)) {
                    //.setEnabled(false);
                    console.updateGUIConsole(txpGitConsole, "WARNING: mysql.exe command not found on system. Check mysql installation or path '" + mysqlToolPath + "' to mysql.exe into database folder.", console.TEXT_RED);
                } else {
                    mySQLPath = mysqlToolPath;
                    console.updateGUIConsole(txpGitConsole, "INFO: Founded MySQL commands.", console.TEXT_BLUE);
                }
            } else {
                console.updateGUIConsole(txpGitConsole, "INFO: Founded MySQL commands.", console.TEXT_BLUE);
            }

            if (!cmdManager.checkCMAKE(null)) {
                //.setEnabled(false);
                console.updateGUIConsole(txpGitConsole, "INFO: CMAKE is not installed into PATH/shell environment... checking for cmake.exe installation folder.", console.TEXT_BLUE);
                String cmake32Path = confLoader.getWin32PathCMake();
                String cmake64Path = confLoader.getWin64PathCMake();
                if (cmdManager.checkCMAKE(cmake32Path, null)) {
                    // cmakeOk = true;
                    System.out.println("INFO: Founded CMAKE commands on x86 system.");
                } else if (cmdManager.checkCMAKE(cmake64Path, null)) {
                    // cmakeOk = true;
                    System.out.println("INFO: Founded CMAKE commands on x64 system.");
                } else {
                    console.updateGUIConsole(txpGitConsole, "ERROR: CMAKE commands not found on system. Check CMAKE installation!.", console.TEXT_RED);
                }
                console.updateGUIConsole(txpGitConsole, "ERROR: CMAKE commands not found on system. Check CMAKE installation!.", console.TEXT_RED);
            } else {
                // cmakePath =
                console.updateGUIConsole(txpGitConsole, "INFO: Founded CMAKE commands.", console.TEXT_BLUE);
            }

        } else {
            console.updateGUIConsole(txpGitConsole, "CRITICAL: Operatig system not supported.", console.TEXT_RED);
            tabOperations.setEnabled(false);
            btnGitDownload.setEnabled(false);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        pnlGitDatabase = new javax.swing.JPanel();
        txtGitDatabase = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtFolderDatabase = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtBranchDatabase = new javax.swing.JTextField();
        pnlProxy = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        txtProxyServer = new javax.swing.JTextField();
        chkProxy = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        txtProxyPort = new javax.swing.JTextField();
        btnGitDownload = new javax.swing.JButton();
        pnlDownloadConsole = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txpGitConsole = new javax.swing.JTextPane();
        pnlCompile = new javax.swing.JPanel();
        pnlDatabase = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tabOperations.setName("Download"); // NOI18N

        pnlGitRepos.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Git Repositories", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        pnlGitServer.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Server", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlGitServer.setPreferredSize(new java.awt.Dimension(292, 166));

        txtGitServer.setToolTipText("Insert here the https url to your git server repository");

        jLabel1.setText("Git URL");

        jLabel2.setText("Destination folder");

        txtFolderServer.setToolTipText("(Optional) Specify the destination folder for repository download");

        jLabel3.setText("Git Brach");

        txtBranchServer.setToolTipText("(Optional) Specify a different branch from master");

        javax.swing.GroupLayout pnlGitServerLayout = new javax.swing.GroupLayout(pnlGitServer);
        pnlGitServer.setLayout(pnlGitServerLayout);
        pnlGitServerLayout.setHorizontalGroup(
            pnlGitServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitServerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGitServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtGitServer)
                    .addComponent(txtFolderServer)
                    .addComponent(txtBranchServer)
                    .addGroup(pnlGitServerLayout.createSequentialGroup()
                        .addGroup(pnlGitServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(0, 179, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlGitServerLayout.setVerticalGroup(
            pnlGitServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitServerLayout.createSequentialGroup()
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
                .addComponent(txtBranchServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 39, Short.MAX_VALUE))
        );

        pnlGitDatabase.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Database", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlGitDatabase.setPreferredSize(new java.awt.Dimension(292, 166));

        txtGitDatabase.setToolTipText("Insert here the https url to your git database repository");

        jLabel7.setText("Git URL");

        jLabel8.setText("Destination folder");

        txtFolderDatabase.setToolTipText("(Optional) Specify the destination folder for repository download");

        jLabel9.setText("Git Brach");

        txtBranchDatabase.setToolTipText("(Optional) Specify a different branch from master");

        javax.swing.GroupLayout pnlGitDatabaseLayout = new javax.swing.GroupLayout(pnlGitDatabase);
        pnlGitDatabase.setLayout(pnlGitDatabaseLayout);
        pnlGitDatabaseLayout.setHorizontalGroup(
            pnlGitDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitDatabaseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGitDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtGitDatabase)
                    .addComponent(txtFolderDatabase)
                    .addComponent(txtBranchDatabase)
                    .addGroup(pnlGitDatabaseLayout.createSequentialGroup()
                        .addGroup(pnlGitDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addGap(0, 175, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlGitDatabaseLayout.setVerticalGroup(
            pnlGitDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitDatabaseLayout.createSequentialGroup()
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
                .addComponent(txtBranchDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pnlProxy.setBorder(javax.swing.BorderFactory.createTitledBorder("Proxy"));

        jLabel10.setText("Server name");

        txtProxyServer.setEditable(false);
        txtProxyServer.setText("proxy");

        chkProxy.setText("Use http proxy");
        chkProxy.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chkProxyItemStateChanged(evt);
            }
        });

        jLabel11.setText("Port");

        txtProxyPort.setEditable(false);
        txtProxyPort.setText("8080");

        javax.swing.GroupLayout pnlProxyLayout = new javax.swing.GroupLayout(pnlProxy);
        pnlProxy.setLayout(pnlProxyLayout);
        pnlProxyLayout.setHorizontalGroup(
            pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProxyLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(chkProxy)
                    .addComponent(jLabel10)
                    .addComponent(txtProxyServer, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                    .addComponent(jLabel11)
                    .addComponent(txtProxyPort))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlProxyLayout.setVerticalGroup(
            pnlProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProxyLayout.createSequentialGroup()
                .addComponent(chkProxy)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtProxyServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        btnGitDownload.setText("Download");
        btnGitDownload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnGitDownloadMouseClicked(evt);
            }
        });
        btnGitDownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGitDownloadActionPerformed(evt);
            }
        });
        btnGitDownload.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnGitDownloadKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout pnlGitReposLayout = new javax.swing.GroupLayout(pnlGitRepos);
        pnlGitRepos.setLayout(pnlGitReposLayout);
        pnlGitReposLayout.setHorizontalGroup(
            pnlGitReposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitReposLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlGitServer, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlGitDatabase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlGitReposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGitReposLayout.createSequentialGroup()
                        .addComponent(pnlProxy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlGitReposLayout.createSequentialGroup()
                        .addComponent(btnGitDownload)
                        .addGap(37, 37, 37))))
        );
        pnlGitReposLayout.setVerticalGroup(
            pnlGitReposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGitReposLayout.createSequentialGroup()
                .addComponent(pnlProxy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGitDownload)
                .addGap(84, 84, 84))
            .addGroup(pnlGitReposLayout.createSequentialGroup()
                .addGroup(pnlGitReposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlGitServer, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                    .addComponent(pnlGitDatabase, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pnlDownloadConsole.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Console", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        txpGitConsole.setEditable(false);
        txpGitConsole.setText("Welcome to MaNGOS Universal Installer. Initializing...");
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
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlDownloadLayout = new javax.swing.GroupLayout(pnlDownload);
        pnlDownload.setLayout(pnlDownloadLayout);
        pnlDownloadLayout.setHorizontalGroup(
            pnlDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDownloadLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlDownloadConsole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlGitRepos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlDownloadLayout.setVerticalGroup(
            pnlDownloadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDownloadLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlGitRepos, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDownloadConsole, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabOperations.addTab("Download", pnlDownload);

        javax.swing.GroupLayout pnlCompileLayout = new javax.swing.GroupLayout(pnlCompile);
        pnlCompile.setLayout(pnlCompileLayout);
        pnlCompileLayout.setHorizontalGroup(
            pnlCompileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 796, Short.MAX_VALUE)
        );
        pnlCompileLayout.setVerticalGroup(
            pnlCompileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 652, Short.MAX_VALUE)
        );

        tabOperations.addTab("Compile", pnlCompile);

        javax.swing.GroupLayout pnlDatabaseLayout = new javax.swing.GroupLayout(pnlDatabase);
        pnlDatabase.setLayout(pnlDatabaseLayout);
        pnlDatabaseLayout.setHorizontalGroup(
            pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 796, Short.MAX_VALUE)
        );
        pnlDatabaseLayout.setVerticalGroup(
            pnlDatabaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 652, Short.MAX_VALUE)
        );

        tabOperations.addTab("Database", pnlDatabase);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabOperations)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabOperations)
                .addContainerGap())
        );

        tabOperations.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void chkProxyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkProxyItemStateChanged
        // TODO add your handling code here:
        if (chkProxy.isSelected()) {
            // Ensable components
            txtProxyServer.setEnabled(true);
            txtProxyPort.setEnabled(true);
        } else {
            // Disable components
            txtProxyServer.setEnabled(false);
            txtProxyPort.setEnabled(false);
        }
    }//GEN-LAST:event_chkProxyItemStateChanged

    private void btnGitDownloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGitDownloadMouseClicked
        //btnGitDownload.setEnabled(false);
    }//GEN-LAST:event_btnGitDownloadMouseClicked

    private void btnGitDownloadKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnGitDownloadKeyPressed
        //btnGitDownload.setEnabled(false);
        //cmdManager
    }//GEN-LAST:event_btnGitDownloadKeyPressed

    private void btnGitDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGitDownloadActionPerformed
        btnGitDownload.setEnabled(false);
        cmdManager.setWinGitPath(confLoader.getWinPathGit());
        console.updateGUIConsole(txpGitConsole, " ");
        if (chkProxy.isSelected()) {
            console.updateGUIConsole(txpGitConsole, "Starting Git downlodad for Server repository...", console.TEXT_GREEN);
            cmdManager.gitDownload(txtGitServer.getText(), txtFolderServer.getText(), txtBranchServer.getText(), txtProxyServer.getText(), txtProxyPort.getText(), txpGitConsole);
            console.updateGUIConsole(txpGitConsole, "Starting Git downlodad for Database repository...", console.TEXT_GREEN);
            cmdManager.gitDownload(txtGitDatabase.getText(), txtFolderDatabase.getText(), txtBranchDatabase.getText(), txtProxyServer.getText(), txtProxyPort.getText(), txpGitConsole);
        } else {
            console.updateGUIConsole(txpGitConsole, "Starting Git downlodad for Server repository...", console.TEXT_GREEN);
            cmdManager.gitDownload(txtGitServer.getText(), txtFolderServer.getText(), txtBranchServer.getText(), txpGitConsole);
            console.updateGUIConsole(txpGitConsole, "Starting Git downlodad for Database repository...", console.TEXT_GREEN);
            cmdManager.gitDownload(txtGitDatabase.getText(), txtFolderDatabase.getText(), txtBranchDatabase.getText(), txpGitConsole);
        }
    }//GEN-LAST:event_btnGitDownloadActionPerformed

    private void applyConfLoaded() {
        txtGitServer.setText(confLoader.getGitURLServer());
        txtFolderServer.setText(confLoader.getGitFolderServer());
        txtBranchServer.setText(confLoader.getGitBranchServer());

        txtGitDatabase.setText(confLoader.getGitURLDatabase());
        txtFolderDatabase.setText(confLoader.getGitFolderDatabase());
        txtBranchDatabase.setText(confLoader.getGitBranchDatabase());
    }

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
    private javax.swing.JButton btnGitDownload;
    private javax.swing.JCheckBox chkProxy;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel pnlCompile;
    private javax.swing.JPanel pnlDatabase;
    private javax.swing.JPanel pnlDownload;
    private javax.swing.JPanel pnlDownloadConsole;
    private javax.swing.JPanel pnlGitDatabase;
    private javax.swing.JPanel pnlGitRepos;
    private javax.swing.JPanel pnlGitServer;
    private javax.swing.JPanel pnlProxy;
    private javax.swing.JTabbedPane tabOperations;
    private javax.swing.JTextPane txpGitConsole;
    private javax.swing.JTextField txtBranchDatabase;
    private javax.swing.JTextField txtBranchServer;
    private javax.swing.JTextField txtFolderDatabase;
    private javax.swing.JTextField txtFolderServer;
    private javax.swing.JTextField txtGitDatabase;
    private javax.swing.JTextField txtGitServer;
    private javax.swing.JTextField txtProxyPort;
    private javax.swing.JTextField txtProxyServer;
    // End of variables declaration//GEN-END:variables
}
