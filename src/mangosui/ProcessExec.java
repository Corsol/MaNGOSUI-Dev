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

import java.awt.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JProgressBar;

/**
 *
 * @author Boni Simone <simo.boni@gmail.com>
 */
public class ProcessExec extends javax.swing.SwingWorker {

    private int debugLevel = 0;
    private String line;
    private final String[] commands;
    private final Object guiConsole;
    private final JButton btnInvoker;
    private final JProgressBar prbCurrWork;
    private static final Logger LOG = Logger.getLogger(ProcessExec.class.getName());

    /**
     * Default constructor that set all object needed for background execution
     * and return
     *
     * @param commands The String value for commands to execute
     * @param guiConsole The JTextPane object to be used for output in swing GUI
     * @param debugLevel The int value for output debug level
     * @param btnInvoker The JButon object for status return when background
     * execution end
     * @param prbCurrWork The JProgressBar object to be used for long and
     * multi-step process
     */
    public ProcessExec(String[] commands, Object guiConsole, int debugLevel, JButton btnInvoker, JProgressBar prbCurrWork) {
        this.commands = commands;
        this.guiConsole = guiConsole;
        this.btnInvoker = btnInvoker;
        this.prbCurrWork = prbCurrWork;
        this.debugLevel = debugLevel;
    }

    /**
     * Execute the process in background with swingworker and let the GUI do not
     * freeze
     *
     * @return The Object with process exit value
     * @throws Exception
     */
    @Override
    public Object doInBackground() throws Exception {
        boolean done = false;
        int exitValue = 0;
        ProcessBuilder builder = new ProcessBuilder(this.commands);
        builder.redirectErrorStream(true);
        String msg = "";
        if (this.debugLevel > 0) {
            msg = "EXECUTING:";
            for (String cmd : this.commands) {
                msg += " " + cmd;
            }
            System.console().printf("%s\n", msg);
        }
        Process proc = builder.start();
        while (!done) {
            try {
                BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                while ((this.line = is.readLine()) != null) {
                    if (this.prbCurrWork != null) {
                        // Write to GUI console the command executed
                        ConsoleManager.getInstance().updateGUIConsole(this.guiConsole, msg, ConsoleManager.TEXT_ORANGE);
                    }
                    // Write to GUI console the output of process
                    ConsoleManager.getInstance().updateGUIConsole(this.guiConsole, this.line, ConsoleManager.TEXT_BLACK);
                }
                exitValue = proc.exitValue();
                done = true;
            } catch (IllegalThreadStateException e) {
                // This exception will be thrown only if the process is still running 
                // because exitValue() will not be a valid method call yet...
            }
        }
        return exitValue;
    }

    /**
     *
     * @param lines
     */
    protected void process(List lines) {
        //This will happen on the UI Thread.
        for (String o : lines.getItems()) {
            ConsoleManager.getInstance().updateGUIConsole(this.guiConsole, o, ConsoleManager.TEXT_BLACK);
        }
    }

    /**
     * When the background process end set the return status to GUI via JButton
     * Text parameter or JProgressBar value that will raise and event catchabe.
     */
    @Override
    public void done() {
        try {
            if (this.btnInvoker != null) {
                if ((int) this.get() == 0) {
                    this.btnInvoker.setText("DONE");
                } else {
                    this.btnInvoker.setText("ERROR");
                }
                this.btnInvoker.setEnabled(true);
            } else if (this.prbCurrWork != null) {
                this.prbCurrWork.setValue(this.prbCurrWork.getValue() + 1);
            }
        } catch (InterruptedException | ExecutionException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
