/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

/**
 *
 * @author Simone
 */
public class ProcessExec extends javax.swing.SwingWorker {

    String line;
    Process p;
    JTextPane jtp;

    /**
     *
     * @param process
     * @param jtp
     */
    public ProcessExec(Process process, Object jtp) {
        p = process;
        this.jtp = (JTextPane)jtp;
    }

    //implements a method in the swingworker
    @Override
    public Object doInBackground() throws Exception {
        //Read Process Stream Output and write to LOG file
        BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));

        while ((line = is.readLine()) != null) {
            //ConsoleManager.getInstance().updateGUIConsole(jtp, line, ConsoleManager.getInstance().TEXT_BLACK);
            publish((line + "\n"));
        }
        System.out.flush();
        return null;
    }

    //This will happen on the UI Thread.

    /**
     *
     * @param lines
     */
    
    protected void process(List lines) {
        for (String o : lines.getItems()) {
            //ConsoleManager.getInstance().updateGUIConsole(jtp, o, ConsoleManager.getInstance().TEXT_BLACK);
            //String currText = jtp.getText();
            //jtp.setText(currText + "\n" + o);
        }
    }

    @Override
    public void done() {
        try {
            get();
            //You will get here if everything was OK.  So show a popup or something to signal done.
        } catch (InterruptedException | ExecutionException ex) {
            //this is where your IO Exception will surface, should you have one.
        }
    }
}
