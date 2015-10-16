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

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Boni Simone <simo.boni@gmail.com>
 */
public class ConsoleManager {

    private static final Logger LOG = Logger.getLogger(ConsoleManager.class.getName());

    private static ConsoleManager instance = null;

    /**
     *
     */
    public static final int TEXT_NO_COLOR = 0;

    /**
     *
     */
    public static final int TEXT_RED = 1;

    /**
     *
     */
    public static final int TEXT_GREEN = 2;

    /**
     *
     */
    public static final int TEXT_ORANGE = 3;

    /**
     *
     */
    public static final int TEXT_BLACK = 4;

    /**
     *
     */
    public static final int TEXT_BLUE = 5;

    /**
     * Default constructor
     */
    public ConsoleManager() {
    }

    /**
     * Get class instance to use in singleton mode
     *
     * @return Command class instance
     */
    public static ConsoleManager getInstance() {
        if (instance == null) {
            instance = new ConsoleManager();
        }
        return instance;
    }

    /**
     * Update JTextPane object console in GUI mode adding text line to the end.
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @param text The String text to insert at bottom of JTextPane
     */
    public void updateGUIConsole(Object console, String text) {
        this.updateGUIConsole(console, text, TEXT_NO_COLOR);
    }

    /**
     * Update JTextPane object console in GUI mode adding text line to the end.
     *
     * @param console The JTextPane object to be used for output in swing GUI
     * @param text The String text to insert at bottom of JTextPane
     * @param textColor The int value of color to use for text
     */
    public void updateGUIConsole(final Object console, String text, int textColor) {
        try {
            StyledDocument doc = ((JTextPane) console).getStyledDocument();
            Style style;
            switch (textColor) {
                case 1:
                    style = ((JTextPane) console).getStyle("Red Style");
                    if (style == null) {
                        style = ((JTextPane) console).addStyle("Red Style", null);
                    }
                    StyleConstants.setForeground(style, Color.RED);
                    break;
                case 2:
                    style = ((JTextPane) console).getStyle("Green Style");
                    if (style == null) {
                        style = ((JTextPane) console).addStyle("Green Style", null);
                    }
                    StyleConstants.setForeground(style, Color.GREEN);
                    break;
                case 3:
                    style = ((JTextPane) console).getStyle("Orange Style");
                    if (style == null) {
                        style = ((JTextPane) console).addStyle("Orange Style", null);
                    }
                    StyleConstants.setForeground(style, Color.ORANGE);
                    break;
                case 4:
                    style = ((JTextPane) console).getStyle("Black Style");
                    if (style == null) {
                        style = ((JTextPane) console).addStyle("Black Style", null);
                    }
                    StyleConstants.setForeground(style, Color.BLACK);
                    break;
                case 5:
                    style = ((JTextPane) console).getStyle("Blue Style");
                    if (style == null) {
                        style = ((JTextPane) console).addStyle("Blue Style", null);
                    }
                    StyleConstants.setForeground(style, Color.BLUE);
                    break;
                default:
                    style = ((JTextPane) console).getStyle("Standard Style");
                    if (style == null) {
                        style = ((JTextPane) console).addStyle("Standard Style", null);
                    }
                    break;
            }

            try {
                doc.insertString(doc.getLength(), "\n" + text, style);
            } catch (BadLocationException e) {
                String currText = ((JTextComponent) console).getText();
                ((JTextComponent) console).setText(currText + "\n" + text);
            }
        } catch (Exception ex) {
            System.console().printf("%s\n", "Unable to write to GUI console: " + ex.getLocalizedMessage());
            LOG.log(Level.SEVERE, null, ex);
        }
        ((JTextComponent) console).setCaretPosition(((JTextComponent) console).getDocument().getLength());
    }
}
