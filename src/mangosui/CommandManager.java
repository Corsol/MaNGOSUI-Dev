/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Object;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Simone
 */
public class CommandManager {

    private static final String OS_NAME = "os.name";
    private static final String OS_VERSION = "os.version";
    private static final String OS_ARCH = "os.arch";
    public final int WINDOWS = 1;
    public final int UNIX = 2;
    public final int MACOS = 3;
    private WindowsCommands winCmd;
    private UnixCommand unixCmd;
    private MacOSCommand macCmd;

    private String osName = "";
    private String osVersion = "";
    private String osArch = "";
    private int CURR_OS = 0; // 0: N/A, 1: Windows, 2: Unix, 3: MAC?
    private boolean PSScriptEnabled = false;
    private boolean MySQLInstalled = false;
    private String pathSeparator = "\\";
    private String MySQLPath = "";
    //private String OpenSSLPath = "";
    //private String MySQLWinPath = "";

    public CommandManager() {
        osName = System.getProperty(OS_NAME);
        osVersion = System.getProperty(OS_VERSION);
        osArch = System.getProperty(OS_ARCH);
        if (osName.toLowerCase().contains("windows")) {
            CURR_OS = 1;
            winCmd = new WindowsCommands();
            PSScriptEnabled = winCmd.checkPSScript();
        } else if (osName.toLowerCase().contains("linux")) {
            CURR_OS = 2;
            unixCmd = new UnixCommand();
        } else if (osName.toLowerCase().contains("mac")) {
            CURR_OS = 3;
            //macCmd = new MacOSCommand();
        } else {
        }
    }

    public boolean checkGit(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "git --version";
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    ret_val = winCmd.gitOperation(command, console, true);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.executeShell(command, console, sb, true);
                    break;
                case 3: // MAC OS
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

    public boolean checkCMAKE(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "cmake --help";
        // Set toBuffer param to true to avoid console text
        return runOSCommand(command, console, sb, true);
    }

    public boolean checkCMAKE(String pathToCMake, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "\"" + pathToCMake + File.separator + "cmake.exe\" --help";
        // Set toBuffer param to true to avoid console text
        boolean ret = runOSCommand(command, console, sb, true);
        if (ret) {
            this.setWinCmakePath(pathToCMake + File.separator);
        }
        return ret;
    }

    public boolean checkMySQL(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "mysql.exe --help";
        // Set toBuffer param to true to avoid console text
        boolean ret = runOSCommand(command, console, sb, true);
        if (ret) {
            MySQLInstalled = true;
        }
        return ret;
    }

    public boolean checkMySQL(String pathToMySQL, Object console) {
        StringBuilder sb = new StringBuilder();
        String command = pathToMySQL + File.separator + "mysql.exe --help";
        // Set toBuffer param to true to avoid console text
        boolean ret = runOSCommand(command, console, sb, true);
        if (ret) {
            MySQLInstalled = false;
            MySQLPath = pathToMySQL + File.separator;
        }
        return ret;
    }

    public String checkMySQLLib(String pathToMySQL, Object console) {
        String ret_val = "";
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.checkMySQLLib(pathToMySQL, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.checkMySQLLib(pathToMySQL, console);
                    break;
                case 3: // MAC OS
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

    public String checkMySQLInclude(String pathToMySQL, Object console) {
        String ret_val = "";
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.checkMySQLInclude(pathToMySQL, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.checkMySQLInclude(pathToMySQL, console);
                    break;
                case 3: // MAC OS
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

    public String checkOpenSSLLib(String pathToOpenSSL, Object console) {
        String ret_val = "";
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.checkOpenSSLLib(pathToOpenSSL, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.checkOpenSSLLib(pathToOpenSSL, console);
                    break;
                case 3: // MAC OS
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

    public String checkOpenSSLInclude(String pathToOpenSSL, Object console) {
        String ret_val = "";
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.checkOpenSSLInclude(pathToOpenSSL, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.checkOpenSSLInclude(pathToOpenSSL, console);
                    break;
                case 3: // MAC OS
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

    public boolean installMySQLPortable(Object console) {
        String basePath;
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    basePath = System.getenv("ProgramFiles(x86)") + File.separator + "MySQL" + File.separator + "MySQL Server 5.6";
                    // Set toBuffer param to true to avoid console text
                    //ret_val = winCmd.gitOperation(command, console, true);
                    break;
                case 2: // Unix
                    basePath = "";
                    //ret_val = unixCmd.executeShell(command, console, sb, true);
                    break;
                case 3: // MAC OS
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
                    System.out.println("Unable to create portable directory: " + basePath);
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
                    extractFile(zipIn, filePath);
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
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            return false;
        }
        return ret_val;
    }

    public boolean installOpenSSLPortable(Object console) {
        String basePath;
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    basePath = "C:" + File.separator + "OpenSSL-Win32";
                    // Set toBuffer param to true to avoid console text
                    //ret_val = winCmd.gitOperation(command, console, true);
                    break;
                case 2: // Unix
                    basePath = "";
                    //ret_val = unixCmd.executeShell(command, console, sb, true);
                    break;
                case 3: // MAC OS
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
                    System.out.println("Unable to create portable directory: " + basePath);
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
                    extractFile(zipIn, filePath);
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
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            return false;
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
    /*public boolean checkOpenSSL(String pathToOpenSSL, Object console){
     StringBuilder sb = new StringBuilder();
     File file = new File(pathToOpenSSL);
     // Set toBuffer param to true to avoid console text
     boolean ret = file.exists();
     if (ret) {
     MySQLPath = pathToOpenSSL + File.separator;
     }
     return ret;
     }*/

    public boolean createDB(ConfLoader config, Object console) {
        StringBuilder sb = new StringBuilder();
        //String command = MySQLPath + "mysql.exe -q -s -h "+server+" --port="+port+" --user="+usrAdmin+" --password="+usrAdminPwd+" < World"+File.separator+"Setup"+File.separator+"mangosdCreateDB.sql";
        String command = MySQLPath + "mysql.exe -q -s -f -h " + config.getDatabaseServer() + " --port=" + config.getDatabasePort() + " --user=" + config.getDatabaseAdmin() + " --password=" + config.getDatabaseAdminPass() + " -e \""
                + "CREATE DATABASE `" + config.getWorldDBName() + "` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE DATABASE `" + config.getCharDBName() + "` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE DATABASE `" + config.getRealmDBName() + "` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;"
                + "CREATE USER '" + config.getDatabaseUser() + "'@'localhost' IDENTIFIED BY '" + config.getDatabaseUserPass() + "';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON `" + config.getWorldDBName() + "`.* TO '" + config.getDatabaseUser() + "'@'localhost';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON `" + config.getCharDBName() + "`.* TO '" + config.getDatabaseUser() + "'@'localhost';"
                + "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, LOCK TABLES ON `" + config.getRealmDBName() + "`.* TO '" + config.getDatabaseUser() + "'@'localhost';\"";
        //String[] command = new String[]{"cmd.exe"};
        return runOSCommand(command, console, sb, false);
    }

    public boolean loadDB(ConfLoader config, String Database, String setupFolder, Object console) {
        StringBuilder sb = new StringBuilder();
        //String command = "";
        String command = MySQLPath + "mysql.exe -q -s -h " + config.getDatabaseServer() + " --port=" + config.getDatabasePort() + " --user=" + config.getDatabaseAdmin() + " --password=" + config.getDatabaseAdminPass() + " "
                + Database + " < " + setupFolder;
        return runOSCommand(command, console, sb, false);
    }

    public boolean loadDBUpdate(ConfLoader config, String Database, String updateFolder, Object console) {
        StringBuilder sb = new StringBuilder();
        String command;
        boolean retOS = true;
        File file = new File(updateFolder);
        if (file.isDirectory() && file.list().length > 0) {
            for (File subFile : file.listFiles(new SQLFileFilter())) {
                if (!subFile.isDirectory()) {
                    command = MySQLPath + "mysql.exe -q -s -h " + config.getDatabaseServer() + " --port=" + config.getDatabasePort() + " --user=" + config.getDatabaseAdmin() + " --password=" + config.getDatabaseAdminPass() + " "
                            + Database + " < " + updateFolder + File.separator + subFile.getName();
                    retOS &= runOSCommand(command, console, sb, false);
                }
            }
        }
        return retOS;
    }

    public boolean cmakeConfig(String serverFolder, String buildFolder, HashMap<String, String> options, Object console) {
        boolean ret_val = false;
        try {
            if (checkFolder(buildFolder)){
                deleteFolder(buildFolder);
            }
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.cmakeConfig(serverFolder, buildFolder, options, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.cmakeConfig(serverFolder, buildFolder, options, console);
                    break;
                case 3: // MAC OS
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

    public boolean cmakeInstall(String buildFolder, String runFolder, Object console) {
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.cmakeInstall(buildFolder, runFolder, console);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.cmakeInstall(buildFolder, runFolder, console);
                    break;
                case 3: // MAC OS
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

    public boolean checkFolder(String path) {
        try {
            File file = new File(path);
            return file.exists() && file.isDirectory();
        } catch (Exception ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getLocalizedMessage());
            return false;
        }
    }

    public boolean deleteFolder(String path) {
        try {
            File file = new File(path);
            if (file.isDirectory() && file.list().length > 0) {
                for (String subFile : file.list()) {
                    deleteFolder(path + File.separator + subFile);
                }
            }
            return file.delete();
        } catch (Exception ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getLocalizedMessage());
            return false;
        }
    }

    public boolean isRepoUpToDate(String pathToRepo) {
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.isRepoUpToDate(pathToRepo);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.isRepoUpToDate(pathToRepo);
                    break;
                case 3: // MAC OS
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

    public boolean gitDownload(String url, String folder, String branch, Object console) {
        return gitDownload(url, folder, branch, "", "", console);
    }

    public boolean gitDownload(String url, String folder, String branch, String proxyServer, String proxyPort, Object console) {
        StringBuilder sb = new StringBuilder();
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
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.gitOperation(command, console, false);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.executeShell(command, console, sb, false);
                    break;
                case 3: // MAC OS
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

    public boolean gitCheckout(Object console) {
        StringBuilder sb = new StringBuilder();
        String command = "git pull";
        return runOSCommand(command, console, sb, false);
    }

    public void setWinGitPath(String gitPath) {
        switch (CURR_OS) {
            case 1: // Windows
                winCmd.setGitPath(gitPath);
                break;
            case 2: // Unix
                break;
            case 3: // MAC OS
                break;
            default:
                break;
        }
    }

    public void setWinCmakePath(String cmakePath) {
        switch (CURR_OS) {
            case 1: // Windows
                winCmd.setCmakePath(cmakePath);
                break;
            case 2: // Unix
                break;
            case 3: // MAC OS
                break;
            default:
                break;
        }
    }

    private boolean runOSCommand(String command, Object console, final StringBuilder sb, boolean toBuffer) {
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.executeCmd(command, console, sb, toBuffer);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.executeShell(command, console, sb, toBuffer);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            return false;
        }
        return ret_val;
    }

    private boolean runOSCommand(ArrayList<String> command, Object console, final StringBuilder sb, boolean toBuffer) {
        boolean ret_val = false;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    ret_val = winCmd.executeCmd(command, console, sb, toBuffer);
                    break;
                case 2: // Unix
                    ret_val = unixCmd.executeShell(command, console, sb, toBuffer);
                    break;
                case 3: // MAC OS
                    break;
                default:
                    ret_val = false;
                    break;
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            return false;
        }
        return ret_val;
    }

    /**
     * @return the macCmd
     */
    public MacOSCommand getMacCmd() {
        return macCmd;
    }

    /**
     * @return the osName
     */
    public String getOsName() {
        return osName;
    }

    /**
     * @return the osVersion
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * @return the osArch
     */
    public String getOsArch() {
        return osArch;
    }

    /**
     * @return the CURR_OS
     */
    public int getCURR_OS() {
        return CURR_OS;
    }

    /**
     * @return the PSScriptEnabled
     */
    public boolean isPSScriptEnabled() {
        return PSScriptEnabled;
    }

    public int getDebugLevel() {
        int ret_val = 0;
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    ret_val = winCmd.getDebugLevel();
                    break;
                case 2: // Unix
                    ret_val = unixCmd.getDebugLevel();
                    break;
                case 3: // MAC OS
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

    public void setDebugLevel(int debugLevel) {
        try {
            switch (CURR_OS) {
                case 1: // Windows
                    // Set toBuffer param to true to avoid console text
                    winCmd.setDebugLevel(debugLevel);
                    break;
                case 2: // Unix
                    break;
                case 3: // MAC OS
                    break;
                default:
                    //ret_val = 0;
                    break;
            }
        } catch (Exception ex) {
        }
    }
}

/* Default check structure
 boolean ret_val = false;
 try {
 switch (CURR_OS) {
 case 1: // Windows
 break;
 case 2: // Unix
 break;
 case 3: // MAC OS
 break;
 default:
 ret_val = false;
 break;
 }
 } catch (Exception ex) {
 return false;
 }
 return ret_val;
 */
