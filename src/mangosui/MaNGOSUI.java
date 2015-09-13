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
public class MaNGOSUI {

    private static boolean gitOk = false;
    private static boolean mysqlOk = false;
    private static String mySQLPath = "";
    private static boolean cmakeOk = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        if (true) { //args.length > 0 && args[0].equalsIgnoreCase("-c")) {
            System.out.println("MaNGOS Universal Installer: console mode.\n");

            System.out.print("Loading configuration file...");
            ConfLoader confLoader = new ConfLoader();
            if (confLoader.isConfLoaded()) {
                System.out.println(" Done!\n");
            } else {
                System.out.println(" FAILED! Check conf.property file.\n");
                return;
            }

            CommandManager cmdManager = new CommandManager();
            cmdManager.setDebugLevel(confLoader.getDebugLevel());
            if (cmdManager.getCURR_OS() > 0) {

                System.out.println("INFO: Your OS is: " + cmdManager.getOsName() + " version " + cmdManager.getOsVersion() + " with java architecture: " + cmdManager.getOsArch() + "\n");

                String input;
                boolean optGitSrvInstall = false;
                boolean optGitSrvWipe = false;
                boolean optGitSrvUpdate = false;
                boolean optGitDBInstall = false;
                boolean optGitDBWipe = false;
                boolean optGitDBUpdate = false;
                boolean optGitElunaInstall = false;
                boolean optGitElunaWipe = false;
                boolean optGitElunaUpdate = false;
                String databaseFolder = "";
                String serverFolder = "";

                // Version selection
                System.out.println("\nSelect wich version of MaNGOS install:");
                System.out.println("0 - MaNGOS Zero");
                System.out.println("1 - MaNGOS One");
                System.out.println("2 - MaNGOS Two");
                System.out.println("3 - MaNGOS Three");
                System.out.println("4 - MaNGOS Four");
                System.out.print("Version ? [0-4, default:0] ");
                input = System.console().readLine();
                confLoader.getGitURLServer(input);
                confLoader.getGitURLDatabase(input);
                confLoader.getGitBranchServer(input);
                confLoader.getGitBranchDatabase(input);
                /**
                 * **** Git download *****
                 */
                System.out.print("\nDo you want to download source from Git repositories? [y/n, default:n] ");
                input = System.console().readLine();
                if ("y".equalsIgnoreCase(input)) {
                    System.out.println("Checking Git installation... ");
                    if (cmdManager.getCURR_OS() == cmdManager.WINDOWS && !cmdManager.isPSScriptEnabled()) {
                        System.out.println("WARNING: PowerShell script execution is not ebabled. To enable it run PS (x86) as Administrator and use \"Set-ExecutionPolicy Unrestricted\" command.");
                    }
                    cmdManager.setWinGitPath(confLoader.getWinPathGit());
                    if (!cmdManager.checkGit(null)) {
                        System.out.println("ERROR: Git commands not found on system. Check Git installation or manually download repositories.");
                    } else {
                        gitOk = true;
                        System.out.println("INFO: Founded Git commands.");
                    }

                    if (gitOk) {
                        System.out.println("\n\nProxy configuration:");
                        System.out.println("Server name: " + confLoader.getProxyServer());
                        System.out.println("Server port: " + confLoader.getProxyPort());
                        System.out.print("\nDo you want to use a proxy server with this parameter? [y/n, default:n] ");
                        input = System.console().readLine();
                        if (!"y".equalsIgnoreCase(input)) {
                            confLoader.setProxyPort("");
                            confLoader.setProxyServer("");
                        }
                        // Show server git param
                        System.out.println("\n\n*** Server download parameters:");
                        System.out.println("URL                          : " + confLoader.getGitURLServer());
                        System.out.println("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderServer());
                        System.out.println("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchServer());
                        System.out.print("\nDo you want to change theese parameters? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            confLoader.setGitURLServer(readNewParam("URL", confLoader.getGitURLServer()));
                            confLoader.setGitFolderServer(readNewParam("DESTINATION FOLDER", confLoader.getGitFolderServer()));
                            confLoader.setGitBranchServer(readNewParam("REPOSITORY BRANCH", confLoader.getGitBranchServer()));
                            System.out.println("\n\n*** NEW Server download parameters:");
                            System.out.println("URL                          : " + confLoader.getGitURLServer());
                            System.out.println("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderServer());
                            System.out.println("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchServer());
                        }
                        // Check server folder
                        serverFolder = confLoader.getGitFolderServer().isEmpty() ? "server" : confLoader.getGitFolderServer();
                        if (cmdManager.checkFolder(serverFolder)) {
                            optGitSrvWipe = true;
                            // Check server version for update
                            if (!cmdManager.isRepoUpToDate(serverFolder)) {
                                optGitSrvUpdate = true;
                            }
                        } else {
                            optGitSrvInstall = true;
                        }
                        // Ask what to do (first clone, wipe and clone, checkout and update) for server
                        System.out.println("\n*** Server download option avaiable:");
                        if (optGitSrvInstall) {
                            System.out.println("N - New download.");
                        }
                        if (optGitSrvWipe) {
                            System.out.println("W - Wipe current and download again.");
                        }
                        if (optGitSrvUpdate) {
                            System.out.println("U - Update local server.");
                        }
                        System.out.println("Empty for no action.");
                        System.out.print("\nInsert action: ");
                        input = System.console().readLine();
                        if (input.equalsIgnoreCase("N")) {
                            System.out.println("Downloading new server repository...");
                            if (cmdManager.gitDownload(confLoader.getGitURLServer(), serverFolder, confLoader.getGitBranchServer(), confLoader.getProxyServer(), confLoader.getProxyPort(), null)) {
                                System.out.println("\nDone.");
                            } else {
                                System.out.println("\nERROR: Check console output and redo process with W (wipe) option.");
                            }
                        } else if (input.equalsIgnoreCase("W")) {
                            System.out.print("Wiping current server folder...");
                            if (cmdManager.deleteFolder(serverFolder)) {
                                System.out.println("Done.");
                                System.out.println("Downloading server repository...");
                                if (cmdManager.gitDownload(confLoader.getGitURLServer(), serverFolder, confLoader.getGitBranchServer(), confLoader.getProxyServer(), confLoader.getProxyPort(), null)) {
                                    System.out.println("\nDone.");
                                } else {
                                    System.out.println("\nERROR: Check console output and redo process with W (wipe) option.");
                                }
                            }
                        } else if (input.equalsIgnoreCase("U")) {
                            System.out.println("Updating current server folder...");
                            if (cmdManager.gitCheckout(null)) {
                                System.out.println("\nDone.");
                            } else {
                                System.out.println("\nERROR: Check console output.");
                            }
                        } else {
                            System.out.println("No action selected. Download skipped");
                        }

                        // Show database git param
                        System.out.println("\n\n*** Database download parameters:");
                        System.out.println("URL                          : " + confLoader.getGitURLDatabase());
                        System.out.println("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderDatabase());
                        System.out.println("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchDatabase());
                        System.out.print("\nDo you want to change theese parameters? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            confLoader.setGitURLDatabase(readNewParam("URL", confLoader.getGitURLDatabase()));
                            confLoader.setGitFolderDatabase(readNewParam("DESTINATION FOLDER", confLoader.getGitFolderDatabase()));
                            confLoader.setGitBranchDatabase(readNewParam("REPOSITORY BRANCH", confLoader.getGitBranchDatabase()));
                            System.out.println("\n\n*** NEW Database download parameters:");
                            System.out.println("URL                          : " + confLoader.getGitURLDatabase());
                            System.out.println("(Optional) DESTINATION FOLDER: " + confLoader.getGitFolderDatabase());
                            System.out.println("(Optional) REPOSITORY BRANCH : " + confLoader.getGitBranchDatabase());
                        }
                        // Check database folder
                        databaseFolder = confLoader.getGitFolderDatabase().isEmpty() ? "database" : confLoader.getGitFolderDatabase();
                        if (cmdManager.checkFolder(databaseFolder)) {
                            optGitDBWipe = true;
                            // Check database version for update
                            if (!cmdManager.isRepoUpToDate(databaseFolder)) {
                                optGitDBUpdate = true;
                            }
                        } else {
                            optGitDBInstall = true;
                        }
                        // Ask what to do (first clone, wipe and clone, checkout and update) for database
                        System.out.println("\n*** Database download option avaiable:");
                        if (optGitDBInstall) {
                            System.out.println("N - New download.");
                        }
                        if (optGitDBWipe) {
                            System.out.println("W - Wipe current and download again.");
                        }
                        if (optGitDBUpdate) {
                            System.out.println("U - Update local database.");
                        }
                        System.out.println("Empty for no action.");
                        System.out.print("\nInsert action: ");
                        input = System.console().readLine();
                        if (input.equalsIgnoreCase("N")) {
                            System.out.println("Downloading new database repository...");
                            if (cmdManager.gitDownload(confLoader.getGitURLDatabase(), databaseFolder, confLoader.getGitBranchDatabase(), confLoader.getProxyServer(), confLoader.getProxyPort(), null)) {
                                System.out.println("\nDone.");
                            } else {
                                System.out.println("\nERROR: Check console output and redo process with W (wipe) option.");
                            }
                        } else if (input.equalsIgnoreCase("W")) {
                            System.out.print("Wiping current database folder...");
                            if (cmdManager.deleteFolder(databaseFolder)) {
                                System.out.println("Done.");
                                System.out.println("Downloading database repository...");
                                if (cmdManager.gitDownload(confLoader.getGitURLDatabase(), databaseFolder, confLoader.getGitBranchDatabase(), confLoader.getProxyServer(), confLoader.getProxyPort(), null)) {
                                    System.out.println("\nDone.");
                                } else {
                                    System.out.println("\nERROR: Check console output and redo process with W (wipe) option.");
                                }
                            }
                        } else if (input.equalsIgnoreCase("U")) {
                            System.out.println("Updating current database folder...");
                            if (cmdManager.gitCheckout(null)) {
                                System.out.println("\nDone.");
                            } else {
                                System.out.println("\nERROR: Check console output.");
                            }
                        } else {
                            System.out.println("No action selected. Download skipped");
                        }

                        // Show eluna git param
                        // Check eluna folder
                        // Check eluna version for update
                        // Ask what to do (first clone, wipe and clone, checkout and update) for eluna
                    } else {
                        System.out.println("\n*** Download operation skipped. Git command not ready.");
                    }
                }

                /**
                 * **** Database install *****
                 */
                System.out.print("\nDo you want to install databases? [y/n, default:n] ");
                input = System.console().readLine();
                if ("y".equalsIgnoreCase(input)) {
                    System.out.println("Checking MySQL installation... ");
                    String mysqlToolPath = "";
                    if (!cmdManager.checkMySQL(null)) {
                        System.out.println("INFO: MySQL is not locally installed... checking for mysql.exe tool.");
                        mysqlToolPath = "database" + File.separator + confLoader.getPathToMySQL();
                        if (!cmdManager.checkMySQL(mysqlToolPath, null)) {
                            System.out.println("WARNING: mysql.exe command not found on system. Check mysql installation or path '" + mysqlToolPath + "' to mysql.exe into database folder.");
                        } else {
                            mysqlOk = true;
                            mySQLPath = mysqlToolPath;
                            System.out.println("INFO: Founded MySQL commands.");
                        }
                    } else {
                        mysqlOk = true;
                        System.out.println("INFO: Founded MySQL commands.");
                    }

                    if (mysqlOk) {
                        System.out.println("\n\n*** Database server installation parameters:");
                        System.out.println("SERVER     : " + confLoader.getDatabaseServer());
                        System.out.println("PORT       : " + confLoader.getDatabasePort());
                        System.out.println("ADMIN USER : " + confLoader.getDatabaseAdmin());
                        System.out.println("ADMIN PASS : " + confLoader.getDatabaseAdminPass());
                        System.out.println("DB USER    : " + confLoader.getDatabaseUser());
                        System.out.println("DB PASS    : " + confLoader.getDatabaseUserPass());
                        System.out.print("\nDo you want to change theese parameters? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            confLoader.setDatabaseServer(readNewParam("SERVER", confLoader.getDatabaseServer()));
                            confLoader.setDatabasePort(readNewParam("PORT", confLoader.getDatabasePort()));
                            confLoader.setDatabaseAdmin(readNewParam("ADMIN USER", confLoader.getDatabaseAdmin()));
                            confLoader.setDatabaseAdminPass(readNewParam("ADMIN PASS", confLoader.getDatabaseAdminPass()));
                            confLoader.setDatabaseUser(readNewParam("DB USER", confLoader.getDatabaseUser()));
                            confLoader.setDatabaseUserPass(readNewParam("DB PASS", confLoader.getDatabaseUserPass()));
                            System.out.println("\n\n*** NEW Database server installation parameters:");
                            System.out.println("SERVER     : " + confLoader.getDatabaseServer());
                            System.out.println("PORT       : " + confLoader.getDatabasePort());
                            System.out.println("ADMIN USER : " + confLoader.getDatabaseAdmin());
                            System.out.println("ADMIN PASS : " + confLoader.getDatabaseAdminPass());
                            System.out.println("DB USER    : " + confLoader.getDatabaseUser());
                            System.out.println("DB PASS    : " + confLoader.getDatabaseUserPass());
                        }
                        System.out.println("\n\n*** Database install configuration:");
                        System.out.println("WORLD DB    : " + confLoader.getWorldDBName());
                        System.out.println("CHAR DB     : " + confLoader.getCharDBName());
                        System.out.println("REALM DB    : " + confLoader.getRealmDBName());
                        System.out.println("UPD RELEASE : " + confLoader.getWorldUpdRel());
                        System.out.print("\nDo you want to change theese parameters? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            confLoader.setWorldDBName(readNewParam("WORLD DB", confLoader.getWorldDBName()));
                            confLoader.setCharDBName(readNewParam("CHAR DB", confLoader.getCharDBName()));
                            confLoader.setRealmDBName(readNewParam("REALM DB", confLoader.getRealmDBName()));
                            confLoader.setWorldUpdRel(readNewParam("UPD RELEASE", confLoader.getWorldUpdRel()));
                            System.out.println("\n\n*** NEW Database install configuration:");
                            System.out.println("WORLD DB    : " + confLoader.getWorldDBName());
                            System.out.println("CHAR DB     : " + confLoader.getCharDBName());
                            System.out.println("REALM DB    : " + confLoader.getRealmDBName());
                            System.out.println("UPD RELEASE : " + confLoader.getWorldUpdRel());
                        }

                        if (!optGitDBInstall && !optGitDBUpdate) {
                            // Reset after previous check done.
                            optGitDBWipe = false;
                            System.out.println("\n\n*** Database installation option avaiable:");
                            System.out.println("N - New install.");
                            System.out.println("W - Wipe current and install again.");
                            System.out.println("U - Update database.");
                            System.out.println("Empty for no action.");
                            System.out.print("\nInsert action: ");
                            input = System.console().readLine();
                            if (input.equalsIgnoreCase("N")) {
                                optGitDBInstall = true;
                            } else if (input.equalsIgnoreCase("W")) {
                                optGitDBWipe = true;
                            } else if (input.equalsIgnoreCase("U")) {
                                optGitDBUpdate = true;
                            }
                        }
                        if (optGitDBInstall || optGitDBWipe) {
                            System.out.print("\nIs your first DB installation: [y/n, default:n] ");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                System.out.println("Installing new database...");
                                cmdManager.createDB(confLoader, null);
                            }

                            System.out.print("\nDo you want to wipe (if already installed) and install Realm database? [y/n, default:n] ");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                // Installing Realm database
                                String setupPath = databaseFolder + File.separator
                                        + confLoader.getRealmFolder() + File.separator
                                        + confLoader.getDatabaseSetupFolder() + File.separator
                                        + confLoader.getRealmLoadDBName();
                                System.out.println("Creating Realm database from: " + setupPath);
                                boolean dbRet = cmdManager.loadDB(confLoader, confLoader.getRealmDBName(), setupPath, null);
                                if (dbRet) {
                                    System.out.println("Done");
                                } else {
                                    System.out.println("ERROR: check console log.");
                                }
                                String updatePath = databaseFolder + File.separator
                                        + confLoader.getRealmFolder() + File.separator
                                        + confLoader.getDatabaseUpdateFolder() + File.separator
                                        + confLoader.getRealmUpdRel();
                                System.out.println("Importing Realm updates from: " + updatePath);
                                dbRet = cmdManager.loadDBUpdate(confLoader, confLoader.getRealmDBName(), updatePath, null);
                                if (dbRet) {
                                    System.out.println("Done");
                                } else {
                                    System.out.println("ERROR: check console log.");
                                }
                            }

                            System.out.print("\nDo you want to wipe (if already installed) and install Character database? [y/n, default:n] ");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                // Installing Realm database
                                String setupPath = databaseFolder + File.separator
                                        + confLoader.getCharFolder() + File.separator
                                        + confLoader.getDatabaseSetupFolder() + File.separator
                                        + confLoader.getCharLoadDBName();
                                System.out.println("Creating Character database from: " + setupPath);
                                boolean dbRet = cmdManager.loadDB(confLoader, confLoader.getCharDBName(), setupPath, null);
                                if (dbRet) {
                                    System.out.println("Done");
                                } else {
                                    System.out.println("ERROR: check console log.");
                                }
                                String updatePath = databaseFolder + File.separator
                                        + confLoader.getCharFolder() + File.separator
                                        + confLoader.getDatabaseUpdateFolder() + File.separator
                                        + confLoader.getCharUpdRel();
                                System.out.println("Importing Character updates from: " + updatePath);
                                dbRet = cmdManager.loadDBUpdate(confLoader, confLoader.getCharDBName(), updatePath, null);
                                if (dbRet) {
                                    System.out.println("Done");
                                } else {
                                    System.out.println("ERROR: check console log.");
                                }
                            }

                            System.out.print("\nDo you want to wipe (if already installed) and install World database? [y/n, default:n] ");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                // Installing Realm database
                                String setupPath = databaseFolder + File.separator
                                        + confLoader.getWorldFolder() + File.separator
                                        + confLoader.getDatabaseSetupFolder() + File.separator
                                        + confLoader.getWorldLoadDBName();
                                System.out.println("Creating World database from: " + setupPath);
                                boolean dbRet = cmdManager.loadDB(confLoader, confLoader.getWorldDBName(), setupPath, null);
                                if (dbRet) {
                                    System.out.println("Done");
                                } else {
                                    System.out.println("ERROR: check console log.");
                                }
                                String updatePath = databaseFolder + File.separator
                                        + confLoader.getWorldFolder() + File.separator
                                        + confLoader.getDatabaseSetupFolder() + File.separator
                                        + confLoader.getWorldFullDB();
                                System.out.println("Importing World data from: " + updatePath);
                                dbRet = cmdManager.loadDBUpdate(confLoader, confLoader.getWorldDBName(), updatePath, null);
                                if (dbRet) {
                                    System.out.println("Done");
                                } else {
                                    System.out.println("ERROR: check console log.");
                                }
                                updatePath = databaseFolder + File.separator
                                        + confLoader.getWorldFolder() + File.separator
                                        + confLoader.getDatabaseUpdateFolder() + File.separator
                                        + confLoader.getWorldUpdRel();
                                System.out.println("Importing World updates from: " + updatePath);
                                dbRet = cmdManager.loadDBUpdate(confLoader, confLoader.getWorldDBName(), updatePath, null);
                                if (dbRet) {
                                    System.out.println("Done");
                                } else {
                                    System.out.println("ERROR: check console log.");
                                }
                            }
                        } else if (optGitDBUpdate) {
                        }
                    } else {
                        System.out.println("\n*** Database operation skipped. MySQL command not ready.");
                    }
                }
                /**
                 * **** Server compile and install *****
                 */
                System.out.print("\nDo you want to build and install serve sources? [y/n, default:n] ");
                input = System.console().readLine();
                if ("y".equalsIgnoreCase(input)) {
                    System.out.println("Checking Cmake installation... ");
                    if (!cmdManager.checkCMAKE(null)) {
                        System.out.println("INFO: CMAKE is not installed into PATH/shell environment... checking for cmake.exe installation folder.");
                        String cmake32Path = confLoader.getWin32PathCMake();
                        String cmake64Path = confLoader.getWin64PathCMake();
                        if (cmdManager.checkCMAKE(cmake32Path, null)) {
                            cmakeOk = true;
                            System.out.println("INFO: Founded CMAKE commands on x86 system.");
                        } else if (cmdManager.checkCMAKE(cmake64Path, null)) {
                            cmakeOk = true;
                            System.out.println("INFO: Founded CMAKE commands on x64 system.");
                        } else {
                            System.out.println("ERROR: CMAKE commands not found on system. Check CMAKE installation!.");
                        }
                    } else {
                        // cmakePath =
                        cmakeOk = true;
                        System.out.println("INFO: Founded CMAKE commands.");
                    }
                    if (cmakeOk) {
                        if (cmdManager.checkMySQLLib("", null).isEmpty() || cmdManager.checkMySQLInclude("", null).isEmpty()) {
                            System.out.println("ERROR: MySQL library for CMAKE was not found on system. Do you want to use portable version?");
                            System.out.println("WARNING: Portable MySQL library is for 5.6 version. Use this for other MySQL version can be insecure. [y/n, default:n] ");
                            System.out.print("Do you want to install MySQL portable library? [y/n, default:n] ");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                cmakeOk = cmdManager.installMySQLPortable(null);
                                if (!cmakeOk) {
                                    System.out.println("ERROR: MySQL library for CMAKE not installed. Try again running as Administrator!");
                                }
                            } else {
                                cmakeOk = false;
                            }
                        }

                        /*if (cmdManager.checkOpenSSLLib("", null).isEmpty() || cmdManager.checkOpenSSLInclude("", null).isEmpty()) {
                            System.out.println("ERROR: OpenSSL library for CMAKE was not found on system. Do you want to use portable version?");
                            System.out.println("WARNING: Portable OpenSSL library is for 1.0.2d 32bit version. Use this version can be insecure. [y/n, default:n] ");
                            System.out.print("Do you want to istall OpenSSL portable library? [y/n, default:n] ");
                            input = System.console().readLine();
                            if ("y".equalsIgnoreCase(input)) {
                                cmakeOk = cmdManager.installOpenSSLPortable(null);
                                if (cmakeOk){
                                    confLoader.setOPENSSL_LIBRARIES(cmdManager.checkOpenSSLLib("", null));
                                    confLoader.setOPENSSL_INCLUDE_DIR(cmdManager.checkOpenSSLInclude("", null));
                                } else {
                                    System.out.println("ERROR: OpenSSL library for CMAKE not installed. Try again running as Administrator!");
                                }
                            } else {
                                cmakeOk = false;
                            }
                        }*/
                    }

                    if (cmakeOk) {
                        System.out.print("\nDo you want to build source code (into folder '"+confLoader.getCMakeBuildFolder()+"')? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            System.out.println("Configuring CMake option for compile.");
                            serverFolder = confLoader.getGitFolderServer().isEmpty() ? "server" : confLoader.getGitFolderServer();
                            cmdManager.cmakeConfig(serverFolder, confLoader.getCMakeBuildFolder(), confLoader.getCmakeOptions(), null);
                        }
                        System.out.print("\nDo you want to compile and install built source (into folder '"+confLoader.getCMakeRunFolder()+"')? [y/n, default:n] ");
                        input = System.console().readLine();
                        if ("y".equalsIgnoreCase(input)) {
                            cmdManager.cmakeInstall(confLoader.getCMakeBuildFolder(), confLoader.getCMakeRunFolder(), null);
                        }
                    } else {
                        System.out.println("\n*** Compiling operation skipped. CMAKE command not ready.");
                    }
                }
            } else {
                System.out.println("CRITICAL: Operatig system not supported.");
            }

        } else {
            MainWindow.main(args);
        }

    }

    private static String readNewParam(String paramName, String paramValue) {
        System.out.print("Insert new value for '" + paramName + "' parameter [default: " + paramValue + "]:");
        String input = System.console().readLine();
        return (!input.isEmpty()) ? input : paramValue;
    }

}
