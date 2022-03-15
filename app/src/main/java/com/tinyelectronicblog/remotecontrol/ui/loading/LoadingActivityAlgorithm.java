package com.tinyelectronicblog.remotecontrol.ui.loading;

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

import android.content.Intent;
import com.google.api.services.drive.Drive;
import com.tinyelectronicblog.remotecontrol.R;
import com.tinyelectronicblog.remotecontrol.Settings;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.file.FileIO;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.google.drive.DriveServiceHelper;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.multithreading.MainThreadTask;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.multithreading.NewThreadTask;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.gui.dialog.SimpleDialog;

class LoadingActivityAlgorithm extends NewThreadTask {

    private static final int STEP = 250;
    private static final int LOADING_TIME = 3000;

    private static LoadingActivityAlgorithm reference = null;

    private LoadingActivity activity;

    private int restartPoint, loadingTime, loadingState;
    private boolean stopLoading;

    private LoadingActivityAlgorithm (String name) {
        super(name);
        restartPoint = 0;
        loadingTime = 0;
        loadingState = 0;
    }

    //to be executed when the activity becomes visible
    static void onResume (LoadingActivity activity) {
        if (reference == null) {
            reference = new LoadingActivityAlgorithm("LoadingActivityAlgorithm");
        }
        //update the activity reference
        reference.activity = activity;

        //ui update
        switch (reference.restartPoint) {
            case 0:
                activity.updateUI(true, false);
                break;
            case 1:
                activity.updateUI(true, false);
                break;
            case 2:
                activity.updateUI(true, false);
                break;
            case 3:
                activity.updateUI(false, false);
                break;
            case 4:
                activity.updateUI(false, true);
                break;
            case -1:
                return;
        }

        reference.start();
    }

    static void onPause () {
        if (reference.activity.exitDialogue != null) {
            reference.activity.exitDialogue.dismiss();
            reference.activity.exitDialogue = null;
        }
        if (reference.activity.dialogueIncorrectSettings != null) {
            reference.activity.dialogueIncorrectSettings.dismiss();
            reference.activity.dialogueIncorrectSettings = null;
        }
        reference.activity.updateUI(false, false);
        reference.activity = null;
    }

    @Override
    public void toDo() {
        Debug.print("LoadingActivityAlgorithm start");

        setNextTask(null);
        settingsTask.setNextTask(null);
        settingsDialogTask.setNextTask(null);
        CheckLoginTask.setNextTask(null);
        loadingTask.setNextTask(null);
        exitDialogTask.setNextTask(null);

        switch (restartPoint) {
            case 0:
                setNextTask(settingsTask);
                break;
            case 1:
                exitDialogTask.start();
                break;
            case 2:
                if (loadingTime <= LOADING_TIME) { //I await the end of the loading animation
                    loadingTask.start();
                    while (loadingTime <= LOADING_TIME) {
                        if (activity == null) return;
                        try {Thread.sleep(STEP);} catch (InterruptedException e) {Debug.print(e.toString());}
                    }
                }
                settingsDialogTask.start();
                break;
            case 3:
                CheckLoginTask.start();
                break;
            case 4://manual login
                break;
        }
    }

    private final NewThreadTask loadingTask = new NewThreadTask("loadingTask") {
        @Override
        public void toDo() {
            Debug.print("loadingTask start");
            stopLoading = false;
            while (!stopLoading && activity != null) {
                try {
                    Thread.sleep(STEP);
                    if (stopLoading || activity == null) break;
                    loadingState++;
                    if (loadingState == 7) loadingState = 0;
                    new MainThreadTask() {
                        @Override
                        public void toDo() {
                            try {activity.setLoadingState(loadingState);} catch (Exception e) {Debug.print(e.toString());}
                        }
                    }.start();
                    loadingTime = loadingTime + STEP;
                }
                catch (Exception e) {
                    Debug.print(e.toString());
                }
            }
        }
    };

    private final NewThreadTask settingsTask = new NewThreadTask("settingsTask") {
        @Override
        public void toDo() {
            Debug.print("settingsTask start");
            loadingTask.start();
            final String filename = "testFile", stringTest1 = "00000000000000000\n0000000000000000000000\n";
            if (FileIO.write(activity, filename, stringTest1)) {
                final String stringTest2 = FileIO.read(activity, filename);
                if (stringTest2 != null)
                    if (stringTest1.equals(stringTest2)) {//test ok, loading settings
                        LoadingActivity.settings = new Settings(activity); //generate new settings
                        if (LoadingActivity.settings.status == 2) { //generated a new settings file because it is damaged, it will show an informative dialog
                            restartPoint = 2;
                            while (loadingTime <= LOADING_TIME) {
                                if (activity == null) return;
                                try {Thread.sleep(STEP);} catch (InterruptedException e) {Debug.print(e.toString());}
                            }
                            settingsDialogTask.start();
                            return;
                        } else { //settings file correctly loaded or created new, I will proceed with the login
                            while (loadingTime <= LOADING_TIME) {
                                if (activity == null) return;
                                try {Thread.sleep(STEP);} catch (InterruptedException e) {Debug.print(e.toString());}
                            }
                            restartPoint = 3;
                            CheckLoginTask.start();
                            return;
                        }
                    }
            }
            //test failed, exit jdialog
            while (loadingTime <= LOADING_TIME) {
                if (activity == null) return;
                try {Thread.sleep(STEP);} catch (InterruptedException e) {Debug.print(e.toString());}
            }
            restartPoint = 1;
            exitDialogTask.start();
        }
    };

    private final MainThreadTask exitDialogTask = new MainThreadTask() {
        @Override
        public void toDo() {
            Debug.print("exitDialogTask start");
            loadingTask.start();
            final String msg = activity.getString(R.string.loading_activity_dialog_error1_eng);
            final String buttonText = activity.getString(R.string.loading_activity_dialog_button_eng);
            if (activity != null) activity.exitDialogue = new SimpleDialog(activity, msg, buttonText, false) {
                @Override
                public void onButtonClick() {
                    destroy();
                }
            };
        }
    };

    private final MainThreadTask settingsDialogTask = new MainThreadTask() {
        @Override
        public void toDo() {
            Debug.print("settingsDialogTask start");
            loadingTask.start();
            final String msg = activity.getString(R.string.loading_activity_dialog_error2_eng);
            final String buttonText = activity.getString(R.string.loading_activity_dialog_button_eng);
            if (activity != null) activity.dialogueIncorrectSettings = new SimpleDialog(activity, msg, buttonText, false) {
                @Override
                public void onButtonClick() {
                    restartPoint = 3;
                    CheckLoginTask.start();
                }
            };
        }
    };

    private final MainThreadTask CheckLoginTask = new MainThreadTask() {
        @Override
        public void toDo() {
            Debug.print("CheckLoginTask start");
            activity.googleLoginHelper.updateLastSignedInAccount();
            if (activity.googleLoginHelper.getLastSignedInAccount() == null) {//manual login
                restartPoint = 4;
                stopLoading = true;
                activity.updateUI(false, true);
            }
            else { // automatic login
                restartPoint = 3;
                stopLoading = true;
                activity.updateUI(false, false);
                startRemoteControlActivity();
            }
        }
    };

    static LoadingActivityAlgorithm getReference () {
        return reference;
    }

    void startRemoteControlActivity () {
        try {
            Debug.print("start RemoteControlActivity");
            activity.updateUI(false, false);
            LoadingActivity.googleAccout = activity.googleLoginHelper.getLastSignedInAccount();
            Drive drive = DriveServiceHelper.getDriveService(activity, LoadingActivity.googleAccout, activity.getString(R.string.app_name));
            LoadingActivity.googleDrive = new DriveServiceHelper(drive);
            restartPoint = 3;
            activity.mStartForResult.launch(new Intent(activity, com.tinyelectronicblog.remotecontrol.ui.remotecontrol.RemoteControlActivity.class));
        }
        catch (Exception e) {
            Debug.print(e.toString());
            restartPoint = 3;
        }
    }

    static void destroy () {
        reference = null;
    }

    static void start (int restartPoint) {
        reference.restartPoint = restartPoint;
        reference.start();
    }

}