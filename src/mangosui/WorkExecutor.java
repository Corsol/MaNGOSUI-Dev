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

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.JProgressBar;

/**
 *
 * @author Boni Simone <simo.boni@gmail.com>
 */
public class WorkExecutor extends javax.swing.JFrame {

    /**
     *
     */
    public static final int CONSOLE = 1;

    /**
     *
     */
    public static final int JTEXT = 2;

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(WorkExecutor.class.getName());

    /**
     * Default constructor
     */
    public WorkExecutor() {
    }

    /**
     * Check if Git is installed and if use a specific path or is set into
     * environment
     *
     * @param winPSPath The String value for git.exe path that need PowerShell
     * execution
     * @param winCMDPath The String value for git.exe path that need CMD o Shell
     * execution
     * @param cmdManager The CommandManager object with routine for current
     * operating system
     * @param txpConsole The JTextPane object to be used for output in swing GUI
     * @return True if Git command is found, false otherwise
     */
    public static boolean checkGit(String winPSPath, String winCMDPath, CommandManager cmdManager, Object txpConsole) {
        String outMsg;
        if (!cmdManager.checkGit(null)) {
            if (cmdManager.getCURR_OS() == cmdManager.WINDOWS && !cmdManager.isPSScriptEnabled()) {
                outMsg = "WARNING: PowerShell script execution is not ebabled. To enable it run PS as Administrator and use \"Set-ExecutionPolicy Unrestricted\" command.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_ORANGE);
            }
            // Check if path to git.exe exists
            String winGitPath = cmdManager.checkGit(winCMDPath, null) ? winCMDPath : (cmdManager.checkGit(winPSPath, null) ? winPSPath : "");

            if (winGitPath.isEmpty()) {
                outMsg = "ERROR: Git commands not found on system. Check Git installation or manually download repositories.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_RED);
                return false;
            } else {
                outMsg = "INFO: found Git commands.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
                return true;
            }
        } else {
            outMsg = "INFO: found Git commands.";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
            return true;
        }
    }

    /**
     * Perform the specified action for git repository specified. Available
     * action are:<br />
     * N: download repository into new local folder W: wipe current local
     * repository folder and download again the remote repository U: update (git
     * pull) local repository folder with remote changes
     *
     * @param action The String value for action to do.
     * @param gitURL The String URL value of remote git repository
     * @param destFolder The String value for local git repository folder
     * @param gitBranch The String value for remote git repository branch to use
     * @param gitProxyServer The String value for proxy server
     * @param gitProxyPort The String value for proxy port
     * @param cmdManager The CommandManager object with routine for current
     * operating system
     * @param txpConsole The JTextPane object to be used for output in swing GUI
     * @return True if action suceeded, false if Object console is not null,
     * false otherwise
     */
    public static boolean gitDownload(String action, String gitURL, String destFolder, String gitBranch, String gitProxyServer, String gitProxyPort, CommandManager cmdManager, Object txpConsole) {
        String outMsg;
        if (action.equalsIgnoreCase("N")) {
            outMsg = "\nDownloading new server repository...";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
            return doGitDownload(gitURL, destFolder, gitBranch, gitProxyServer, gitProxyPort, cmdManager, txpConsole);
        } else if (action.equalsIgnoreCase("W")) {
            outMsg = "\nWiping current server folder...";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
            if (cmdManager.deleteFolder(destFolder)) {
                outMsg = "Done.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
                outMsg = "\nDownloading server repository...";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
                return doGitDownload(gitURL, destFolder, gitBranch, gitProxyServer, gitProxyPort, cmdManager, txpConsole);
            } else {
                outMsg = "\nERROR: cannot wipe " + destFolder + " folder. Process aborted. Try it again.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_RED);
                return false;
            }

        } else if (action.equalsIgnoreCase("U")) {
            outMsg = "\nUpdating current server folder...";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
            if (cmdManager.gitCheckout(txpConsole)) {
                outMsg = "Done.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
                return true;
            } else {
                outMsg = "\nERROR: Check console output.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_RED);
                return false;
            }
        } else {
            outMsg = "\nWARNING: No action selected. Download skipped";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_ORANGE);
            return true;
        }

    }

    /**
     * Perform the download action for git repository specified.
     *
     * @param gitURL The String URL value of remote git repository
     * @param destFolder The String value for local git repository folder
     * @param gitBranch The String value for remote git repository branch to use
     * @param gitProxyServer The String value for proxy server
     * @param gitProxyPort The String value for proxy port
     * @param cmdManager The CommandManager object with routine for current
     * operating system
     * @param txpConsole The JTextPane object to be used for output in swing GUI
     * @return True if action suceeded, false if Object console is not null,
     * false otherwise
     */
    private static boolean doGitDownload(String gitURL, String destFolder, String gitBranch, String gitProxyServer, String gitProxyPort, CommandManager cmdManager, Object txpConsole) {
        String outMsg;
        if (cmdManager.gitDownload(gitURL, destFolder, gitBranch, gitProxyServer, gitProxyPort, txpConsole)) {
            outMsg = "\nDone.";
            sendOutput(outMsg, null, ConsoleManager.TEXT_BLUE);
            return true;
        } else {
            outMsg = "\nERROR: Check console output and redo process with W (wipe) option.";
            sendOutput(outMsg, null, ConsoleManager.TEXT_RED);
            return false;
        }

    }

    /**
     * Extract the local git repository folder from last part of gir URL
     *
     * @param gitFolder The String value for base git local folder
     * @param gitUrl The String URL value for git remote repository from where
     * get local respository folder
     * @return The String value for local folder to use
     */
    public static String setGitFolder(String gitFolder, String gitUrl) {
        return gitFolder.isEmpty() && !gitUrl.isEmpty()
                ? gitUrl.substring(gitUrl.lastIndexOf('/') + 1, gitUrl.length() - 4)
                : gitFolder;
    }

    /**
     * Check if MySQL is installed and if use a specific path or is set into
     * environment
     *
     * @param dbFolder The String value for local database folder with mysql.exe
     * tool
     * @param pathToMySQL The String value with path to MySQL installation
     * folder
     * @param cmdManager The CommandManager object with routine for current
     * operating system
     * @param txpConsole The JTextPane object to be used for output in swing GUI
     * @return True if MySQL command is found, false otherwise
     */
    public static boolean checkMySQL(String dbFolder, String pathToMySQL, CommandManager cmdManager, Object txpConsole) {
        String outMsg;
        if (!cmdManager.checkMySQL(null)) {
            outMsg = "INFO: MySQL is not locally installed... checking for mysql.exe tool.";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_ORANGE);
            String mysqlToolPath = (dbFolder != null && !dbFolder.isEmpty() ? dbFolder : "database") + File.separator + pathToMySQL;
            if (!cmdManager.checkMySQL(mysqlToolPath, null)) {
                outMsg = "WARNING: mysql.exe command not found on system. Check mysql installation or path '" + mysqlToolPath + "' to mysql.exe into database folder.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_RED);
                return false;
            } else {
                outMsg = "INFO: found MySQL commands.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
                return true;
            }
        } else {
            outMsg = "INFO: found MySQL commands.";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
            return true;
        }
    }

    /**
     * Check if admin and local user are configured and databases are present
     *
     * @param dbServer The String value for IP/Name of database server
     * @param dbPort The String value for port of database server
     * @param dbAdmin The String value for database admin user
     * @param dbAdminPwd The String value for database admin password
     * @param dbUser The String value for local database user
     * @param dbUserPwd The String value for local database user password
     * @param worldDBName The String value for mangos database
     * @param charDBName The String value for characters database
     * @param realmDBName The String value for realmd database
     * @param cmdManager The CommandManager object with routine for current
     * operating system
     * @param txpConsole The JTextPane object to be used for output in swing GUI
     * @return True if all check suceeded, false otherwise
     */
    public static boolean checkDBExistance(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String dbUser, String dbUserPwd, String worldDBName, String charDBName, String realmDBName, CommandManager cmdManager, Object txpConsole) {
        String outMsg;
        if (cmdManager.checkDBUser(dbServer, dbPort, dbAdmin, dbAdminPwd, txpConsole)
                && cmdManager.checkDBUser(dbServer, dbPort, dbUser, dbUserPwd, txpConsole)
                && cmdManager.checkDBStructure(dbServer, dbPort, dbAdmin, dbAdminPwd, worldDBName, charDBName, realmDBName, txpConsole)) {
            outMsg = "INFO: MySQL databases found.";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
            return true;
        } else {
            outMsg = "ERROR: MySQL databases and user not fully configured. Need to setup it first!";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_RED);
            return false;
        }
    }

    /**
     * Chreate the base database structure with local user for next connection
     * and three databases for data
     *
     * @param dbServer The String value for IP/Name of database server
     * @param dbPort The String value for port of database server
     * @param dbAdmin The String value for database admin user
     * @param dbAdminPwd The String value for database admin password
     * @param dbUser The String value for local database user
     * @param dbUserPwd The String value for local database user password
     * @param worldDBName The String value for mangos database
     * @param charDBName The String value for characters database
     * @param realmDBName The String value for realmd database
     * @param cmdManager The CommandManager object with routine for current
     * operating system
     * @param txpConsole The JTextPane object to be used for output in swing GUI
     * @return True if creation suceeded, false if Object console is not null,
     * false otherwise
     */
    public static boolean mysqlCreateDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String dbUser, String dbUserPwd, String worldDBName, String charDBName, String realmDBName, CommandManager cmdManager, Object txpConsole) {
        String outMsg = "Installing new database...";
        sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
        return cmdManager.createDB(dbServer, dbPort, dbAdmin, dbAdminPwd, dbUser, dbUserPwd, worldDBName, charDBName, realmDBName, txpConsole);
    }

    /**
     * Load data from script file into database
     *
     * @param dbServer The String value for IP/Name of database server
     * @param dbPort The String value for port of database server
     * @param dbAdmin The String value for database admin user
     * @param dbAdminPwd The String value for database admin password
     * @param baseFolder The String value for root folder of database load
     * script
     * @param dbFolder The String value for the database name folder where load
     * script is placed
     * @param loadScript The String value for the name of load script
     * @param dbName The String value of database name where put data
     * @param dbSetupFolder The String value for folder where load script inside
     * dbFolder is placed (tipically these are Setup or Update)
     * @param cmdManager The CommandManager object with routine for current
     * operating system
     * @param txpConsole The JTextPane object to be used for output in swing GUI
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if data load suceeded, false if Object console is not null,
     * false otherwise
     */
    public static boolean mysqlLoadDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String baseFolder, String dbFolder, String loadScript, String dbName, String dbSetupFolder, CommandManager cmdManager, Object txpConsole, final JProgressBar prbCurrWork) {
        String outMsg;
        String setupPath = baseFolder + File.separator
                + dbFolder + File.separator
                + dbSetupFolder + File.separator
                + loadScript;
        outMsg = "\nCreating " + dbName + " database from: " + setupPath;
        sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
        if (cmdManager.loadDB(dbServer, dbPort, dbAdmin, dbAdminPwd, dbName, setupPath, txpConsole, prbCurrWork)) {
            outMsg = "\nDone.";
            sendOutput(outMsg, null, ConsoleManager.TEXT_BLUE);
            return true;
        } else {
            outMsg = "\nERROR: Check console log.";
            sendOutput(outMsg, null, ConsoleManager.TEXT_RED);
            return false;
        }
    }

    /**
     *
     * @param dbServer The String value for IP/Name of database server
     * @param dbPort The String value for port of database server
     * @param dbAdmin The String value for database admin user
     * @param dbAdminPwd The String value for database admin password
     * @param baseFolder The String value for root folder of database load
     * script
     * @param dbFolder The String value for the database name folder where load
     * script is placed
     * @param updFolders The {@literal ArrayList<String>} object with list of
     * update folder where script will be loaded for updates. (leave empty if
     * fullUpdPath will be used)
     * @param fullUpdPath The String value for a specific path from where load
     * update scripts (leave empty if updFolders will be used)
     * @param dbName The String value of database name where put data
     * @param dbUpdateFolder The String value for folder where load script
     * inside dbFolder is placed (tipically these are Setup or Update)
     * @param cmdManager The CommandManager object with routine for current
     * operating system
     * @param txpConsole The JTextPane object to be used for output in swing GUI
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     * @return True if data load suceeded, false if Object console is not null,
     * false otherwise
     */
    public static boolean mysqlUpdateDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String baseFolder, String dbFolder, ArrayList<String> updFolders, String fullUpdPath, String dbName, String dbUpdateFolder, CommandManager cmdManager, Object txpConsole, final JProgressBar prbCurrWork) {
        String outMsg;
        boolean dbRet = true;
        if (fullUpdPath != null && !fullUpdPath.isEmpty()) {
            outMsg = "\nImporting " + dbName + " updates from: " + fullUpdPath;
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
            dbRet &= cmdManager.loadDBUpdate(dbServer, dbPort, dbAdmin, dbAdminPwd, dbName, fullUpdPath, txpConsole, prbCurrWork);
        } else {
            for (String updFolder : updFolders) {
                if (!updFolder.isEmpty()) {
                    String updatePath = baseFolder + File.separator
                            + dbFolder + File.separator
                            + dbUpdateFolder + File.separator
                            + updFolder;
                    outMsg = "\nImporting " + dbName + " updates from: " + updatePath;
                    sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
                    dbRet &= cmdManager.loadDBUpdate(dbServer, dbPort, dbAdmin, dbAdminPwd, dbName, updatePath, txpConsole, prbCurrWork);
                } else {
                    dbRet = false;
                }
            }
        }
        if (dbRet) {
            outMsg = "\nDone.";
            sendOutput(outMsg, null, ConsoleManager.TEXT_BLUE);
            return true;
        } else {
            outMsg = "\nERROR: Check console log.";
            sendOutput(outMsg, null, ConsoleManager.TEXT_RED);
            return false;
        }
    }

    /**
     * Check if CMake command is installed and if use a specific path or is set
     * into environment
     *
     * @param cmake32Path The String value for windows 32bit cmake path
     * @param cmake64Path The String value for windows 64bit cmake path
     * @param cmdManager The CommandManager object with routine for current
     * operating system
     * @param txpConsole The JTextPane object to be used for output in swing GUI
     * @return True if CMake command is found, false otherwise
     */
    public static boolean checkCMake(String cmake32Path, String cmake64Path, CommandManager cmdManager, Object txpConsole) {
        String outMsg;
        if (!cmdManager.checkCMAKE(null)) {
            outMsg = "INFO: CMAKE is not installed into PATH/shell environment... checking for cmake.exe installation folder.";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
            if (cmdManager.checkCMAKE(cmake32Path, null)) {
                outMsg = "INFO: found CMAKE commands on x86 system.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
                return true;
            } else if (cmdManager.checkCMAKE(cmake64Path, null)) {
                outMsg = "INFO: found CMAKE commands on x64 system.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
                return true;
            } else {
                outMsg = "ERROR: CMAKE commands not found on system. Check CMAKE installation!.";
                sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_RED);
            }
            return false;
        } else {
            outMsg = "INFO: found CMAKE commands.";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
            return true;
        }

    }

    /**
     * Check if OpenSSL library is installed and if use a specific path or is
     * set into environment
     *
     * @param cmdManager The CommandManager object with routine for current
     * operating system
     * @param txpConsole The JTextPane object to be used for output in swing GUI
     * @return True if OpenSSL command is found, false otherwise
     */
    public static boolean checkOpenSSL(CommandManager cmdManager, Object txpConsole) {
        String outMsg;
        if (cmdManager.checkOpenSSLInclude("", null).isEmpty() || cmdManager.checkOpenSSLLib("", null).isEmpty()) {
            outMsg = "ERROR: OpenSSL libraries not found on system. Check OpenSSL installation!.";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_RED);
            return false;
        } else {
            outMsg = "INFO: found OpenSSL libraries.";
            sendOutput(outMsg, txpConsole, ConsoleManager.TEXT_BLUE);
            return true;
        }
    }

    /**
     * Display the specified text into console or GUI
     *
     * @param outMsg The String value of text to be displayed
     * @param txpConsole The JTextPane object to be used for output in swing GUI
     * @param consoleColor The int value for text color
     */
    private static void sendOutput(String outMsg, Object txpConsole, int consoleColor) {
        if (txpConsole != null) {
            ConsoleManager.getInstance().updateGUIConsole(txpConsole, outMsg, consoleColor);
        } else {
            System.out.printf("%s\n", outMsg);
        }

    }
}
