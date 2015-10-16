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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Boni Simone <simo.boni@gmail.com>
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
    private HashMap<String, String> WorldUpdRel;
    private HashMap<String, String> CharUpdRel;
    private HashMap<String, String> RealmUpdRel;
    private String WorldLoadDBName = "";
    private String CharLoadDBName = "";
    private String RealmLoadDBName = "";

    private HashMap<String, String> cmakeOptions;
    private String CMakeBuildFolder = "";
    private String CMakeRunFolder = "";
    private String CMakeBuildType = "";
    private String OPENSSL_LIBRARIES = "";
    private String OPENSSL_INCLUDE_DIR = "";

    private String debugLevel = "";
    private String PathToMySQL = "";
    private String WinGitHubPath = "";
    private String WinGitExtPath = "";
    private String Win32PathCMake = "";
    private String Win64PathCMake = "";
    private String URLGit = "";
    private String URLMySQL = "";
    private String URLCMake = "";
    private String URLOpenSSL = "";

    private boolean confLoaded = false;

    private PropertiesEx prop;


    /**
     * Default constructor that load configuration from "config.properties"
     * file and set "confLoaded" variable with load status.
     */
    public ConfLoader() {
        try {
            this.prop = new PropertiesEx();
            this.prop.load(new FileInputStream("config.properties"));

            this.MaNGOSVersions = this.prop.getPropertyArray("MaNGOS");
            this.GitURLServer = this.prop.getProperty("GitURLServer.0", "");
            this.GitBranchServer = this.prop.getProperty("GitBranchServer.0", "");
            this.GitFolderServer = this.prop.getProperty("GitFolderServer", "");
            this.GitURLDatabase = this.prop.getProperty("GitURLDatabase.0", "");
            this.GitBranchDatabase = this.prop.getProperty("GitBranchDatabase.0", "");
            this.GitFolderDatabase = this.prop.getProperty("GitFolderDatabase", "");
            this.GitURLEluna = this.prop.getProperty("GitURLEluna.0", "");
            this.GitBranchEluna = this.prop.getProperty("GitBranchEluna.0", "");
            this.GitFolderEluna = this.prop.getProperty("GitFolderEluna", "");

            this.ProxyServer = this.prop.getProperty("ProxyServer", "");
            this.ProxyPort = this.prop.getProperty("ProxyPort", "");

            this.DatabaseServer = this.prop.getProperty("DatabaseServer", "");
            this.DatabasePort = this.prop.getProperty("DatabasePort", "");
            this.DatabaseAdmin = this.prop.getProperty("DatabaseAdmin", "");
            this.DatabaseAdminPass = this.prop.getProperty("DatabaseAdminPass", "");
            this.DatabaseUser = this.prop.getProperty("DatabaseUser", "");
            this.DatabaseUserPass = this.prop.getProperty("DatabaseUserPass", "");

            this.WorldDBName = this.prop.getProperty("WorldDBName", "");
            this.CharDBName = this.prop.getProperty("CharDBName", "");
            this.RealmDBName = this.prop.getProperty("RealmDBName", "");

            this.WorldFolder = this.prop.getProperty("WorldFolder", "");
            this.CharFolder = this.prop.getProperty("CharFolder", "");
            this.RealmFolder = this.prop.getProperty("RealmFolder", "");
            this.WorldFullDB = this.prop.getProperty("WorldFullDB", "");
            this.DatabaseSetupFolder = this.prop.getProperty("DatabaseSetupFolder", "");
            this.DatabaseUpdateFolder = this.prop.getProperty("DatabaseUpdateFolder", "");
            this.WorldUpdRel = this.prop.getPropertyArray("WorldUpdRel");
            this.CharUpdRel = this.prop.getPropertyArray("CharUpdRel");
            this.RealmUpdRel = this.prop.getPropertyArray("RealmUpdRel");
            this.WorldLoadDBName = this.prop.getProperty("WorldLoadDBName", "");
            this.CharLoadDBName = this.prop.getProperty("CharLoadDBName", "");
            this.RealmLoadDBName = this.prop.getProperty("RealmLoadDBName", "");

            this.cmakeOptions = this.prop.getPropertyArray("cmake");
            this.CMakeBuildFolder = this.prop.getProperty("CMakeBuildFolder", "");
            this.CMakeRunFolder = this.prop.getProperty("cmake.CMAKE_INSTALL_PREFIX", "");
            this.CMakeBuildType = this.prop.getProperty("cmake.CMAKE_BUILD_TYPE", "");
            this.OPENSSL_LIBRARIES = this.prop.getProperty("cmake.OPENSSL_LIBRARIES", "");
            this.OPENSSL_INCLUDE_DIR = this.prop.getProperty("cmake.OPENSSL_INCLUDE_DIR", "");

            this.debugLevel = this.prop.getProperty("debugLevel", "");
            this.PathToMySQL = this.prop.getProperty("PathToMySQL", "");
            this.WinGitHubPath = this.prop.getProperty("WinGitHubPath", "");
            this.WinGitExtPath = this.prop.getProperty("WinGitExtPath", "");
            this.Win32PathCMake = this.prop.getProperty("Win32PathCMake", "");
            this.Win64PathCMake = this.prop.getProperty("Win64PathCMake", "");
            this.URLGit = this.prop.getProperty("URLGit", "");
            this.URLMySQL = this.prop.getProperty("URLMySQL", "");
            this.URLCMake = this.prop.getProperty("URLCMake", "");
            this.URLOpenSSL = this.prop.getProperty("URLOpenSSL", "");

            this.confLoaded = true;
        } catch (IOException ex) {
            this.confLoaded = false;
        }
    }
    
    /**
     *
     * @return
     */
    public String getCMakeBuildFolder() {
        return this.CMakeBuildFolder;
    }

    /**
     *
     * @return
     */
    public String getCMakeBuildType() {
        return this.CMakeBuildType;
    }

    /**
     *
     * @return
     */
    public String getCMakeRunFolder() {
        return this.CMakeRunFolder;
    }

    /**
     *
     * @return
     */
    public String getCharDBName() {
        return this.CharDBName;
    }

    /**
     *
     * @return
     */
    public String getCharFolder() {
        return this.CharFolder;
    }

    /**
     *
     * @return
     */
    public String getCharLoadDBName() {
        return this.CharLoadDBName;
    }

    /**
     *
     * @return
     */
    public HashMap<String, String> getCharUpdRel() {
        return this.CharUpdRel;
    }

    /**
     *
     * @return
     */
    public HashMap<String, String> getCmakeOptions() {
        return this.cmakeOptions;
    }

    /**
     *
     * @return
     */
    public String getDatabaseAdmin() {
        return this.DatabaseAdmin;
    }

    /**
     *
     * @return
     */
    public String getDatabaseAdminPass() {
        return this.DatabaseAdminPass;
    }

    /**
     *
     * @return
     */
    public String getDatabasePort() {
        return this.DatabasePort;
    }

    /**
     *
     * @return
     */
    public String getDatabaseServer() {
        return this.DatabaseServer;
    }

    /**
     *
     * @return
     */
    public String getDatabaseSetupFolder() {
        return this.DatabaseSetupFolder;
    }

    /**
     *
     * @return
     */
    public String getDatabaseUpdateFolder() {
        return this.DatabaseUpdateFolder;
    }

    /**
     *
     * @return
     */
    public String getDatabaseUser() {
        return this.DatabaseUser;
    }

    /**
     *
     * @return
     */
    public String getDatabaseUserPass() {
        return this.DatabaseUserPass;
    }

    /**
     *
     * @return
     */
    public int getDebugLevel() {
        try {
            return Integer.parseInt(this.debugLevel);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /**
     *
     * @param version
     */
    public void getGitBranchDatabase(String version) {
        this.GitBranchDatabase = this.prop.getProperty("GitBranchDatabase." + version);
    }

    /**
     *
     * @return
     */
    public String getGitBranchDatabase() {
        return this.GitBranchDatabase;
    }

    /**
     *
     * @param version
     */
    public void getGitBranchEluna(String version) {
        this.GitBranchEluna = this.prop.getProperty("GitBranchEluna." + version);
    }

    /**
     *
     * @return
     */
    public String getGitBranchEluna() {
        return this.GitBranchEluna;
    }

    /**
     *
     * @param version
     */
    public void getGitBranchServer(String version) {
        this.GitBranchServer = this.prop.getProperty("GitBranchServer." + version);
    }

    /**
     * @return the GitBranchServer
     */
    public String getGitBranchServer() {
        return this.GitBranchServer;
    }

    /**
     *
     * @return
     */
    public String getGitFolderDatabase() {
        return this.GitFolderDatabase;
    }

    /**
     *
     * @return
     */
    public String getGitFolderEluna() {
        return this.GitFolderEluna;
    }

    /**
     * @return the GitFolderServer
     */
    public String getGitFolderServer() {
        return this.GitFolderServer;
    }

    /**
     *
     * @param version
     */
    public void getGitURLDatabase(String version) {
        this.GitURLDatabase = this.prop.getProperty("GitURLDatabase." + version);
    }

    /**
     * @return the GitURLDatabase
     */
    public String getGitURLDatabase() {
        return this.GitURLDatabase;
    }

    /**
     *
     * @param version
     */
    public void getGitURLEluna(String version) {
        this.GitURLEluna = this.prop.getProperty("GitURLEluna." + version);
    }

    /**
     *
     * @return
     */
    public String getGitURLEluna() {
        return this.GitURLEluna;
    }

    /**
     *
     * @param version
     */
    public void getGitURLServer(String version) {
        this.GitURLServer = this.prop.getProperty("GitURLServer." + version);
    }

    /**
     *
     * @return
     */
    public String getGitURLServer() {
        return this.GitURLServer;
    }

    /**
     *
     * @return
     */
    public HashMap<String, String> getMaNGOSVersions() {
        return this.MaNGOSVersions;
    }

    /**
     *
     * @return
     */
    public String getOPENSSL_INCLUDE_DIR() {
        return this.OPENSSL_INCLUDE_DIR;
    }

    /**
     *
     * @return
     */
    public String getOPENSSL_LIBRARIES() {
        return this.OPENSSL_LIBRARIES;
    }

    /**
     *
     * @return
     */
    public String getPathToMySQL() {
        return this.PathToMySQL;
    }

    /**
     *
     * @return
     */
    public String getProxyPort() {
        return this.ProxyPort;
    }

    /**
     *
     * @return
     */
    public String getProxyServer() {
        return this.ProxyServer;
    }

    /**
     *
     * @return
     */
    public String getRealmDBName() {
        return this.RealmDBName;
    }

    /**
     *
     * @return
     */
    public String getRealmFolder() {
        return this.RealmFolder;
    }

    /**
     *
     * @return
     */
    public String getRealmLoadDBName() {
        return this.RealmLoadDBName;
    }

    /**
     *
     * @return
     */
    public HashMap<String, String> getRealmUpdRel() {
        return this.RealmUpdRel;
    }

    /**
     *
     * @return
     */
    public String getURLCMake() {
        return this.URLCMake;
    }

    /**
     *
     * @return
     */
    public String getURLGit() {
        return this.URLGit;
    }

    /**
     *
     * @return
     */
    public String getURLMySQL() {
        return this.URLMySQL;
    }

    /**
     *
     * @return
     */
    public String getURLOpenSSL() {
        return this.URLOpenSSL;
    }

    /**
     *
     * @return
     */
    public String getUpdateFolder() {
        return this.DatabaseUpdateFolder;
    }

    /**
     *
     * @return
     */
    public String getWin32PathCMake() {
        return this.Win32PathCMake;
    }

    /**
     *
     * @return
     */
    public String getWin64PathCMake() {
        return this.Win64PathCMake;
    }

    /**
     *
     * @return
     */
    public String getWinGitExtPath() {
        return this.WinGitExtPath;
    }

    /**
     *
     * @return
     */
    public String getWinGitHubPath() {
        return this.WinGitHubPath;
    }

    /**
     *
     * @return
     */
    public String getWorldDBName() {
        return this.WorldDBName;
    }

    /**
     *
     * @return
     */
    public String getWorldFolder() {
        return this.WorldFolder;
    }

    /**
     *
     * @return
     */
    public String getWorldFullDB() {
        return this.WorldFullDB;
    }

    /**
     *
     * @return
     */
    public String getWorldLoadDBName() {
        return this.WorldLoadDBName;
    }

    /**
     *
     * @return
     */
    public HashMap<String, String> getWorldUpdRel() {
        return this.WorldUpdRel;
    }

    /**
     *
     * @return
     */
    public boolean isConfLoaded() {
        return this.confLoaded;
    }

    /**
     *
     * @param CMakeBuildFolder
     */
    public void setCMakeBuildFolder(String CMakeBuildFolder) {
        this.CMakeBuildFolder = CMakeBuildFolder;
    }

    /**
     *
     * @param CMakeBuildType
     */
    public void setCMakeBuildType(String CMakeBuildType) {
        this.CMakeBuildType = CMakeBuildType;
    }

    /**
     *
     * @param CMakeRunFolder
     */
    public void setCMakeRunFolder(String CMakeRunFolder) {
        this.CMakeRunFolder = CMakeRunFolder;
    }

    /**
     *
     * @param CharDBName
     */
    public void setCharDBName(String CharDBName) {
        this.CharDBName = CharDBName;
    }

    /**
     *
     * @param CharFolder
     */
    public void setCharFolder(String CharFolder) {
        this.CharFolder = CharFolder;
    }

    /**
     *
     * @param CharLoadDBName
     */
    public void setCharLoadDBName(String CharLoadDBName) {
        this.CharLoadDBName = CharLoadDBName;
    }

    /**
     *
     * @param CharUpdRel
     */
    public void setCharUpdRel(HashMap<String, String> CharUpdRel) {
        this.CharUpdRel = CharUpdRel;
    }

    /**
     *
     * @param cmakeOptions
     */
    public void setCmakeOptions(HashMap<String, String> cmakeOptions) {
        this.cmakeOptions = cmakeOptions;
    }

    /**
     *
     * @param DatabaseAdmin
     */
    public void setDatabaseAdmin(String DatabaseAdmin) {
        this.DatabaseAdmin = DatabaseAdmin;
    }

    /**
     *
     * @param DatabaseAdminPass
     */
    public void setDatabaseAdminPass(String DatabaseAdminPass) {
        this.DatabaseAdminPass = DatabaseAdminPass;
    }

    /**
     *
     * @param DatabasePort
     */
    public void setDatabasePort(String DatabasePort) {
        this.DatabasePort = DatabasePort;
    }

    /**
     *
     * @param DatabaseServer
     */
    public void setDatabaseServer(String DatabaseServer) {
        this.DatabaseServer = DatabaseServer;
    }

    /**
     *
     * @param DatabaseSetupFolder
     */
    public void setDatabaseSetupFolder(String DatabaseSetupFolder) {
        this.DatabaseSetupFolder = DatabaseSetupFolder;
    }

    /**
     *
     * @param DatabaseUpdateFolder
     */
    public void setDatabaseUpdateFolder(String DatabaseUpdateFolder) {
        this.DatabaseUpdateFolder = DatabaseUpdateFolder;
    }

    /**
     *
     * @param DatabaseUser
     */
    public void setDatabaseUser(String DatabaseUser) {
        this.DatabaseUser = DatabaseUser;
    }

    /**
     *
     * @param DatabaseUserPass
     */
    public void setDatabaseUserPass(String DatabaseUserPass) {
        this.DatabaseUserPass = DatabaseUserPass;
    }

    /**
     *
     * @param debugLevel
     */
    public void setDebugLevel(int debugLevel) {
        try {
            this.debugLevel = String.valueOf(debugLevel);
        } catch (Exception ex) {
            this.debugLevel = "0";
        }

    }

    /**
     *
     * @param GitBranchDatabase
     */
    public void setGitBranchDatabase(String GitBranchDatabase) {
        this.GitBranchDatabase = GitBranchDatabase;
    }

    /**
     *
     * @param GitBranchEluna
     */
    public void setGitBranchEluna(String GitBranchEluna) {
        this.GitBranchEluna = GitBranchEluna;
    }

    /**
     *
     * @param GitBranchServer
     */
    public void setGitBranchServer(String GitBranchServer) {
        this.GitBranchServer = GitBranchServer;
    }

    /**
     *
     * @param FolderDatabase
     */
    public void setGitFolderDatabase(String FolderDatabase) {
        this.GitFolderDatabase = FolderDatabase;
    }

    /**
     *
     * @param GitFolderEluna
     */
    public void setGitFolderEluna(String GitFolderEluna) {
        this.GitFolderEluna = GitFolderEluna;
    }

    /**
     *
     * @param FolderServer
     */
    public void setGitFolderServer(String FolderServer) {
        this.GitFolderServer = FolderServer;
    }

    /**
     *
     * @param GitURLDatabase
     */
    public void setGitURLDatabase(String GitURLDatabase) {
        this.GitURLDatabase = GitURLDatabase;
    }

    /**
     *
     * @param GitURLEluna
     */
    public void setGitURLEluna(String GitURLEluna) {
        this.GitURLEluna = GitURLEluna;
    }

    /**
     *
     * @param GitURLServer
     */
    public void setGitURLServer(String GitURLServer) {
        this.GitURLServer = GitURLServer;
    }

    /**
     *
     * @param MaNGOSVersions
     */
    public void setMaNGOSVersions(HashMap<String, String> MaNGOSVersions) {
        this.MaNGOSVersions = MaNGOSVersions;
    }

    /**
     *
     * @param OPENSSL_INCLUDE_DIR
     */
    public void setOPENSSL_INCLUDE_DIR(String OPENSSL_INCLUDE_DIR) {
        this.cmakeOptions.put("cmake.OPENSSL_INCLUDE_DIR", OPENSSL_INCLUDE_DIR);
        this.OPENSSL_INCLUDE_DIR = OPENSSL_INCLUDE_DIR;
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
     *
     * @param WinPathMySQL
     */
    public void setPathToMySQL(String WinPathMySQL) {
        this.PathToMySQL = WinPathMySQL;
    }

    /**
     *
     * @param ProxyPort
     */
    public void setProxyPort(String ProxyPort) {
        this.ProxyPort = ProxyPort;
    }

    /**
     *
     * @param ProxyServer
     */
    public void setProxyServer(String ProxyServer) {
        this.ProxyServer = ProxyServer;
    }

    /**
     *
     * @param RealmDBName
     */
    public void setRealmDBName(String RealmDBName) {
        this.RealmDBName = RealmDBName;
    }

    /**
     *
     * @param RealmFolder
     */
    public void setRealmFolder(String RealmFolder) {
        this.RealmFolder = RealmFolder;
    }

    /**
     *
     * @param RealmLoadDBName
     */
    public void setRealmLoadDBName(String RealmLoadDBName) {
        this.RealmLoadDBName = RealmLoadDBName;
    }

    /**
     *
     * @param RealmUpdRel
     */
    public void setRealmUpdRel(HashMap<String, String> RealmUpdRel) {
        this.RealmUpdRel = RealmUpdRel;
    }

    /**
     *
     * @param URLCMake
     */
    public void setURLCMake(String URLCMake) {
        this.URLCMake = URLCMake;
    }

    /**
     *
     * @param URLGit
     */
    public void setURLGit(String URLGit) {
        this.URLGit = URLGit;
    }

    /**
     *
     * @param URLMySQL
     */
    public void setURLMySQL(String URLMySQL) {
        this.URLMySQL = URLMySQL;
    }

    /**
     *
     * @param URLOpenSSL
     */
    public void setURLOpenSSL(String URLOpenSSL) {
        this.URLOpenSSL = URLOpenSSL;
    }

    /**
     *
     * @param UpdateFolder
     */
    public void setUpdateFolder(String UpdateFolder) {
        this.DatabaseUpdateFolder = UpdateFolder;
    }

    /**
     *
     * @param Win32PathCMake
     */
    public void setWin32PathCMake(String Win32PathCMake) {
        this.Win32PathCMake = Win32PathCMake;
    }

    /**
     *
     * @param Win64PathCMake
     */
    public void setWin64PathCMake(String Win64PathCMake) {
        this.Win64PathCMake = Win64PathCMake;
    }

    /**
     *
     * @param WinGitExtPath
     */
    public void setWinGitExtPath(String WinGitExtPath) {
        this.WinGitExtPath = WinGitExtPath;
    }

    /**
     *
     * @param WinPathGit
     */
    public void setWinGitHubPath(String WinPathGit) {
        this.WinGitHubPath = WinPathGit;
    }

    /**
     *
     * @param WorldDBName
     */
    public void setWorldDBName(String WorldDBName) {
        this.WorldDBName = WorldDBName;
    }

    /**
     *
     * @param WorldFolder
     */
    public void setWorldFolder(String WorldFolder) {
        this.WorldFolder = WorldFolder;
    }

    /**
     *
     * @param WorldFullDB
     */
    public void setWorldFullDB(String WorldFullDB) {
        this.WorldFullDB = WorldFullDB;
    }

    /**
     *
     * @param WorldLoadDBName
     */
    public void setWorldLoadDBName(String WorldLoadDBName) {
        this.WorldLoadDBName = WorldLoadDBName;
    }

    /**
     *
     * @param WorldUpdRel
     */
    public void setWorldUpdRel(HashMap<String, String> WorldUpdRel) {
        this.WorldUpdRel = WorldUpdRel;
    }

}
