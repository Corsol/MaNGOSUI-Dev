/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangosui;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author Boni
 */
public class FileFilterSQL implements FileFilter {

    private final String[] okFileExtensions
            = new String[]{"sql",};

    @Override
    public boolean accept(File file) {
        for (String extension : okFileExtensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}
