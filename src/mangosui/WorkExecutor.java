/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.File;
import java.util.ArrayList;
import javax.swing.JProgressBar;

/**
 *
 * @author Boni
 */
public class WorkExecutor extends javax.swing.JFrame {

    public static final int CONSOLE = 1;
    public static final int JTEXT = 2;
    //private static ConsoleManager consColor;

    public WorkExecutor() {
    }

    public static boolean checkGit(String winPSPath, String winCMDPath, CommandManager cmdManager, ConsoleManager console, Object txpConsole) {
        /*
         if (!cmdManager.checkCMAKE(null)) {
         //.setEnabled(false);
         outMsg = "INFO: CMAKE is not installed into PATH/shell environment... checking for cmake.exe installation folder.";
         sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
         if (cmdManager.checkCMAKE(cmake32Path, null)) {
         // cmakeOk = true;
         outMsg = "INFO: Founded CMAKE commands on x86 system.";
         sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
         return true;
         } else if (cmdManager.checkCMAKE(cmake64Path, null)) {
         // cmakeOk = true;
         outMsg = "INFO: Founded CMAKE commands on x64 system.";
         sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
         return true;
         } else {
         outMsg = "ERROR: CMAKE commands not found on system. Check CMAKE installation!.";
         sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_RED);
         }
         return false;

         */
        String outMsg;
        if (!cmdManager.checkGit(null)) {
            if (cmdManager.getCURR_OS() == cmdManager.WINDOWS && !cmdManager.isPSScriptEnabled()) {
                outMsg = "WARNING: PowerShell script execution is not ebabled. To enable it run PS as Administrator and use \"Set-ExecutionPolicy Unrestricted\" command.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_ORANGE);
            }
            String winGitPath = cmdManager.checkGit(winCMDPath, null) ? winCMDPath : (cmdManager.checkGit(winPSPath, null) ? winPSPath : "");

            //if (!cmdManager.checkGit(winPSPath, null) && ! cmdManager.checkGit(winCMDPath, null)) {
            if (winGitPath.isEmpty()) {
                outMsg = "ERROR: Git commands not found on system. Check Git installation or manually download repositories.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_RED);
                return false;
            } else {
                //cmdManager.setWinGitPath(winGitPath);
                outMsg = "INFO: Founded Git commands.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
                return true;
            }
        } else {
            //cmdManager.setWinGitPath(winGitPath);
            outMsg = "INFO: Founded Git commands.";
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
            return true;
        }
    }

    public static boolean gitDownload(String action, String gitURL, String destFolder, String gitBranch, String gitProxyServer, String gitProxyPort, CommandManager cmdManager, ConsoleManager console, Object txpConsole) {
        String outMsg;
        if (action.equalsIgnoreCase("N")) {
            outMsg = "\nDownloading new server repository...";
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
            return doGitDownload(gitURL, destFolder, gitBranch, gitProxyServer, gitProxyPort, cmdManager, console, txpConsole);
            //return doGitDownload(gitURL, destFolder, gitBranch, gitProxyServer, gitProxyPort, cmdManager);
        } else if (action.equalsIgnoreCase("W")) {
            outMsg = "\nWiping current server folder...";
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
            if (cmdManager.deleteFolder(destFolder)) {
                outMsg = "Done.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
                outMsg = "\nDownloading server repository...";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
                return doGitDownload(gitURL, destFolder, gitBranch, gitProxyServer, gitProxyPort, cmdManager, console, txpConsole);
                //return doGitDownload(gitURL, destFolder, gitBranch, gitProxyServer, gitProxyPort, cmdManager);
            } else {
                outMsg = "\nERROR: cannot wipe " + destFolder + " folder. Process aborted. Try it again.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_RED);
                return false;
            }

        } else if (action.equalsIgnoreCase("U")) {
            outMsg = "\nUpdating current server folder...";
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
            if (cmdManager.gitCheckout(console)) {
                outMsg = "Done.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
                return true;
            } else {
                outMsg = "\nERROR: Check console output.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_RED);
                return false;
            }
        } else {
            outMsg = "\nWARNING: No action selected. Download skipped";
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_ORANGE);
            return true;
        }

    }

    private static boolean doGitDownload(String gitURL, String destFolder, String gitBranch, String gitProxyServer, String gitProxyPort, CommandManager cmdManager, ConsoleManager console, Object txpConsole) {
        //    private static boolean doGitDownload(String gitURL, String destFolder, String gitBranch, String gitProxyServer, String gitProxyPort, CommandManager cmdManager) {
        String outMsg;
        if (cmdManager.gitDownload(gitURL, destFolder, gitBranch, gitProxyServer, gitProxyPort, txpConsole)) {
            outMsg = "\nDone.";
            sendOutput(outMsg, null, null, ConsoleManager.TEXT_BLUE);
            return true;
        } else {
            outMsg = "\nERROR: Check console output and redo process with W (wipe) option.";
            sendOutput(outMsg, null, null, ConsoleManager.TEXT_RED);
            return false;
        }

    }

    public static String setGitFolder(String txtBox, String gitUrl) {
        return txtBox.isEmpty() && !gitUrl.isEmpty()
                ? gitUrl.substring(gitUrl.lastIndexOf("/") + 1, gitUrl.length() - 4)
                : txtBox;
    }

    public static boolean checkMySQL(String dbFolder, String pathToMySQL, CommandManager cmdManager, ConsoleManager console, Object txpConsole) {
        String outMsg;
        if (!cmdManager.checkMySQL(null)) {
            outMsg = "INFO: MySQL is not locally installed... checking for mysql.exe tool.";
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_ORANGE);
            String mysqlToolPath = (dbFolder != null && !dbFolder.isEmpty() ? dbFolder : "database") + File.separator + pathToMySQL;
            if (!cmdManager.checkMySQL(mysqlToolPath, null)) {
                outMsg = "WARNING: mysql.exe command not found on system. Check mysql installation or path '" + mysqlToolPath + "' to mysql.exe into database folder.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_RED);
                return false;
            } else {
                outMsg = "INFO: Founded MySQL commands.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
                return true;
            }
        } else {
            outMsg = "INFO: Founded MySQL commands.";
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
            return true;
        }
    }

    public static boolean checkDBExistance(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String dbUser, String dbUserPwd, String worldDBName, String charDBName, String realmDBName, CommandManager cmdManager, ConsoleManager console, Object txpConsole) {
        String outMsg;
        if (cmdManager.checkDBUser(dbServer, dbPort, dbAdmin, dbAdminPwd, txpConsole)
                && cmdManager.checkDBUser(dbServer, dbPort, dbUser, dbUserPwd, txpConsole)
                && cmdManager.checkDBStructure(dbServer, dbPort, dbAdmin, dbAdminPwd, worldDBName, charDBName, realmDBName, txpConsole)) {
            outMsg = "INFO: MySQL databases founded.";
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
            return true;
        } else {
            outMsg = "ERROR: MySQL databases and user not fully configured. Need to setup it first!";
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_RED);
            return false;
        }
    }

    public static boolean mysqlCreateDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String dbUser, String dbUserPwd, String worldDBName, String charDBName, String realmDBName, CommandManager cmdManager, ConsoleManager console, Object txpConsole) {
        String outMsg = "Installing new database...";
        sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
        return cmdManager.createDB(dbServer, dbPort, dbAdmin, dbAdminPwd, dbUser, dbUserPwd, worldDBName, charDBName, realmDBName, txpConsole);
    }

    public static boolean mysqlLoadDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String baseFolder, String dbFolder, String loadScript, String dbName, String dbSetupFolder, CommandManager cmdManager, ConsoleManager console, Object txpConsole, final JProgressBar prbCurrWork) {
        String outMsg;
        String setupPath = baseFolder + File.separator
                + dbFolder + File.separator
                + dbSetupFolder + File.separator
                + loadScript;
        outMsg = "\nCreating " + dbName + " database from: " + setupPath;
        sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
        if (cmdManager.loadDB(dbServer, dbPort, dbAdmin, dbAdminPwd, dbName, setupPath, txpConsole, prbCurrWork)) {
            outMsg = "\nDone.";
            sendOutput(outMsg, null, null, ConsoleManager.TEXT_BLUE);
            return true;
        } else {
            outMsg = "\nERROR: Check console log.";
            sendOutput(outMsg, null, null, ConsoleManager.TEXT_RED);
            return false;
        }
    }

    public static boolean mysqlUpdateDB(String dbServer, String dbPort, String dbAdmin, String dbAdminPwd, String baseFolder, String dbFolder, ArrayList<String> updFolders, String fullUpdPath, String dbName, String dbUpdateFolder, CommandManager cmdManager, ConsoleManager console, Object txpConsole, final JProgressBar prbCurrWork) {
        String outMsg;
        boolean dbRet = true;
        if (fullUpdPath != null && !fullUpdPath.isEmpty()) {
            outMsg = "\nImporting " + dbName + " updates from: " + fullUpdPath;
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
            dbRet &= cmdManager.loadDBUpdate(dbServer, dbPort, dbAdmin, dbAdminPwd, dbName, fullUpdPath, txpConsole, prbCurrWork);
        } else {
            for (String updFolder : updFolders) {
                if (!updFolder.isEmpty()) {
                    String updatePath = baseFolder + File.separator
                            + dbFolder + File.separator
                            + dbUpdateFolder + File.separator
                            + updFolder;
                    outMsg = "\nImporting " + dbName + " updates from: " + updatePath;
                    sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
                    dbRet &= cmdManager.loadDBUpdate(dbServer, dbPort, dbAdmin, dbAdminPwd, dbName, updatePath, txpConsole, prbCurrWork);
                } else {
                    dbRet = false;
                }
            }
        }
        if (dbRet) {
            outMsg = "\nDone.";
            sendOutput(outMsg, null, null, ConsoleManager.TEXT_BLUE);
            return true;
        } else {
            outMsg = "\nERROR: Check console log.";
            sendOutput(outMsg, null, null, ConsoleManager.TEXT_RED);
            return false;
        }
    }

    public static boolean checkCMake(String cmake32Path, String cmake64Path, CommandManager cmdManager, ConsoleManager console, Object txpConsole) {
        String outMsg;
        if (!cmdManager.checkCMAKE(null)) {
            //.setEnabled(false);
            outMsg = "INFO: CMAKE is not installed into PATH/shell environment... checking for cmake.exe installation folder.";
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
            if (cmdManager.checkCMAKE(cmake32Path, null)) {
                // cmakeOk = true;
                outMsg = "INFO: Founded CMAKE commands on x86 system.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
                return true;
            } else if (cmdManager.checkCMAKE(cmake64Path, null)) {
                // cmakeOk = true;
                outMsg = "INFO: Founded CMAKE commands on x64 system.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
                return true;
            } else {
                outMsg = "ERROR: CMAKE commands not found on system. Check CMAKE installation!.";
                sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_RED);
            }
            return false;
        } else {
            // cmakePath =
            outMsg = "INFO: Founded CMAKE commands.";
            sendOutput(outMsg, console, txpConsole, ConsoleManager.TEXT_BLUE);
            return true;
        }

    }

    /*    public static boolean checkCMakeBuild(String buildFolder, CommandManager cmdManager, ConsoleManager console, Object txpConsole) {
     String outMsg;
     if (cmdManager.checkFolder(buildFolder)){
            
     }
     }*/
    private static void sendOutput(String outMsg, ConsoleManager console, Object txpConsole, int consoleColor) {
        if (console != null && txpConsole != null) {
            console.updateGUIConsole(txpConsole, outMsg, consoleColor);
        } else {
            System.out.println(outMsg);
        }

    }
}
