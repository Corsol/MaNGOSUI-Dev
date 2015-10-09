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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JButton;
import javax.swing.JProgressBar;

/**
 *
 * @author Boni Simone <simo.boni@gmail.com>
 */
public class CommandManager {

    private static final String OS_NAME = "os.name";
    private static final String OS_VERSION = "os.version";
    private static final String OS_ARCH = "os.arch";
    private static final Logger LOG = Logger.getLogger(CommandManager.class.getName());

    /**
     * Value for Windows operating system
     */
    public final int WINDOWS = 1;

    /**
     * Value for Linux operating system
     */
    public final int UNIX = 2;

    /**
     * Value for MAC operating system
     */
    public final int MACOS = 3;

    private CommandsWindows winCmd;
    private CommandsLinux unixCmd;
    private CommandsMacOS macCmd;

    private String osName = "";
    private String osVersion = "";
    private String osArch = "";
    private int CURR_OS = 0; // 0: N/A, 1: Windows, 2: Unix, 3: MAC?
    private boolean PSScriptEnabled = false;
    private String MySQLPath = "";
    private JButton btnInvoker;
    //private JProgressBar prbCurrWork;

    /**
     * Basic class constructor that initilize all variable and identify the current OS for next operations
     */
    public CommandManager() {
        this.osName = System.getProperty(OS_NAME);
        this.osVersion = System.getProperty(OS_VERSION);
        this.osArch = System.getProperty(OS_ARCH);
        if (this.osName.toLowerCase().contains("windows")) {
            this.CURR_OS = this.WINDOWS;
            this.winCmd = new CommandsWindows();
            this.PSScriptEnabled = this.winCmd.checkPSScript();
        } else if (this.osName.toLowerCase().contains("linux")) {
            this.CURR_OS = this.UNIX;
            this.unixCmd = new CommandsLinux();
        } else if (this.osName.toLowerCase().contains("mac")) {
            this.CURR_OS = this.MACOS;
            //macCmd = new CommandsMacOS();
        } else {
            LOG.log(Level.SEVERE, "Unknow operating system... need to be encoded!");
        }
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean checkRootConsole(Object console) {
        StringBuilder sb = new StringBuilder();
        String command;
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = true;
                    break;
                case UNIX:
                    command = "id -u";
                    // Set toBuffer param to true to avoid console text
                    this.unixCmd.executeShell(command, console, sb, true, null);
                    ret_val = sb.toString() != null && "0".equals(sb.toString().trim());
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;

    }

    /**
     * Check if current Linux OS has sudo installed<br />
     * On Windowns system return always true.
     *
     * @param console
     * @return
     */
    public boolean checkSudoConf(Object console) {
        StringBuilder sb = new StringBuilder();
        String command;
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = true;
                    break;
                case UNIX:
                    command = "find /etc/sudoers";
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.unixCmd.executeShell(command, console, sb, true, null);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     * Enable passed user into sudoers configuration on Linux systems<br />
     * On Windowns system return always true.
     *
     * @param user The String value of user to enable
     * @param console The JTextPane object used for output
     * @return True if user configuration is done, false otherwise
     */
    public boolean addSudoUser(String user, Object console) {
        StringBuilder sb = new StringBuilder();
        String command;
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = true;
                    break;
                case UNIX:
                    command = "echo \"" + user + " ALL=(ALL) NOPASSWD: ALL\" >> /etc/sudoers";
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.unixCmd.executeShell(command, console, sb, true, null);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean checkGit(Object console) {
        String command = "git --version";
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.winCmd.gitOperation(command, console, true);
                    break;
                case UNIX:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.unixCmd.gitOperation(command, console, true);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param winPath
     * @param console
     * @return
     */
    public boolean checkGit(String winPath, Object console) {
        String command = "git --version";
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    this.winCmd.setGitPath(winPath);
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.winCmd.gitOperation(command, console, true);
                    break;
                case UNIX:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.unixCmd.gitOperation(command, console, true);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @param prbCurrWork
     * @return
     */
    public boolean setupGit(Object console, final JProgressBar prbCurrWork) {
        StringBuilder sb = new StringBuilder();
        String command;
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = true;
                    break;
                case UNIX:
                    command = this.unixCmd.getCatalogCommand() + " install git";
                    ret_val = this.unixCmd.executeShell(command, console, sb, true, prbCurrWork);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean checkCMAKE(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "cmake --help";
        // Set toBuffer param to true to avoid console text
        return this.runOSCommand(command, console, sb, true, null);
    }

    /**
     *
     * @param pathToCMake
     * @param console
     * @return
     */
    public boolean checkCMAKE(String pathToCMake, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "\"" + pathToCMake + File.separator + "cmake.exe\" --help";
        // Set toBuffer param to true to avoid console text
        boolean ret = this.runOSCommand(command, console, sb, true, null);
        if (ret) {
            this.setWinCmakePath(pathToCMake + File.separator);
        }
        return ret;
    }

    /**
     *
     * @param console
     * @param prbCurrWork
     * @return
     */
    public boolean setupCMake(Object console, final JProgressBar prbCurrWork) {
        StringBuilder sb = new StringBuilder();
        String command;
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = true;
                    break;
                case UNIX:
                    command = this.unixCmd.getCatalogCommand() + " install cmake cmake-qt-gui g++ gcc make autoconf libace-ssl-dev libace-dev libbz2-dev zlib1g-dev libtool";
                    ret_val = this.unixCmd.executeShell(command, console, sb, true, prbCurrWork);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @param prbCurrWork
     * @return
     */
    public boolean setupOpenSSL(Object console, final JProgressBar prbCurrWork) {
        StringBuilder sb = new StringBuilder();
        String command;
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = true;
                    break;
                case UNIX:
                    command = this.unixCmd.getCatalogCommand() + " install libssl-dev";
                    ret_val = this.unixCmd.executeShell(command, console, sb, true, prbCurrWork);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean checkMySQL(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "mysql --help";
        // Set toBuffer param to true to avoid console text
        return this.runOSCommand(command, console, sb, true, null);
    }

    /**
     *
     * @param pathToMySQL
     * @param console
     * @return
     */
    public boolean checkMySQL(String pathToMySQL, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "." + File.separator + pathToMySQL + File.separator + "mysql.exe --help";
        // Set toBuffer param to true to avoid console text
        boolean ret = this.runOSCommand(command, console, sb, true, null);
        if (ret) {
            this.MySQLPath = pathToMySQL + File.separator;
        }
        return ret;
    }

    /**
     *
     * @param pathToMySQL
     * @param console
     * @return
     */
    public String checkMySQLLib(String pathToMySQL, Object console) {
        String ret_val = "";
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.checkMySQLLib(pathToMySQL, console);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.checkMySQLLib(pathToMySQL, console);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = "";
                    break;
            }
        } catch (Exception ex) {
            return "";
        }
        return ret_val;
    }

    /**
     *
     * @param pathToMySQL
     * @param console
     * @return
     */
    public String checkMySQLInclude(String pathToMySQL, Object console) {
        String ret_val = "";
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.checkMySQLInclude(pathToMySQL, console);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.checkMySQLInclude(pathToMySQL, console);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = "";
                    break;
            }
        } catch (Exception ex) {
            return "";
        }
        return ret_val;
    }

    /**
     *
     * @param pathToOpenSSL
     * @param console
     * @return
     */
    public String checkOpenSSLLib(String pathToOpenSSL, Object console) {
        String ret_val = "";
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.checkOpenSSLLib(pathToOpenSSL, console);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.checkOpenSSLLib(pathToOpenSSL, console);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = "";
                    break;
            }
        } catch (Exception ex) {
            return "";
        }
        return ret_val;
    }

    /**
     *
     * @param pathToOpenSSL
     * @param console
     * @return
     */
    public String checkOpenSSLInclude(String pathToOpenSSL, Object console) {
        String ret_val = "";
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.checkOpenSSLInclude(pathToOpenSSL, console);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.checkOpenSSLInclude(pathToOpenSSL, console);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = "";
                    break;
            }
        } catch (Exception ex) {
            return "";
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean installMySQLPortable(Object console) {
        String basePath;
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    basePath = System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.6";
                    // Set toBuffer param to true to avoid console text
                    //ret_val = winCmd.gitOperation(command, console, true);
                    break;
                case UNIX:
                    basePath = "";
                    //ret_val = unixCmd.executeShell(command, console, sb, true);
                    break;
                case MACOS:
                    basePath = "";
                    break;
                default:
                    basePath = "";
                    ret_val = false;
                    break;
            }
            File destDir = new File(basePath);
            if (!destDir.exists()) {
                boolean ret = destDir.mkdirs();
                if (!ret) {
                    System.console().printf("Unable to create portable directory: " + basePath);
                    return false;
                }
            }
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream("mysql_portable.zip"));
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = basePath + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    this.extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            ret_val = true;
        } catch (IOException ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.console().printf("%s", ex.getLocalizedMessage());
            ex.printStackTrace();
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean installOpenSSLPortable(Object console) {
        String basePath;
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    basePath = "C:" + File.separator + "OpenSSL-Win32";
                    // Set toBuffer param to true to avoid console text
                    //ret_val = winCmd.gitOperation(command, console, true);
                    break;
                case UNIX:
                    basePath = "";
                    //ret_val = unixCmd.executeShell(command, console, sb, true);
                    break;
                case MACOS:
                    basePath = "";
                    break;
                default:
                    basePath = "";
                    ret_val = false;
                    break;
            }
            File destDir = new File(basePath);
            if (!destDir.exists()) {
                boolean ret = destDir.mkdirs();
                if (!ret) {
                    System.console().printf("Unable to create portable directory: " + basePath);
                    return false;
                }
            }
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream("openssl_portable.zip"));
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = basePath + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    this.extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            ret_val = true;
        } catch (IOException ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.console().printf("%s", ex.getLocalizedMessage());
            ex.printStackTrace();
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @param prbCurrWork
     * @return
     */
    public boolean setupMySQL(Object console, final JProgressBar prbCurrWork) {
        StringBuilder sb = new StringBuilder();
        String command;
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = true;
                    break;
                case UNIX:
                    command = this.unixCmd.getCatalogCommand() + " install mysql-server mysql-common mysql-client libmysql++-dev libmysqlclient-dev";
                    ret_val = this.unixCmd.executeShell(command, console, sb, true, prbCurrWork);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param dbServer
     * @param dbPort
     * @param username
     * @param password
     * @param console
     * @return
     */
    public boolean checkDBUser(String dbServer, String dbPort, String username, String password, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = this.MySQLPath + "mysql.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + username + " --password=" + password;
        String sqlCommand = " -e \"exit\"";
        // Set toBuffer param to true to avoid console text
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.runOSCommand(command + sqlCommand, null, sb, true, null);
                    break;
                case UNIX:
                    //ArrayList<String> commands = new ArrayList(Arrays.asList(command.replace(".exe", "").split(" ")));
                    //commands.add(sqlCommand);
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.runOSCommand(command.replace(".exe", "") + sqlCommand, null, sb, true, null);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (Exception ex) {
            return false;
        }
        if (!ret_val) {
            String msg = "\nERROR: " + sb.toString();
            if (console != null) {
                ConsoleManager.getInstance().updateGUIConsole(console, msg, ConsoleManager.TEXT_RED);
            } else {
                System.console().printf("%s", msg);
            }
        }
        return ret_val;
    }

    /**
     *
     * @param dbServer
     * @param dbPort
     * @param dbAdmin
     * @param dbAdminPwd
     * @param worldDBName
     * @param charDBName
     * @param realmDBName
     * @param console
     * @return
     */
    public boolean checkDBStructure(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String worldDBName, String charDBName, String realmDBName, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = this.MySQLPath + "mysql.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
        String sqlCommand = " -e \"CREATE TABLE " + worldDBName + ".dbcheck(test bit(1)) engine = MEMORY;"
                + "DROP TABLE " + worldDBName + ".dbcheck;"
                + "CREATE TABLE " + charDBName + ".dbcheck(test bit(1)) engine = MEMORY;"
                + "DROP TABLE " + charDBName + ".dbcheck;"
                + "CREATE TABLE " + realmDBName + ".dbcheck(test bit(1)) engine = MEMORY;"
                + "DROP TABLE " + realmDBName + ".dbcheck;\"";
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.runOSCommand(command + sqlCommand, null, sb, true, null);
                    break;
                case UNIX:
                    //ArrayList<String> commands = new ArrayList(Arrays.asList(command.replace(".exe", "").split(" ")));
                    //commands.add(sqlCommand);
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.runOSCommand(command.replace(".exe", "") + sqlCommand, null, sb, true, null);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (Exception ex) {
            return false;
        }
        if (!ret_val) {
            String msg = "\nERROR: " + sb.toString();
            if (console != null) {
                ConsoleManager.getInstance().updateGUIConsole(console, msg, ConsoleManager.TEXT_RED);
            } else {
                System.console().printf("%s", msg);
            }
        }
        return ret_val;
    }

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }

    }

    /**
     *
     * @param dbServer
     * @param dbPort
     * @param dbAdminPwd
     * @param dbAdmin
     * @param console
     * @param dbUserPwd
     * @param dbUser
     * @param charDBName
     * @param worldDBName
     * @param realmDBName
     * @return
     */
    //public boolean createDB(ConfLoader config, Object console) {
    public boolean createDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String dbUser, String dbUserPwd, String worldDBName, String charDBName, String realmDBName, Object console) {
        StringBuilder sb = new StringBuilder();
        //String command = MySQLPath + "mysql.exe -q -s -h "+server+" --port="+port+" --user="+usrAdmin+" --password="+usrAdminPwd+" < World"+File.separator+"Setup"+File.separator+"mangosdCreateDB.sql";
        String command = this.MySQLPath + "mysql.exe -q -s -f --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
        String sqlCommand = " -e \""
                + "CREATE DATABASE " + worldDBName + " DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE DATABASE " + charDBName + " DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE DATABASE " + realmDBName + " DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE USER '" + dbUser + "'@'%' IDENTIFIED BY '" + dbUserPwd + "';"
                + "CREATE USER '" + dbUser + "'@'localhost' IDENTIFIED BY '" + dbUserPwd + "';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON " + worldDBName + ".* TO '" + dbUser + "'@'%', '" + dbUserPwd + "'@'localhost';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON " + charDBName + ".* TO '" + dbUser + "'@'%', '" + dbUserPwd + "'@'localhost';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON " + realmDBName + ".* TO '" + dbUser + "'@'%', '" + dbUserPwd + "'@'localhost';\"";
        //String[] command = new String[]{"cmd.exe"};
        //return runOSCommand(command, console, sb, false);
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.runOSCommand(command + sqlCommand, console, sb, false, null);
                    break;
                case UNIX:
                    //ArrayList<String> commands = new ArrayList(Arrays.asList(command.replace(".exe", "").split(" ")));
                    //commands.add(sqlCommand);
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.runOSCommand(command.replace(".exe", "") + sqlCommand, console, sb, false, null);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (Exception ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param dbServer
     * @param dbPort
     * @param dbAdmin
     * @param Database
     * @param setupFolder
     * @param dbAdminPwd
     * @param console
     * @param prbCurrWork
     * @return
     */
    //public boolean loadDB(ConfLoader config, String Database, String setupFolder, Object console) {
    public boolean loadDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String Database, String setupFolder, Object console, final JProgressBar prbCurrWork) {
        StringBuilder sb = new StringBuilder();
        boolean toBuffer = false;
        //String command = "";
        String command = this.MySQLPath + "mysql.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
        String sqlCommand = " " + Database + " < " + setupFolder;
        //return runOSCommand(command, console, sb, false);
        if (prbCurrWork != null) {
            prbCurrWork.setMaximum(1);
            toBuffer = true;
        }
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.runOSCommand(command + sqlCommand, console, sb, toBuffer, prbCurrWork);
                    break;
                case UNIX:
                    //ArrayList<String> commands = new ArrayList(Arrays.asList(command.replace(".exe", "").split(" ")));
                    //commands.add(sqlCommand);
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.runOSCommand(command.replace(".exe", "") + sqlCommand, console, sb, toBuffer, prbCurrWork);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (Exception ex) {
            ret_val = false;
        }
        if (prbCurrWork != null) {
            prbCurrWork.setValue(prbCurrWork.getMaximum());
            //ConsoleManager.getInstance().updateGUIConsole(guiConsole, msg, ConsoleManager.TEXT_ORANGE);
        }
        return ret_val;
    }

    /**
     *
     * @param dbServer
     * @param Database
     * @param dbPort
     * @param updateFolder
     * @param dbAdmin
     * @param console
     * @param dbAdminPwd
     * @param prbCurrWork
     * @return
     */
    //public boolean loadDBUpdate(ConfLoader config, String Database, String updateFolder, Object console) {
    public boolean loadDBUpdate(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String Database, String updateFolder, Object console, final JProgressBar prbCurrWork) {
        StringBuilder sb = new StringBuilder();
        String command;
        boolean ret_val = true;
        boolean toBuffer = false;
        File file = new File(updateFolder);
        if (file.isDirectory() && file.list().length > 0) {
            if (prbCurrWork != null) {
                prbCurrWork.setMaximum(file.list().length);
                toBuffer = true;
            }
            File[] listFiles = file.listFiles(new FileFilterSQL());
            Arrays.sort(listFiles);
            for (File subFile : listFiles) {
                if (!subFile.isDirectory()) {
                    command = this.MySQLPath + "mysql.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
                    String sqlCommand = " " + Database + " < " + updateFolder + File.separator + subFile.getName();
                    //ret_val &= runOSCommand(command, console, sb, false);
                    try {
                        switch (this.CURR_OS) {
                            case WINDOWS:
                                // Set toBuffer param to true to avoid console text
                                ret_val &= this.runOSCommand(command + sqlCommand, console, sb, toBuffer, prbCurrWork);
                                break;
                            case UNIX:
                                //ArrayList<String> commands = new ArrayList(Arrays.asList(command.replace(".exe", "").split(" ")));
                                //commands.add(sqlCommand);
                                // Set toBuffer param to true to avoid console text
                                ret_val &= this.runOSCommand(command.replace(".exe", "") + sqlCommand, console, sb, toBuffer, prbCurrWork);
                                break;
                            case MACOS:
                                break;
                            default:
                                ret_val &= false;
                                break;
                        }
                    } catch (Exception ex) {
                        ret_val &= false;
                    }
                    /*if (toBuffer && !ret_val) {
                     System.console().printf("%s", sb.toString());
                     ConsoleManager.getInstance().updateGUIConsole(console, sb.toString(), ConsoleManager.TEXT_RED);
                     }*/
                    if (prbCurrWork != null) {
                        prbCurrWork.setValue(prbCurrWork.getValue() + 1);
                        //ConsoleManager.getInstance().updateGUIConsole(guiConsole, msg, ConsoleManager.TEXT_ORANGE);
                    }
                }
            }
        } else {
            if (prbCurrWork != null) {
                prbCurrWork.setValue(prbCurrWork.getValue() + 1);
                //ConsoleManager.getInstance().updateGUIConsole(guiConsole, msg, ConsoleManager.TEXT_ORANGE);
            }
        }
        return ret_val;
    }

    /**
     *
     * @param serverFolder
     * @param buildFolder
     * @param options
     * @param console
     * @return
     */
    public boolean cmakeConfig(String serverFolder, String buildFolder, HashMap<String, String> options, Object console) {
        boolean ret_val = false;
        try {
            if (this.checkFolder(buildFolder)) {
                this.deleteFolder(buildFolder);
            }
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.cmakeConfig(serverFolder, buildFolder, options, console);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.cmakeConfig(serverFolder, buildFolder, options, console);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (IOException | ExecutionException | InterruptedException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param buildFolder
     * @param runFolder
     * @param buildType
     * @param console
     * @return
     */
    public boolean cmakeInstall(String buildFolder, String runFolder, String buildType, Object console) {
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.cmakeInstall(buildFolder, runFolder, buildType, console);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.cmakeInstall(buildFolder, runFolder, console);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (IOException | ExecutionException | InterruptedException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param path
     * @return
     */
    public boolean checkFolder(String path) {
        try {
            File file = new File(path);
            return file.exists() && file.isDirectory();
        } catch (Exception ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.console().printf("%s", ex.getLocalizedMessage());
            return false;
        }
    }

    /**
     *
     * @param path
     * @return
     */
    public boolean deleteFolder(String path) {
        try {
            File file = new File(path);
            if (file.isDirectory() && file.list().length > 0) {
                for (String subFile : file.list()) {
                    this.deleteFolder(path + File.separator + subFile);
                }
            }
            return file.delete();
        } catch (Exception ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.console().printf("%s", ex.getLocalizedMessage());
            return false;
        }
    }

    /**
     *
     * @param pathToRepo
     * @return
     */
    public boolean isRepoUpToDate(String pathToRepo) {
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.isRepoUpToDate(pathToRepo);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.isRepoUpToDate(pathToRepo);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param proxyServer
     * @param proxyPort
     * @param console
     * @return
     */
    public boolean setGitProxy(String proxyServer, String proxyPort, Object console) {
        String command = "git config --global --add http.proxy http://" + proxyServer + ":" + proxyPort;
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.winCmd.gitOperation(command, console, true);
                    break;
                case UNIX:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.unixCmd.gitOperation(command, console, true);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param console
     * @return
     */
    public boolean remGitProxy(Object console) {
        String command = "git config --global --unset http.proxy";
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.winCmd.gitOperation(command, console, true);
                    break;
                case UNIX:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.unixCmd.gitOperation(command, console, true);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param url
     * @param folder
     * @param branch
     * @param console
     * @return
     */
    public boolean gitDownload(String url, String folder, String branch, Object console) {
        return this.gitDownload(url, folder, branch, "", "", console);
    }

    /**
     *
     * @param url
     * @param folder
     * @param branch
     * @param proxyServer
     * @param proxyPort
     * @param console
     * @return
     */
    public boolean gitDownload(String url, String folder, String branch, String proxyServer, String proxyPort, Object console) {
        String command = "git clone --recursive";
        if (!branch.isEmpty()) {
            command += " -b " + branch;
        }
        if (!proxyServer.isEmpty()) {
            command += " -c http.proxy=http://" + proxyServer + ":" + proxyPort;
        }
        command += " " + url;
        if (!folder.isEmpty()) {
            command += " " + folder;
        }
        //return runOSCommand(command, console, sb, false);
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.gitOperation(command, console, false);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.gitOperation(command, console, false);
                    //ret_val = runOSCommand(command, console, sb, false);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (IOException | InterruptedException | ExecutionException ex) {
            return false;
        }
        return ret_val;

    }

    /**
     *
     * @param console
     * @return
     */
    public boolean gitCheckout(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "git pull";
        return this.runOSCommand(command, console, sb, false, null);
    }

    /**
     *
     * @param toolFolder
     * @param clientFolder
     * @param serverFolder
     * @param step
     * @param console
     * @param prbCurrWork
     * @return
     */
    public boolean mapExtraction(String toolFolder, String clientFolder, String serverFolder, int step, Object console, final JProgressBar prbCurrWork) {
        StringBuilder sb = new StringBuilder();
        boolean ret_val = false;
        if (this.checkFolder(toolFolder)) {
            if (!this.checkFolder(serverFolder)) {
                File instFld = new File(serverFolder);
                instFld.mkdirs();
            }
            String command = "";
            switch (step) {
                case 1:
                    if (this.checkFolder(clientFolder)) {
                        command = "\"\"" + toolFolder + File.separator + "map-extractor.exe\" -i \"" + clientFolder + "\" -o \"" + serverFolder + "\"\"";
                        if (this.CURR_OS == 2) {
                            command = command.substring(1, command.length() - 1);
                        }
                    } else {
                        String msg = "\nERROR: WoW client folder does not exist.";
                        if (console != null) {
                            ConsoleManager.getInstance().updateGUIConsole(console, msg, ConsoleManager.TEXT_RED);
                        } else {
                            System.console().printf("%s", msg);
                        }
                        this.btnInvoker.setText("ERROR");
                    }
                    break;
                case 2:
                    if (this.checkFolder(clientFolder)) {
                        if (this.checkFolder(serverFolder + File.separator + "Buildings")) {
                            String msg = "\nWARNING: Cleaning Buildings folder of a previus execution.";
                            if (console != null) {
                                ConsoleManager.getInstance().updateGUIConsole(console, msg, ConsoleManager.TEXT_ORANGE);
                            } else {
                                System.console().printf("%s", msg);
                            }
                            this.deleteFolder(serverFolder + File.separator + "Buildings");
                        }
                        String vmap = toolFolder + File.separator + "vmap-extractor.exe";
                        String dst = serverFolder + File.separator + "vmap-extractor.exe";
                        if (this.CURR_OS == 2) {
                            vmap = vmap.replace(".exe", "");
                            dst = dst.replace(".exe", "");
                        }
                        this.copyFolder(vmap, dst, console);
                        String fixPermission = "";
                        if (this.CURR_OS == 2) {
                            fixPermission = "; chmod 755 vmap-extractor ";
                        }
                        command = "cd \"" + serverFolder + "\" " + fixPermission + " & ." + File.separator + "vmap-extractor.exe -d \"" + clientFolder + "\"";
                    } else {
                        String msg = "\nERROR: WoW client folder does not exist.";
                        if (console != null) {
                            ConsoleManager.getInstance().updateGUIConsole(console, msg, ConsoleManager.TEXT_RED);
                        } else {
                            System.console().printf("%s", msg);
                        }
                        this.btnInvoker.setText("ERROR");
                    }
                    break;
                case 3:
                    if (this.checkFolder(serverFolder + File.separator + "Buildings")) {
                        if (this.CURR_OS == this.WINDOWS) {
                            String acedll = toolFolder.substring(0, toolFolder.lastIndexOf(File.separator)) + File.separator + "ace.dll";
                            this.copyFolder(acedll, serverFolder + File.separator + "ace.dll", console);
                        }
                        String vmap = toolFolder + File.separator + "vmap-assembler.exe";
                        String dst = serverFolder + File.separator + "vmap-assembler.exe";
                        if (this.CURR_OS == this.UNIX) {
                            vmap = vmap.replace(".exe", "");
                            dst = dst.replace(".exe", "");
                        }
                        this.copyFolder(vmap, dst, console);
                        String fixPermission = "";
                        if (this.CURR_OS == this.UNIX) {
                            fixPermission = "; chmod 755 vmap-assembler ";
                        }
                        command = "cd \"" + serverFolder + "\" " + fixPermission + " & ." + File.separator + "vmap-assembler.exe Buildings vmaps";
                    } else {
                        String msg = "\nERROR: Buildings folder does not exist.";
                        if (console != null) {
                            ConsoleManager.getInstance().updateGUIConsole(console, msg, ConsoleManager.TEXT_RED);
                        } else {
                            System.console().printf("%s", msg);
                        }
                        this.btnInvoker.setText("ERROR");
                    }
                    break;
                case 4:
                    if (this.checkFolder(serverFolder + File.separator + "vmaps")) {
                        String mmap = toolFolder + File.separator + "movemap-generator.exe";
                        String dst = serverFolder + File.separator + "movemap-generator.exe";
                        if (this.CURR_OS == this.UNIX) {
                            mmap = mmap.replace(".exe", "");
                            dst = dst.replace(".exe", "");
                        }
                        this.copyFolder(mmap, dst, console);
                        String fixPermission = "";
                        if (this.CURR_OS == this.UNIX) {
                            fixPermission = "; chmod 755 movemap-generator ";
                        }
                        command = "cd \"" + serverFolder + "\" " + fixPermission + " & ." + File.separator + "movemap-generator.exe ";
                    } else {
                        String msg = "\nERROR: vmaps folder does not exist.";
                        if (console != null) {
                            ConsoleManager.getInstance().updateGUIConsole(console, msg, ConsoleManager.TEXT_RED);
                        } else {
                            System.console().printf("%s", msg);
                        }
                        this.btnInvoker.setText("ERROR");
                    }
                    break;
                default:
                    break;
            }
            //String 
            //return runOSCommand(String command, Object console, final StringBuilder sb, boolean toBuffer, final JProgressBar prbCurrWork)
            try {
                switch (this.CURR_OS) {
                    case WINDOWS:
                        ret_val = this.winCmd.executeCmd(command, console, sb, false, prbCurrWork);
                        break;
                    case UNIX:
                        ret_val = this.unixCmd.executeShell(command.replace(".exe", "").replace("&", ";"), console, sb, false, prbCurrWork);
                        //ret_val = runOSCommand(command, console, sb, false);
                        break;
                    case MACOS:
                        break;
                    default:
                        ret_val = false;
                        break;
                }
            } catch (IOException | InterruptedException | ExecutionException ex) {
                return false;
            }
        } else {
            String msg = "\nERROR: Tools forlder did not exist.";
            if (console != null) {
                ConsoleManager.getInstance().updateGUIConsole(console, msg, ConsoleManager.TEXT_RED);
            } else {
                System.console().printf("%s", msg);
            }
            this.btnInvoker.setText("ERROR");
            ret_val = false;
        }

        return ret_val;
    }

    /**
     *
     * @param gitPath
     */
    public void setWinGitPath(String gitPath) {
        switch (this.CURR_OS) {
            case WINDOWS:
                this.winCmd.setGitPath(gitPath);
                break;
            case UNIX:
                break;
            case MACOS:
                break;
            default:
                break;
        }
    }

    /**
     *
     * @param cmakePath
     */
    public void setWinCmakePath(String cmakePath) {
        switch (this.CURR_OS) {
            case WINDOWS:
                this.winCmd.setCmakePath(cmakePath);
                break;
            case UNIX:
                break;
            case MACOS:
                break;
            default:
                break;
        }
    }

    private boolean runOSCommand(String command, Object console, final StringBuilder sb, boolean toBuffer, final JProgressBar prbCurrWork) {
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.executeCmd(command, console, sb, toBuffer, prbCurrWork);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.executeShell(command, console, sb, toBuffer, prbCurrWork);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;

            }
        } catch (IOException | ExecutionException | InterruptedException ex) {
            Logger.getLogger(CommandManager.class
                    .getName()).log(Level.SEVERE, null, ex);
            System.console().printf("%s", ex.getLocalizedMessage());
            ex.printStackTrace();

            return false;
        }
        return ret_val;
    }

    private boolean runOSCommand(ArrayList<String> command, Object console, final StringBuilder sb, boolean toBuffer, final JProgressBar prbCurrWork) {
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.executeCmd(command, console, sb, toBuffer, prbCurrWork);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.executeShell(command, console, sb, toBuffer, prbCurrWork);
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = false;
                    break;

            }
        } catch (IOException | ExecutionException | InterruptedException ex) {
            Logger.getLogger(CommandManager.class
                    .getName()).log(Level.SEVERE, null, ex);
            System.console().printf("%s", ex.getLocalizedMessage());
            ex.printStackTrace();

            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param source
     * @param dest
     * @param console
     * @return
     */
    public boolean copyFolder(String source, String dest, Object console) {
        return Command.getInstance().copyFolder(new File(source), new File(dest), console);
    }

    /**
     * @return the osName
     */
    public String getOsName() {
        return this.osName;
    }

    /**
     * @return the osVersion
     */
    public String getOsVersion() {
        return this.osVersion;
    }

    /**
     * @return the osArch
     */
    public String getOsArch() {
        return this.osArch;
    }

    /**
     * @return the CURR_OS
     */
    public int getCURR_OS() {
        return this.CURR_OS;
    }

    /**
     * @return the PSScriptEnabled
     */
    public boolean isPSScriptEnabled() {
        return this.PSScriptEnabled;
    }

    /**
     *
     * @return
     */
    public int getDebugLevel() {
        int ret_val = 0;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.winCmd.getDebugLevel();
                    break;
                case UNIX:
                    ret_val = this.unixCmd.getDebugLevel();
                    break;
                case MACOS:
                    break;
                default:
                    ret_val = 0;
                    break;
            }
        } catch (Exception ex) {
            return 0;
        }
        return ret_val;
    }

    /**
     *
     * @param debugLevel
     */
    public void setDebugLevel(int debugLevel) {
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    this.winCmd.setDebugLevel(debugLevel);
                    break;
                case UNIX:
                    this.unixCmd.setDebugLevel(debugLevel);
                    break;
                case MACOS:
                    break;
                default:
                    //ret_val = 0;
                    break;
            }
        } catch (Exception ex) {
        }
    }

    /**
     *
     * @return
     */
    public JButton getBtnInvoker() {
        return this.btnInvoker;
    }

    /**
     *
     * @param btnInvoker
     */
    public void setBtnInvoker(JButton btnInvoker) {
        this.btnInvoker = btnInvoker;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    this.winCmd.setBtnInvoker(btnInvoker);
                    break;
                case UNIX:
                    this.unixCmd.setBtnInvoker(btnInvoker);
                    break;
                case MACOS:
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
        }
    }
    /*
     public JProgressBar getPrbCurrWork() {
     return prbCurrWork;
     }

     public void setPrbCurrWork(JProgressBar prbCurrWork) {
     this.prbCurrWork = prbCurrWork;
     try {
     switch (CURR_OS) {
     case WINDOWS: 
     winCmd.setPrbCurrWork(prbCurrWork);
     break;
     case UNIX: 
     unixCmd.setPrbCurrWork(prbCurrWork);
     break;
     case MACOS: 
     break;
     default:
     break;
     }
     } catch (Exception ex) {
     }
     }*/

}
