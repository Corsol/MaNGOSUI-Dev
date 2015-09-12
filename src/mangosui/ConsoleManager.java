/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.awt.Color;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Simone
 */
public class ConsoleManager {
    
    private static ConsoleManager instance = null;
    
    public int TEXT_NO_COLOR = 0;
    public int TEXT_RED = 1;
    public int TEXT_GREEN = 2;
    public int TEXT_ORANGE = 3;
    public int TEXT_BLACK = 4;
    public int TEXT_BLUE = 5;
    
    public ConsoleManager() {
    }
    
    public static ConsoleManager getInstance() {
        if (instance == null) {
            instance = new ConsoleManager();
        }
        return instance;
    }
    
    public void updateGUIConsole(Object console, String text) {
        updateGUIConsole(console, text, TEXT_NO_COLOR);
    }
    
    public void updateGUIConsole(Object console, String text, int textColor) {
        try {
            StyledDocument doc = ((JTextPane) console).getStyledDocument();
            Style style;
            switch (textColor) {
                case 1:
                    style = ((JTextPane) console).addStyle("Red Style", null);
                    StyleConstants.setForeground(style, Color.RED);
                    break;
                case 2:
                    style = ((JTextPane) console).addStyle("Green Style", null);
                    StyleConstants.setForeground(style, Color.GREEN);
                    break;
                case 3:
                    style = ((JTextPane) console).addStyle("Orange Style", null);
                    StyleConstants.setForeground(style, Color.ORANGE);
                    break;
                case 4:
                    style = ((JTextPane) console).addStyle("Black Style", null);
                    StyleConstants.setForeground(style, Color.BLACK);
                    break;
                case 5:
                    style = ((JTextPane) console).addStyle("Blue Style", null);
                    StyleConstants.setForeground(style, Color.BLUE);
                    break;
                default:
                    style = ((JTextPane) console).addStyle("Standard Style", null);
                    //StyleConstants.setForeground(style, Color.GRAY);
                    break;
            }
            
            try {
                doc.insertString(doc.getLength(), "\n" + text, style);
            } catch (BadLocationException e) {
                String currText = ((JTextPane) console).getText();
                ((JTextPane) console).setText(currText + "\n" + text);
            }
        } catch (Exception ex) {
            System.out.println("Unable to write to GUI console: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
}