package com.tinyelectronicblog.remotecontrol;

/**
 * MIT License
 * Copyright (c) 2022 TinyElectronicBlog
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.content.Context;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.file.FileIO;

/**
 * This class manages the user's settings and saving them to files.
 */
public class Settings {

    public String language, workspaceFolder, controllerID, dataUpdate;
    public int languageID;
    public final int status; //0 = settings loaded correctly; 1 = generate new settings; 2 = re-create new settings because the previous ones were unreadable.

    public static final String TEBfolder = "Tiny Electronic Blog folder";
    public static final String dataFile = "File of Remote control ";
    public static final int updateDataMIN = 0;
    public static final int updateDataMAX = 3000;
    public static final int updateDataDEFAULT = 1;

    public final static String[] availableLanguages = {"English", "Italiano"};
    private final static String languageLabel = "language:";
    private final static String workspaceFolderLabel = "workspace folder:";
    private final static String controllerIDLabel = "controller id:";
    private final static String dataUpdateLabel = "data update:";
    private final static String defaultSettings = languageLabel + availableLanguages[0]+"\n" + workspaceFolderLabel + "\n" + controllerIDLabel + "\n" + dataUpdateLabel + updateDataDEFAULT +"\n";
    private final static String settingsFilename = "Settings";

    /**
     * Load settings from file. If the file is missing or damaged, a new one is created.
     */
    public Settings (Context context) {
        language = null;
        workspaceFolder = null;
        controllerID = null;
        dataUpdate = null;
        String s = FileIO.read(context, settingsFilename);
        if (s != null) {
            int i1 = s.indexOf(languageLabel), i2;
            if (i1 != -1) {
                i1 = i1 + languageLabel.length();
                i2 = s.indexOf("\n", i1);
                languageID = -1;
                if (i2 != -1 && i2 >= i1) {
                    language = s.substring(i1, i2).trim();
                    int i = 0;
                    while (i < availableLanguages.length) {
                        if (language.equals(availableLanguages[i])) break;
                        i++;
                    }
                    if (i == availableLanguages.length) language = null;
                    else languageID = i;
                }
            }
            i1 = s.indexOf(workspaceFolderLabel);
            if (i1 != -1) {
                i1 = i1 + workspaceFolderLabel.length();
                i2 = s.indexOf("\n", i1);
                if (i2 != -1 && i2 >= i1) workspaceFolder = s.substring(i1, i2).trim();
            }
            i1 = s.indexOf(controllerIDLabel);
            if (i1 != -1) {
                i1 = i1 + controllerIDLabel.length();
                i2 = s.indexOf("\n", i1);
                if (i2 != -1 && i2 >= i1) controllerID = s.substring(i1, i2).trim();
            }
            i1 = s.indexOf(dataUpdateLabel);
            if (i1 != -1) {
                i1 = i1 + dataUpdateLabel.length();
                i2 = s.indexOf("\n", i1);
                if (i2 != -1 && i2 >= i1) dataUpdate = s.substring(i1, i2).trim();
            }
            if (language == null || workspaceFolder == null || controllerID == null || dataUpdate == null) {
                defaultSettings(context, true);
                status = 2;
                Debug.print("Damaged settings file, a new one has been created.");
            }
            else {
                status = 0;
                Debug.print("Settings correctly loaded from file.");
            }
        }
        else {
            defaultSettings(context, true);
            status = 1;
            Debug.print("Settings file not found, new one created.");
        }
    }

    public boolean defaultSettings (Context context, boolean resetLanguage) {
        if (resetLanguage) {
            language = availableLanguages[0];
            languageID = 0;
        }
        workspaceFolder = "";
        controllerID = "";
        dataUpdate = ""+updateDataDEFAULT;
        if (resetLanguage) return FileIO.write(context, settingsFilename, defaultSettings);
        else return saveSettings(context);
    }

    /**
     * Save current settings to file. Returns true on success.
     */
    public boolean saveSettings (Context context) {
        String temp = languageLabel + language +
                "\n" +
                workspaceFolderLabel +
                workspaceFolder +
                "\n" +
                controllerIDLabel +
                controllerID +
                "\n" +
                dataUpdateLabel +
                dataUpdate +
                "\n";
        return FileIO.write(context, settingsFilename, temp);
    }

}