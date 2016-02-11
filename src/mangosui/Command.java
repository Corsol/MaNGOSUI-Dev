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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JProgressBar;

/**
 *
 * @author Boni Simone <simo.boni@gmail.com>
 */
public class Command {

    private int debugLevel = 0; // 0: None, 1: console, 2: full
    private static Command instance = null;
    private JButton btnInvoker;
    private static final Logger LOG = Logger.getLogger(Command.class.getName());

    /**
     * Basic constructor for singleton usages
     */
    public Command() {
    }

    /**
     * Get class instance to use in singleton mode
     *
     * @return Command class instance
     */
    public static Command getInstance() {
        if (instance == null) {
            instance = new Command();
        }
        return instance;
    }

    /**
     * Execute the commands list and return the exit status code as boolean
     * value. Output of commands can be redirected to user in many different
     * ways:<br/>
     * * JTextPane in GUI environment<br/>
     * * Console<br/>
     * * String<br/>
     * If guiConsole is null the process use current thread to run commands, and
     * user must wait commands execution to use the interface (gui or console).
     * Else if guiConsole was passed as argument the commands execution will be
     * run in a separated thread, so GUI will not freeze until execution is done
     * and output wiil be pasted into JTextPane specified. To manage subthread
     * ends use a JButton or JProgressBar and setup the PropertyChange event.
     * The separated thread will change "Text" (for Jbutton) and/or "Value" (for
     * JProgressBar) when work is done.
     *
     * @param commands The String[] commands list to be executed
     * @param guiConsole The JTextPane swing component into wich append
     * execution output
     * @param rawConsole The String variable into wich excution output will be
     * saved
     * @param toBuffer The boolean value to save output into String variable and
     * avoid console output
     * @param prbCurrWork The JProgressBar used on separated thread execution
     * @return True if local execution exit value is 0, false if local execution
     * exit value is not equals to 0, false if execution will be executed in
     * separated thread.
     * @throws IOException
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    protected boolean execute(String[] commands, Object guiConsole, final StringBuilder rawConsole, boolean toBuffer, final JProgressBar prbCurrWork) throws IOException, InterruptedException, ExecutionException {
        if (guiConsole != null && prbCurrWork == null) {
            ProcessExec proc = new ProcessExec(commands, guiConsole, this.debugLevel, this.btnInvoker, prbCurrWork);
            proc.execute();
            return false;
        } else {
            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true);
            String msg = "";
            if (this.debugLevel > 0) {
                msg = "EXECUTING:";
                for (String cmd : commands) {
                    msg += " " + cmd;
                }
                System.out.printf("%s\n", msg);
            }
            if (!toBuffer) {
                builder.redirectError(ProcessBuilder.Redirect.INHERIT);
                builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                builder.inheritIO();
            }
            Process proc = builder.start();
            if (!toBuffer) {
                // do nothing, autoredirect do what needed.
            } else if (guiConsole != null) {
                ConsoleManager.getInstance().updateGUIConsole(guiConsole, msg, ConsoleManager.TEXT_ORANGE);
                writeToGUIConsole(msg, proc.getInputStream(), guiConsole, proc);
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    rawConsole.append(line).append("\n");
                }
                reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    rawConsole.append(line).append("\n");
                }
            }
            proc.waitFor();
            return proc.exitValue() <= 0;
        }
    }

    synchronized static void writeToGUIConsole(String cmd, InputStream in, Object console, Process proc) throws IOException, InterruptedException {
        try {
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                ConsoleManager.getInstance().updateGUIConsole(console, line, ConsoleManager.TEXT_BLACK);
            }
        } catch (IOException ioe) {
            LOG.log(Level.OFF, ioe.getLocalizedMessage(), console);
        }
    }

    /**
     * Check if path exists and is a directory.
     *
     * @param path The String path to be checked
     * @return True if path exists and is directory, false otherwise
     */
    public boolean checkFolder(String path) {
        try {
            File file = new File(path);
            return file.exists() && file.isDirectory();
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Copy files and/or folders from source to destination<br/>
     * <b>NOTE</b>: to copy a single file the file name must be specified in
     * both source and destination strings.
     *
     * @param src The String path of source file/folder
     * @param dest The String path of destination file/folder
     * @param console The JTextPane swing component for output
     * @return True if all file/folder were copied, false when ther's an error
     * during copying
     */
    public boolean copyFolder(File src, File dest, Object console) {
        try {
            if (src.isDirectory()) {
                //if directory not exists, create it
                if (!dest.exists()) {
                    dest.mkdir();
                }
                //list all the directory contents
                String files[] = src.list(new FileFilterBuild());
                for (String file : files) {
                    //construct the src and dest file structure
                    File srcFile = new File(src, file);
                    File destFile = new File(dest, file);
                    //recursive copy
                    this.copyFolder(srcFile, destFile, console);
                }
            } else {
                //if file, then copy it
                //Use bytes stream to support all file types
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest);

                byte[] buffer = new byte[1024];

                int length;
                //copy the file content in bytes 
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                in.close();
                out.close();
                String msg = "File copied to " + dest;
                if (console != null) {
                    ConsoleManager.getInstance().updateGUIConsole(console, msg, ConsoleManager.TEXT_BLACK);
                } else {
                    System.out.printf("%s\n", msg);
                }

            }
        } catch (IOException ioe) {
            LOG.log(Level.OFF, ioe.getLocalizedMessage(), console);
            return false;
        }
        return true;
    }

    /**
     * Return the debugLevel variable value
     *
     * @return the int value of variable
     */
    public int getDebugLevel() {
        return this.debugLevel;
    }

    /**
     * Set the debugLevel variable value. Values can be:<br/>
     * 0: No debug output<br/>
     * 1: Only console debug output<br/>
     * 2: Full debug output
     *
     * @param debugLevel
     */
    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    /**
     * Return the btnInvoker variable object
     *
     * @return The JButton object
     */
    public JButton getBtnInvoker() {
        return this.btnInvoker;
    }

    /**
     * Set the btnInvoker variable object with a valid JButton swing component
     *
     * @param btnInvoker
     */
    public void setBtnInvoker(JButton btnInvoker) {
        this.btnInvoker = btnInvoker;
    }
    /*
     public JProgressBar getPrbCurrWork() {
     return prbCurrWork;
     }

     public void setPrbCurrWork(JProgressBar prbCurrWork) {
     this.prbCurrWork = prbCurrWork;
     }
     */
}
