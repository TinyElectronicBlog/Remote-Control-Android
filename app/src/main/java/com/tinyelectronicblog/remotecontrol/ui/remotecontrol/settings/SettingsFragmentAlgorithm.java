package com.tinyelectronicblog.remotecontrol.ui.remotecontrol.settings;

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

import static com.tinyelectronicblog.remotecontrol.Settings.TEBfolder;
import static com.tinyelectronicblog.remotecontrol.Settings.dataFile;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_error1;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_error2;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_error3;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_error4;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_error5;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_error6;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_error7;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_error8;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_error9;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_synchronized;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.tinyelectronicblog.remotecontrol.Settings;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.multithreading.MainThreadTask;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.multithreading.NewThreadTask;
import com.tinyelectronicblog.remotecontrol.ui.loading.LoadingActivity;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class SettingsFragmentAlgorithm extends NewThreadTask {

    private static SettingsFragmentAlgorithm reference = null;

    private SettingsFragment fragment;

    private int restartPoint;
    private boolean driveReady;

    private SettingsFragmentAlgorithm (String name) {
        super(name);
        restartPoint = 0;
        driveReady = false;
        SettingsFragment.profilePhoto = null;
        SettingsFragment.profileText = null;
        SettingsFragment.TEBFolderID = null;
        SettingsFragment.remoteControlConfigFileID = null;
        SettingsFragment.workspaceFolderID = null;
        updateProfileInformationTask.start();
    }

    static void onResume (SettingsFragment fragment) {
        if (reference == null) {
            reference = new SettingsFragmentAlgorithm("SettingsFragmentAlgorithm");
        }

        reference.fragment = fragment;

        switch (reference.restartPoint) {
            case 1:
                //I reactivate the buttons to be able to manually enter the settings
                fragment.setButtonState(true);
                break;
            case -1:
                return;
            default:
                fragment.setButtonState(false);
                break;
        }

        reference.start();
    }

    static void onPause() {
        if (reference != null) {
            reference.fragment.setButtonState(false);
            reference.fragment = null;
        }
    }

    @Override
    public void toDo() {
        Debug.print("SettingsFragmentAlgorithm start");

        checkInitialValuesTask.setNextTask(null);
        checkTebFolderTask.setNextTask(null);
        createTebFolderTask.setNextTask(null);
        checkWorkspaceFolderTask.setNextTask(null);
        createWorkspaceFolderTask.setNextTask(null);
        checkRemoteControlFileTask.setNextTask(null);
        createRemoteControlFileTask.setNextTask(null);
        driveReadyTask.setNextTask(null);
        setNextTask(null);

        switch (restartPoint) {
            case 0:
                setNextTask(checkInitialValuesTask);
                break;
            case 1: // manually enter the settings
                break;
            case 2:
                setNextTask(checkTebFolderTask);
                break;
            case 3:
                setNextTask(checkTebFolderTask);
                break;
            case 4:
                setNextTask(checkWorkspaceFolderTask);
                break;
            case 5:
                setNextTask(checkWorkspaceFolderTask);
                break;
            case 6:
                setNextTask(checkRemoteControlFileTask);
                break;
            case 7:
                setNextTask(checkRemoteControlFileTask);
                break;
            case 8:
                driveReadyTask.start();
                break;
        }
    }

    private class initializationFailed extends MainThreadTask {

        private final String message;

        initializationFailed(String message) {
            super();
            Debug.print("initializationFailed start");
            this.message = message;
            driveReady = false;
            this.start();
        }

        @Override
        public void toDo() {
            reference.restartPoint = 1;
            //the following instructions may crash without problems
            if (reference.fragment != null) {
                reference.fragment.setButtonState(true);
                if (!message.equals("")) Toast.makeText(reference.fragment.getContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private final NewThreadTask checkInitialValuesTask = new NewThreadTask() {
        @Override
        public void toDo() {
            Debug.print("checkInitialValuesTask start");
            Settings settings = LoadingActivity.getSettings();
            if (settings.workspaceFolder.equals("") || settings.controllerID.equals("") || settings.dataUpdate.equals("")) { //values to be confirmed by the setValues button
                new initializationFailed(""); //not failed but this instruction is still good here
                return;
            }
            else { //values already present at startup
                reference.restartPoint = 2;
                setNextTask(checkTebFolderTask);
                return;
            }
        }
    };

    //check the presence of the application folder on google drive
    private final NewThreadTask checkTebFolderTask = new NewThreadTask() {
        @Override
        public void toDo() {
            Debug.print("checkTebFolderTask start");
            try {
                String q = "trashed = false and mimeType = 'application/vnd.google-apps.folder' and name = '" + TEBfolder + "' and 'root' in parents";
                FileList filelist = LoadingActivity.getGoogleDrive().queryFiles(q, null, null, 1000);
                if (filelist == null) {
                    Debug.print("The search for the Tiny Electronic Blog folder on Google Drive failed-1");
                    new initializationFailed(settings_drive_error1.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
                List<File> folderList = filelist.getFiles();
                if (folderList == null) {
                    Debug.print("The search for the Tiny Electronic Blog folder on Google Drive failed-2");
                    new initializationFailed(settings_drive_error1.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
                int fn = folderList.size();
                if (fn == 0) { //folder to create
                    reference.restartPoint = 3;
                    setNextTask(createTebFolderTask);
                    return;
                } else if (fn == 1) { //the Tiny Electronic Blog folder already exists in root
                    SettingsFragment.TEBFolderID = folderList.get(0).getId();
                    reference.restartPoint = 4;
                    setNextTask(checkWorkspaceFolderTask);
                    return;
                } else {
                    // detect multiple Tiny Electronic Blog folders in root; there can be a maximum of 1, so you have to manually remove the others
                    Debug.print("Error, there can be a maximum of one Tiny Electronic Blog folder in the Google Drive root");
                    new initializationFailed(settings_drive_error2.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
            }
            catch (Exception e) {
                new initializationFailed(e.toString());
            }
        }
    };

    private final NewThreadTask createTebFolderTask = new NewThreadTask() {
        @Override
        public void toDo() {
            Debug.print("createTebFolderTask start");
            try {
                String id = LoadingActivity.getGoogleDrive().createFile(TEBfolder, null, true);
                if (id == null) {
                    Debug.print("Tiny Electronic Blog folder creation on Google Drive failed");
                    new initializationFailed(settings_drive_error3.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
                SettingsFragment.TEBFolderID = id;
                reference.restartPoint = 4;
                setNextTask(checkWorkspaceFolderTask);
            }
            catch (Exception e) {
                new initializationFailed(e.toString());
            }
        }
    };

    private final NewThreadTask checkWorkspaceFolderTask = new NewThreadTask() {
        @Override
        public void toDo() {
            Debug.print("checkWorkspaceFolderTask start");
            try {
                String q = "trashed = false and mimeType = 'application/vnd.google-apps.folder' and name = '" + LoadingActivity.getSettings().workspaceFolder + "' and '" + SettingsFragment.TEBFolderID + "' in parents";
                FileList filelist = LoadingActivity.getGoogleDrive().queryFiles(q, null, null, 1000);
                if (filelist == null) {
                    Debug.print("The search for the workspace folder on Google Drive failed-1");
                    new initializationFailed(settings_drive_error4.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
                List<File> folderList = filelist.getFiles();
                if (folderList == null) {
                    Debug.print("The search for the workspace folder on Google Drive failed-2");
                    new initializationFailed(settings_drive_error4.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
                int fn = folderList.size();
                if (fn == 0) {//folder to create
                    reference.restartPoint = 5;
                    setNextTask(createWorkspaceFolderTask);
                    return;
                } else if (fn == 1) {//existing folder
                    SettingsFragment.workspaceFolderID = folderList.get(0).getId();
                    reference.restartPoint = 6;
                    setNextTask(checkRemoteControlFileTask);
                    return;
                } else {
                    //detect more Tiny Electronic Blog folders in root; there can be at most 1, so you have to remove the others manually
                    Debug.print("Error, there can be a maximum of one workspace folder in the Google Drive root");
                    new initializationFailed(settings_drive_error5.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
            }
            catch (Exception e) {
                new initializationFailed(e.toString());
            }
        }
    };

    private final NewThreadTask createWorkspaceFolderTask = new NewThreadTask() {
        @Override
        public void toDo() {
            Debug.print("createWorkspaceFolderTask start");
            try {
                String id = LoadingActivity.getGoogleDrive().createFile(LoadingActivity.getSettings().workspaceFolder, SettingsFragment.TEBFolderID, true);
                if (id == null) {
                    Debug.print("Workspace folder creation on Google Drive failed");
                    new initializationFailed(settings_drive_error6.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
                SettingsFragment.workspaceFolderID = id;
                reference.restartPoint = 6;
                setNextTask(checkRemoteControlFileTask);
            }
            catch (Exception e) {
                new initializationFailed(e.toString());
            }
        }
    };

    private final NewThreadTask checkRemoteControlFileTask = new NewThreadTask() {
        @Override
        public void toDo() {
            Debug.print("checkRemoteControlFileTask start");
            try {
                String q = "trashed = false and mimeType = 'text/plain' and name = '" + dataFile + LoadingActivity.getSettings().controllerID + "' and '" + SettingsFragment.workspaceFolderID + "' in parents";
                FileList filelist = LoadingActivity.getGoogleDrive().queryFiles(q, null, null, 1000);
                if (filelist == null) {
                    Debug.print("The search for the remote control configuration file on Google Drive failed-1");
                    new initializationFailed(settings_drive_error7.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
                List<File> folderList = filelist.getFiles();
                if (folderList == null) {
                    Debug.print("The search for the remote control configuration file on Google Drive failed-2");
                    new initializationFailed(settings_drive_error7.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
                int fn = folderList.size();
                if (fn == 0) { // to create
                    reference.restartPoint = 7;
                    setNextTask(createRemoteControlFileTask);
                    return;
                }
                else if (fn == 1) { //existing folder
                    SettingsFragment.remoteControlConfigFileID = folderList.get(0).getId();
                    reference.restartPoint = 8;
                    driveReadyTask.start();
                    return;
                }
                else {
                    Debug.print("Error, there can be a maximum of one remote control configuration file in the Google Drive root");
                    new initializationFailed(settings_drive_error8.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
            }
            catch (Exception e) {
                new initializationFailed(e.toString());
            }
        }
    };

    private final NewThreadTask createRemoteControlFileTask = new NewThreadTask() {
        @Override
        public void toDo() {
            Debug.print("createRemoteControlFileTask start");
            try {
                String id = LoadingActivity.getGoogleDrive().createFile(dataFile + LoadingActivity.getSettings().controllerID, SettingsFragment.workspaceFolderID, false);
                if (id == null) {
                    Debug.print("Remote control configuration file creation on Google Drive failed");
                    new initializationFailed(settings_drive_error9.get(LoadingActivity.getSettings().languageID).toString());
                    return;
                }
                SettingsFragment.remoteControlConfigFileID = id;
                reference.restartPoint = 8;
                driveReadyTask.start();
            }
            catch (Exception e) {
                new initializationFailed(e.toString());
            }
        }
    };

    private final MainThreadTask driveReadyTask = new MainThreadTask () {
        @Override
        public void toDo() {
            Debug.print("driveReadyTask start");
            reference.restartPoint = 1;
            driveReady = true;
            reference.fragment.setButtonState(true);
            Toast.makeText(reference.fragment.getContext(), settings_drive_synchronized.get(LoadingActivity.getSettings().languageID), Toast.LENGTH_LONG).show();
        }
    };


    private final NewThreadTask updateProfileInformationTask = new NewThreadTask() {
        @Override
        public void toDo() {
            Debug.print("updateProfileInformationTask start");
            GoogleSignInAccount account = LoadingActivity.getGoogleAccout();
            SettingsFragment.profileText = fragment.createProfileText(LoadingActivity.getSettings().languageID);
            try {//I try to get the logo of the profile
                Uri photoURI = account.getPhotoUrl();
                if (photoURI != null) {
                    SettingsFragment.profilePhoto = BitmapFactory.decodeStream((InputStream) new URL(photoURI.toString()).getContent());
                } else SettingsFragment.profilePhoto = null;
            }
            catch (Exception e) {
                SettingsFragment.profilePhoto = null;
            }
            new MainThreadTask() {
                @Override
                public void toDo() {
                    if (SettingsFragment.profilePhoto != null) fragment.logo.setImageBitmap(SettingsFragment.profilePhoto);
                    if (SettingsFragment.profileText != null) fragment.textViewlogo.setText(SettingsFragment.profileText);
                }
            }.start();
        }
    };

    public static void destroy () {
        SettingsFragment.TEBFolderID = null;
        SettingsFragment.remoteControlConfigFileID = null;
        SettingsFragment.workspaceFolderID = null;
        SettingsFragment.profilePhoto = null;
        SettingsFragment.profileText = null;
        reference = null;
    }

    public static boolean isDriveReady () {
        return reference.driveReady;
    }

    static void start (int restartPoint) {
        reference.restartPoint = restartPoint;
        reference.start();
    }

}