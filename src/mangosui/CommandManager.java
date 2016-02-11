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
     * Basic class constructor that initilize all variable and identify the
     * current OS for next operations
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
     * Check if current console session is with root privileges. This condition
     * is required for dependecies installations.<br />
     * On Windowns system return always true.
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if console is with root privileges, false otherwise
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
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if sudo is installed, false otherwise
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
     * @param console The JTextPane object to be used for output in swing GUI
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
     * Check if git is currently installed. <br />
     * Windows note: this method check only if git is installed and install path
     * is present in PATH environment. Some installation did not set this
     * environment, so this function may return false also if git is installed.
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if git command is present in console's commands, false
     * otherwise
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
     * Check if git is currently installed in the specified path. <br />
     * NOTE: this function is explicity designed for windows system where git
     * installation did not set the PATH environment and git installation need
     * to be found explicity
     *
     * @param winPath The String path to git.exe file
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if git command is found in specified path, false otherwise
     */
    public boolean checkGit(String winPath, Object console) {
        String command = "git.exe --version";
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set path for next uses and for check
                    this.winCmd.setGitPath(winPath);
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.winCmd.gitOperation(command, console, true);
                    break;
                case UNIX:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.unixCmd.gitOperation(command.replace(".exe", ""), console, true);
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
     * Install git from catalog with OS predefined method.<br />
     * NOTE: this function works only for Linux system that has a catalog where
     * software can be downloaded and installed.<br /><br />
     * TODO: Check installation procedure Linux distro that don't use "apt-get"
     * or "yum" catalog. Implement also Windows installations.
     *
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if installation is done correctly, false for multi-step or
     * otherwise
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
     * Check if cmake is currently installed. <br />
     * Windows note: this method check only if cmake is installed and install
     * path is present in PATH environment. Some installation did not set this
     * environment, so this function may return false also if cmake is
     * installed.
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if cmake command is found in specified path, false otherwise
     */
    public boolean checkCMAKE(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "cmake --help";
        // Set toBuffer param to true to avoid console text
        return this.runOSCommand(command, console, sb, true, null);
    }

    /**
     * Check if cmake is currently installed in the specified path. <br />
     * NOTE: this function is explicity designed for windows system where cmake
     * installation did not set the PATH environment and cmake installation need
     * to be found explicity
     *
     * @param pathToCMake The String path to cmake.exe file
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if cmake command is found, false otherwise
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
     * Install cmake from catalog with OS predefined method.<br />
     * NOTE: this function works only for Linux system that has a catalog where
     * software can be downloaded and installed.<br /><br />
     * TODO: Check installation procedure Linux distro that don't use "apt-get"
     * or "yum" catalog. Implement also Windows installations.
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if cmake is succesfully installed, false otherwise
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
                    // Set toBuffer param to true to avoid console text
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
     * Install openssl from catalog with OS predefined method.<br />
     * NOTE: this function works only for Linux system that has a catalog where
     * software can be downloaded and installed.<br /><br />
     * TODO: Check installation procedure Linux distro that don't use "apt-get"
     * or "yum" catalog. Implement also Windows installations.
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if installation is done correctly, false for multi-step or
     * otherwise
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
                    // Set toBuffer param to true to avoid console text
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
     * Check if MySQL is currently installed. <br />
     * Windows note: this method check only if MySQL is installed and install
     * path is present in PATH environment. Some installation did not set this
     * environment, so this function may return false also if MySQL is
     * installed.
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if MySQL command is found, false otherwise
     */
    public boolean checkMySQL(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "MySQL --help";
        // Set toBuffer param to true to avoid console text
        return this.runOSCommand(command, console, sb, true, null);
    }

    /**
     * Check if MySQL is currently installed in the specified path.<br />
     * NOTE: this function is explicity designed for windows system where MySQL
     * installation did not set the PATH environment and MySQL installation need
     * to be found explicity
     *
     * @param pathToMySQL The String path to MySQL.exe file
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if MySQL command is found in specified path, false otherwise
     */
    public boolean checkMySQL(String pathToMySQL, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "." + File.separator + pathToMySQL + File.separator + "MySQL.exe --help";
        // Set toBuffer param to true to avoid console text
        boolean ret = this.runOSCommand(command, console, sb, true, null);
        if (ret) {
            this.MySQLPath = pathToMySQL + File.separator;
        }
        return ret;
    }

    /**
     * Check if MySQL library is present in the specified path for CMake
     * installation.
     *
     * @param pathToMySQL The String path to MySQL lirary folder
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if MySQL library were found in specified path, false
     * otherwise
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
     * Check if MySQL include folder is present in the specified path for CMake
     * installation.
     *
     * @param pathToMySQL The String path to MySQL include folder
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if MySQL include folder were found in specified path, false
     * otherwise
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
     * Check if openssl library folder is present in the specified path for
     * CMake installation.
     *
     * @param pathToOpenSSL The String path to openssl lirary folder
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if openssl library were found in specified path, false
     * otherwise
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
     * Check if openssl include folder is present in the specified path for
     * CMake installation.
     *
     * @param pathToOpenSSL The String path to openssl include folder
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if openssl include folder were found in specified path,
     * false otherwise
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
     * Install the MySQL library and include folder from a zip file just for
     * CMake compile and build steps.<br />
     * With this setup yuo can avoid full MySQL installation.<br />
     * <b>NOTE</b>:This setup is available only for WINDOWS operating systems
     * and need Administrator privileges.
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if zip extraction and copy suceeded, false otherwise
     */
    public boolean installMySQLPortable(Object console) {
        String basePath;
        boolean ret_val;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    basePath = System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.6";
                    break;
                case UNIX:
                    basePath = "";
                    break;
                case MACOS:
                    basePath = "";
                    break;
                default:
                    basePath = "";
                    break;
            }
            File destDir = new File(basePath);
            if (!destDir.exists()) {
                boolean ret = destDir.mkdirs();
                if (!ret) {
                    System.console().printf("Unable to create portable directory: " + basePath + "\n");
                    return false;
                }
            }
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream("MySQL_portable.zip"));
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
            LOG.log(Level.SEVERE, null, ex);
            System.console().printf("%s\n", ex.getLocalizedMessage());
            return false;
        }
        return ret_val;
    }

    /**
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @return
     *
     * public boolean installOpenSSLPortable(Object console) { String basePath;
     * boolean ret_val = false; try { switch (this.CURR_OS) { case WINDOWS:
     * basePath = "C:" + File.separator + "OpenSSL-Win32"; // Set toBuffer param
     * to true to avoid console text //ret_val = winCmd.gitOperation(command,
     * console, true); break; case UNIX: basePath = ""; //ret_val =
     * unixCmd.executeShell(command, console, sb, true); break; case MACOS:
     * basePath = ""; break; default: basePath = ""; ret_val = false; break; }
     * File destDir = new File(basePath); if (!destDir.exists()) { boolean ret =
     * destDir.mkdirs(); if (!ret) { System.console().printf("Unable to create
     * portable directory: " + basePath); return false; } } ZipInputStream zipIn
     * = new ZipInputStream(new FileInputStream("openssl_portable.zip"));
     * ZipEntry entry = zipIn.getNextEntry(); // iterates over entries in the
     * zip file while (entry != null) { String filePath = basePath +
     * File.separator + entry.getName(); if (!entry.isDirectory()) { // if the
     * entry is a file, extracts it this.extractFile(zipIn, filePath); } else {
     * // if the entry is a directory, make the directory File dir = new
     * File(filePath); dir.mkdirs(); } zipIn.closeEntry(); entry =
     * zipIn.getNextEntry(); } ret_val = true; } catch (IOException ex) {
     * LOG.log(Level.SEVERE, null, ex); System.console().printf("%s\n",
     * ex.getLocalizedMessage()); ex.printStackTrace(); return false; } return
     * ret_val; }
     */
    /**
     * Install MySQL from catalog with OS predefined method.<br />
     * NOTE: this function works only for Linux system that has a catalog where
     * software can be downloaded and installed.<br /><br />
     * TODO: Check installation procedure Linux distro that don't use "apt-get"
     * or "yum" catalog. Implement also Windows installations.
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if installation suceeded, false otherwise
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
                    command = this.unixCmd.getCatalogCommand() + " install MySQL-server MySQL-common MySQL-client libMySQL++-dev libMySQLclient-dev";
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
     * Check if parameters values are correct for MySQL connection.
     *
     * @param dbServer The String IP/Name value for MySQL server
     * @param dbPort The String TCP port value for MySQL server
     * @param username The String username to be used for server connection
     * @param password The String username's password to be used for server
     * connection
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if server connection with specified parameter suceeded,
     * false otherwise
     */
    public boolean checkDBUser(String dbServer, String dbPort, String username, String password, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = this.MySQLPath + "MySQL.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + username + " --password=" + password;
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
                System.console().printf("%s\n", msg);
            }
        }
        return ret_val;
    }

    /**
     * Check if MaNGOS databases are currently present and also mangos users
     * created
     *
     * @param dbServer The String IP/Name value for MySQL server
     * @param dbPort The String TCP port value for MySQL server
     * @param dbAdmin The String username admin to be used for server connection
     * @param dbAdminPwd The String username admin's password to be used for
     * server connection
     * @param worldDBName The String value for Mangos database name
     * @param charDBName The String value for Characters database name
     * @param realmDBName The String value for Realms database name
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if connection with admin user suceeded and all three
     * databases were present, false otherwise
     */
    public boolean checkDBStructure(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String worldDBName, String charDBName, String realmDBName, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = this.MySQLPath + "MySQL.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
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
                System.console().printf("%s\n", msg);
            }
        }
        return ret_val;
    }

    /**
     * Extract file from zip archive and put into specified folder.
     *
     * @param zipIn The ZipInputStream zip file to be extracted
     * @param filePath The String path where exctracted file will be placed
     * @throws IOException if extraction action or copy action will raise an
     * error
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    /**
     * Create the database structure with database specified name and user for
     * mangosd and realmd deamons database connections
     *
     * @param dbServer The String IP/Name value for MySQL server
     * @param dbPort The String TCP port value for MySQL server
     * @param dbAdmin The String username admin to be used for server connection
     * @param dbAdminPwd The String username admin's password to be used for
     * server connection
     * @param dbUserPwd The String username to be created for deamons server
     * connections
     * @param dbUser The String username's password for deamons server
     * connections
     * @param worldDBName The String value for Mangos database name
     * @param charDBName The String value for Characters database name
     * @param realmDBName The String value for Realms database name
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if databases creation and user creation succeede, false
     * otherwise
     */
    public boolean createDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String dbUser, String dbUserPwd, String worldDBName, String charDBName, String realmDBName, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = this.MySQLPath + "MySQL.exe -q -s -f --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
        String sqlCommand = " -e \""
                + "CREATE DATABASE " + worldDBName + " DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE DATABASE " + charDBName + " DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE DATABASE " + realmDBName + " DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE USER '" + dbUser + "'@'%' IDENTIFIED BY '" + dbUserPwd + "';"
                + "CREATE USER '" + dbUser + "'@'localhost' IDENTIFIED BY '" + dbUserPwd + "';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON " + worldDBName + ".* TO '" + dbUser + "'@'%', '" + dbUserPwd + "'@'localhost';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON " + charDBName + ".* TO '" + dbUser + "'@'%', '" + dbUserPwd + "'@'localhost';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON " + realmDBName + ".* TO '" + dbUser + "'@'%', '" + dbUserPwd + "'@'localhost';\"";
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.runOSCommand(command + sqlCommand, console, sb, false, null);
                    break;
                case UNIX:
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
     * Load database structure data from a sql script file into database.
     *
     * @param dbServer The String IP/Name value for MySQL server
     * @param dbPort The String TCP port value for MySQL server
     * @param dbAdmin The String username admin to be used for server connection
     * @param dbAdminPwd The String username admin's password to be used for
     * @param Database The String value for database name into wich load data
     * @param setupFolder The String path value for sql script file to be loaded
     * @param console The JTextPane object to be used for output in swing GUI
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if data load into database suceeded, false otherwise
     */
    public boolean loadDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String Database, String setupFolder, Object console, final JProgressBar prbCurrWork) {
        StringBuilder sb = new StringBuilder();
        boolean toBuffer = false;
        String command = this.MySQLPath + "MySQL.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
        String sqlCommand = " " + Database + " < " + setupFolder;
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
        }
        return ret_val;
    }

    /**
     * Load databases updates data from a sql script file into database.
     *
     * @param dbServer The String IP/Name value for MySQL server
     * @param dbPort The String TCP port value for MySQL server
     * @param dbAdmin The String username admin to be used for server connection
     * @param dbAdminPwd The String username admin's password to be used for
     * @param Database The String value for database name into wich load data
     * @param updateFolder The String path value for folder where are sql update
     * file to be loaded
     * @param console The JTextPane object to be used for output in swing GUI
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if data load into database suceeded, false otherwise
     */
    public boolean loadDBUpdate(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String Database, String updateFolder, Object console, final JProgressBar prbCurrWork) {
        StringBuilder sb = new StringBuilder();
        String command;
        boolean ret_val = true;
        boolean toBuffer = false;
        File file = new File(updateFolder);
        if (file.isDirectory() && file.list().length > 0) {
            // In GUI mode use the progressbar for reference of how many step this procedure has
            if (prbCurrWork != null) {
                prbCurrWork.setMaximum(file.list().length);
                toBuffer = true;
            }
            File[] listFiles = file.listFiles(new FileFilterSQL());
            Arrays.sort(listFiles);
            for (File subFile : listFiles) {
                // Processing each file of current update folder
                if (!subFile.isDirectory()) {
                    command = this.MySQLPath + "MySQL.exe -q -s --host=" + dbServer + " --port=" + dbPort + " --user=" + dbAdmin + " --password=" + dbAdminPwd;
                    String sqlCommand = " " + Database + " < " + updateFolder + File.separator + subFile.getName();
                    try {
                        switch (this.CURR_OS) {
                            case WINDOWS:
                                // Set toBuffer param to true to avoid console text
                                ret_val &= this.runOSCommand(command + sqlCommand, console, sb, toBuffer, prbCurrWork);
                                break;
                            case UNIX:
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
                    if (prbCurrWork != null) {
                        // In GUI mode, after a sql execution, progress bar will be updated
                        prbCurrWork.setValue(prbCurrWork.getValue() + 1);
                    }
                }
            }
        } else {
            if (prbCurrWork != null) {
                // In GUI mode, if current folder has no file progress bar will be updated to set exit status
                prbCurrWork.setValue(prbCurrWork.getValue() + 1);
            }
        }
        return ret_val;
    }

    /**
     * Create the build folder and set CMake environment ready to compile
     *
     * @param serverFolder The String path to installation folder
     * @param buildFolder The String path to cmake build folder
     * @param options The HashMap<String, String> object with cmake option and
     * value to use
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if cmake configuration suceeded, false if Object console is
     * not null, false otherwise
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
     * Compile the configured cmake project from buildFolder.<br />
     * NOTE: In GUI mode for Windows operating systems, the compiled binary
     * need to be copied into installation folder separately
     *
     * @param buildFolder The String path to cmake build folder
     * @param runFolder The String path to binari installation folder
     * @param buildType The String typology of build (Debug/Release) for ONLY
     * WINDOWS operating systems
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if cmake compile suceeded, false if Object console is not
     * null, false otherwise
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
     * Check if specified path exists and is a folder
     *
     * @param path The String path value of folder to be checked
     * @return True if path is exists and is a folder
     */
    public boolean checkFolder(String path) {
        try {
            File file = new File(path);
            return file.exists() && file.isDirectory();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
            System.console().printf("%s\n", ex.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Delete the specified path (folder or file)
     *
     * @param path The String path value to be deleted
     * @return True if file or folder is deleted, false otherwise
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
            LOG.log(Level.SEVERE, null, ex);
            System.console().printf("%s\n", ex.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Copy files and/or folders from source to destination<br/>
     * <b>NOTE</b>: to copy a single file the file name must be specified in
     * both source and destination strings.
     *
     * @param source The String path of source file/folder
     * @param dest The String path of destination file/folder
     * @param console The JTextPane swing component for output
     * @return True if all file/folder were copied, false when ther's an error
     * during copying
     */
    public boolean copyFolder(String source, String dest, Object console) {
        return Command.getInstance().copyFolder(new File(source), new File(dest), console);
    }

    /**
     * Check if git repository in path specified is Up-To-Date or an update is
     * available
     *
     * @param pathToRepo The String path value fore git repository to check
     * @return True if repository is Up-To-Date, false otherwise
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
     * Set global git configuration to enable proxy for connections under NTLM
     * authentication
     *
     * @param proxyServer The String value for proxy server
     * @param proxyPort The String value for proxy port
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if proxy set suceeded, false otherwise
     */
    public boolean setGitProxy(String proxyServer, String proxyPort, Object console) {
        String command = "git.exe config --global --add http.proxy http://" + proxyServer + ":" + proxyPort;
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.winCmd.gitOperation(command, console, true);
                    break;
                case UNIX:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.unixCmd.gitOperation(command.replace(".exe", ""), console, true);
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
     * Remove global git configuration to disable proxy usages
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if proxy remove suceeded, false otherwise
     */
    public boolean remGitProxy(Object console) {
        String command = "git.exe config --global --unset http.proxy";
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.winCmd.gitOperation(command, console, true);
                    break;
                case UNIX:
                    // Set toBuffer param to true to avoid console text
                    ret_val = this.unixCmd.gitOperation(command.replace(".exe", ""), console, true);
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
     * Perform the download from specified git remote repository into local
     * folder
     *
     * @param url The String URL value of remote repository
     * @param folder The String value for local folder repository. Leave empty
     * (NOT NULL) to use let git create default forlder
     * @param branch The String value for remote repository branch to use. Leave
     * empty (NOT NULL) to use master branch
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if git download suceeded, false if Object console is not
     * null, false otherwise
     */
    public boolean gitDownload(String url, String folder, String branch, Object console) {
        return this.gitDownload(url, folder, branch, "", "", console);
    }

    /**
     * Perform the download from specified git remote repository into local
     * folder
     *
     * @param url The String URL value of remote repository
     * @param folder The String value for local folder repository. Leave empty
     * (NOT NULL) to use let git create default forlder
     * @param branch The String value for remote repository branch to use. Leave
     * empty (NOT NULL) to use master branch
     * @param proxyServer The String value for proxy server
     * @param proxyPort The String value for proxy port
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if git download suceeded, false if Object console is not
     * null, false otherwise
     */
    public boolean gitDownload(String url, String folder, String branch, String proxyServer, String proxyPort, Object console) {
        String command = "git.exe clone --recursive";
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
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.gitOperation(command, console, false);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.gitOperation(command.replace(".exe", ""), console, false);
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
     * Perform a git pull to update local copy of remote repository
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @return True if git pull suceeded, false if Object console is not null,
     * false otherwise
     */
    public boolean gitCheckout(Object console) {
        String command = "git.exe pull";
        boolean ret_val = false;
        try {
            switch (this.CURR_OS) {
                case WINDOWS:
                    ret_val = this.winCmd.gitOperation(command, console, false);
                    break;
                case UNIX:
                    ret_val = this.unixCmd.gitOperation(command.replace(".exe", ""), console, false);
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
     * Extract Map, VMap, and MMap from WoW client folder. This operation is
     * executed in steps that are:<br />
     * <b>1</b>: Extract Map
     * <b>2</b>: Extract VMap (need previus step)
     * <b>3</b>: Assemble VMap (need previus step)
     * <b>4</b>: Extract MMap (indipendent step)
     *
     * @param toolFolder The String path value to tools folder where extractor
     * binaries are placed
     * @param clientFolder The String path value to WoW client folder
     * @param serverFolder The String path value to data folder into witc store
     * extracted Map, VMap, MMap
     * @param step The int value of step to execute
     * @param console The JTextPane object to be used for output in swing GUI
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if current step suceeded, false if Object console is not
     * null, false otherwise
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
                            System.console().printf("%s\n", msg);
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
                                System.console().printf("%s\n", msg);
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
                            System.console().printf("%s\n", msg);
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
                            System.console().printf("%s\n", msg);
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
                            System.console().printf("%s\n", msg);
                        }
                        this.btnInvoker.setText("ERROR");
                    }
                    break;
                default:
                    break;
            }
            try {
                switch (this.CURR_OS) {
                    case WINDOWS:
                        ret_val = this.winCmd.executeCmd(command, console, sb, false, prbCurrWork);
                        break;
                    case UNIX:
                        ret_val = this.unixCmd.executeShell(command.replace(".exe", "").replace("&", ";"), console, sb, false, prbCurrWork);
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
                System.console().printf("%s\n", msg);
            }
            this.btnInvoker.setText("ERROR");
            ret_val = false;
        }
        return ret_val;
    }

    /**
     * Execute the specified command in current operating system shell.
     *
     * @param command The String value for commands to execute
     * @param console The JTextPane object to be used for output in swing GUI
     * @param sb The StringBuilder field for output return not in console or GUI
     * console
     * @param toBuffer The boolean valute to enable output return in
     * StringBuilder
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if command suceeded, false if Object console is not null,
     * false otherwise
     */
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
            LOG.log(Level.SEVERE, null, ex);
            System.console().printf("%s\n", ex.getLocalizedMessage());
            return false;
        }
        return ret_val;
    }

    /**
     * Execute the specified commands list in current operating system shell.
     *
     * @param command The {@literal ArrayList<String>} value for commands to
     * execute
     * @param console The JTextPane object to be used for output in swing GUI
     * @param sb The StringBuilder field for output return not in console or GUI
     * console
     * @param toBuffer The boolean valute to enable output return in
     * StringBuilder
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if command suceeded, false if Object console is not null,
     * false otherwise
     */
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
            LOG.log(Level.SEVERE, null, ex);
            System.console().printf("%s\n", ex.getLocalizedMessage());
            return false;
        }
        return ret_val;
    }

    /**
     * Set the git.exe path for Windows operating systems
     *
     * @param gitPath The String path value to git.exe binary
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
     * Set the cmake.exe path for Windows operating systems
     *
     * @param cmakePath The String path value to cmake.exe binary
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

    /**
     * @return The String value of current operating system name
     */
    public String getOsName() {
        return this.osName;
    }

    /**
     * @return The String value of current operating system version
     */
    public String getOsVersion() {
        return this.osVersion;
    }

    /**
     * @return The String value of current operating system achitecture (x86,
     * amd64, etc...)
     */
    public String getOsArch() {
        return this.osArch;
    }

    /**
     * @return The int value for current operating system.
     */
    public int getCURR_OS() {
        return this.CURR_OS;
    }

    /**
     * @return The boolean value for Power Shell scripts execution status in
     * current Power Shell sessions
     */
    public boolean isPSScriptEnabled() {
        return this.PSScriptEnabled;
    }

    /**
     *
     * @return The int value for output debug level
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
     * Set the output debug level.
     *
     * @param debugLevel The int value for output debug level
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
     * @return The JButton object used for background execution in GUI mode
     */
    public JButton getBtnInvoker() {
        return this.btnInvoker;
    }

    /**
     * Set the JButton objet used at the end of a background execution in GUI
     * mode. At the end of background excution will be changed the Text property
     * value of this button object that can be intercepted from PropertyChange
     * event.
     *
     * @param btnInvoker The JButton object used for background execution in GUI
     * mode
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
