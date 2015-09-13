/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Simone
 */
public class ConfLoader {

    private HashMap<String, String> MaNGOSVersions;

    private String GitURLServer = "";
    private String GitBranchServer = "";
    private String GitFolderServer = "";
    private String GitURLDatabase = "";
    private String GitBranchDatabase = "";
    private String GitFolderDatabase = "";
    private String GitURLEluna = "";
    private String GitBranchEluna = "";
    private String GitFolderEluna = "";

    private String ProxyServer = "";
    private String ProxyPort = "";

    private String DatabaseServer = "";
    private String DatabasePort = "";
    private String DatabaseAdmin = "";
    private String DatabaseAdminPass = "";
    private String DatabaseUser = "";
    private String DatabaseUserPass = "";

    private String WorldDBName = "";
    private String CharDBName = "";
    private String RealmDBName = "";

    private String WorldFolder = "";
    private String CharFolder = "";
    private String RealmFolder = "";
    private String WorldFullDB = "";
    private String DatabaseSetupFolder = "";
    private String DatabaseUpdateFolder = "";
    private String WorldUpdRel = "";
    private String CharUpdRel = "";
    private String RealmUpdRel = "";
    private String WorldLoadDBName = "";
    private String CharLoadDBName = "";
    private String RealmLoadDBName = "";

    private HashMap<String, String> cmakeOptions;
    private String CMakeBuildFolder = "";
    private String CMakeRunFolder = "";
    private String OPENSSL_LIBRARIES = "";
    private String OPENSSL_INCLUDE_DIR = "";

    private String debugLevel = "";
    private String PathToMySQL = "";
    private String WinPathGit = "";
    private String Win32PathCMake = "";
    private String Win64PathCMake = "";

    private boolean confLoaded = false;

    private PropertiesEx prop;

    public ConfLoader() {
        try {
            //BufferedReader metadataReader = new BufferedReader(new InputStreamReader(new FileInputStream("config.properties")));
            prop = new PropertiesEx();
            //ClassLoader loader = Thread.currentThread().getContextClassLoader();
            //InputStream stream = new FileInputStream("config.properties");
            prop.load(new FileInputStream("config.properties"));
            //prop.load(new StringReader(IOUtils.getStringFromReader(metadataReader).replace("\\", "/")));

            MaNGOSVersions = prop.getPropertyArray("MaNGOS");
            //GitURLServer = prop.getProperty("GitURLServer", "");
            //GitBranchServer = prop.getProperty("GitBranchServer", "");
            GitFolderServer = prop.getProperty("GitFolderServer", "");
            //GitURLDatabase = prop.getProperty("GitURLDatabase", "");
            //GitBranchDatabase = prop.getProperty("GitBranchDatabase", "");
            GitFolderDatabase = prop.getProperty("GitFolderDatabase", "");
            GitURLEluna = prop.getProperty("GitURLEluna", "");
            GitBranchEluna = prop.getProperty("GitBranchEluna", "");
            GitFolderEluna = prop.getProperty("GitFolderEluna", "");

            ProxyServer = prop.getProperty("ProxyServer", "");
            ProxyPort = prop.getProperty("ProxyPort", "");

            DatabaseServer = prop.getProperty("DatabaseServer", "");
            DatabasePort = prop.getProperty("DatabasePort", "");
            DatabaseAdmin = prop.getProperty("DatabaseAdmin", "");
            DatabaseAdminPass = prop.getProperty("DatabaseAdminPass", "");
            DatabaseUser = prop.getProperty("DatabaseUser", "");
            DatabaseUserPass = prop.getProperty("DatabaseUserPass", "");

            WorldDBName = prop.getProperty("WorldDBName", "");
            CharDBName = prop.getProperty("CharDBName", "");
            RealmDBName = prop.getProperty("RealmDBName", "");

            WorldFolder = prop.getProperty("WorldFolder", "");
            CharFolder = prop.getProperty("CharFolder", "");
            RealmFolder = prop.getProperty("RealmFolder", "");
            WorldFullDB = prop.getProperty("WorldFullDB", "");
            DatabaseSetupFolder = prop.getProperty("DatabaseSetupFolder", "");
            DatabaseUpdateFolder = prop.getProperty("DatabaseUpdateFolder", "");
            WorldUpdRel = prop.getProperty("WorldUpdRel", "");
            CharUpdRel = prop.getProperty("CharUpdRel", "");
            RealmUpdRel = prop.getProperty("RealmUpdRel", "");
            WorldLoadDBName = prop.getProperty("WorldLoadDBName", "");
            CharLoadDBName = prop.getProperty("CharLoadDBName", "");
            RealmLoadDBName = prop.getProperty("RealmLoadDBName", "");

            cmakeOptions = prop.getPropertyArray("cmake");
            CMakeBuildFolder = prop.getProperty("CMakeBuildFolder", "");
            CMakeRunFolder = prop.getProperty("cmake.CMAKE_INSTALL_PREFIX", "");
            OPENSSL_LIBRARIES = prop.getProperty("cmake.OPENSSL_LIBRARIES", "");
            OPENSSL_INCLUDE_DIR = prop.getProperty("cmake.OPENSSL_INCLUDE_DIR", "");

            debugLevel = prop.getProperty("debugLevel", "");
            PathToMySQL = prop.getProperty("PathToMySQL", "");
            WinPathGit = prop.getProperty("WinPathGit", "");
            Win32PathCMake = prop.getProperty("Win32PathCMake", "");
            Win64PathCMake = prop.getProperty("Win64PathCMake", "");

            confLoaded = true;
        } catch (Exception ex) {
            confLoaded = false;
        }
    }

    public void getGitURLServer(String version) {
        GitURLServer = prop.getProperty("GitURLServer." + version);
    }

    public void getGitBranchServer(String version) {
        GitBranchServer = prop.getProperty("GitBranchServer." + version);
    }

    public void getGitURLDatabase(String version) {
        GitURLDatabase = prop.getProperty("GitURLDatabase." + version);
    }

    public void getGitBranchDatabase(String version) {
        GitBranchDatabase = prop.getProperty("GitBranchDatabase." + version);
    }

    /**
     * @return the GitURLServer
     */
    public String getGitURLServer() {
        return GitURLServer;
    }

    /**
     * @param GitURLServer the GitURLServer to set
     */
    public void setGitURLServer(String GitURLServer) {
        this.GitURLServer = GitURLServer;
    }

    /**
     * @return the GitBranchServer
     */
    public String getGitBranchServer() {
        return GitBranchServer;
    }

    /**
     * @param GitBranchServer the GitBranchServer to set
     */
    public void setGitBranchServer(String GitBranchServer) {
        this.GitBranchServer = GitBranchServer;
    }

    /**
     * @return the GitFolderServer
     */
    public String getGitFolderServer() {
        return GitFolderServer;
    }

    public void setGitFolderServer(String FolderServer) {
        this.GitFolderServer = FolderServer;
    }

    /**
     * @return the GitURLDatabase
     */
    public String getGitURLDatabase() {
        return GitURLDatabase;
    }

    /**
     * @param GitURLDatabase the GitURLDatabase to set
     */
    public void setGitURLDatabase(String GitURLDatabase) {
        this.GitURLDatabase = GitURLDatabase;
    }

    /**
     * @return the GitBranchDatabase
     */
    public String getGitBranchDatabase() {
        return GitBranchDatabase;
    }

    /**
     * @param GitBranchDatabase the GitBranchDatabase to set
     */
    public void setGitBranchDatabase(String GitBranchDatabase) {
        this.GitBranchDatabase = GitBranchDatabase;
    }

    /**
     * @return the GitFolderDatabase
     */
    public String getGitFolderDatabase() {
        return GitFolderDatabase;
    }

    /**
     * @param FolderDatabase the FolderDatabase to set
     */
    public void setGitFolderDatabase(String FolderDatabase) {
        this.GitFolderDatabase = FolderDatabase;
    }

    /**
     * @return the PathToMySQL
     */
    public String getPathToMySQL() {
        return PathToMySQL;
    }

    /**
     * @param WinPathMySQL the WinPathMySQL to set
     */
    public void setPathToMySQL(String WinPathMySQL) {
        this.PathToMySQL = WinPathMySQL;
    }

    /**
     * @return the WinPathGit
     */
    public String getWinPathGit() {
        return WinPathGit;
    }

    /**
     * @param WinPathGit the WinPathGit to set
     */
    public void setWinPathGit(String WinPathGit) {
        this.WinPathGit = WinPathGit;
    }

    /**
     * @return the confLoaded
     */
    public boolean isConfLoaded() {
        return confLoaded;
    }

    public String getGitURLEluna() {
        return GitURLEluna;
    }

    public void setGitURLEluna(String GitURLEluna) {
        this.GitURLEluna = GitURLEluna;
    }

    public String getGitBranchEluna() {
        return GitBranchEluna;
    }

    public void setGitBranchEluna(String GitBranchEluna) {
        this.GitBranchEluna = GitBranchEluna;
    }

    public String getGitFolderEluna() {
        return GitFolderEluna;
    }

    public void setGitFolderEluna(String GitFolderEluna) {
        this.GitFolderEluna = GitFolderEluna;
    }

    public String getProxyServer() {
        return ProxyServer;
    }

    public void setProxyServer(String ProxyServer) {
        this.ProxyServer = ProxyServer;
    }

    public String getProxyPort() {
        return ProxyPort;
    }

    public void setProxyPort(String ProxyPort) {
        this.ProxyPort = ProxyPort;
    }

    public String getDatabaseServer() {
        return DatabaseServer;
    }

    public void setDatabaseServer(String DatabaseServer) {
        this.DatabaseServer = DatabaseServer;
    }

    public String getDatabasePort() {
        return DatabasePort;
    }

    public void setDatabasePort(String DatabasePort) {
        this.DatabasePort = DatabasePort;
    }

    public String getDatabaseAdmin() {
        return DatabaseAdmin;
    }

    public void setDatabaseAdmin(String DatabaseAdmin) {
        this.DatabaseAdmin = DatabaseAdmin;
    }

    public String getDatabaseAdminPass() {
        return DatabaseAdminPass;
    }

    public void setDatabaseAdminPass(String DatabaseAdminPass) {
        this.DatabaseAdminPass = DatabaseAdminPass;
    }

    public String getDatabaseUser() {
        return DatabaseUser;
    }

    public void setDatabaseUser(String DatabaseUser) {
        this.DatabaseUser = DatabaseUser;
    }

    public String getDatabaseUserPass() {
        return DatabaseUserPass;
    }

    public void setDatabaseUserPass(String DatabaseUserPass) {
        this.DatabaseUserPass = DatabaseUserPass;
    }

    public String getWorldDBName() {
        return WorldDBName;
    }

    public void setWorldDBName(String WorldDBName) {
        this.WorldDBName = WorldDBName;
    }

    public String getCharDBName() {
        return CharDBName;
    }

    public void setCharDBName(String CharDBName) {
        this.CharDBName = CharDBName;
    }

    public String getRealmDBName() {
        return RealmDBName;
    }

    public void setRealmDBName(String RealmDBName) {
        this.RealmDBName = RealmDBName;
    }

    public String getWorldFolder() {
        return WorldFolder;
    }

    public void setWorldFolder(String WorldFolder) {
        this.WorldFolder = WorldFolder;
    }

    public String getCharFolder() {
        return CharFolder;
    }

    public void setCharFolder(String CharFolder) {
        this.CharFolder = CharFolder;
    }

    public String getRealmFolder() {
        return RealmFolder;
    }

    public void setRealmFolder(String RealmFolder) {
        this.RealmFolder = RealmFolder;
    }

    public String getDatabaseSetupFolder() {
        return DatabaseSetupFolder;
    }

    public void setDatabaseSetupFolder(String DatabaseSetupFolder) {
        this.DatabaseSetupFolder = DatabaseSetupFolder;
    }

    public String getUpdateFolder() {
        return DatabaseUpdateFolder;
    }

    public void setUpdateFolder(String UpdateFolder) {
        this.DatabaseUpdateFolder = UpdateFolder;
    }

    public String getWorldUpdRel() {
        return WorldUpdRel;
    }

    public void setWorldUpdRel(String WorldUpdRel) {
        this.WorldUpdRel = WorldUpdRel;
    }

    public String getCharUpdRel() {
        return CharUpdRel;
    }

    public void setCharUpdRel(String CharUpdRel) {
        this.CharUpdRel = CharUpdRel;
    }

    public String getRealmUpdRel() {
        return RealmUpdRel;
    }

    public void setRealmUpdRel(String RealmUpdRel) {
        this.RealmUpdRel = RealmUpdRel;
    }

    public String getWorldFullDB() {
        return WorldFullDB;
    }

    public void setWorldFullDB(String WorldFullDB) {
        this.WorldFullDB = WorldFullDB;
    }

    public String getDatabaseUpdateFolder() {
        return DatabaseUpdateFolder;
    }

    public void setDatabaseUpdateFolder(String DatabaseUpdateFolder) {
        this.DatabaseUpdateFolder = DatabaseUpdateFolder;
    }

    public String getWorldLoadDBName() {
        return WorldLoadDBName;
    }

    public void setWorldLoadDBName(String WorldLoadDBName) {
        this.WorldLoadDBName = WorldLoadDBName;
    }

    public String getCharLoadDBName() {
        return CharLoadDBName;
    }

    public void setCharLoadDBName(String CharLoadDBName) {
        this.CharLoadDBName = CharLoadDBName;
    }

    public String getRealmLoadDBName() {
        return RealmLoadDBName;
    }

    public void setRealmLoadDBName(String RealmLoadDBName) {
        this.RealmLoadDBName = RealmLoadDBName;
    }

    public int getDebugLevel() {
        try {
            return Integer.parseInt(debugLevel);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public void setDebugLevel(int debugLevel) {
        try {
            this.debugLevel = String.valueOf(debugLevel);
        } catch (Exception ex) {
            this.debugLevel = "0";
        }

    }

    public String getWin32PathCMake() {
        return Win32PathCMake;
    }

    public void setWin32PathCMake(String Win32PathCMake) {
        this.Win32PathCMake = Win32PathCMake;
    }

    public String getWin64PathCMake() {
        return Win64PathCMake;
    }

    public void setWin64PathCMake(String Win64PathCMake) {
        this.Win64PathCMake = Win64PathCMake;
    }

    public HashMap<String, String> getCmakeOptions() {
        return cmakeOptions;
    }

    public void setCmakeOptions(HashMap<String, String> cmakeOptions) {
        this.cmakeOptions = cmakeOptions;
    }

    public String getCMakeBuildFolder() {
        return CMakeBuildFolder;
    }

    public void setCMakeBuildFolder(String CMakeBuildFolder) {
        this.CMakeBuildFolder = CMakeBuildFolder;
    }

    public String getCMakeRunFolder() {
        return CMakeRunFolder;
    }

    public void setCMakeRunFolder(String CMakeRunFolder) {
        this.CMakeRunFolder = CMakeRunFolder;
    }

    /**
     * @return the OPENSSL_LIBRARIES
     */
    public String getOPENSSL_LIBRARIES() {
        return OPENSSL_LIBRARIES;
    }

    /**
     * @param OPENSSL_LIBRARIES the OPENSSL_LIBRARIES to set
     */
    public void setOPENSSL_LIBRARIES(String OPENSSL_LIBRARIES) {
        //prop.setProperty("cmake.OPENSSL_LIBRARIES", OPENSSL_LIBRARIES);
        this.cmakeOptions.put("cmake.OPENSSL_LIBRARIES", OPENSSL_LIBRARIES);
        this.OPENSSL_LIBRARIES = OPENSSL_LIBRARIES;
    }

    /**
     * @return the OPENSSL_INCLUDE_DIR
     */
    public String getOPENSSL_INCLUDE_DIR() {
        return OPENSSL_INCLUDE_DIR;
    }

    /**
     * @param OPENSSL_INCLUDE_DIR the OPENSSL_INCLUDE_DIR to set
     */
    public void setOPENSSL_INCLUDE_DIR(String OPENSSL_INCLUDE_DIR) {
        this.cmakeOptions.put("cmake.OPENSSL_INCLUDE_DIR", OPENSSL_INCLUDE_DIR);
        this.OPENSSL_INCLUDE_DIR = OPENSSL_INCLUDE_DIR;
    }

}
