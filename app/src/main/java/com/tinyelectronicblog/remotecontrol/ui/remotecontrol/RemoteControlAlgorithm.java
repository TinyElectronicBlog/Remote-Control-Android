package com.tinyelectronicblog.remotecontrol.ui.remotecontrol;

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

import static com.tinyelectronicblog.remotecontrol.device.DeviceButton.sendingCommand;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.inputmethod.InputMethodManager;
import com.tinyelectronicblog.remotecontrol.device.Device;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.multithreading.ForegroundServiceTask;
import com.tinyelectronicblog.remotecontrol.ui.loading.LoadingActivity;
import com.tinyelectronicblog.remotecontrol.ui.remotecontrol.dashboard.DashboardAlgorithm;
import com.tinyelectronicblog.remotecontrol.ui.remotecontrol.on_off.OnOffAlgorithm;
import com.tinyelectronicblog.remotecontrol.ui.remotecontrol.settings.SettingsFragmentAlgorithm;

//Algorithm of the service that manages the remote control
public class RemoteControlAlgorithm extends ForegroundServiceTask {

    private static RemoteControlAlgorithm reference = null;

    private RemoteControlActivity activity;

    private boolean canSendCommands, serviceIsRunning, stopRequest;
    private int state;

    private RemoteControlAlgorithm(String name) {
        super(name);
        canSendCommands = false;
        state = 0;
        stopRequest = false;
        serviceIsRunning = false;
    }

    static void onResume(RemoteControlActivity activity) {
        if (reference == null) {
            reference = new RemoteControlAlgorithm("RemoteControlAlgorithm");
            reference.start(activity);
        }

        reference.activity = activity;
    }

    static void onPause() {
    }

    @Override
    public void toDo() {
        Debug.print("RemoteControlAlgorithm start");

        serviceIsRunning = true; //RemoteControlService started
        canSendCommands = false;
        state = 0;
        stopRequest = false;

        while (true) {
            try {// this while must not be interrupted due to exceptions

                //STEP 0: WAITING FOR GOOGLE DRIVE TO BE READY
                if (!SettingsFragmentAlgorithm.isDriveReady()) {
                    state = 0;
                    while (!stopRequest) {
                        try {
                            if (SettingsFragmentAlgorithm.isDriveReady()) break;
                            Thread.sleep(5);
                        } catch (Exception e) {
                            Debug.print(e.toString());
                        }
                    }
                }

                long time = System.currentTimeMillis();

                if (stopRequest) break;

                //STEP 1: READING FILES ON GOOGLE DRIVE
                long time1 = 0;
                if (Debug.isEnabled()) time1 = Debug.millis();
                canSendCommands = false; //pause sending commands
                try {
                    if (Device.updateDeviceFiles() != 0) state = -1; //reads online files related to device and, for each one, obtains buttons and values
                    else {
                        if (Debug.isEnabled()) {
                            long time2 = System.currentTimeMillis();
                            Debug.print("Execution time step 1-1: "+ (time2-time1)+" ms");
                            time1 = time2;
                        }
                        if (Device.updateAckCommandFiles() != 0) state = -2; //Reads online command and ack files, deleting those no longer needed. Adds, removes, activates and/or deactivates buttons in OnOffFragment
                        else {
                            if (Debug.isEnabled()) {
                                long time2 = System.currentTimeMillis();
                                Debug.print("Execution time step 1-2: "+ (time2-time1)+" ms");
                            }
                            state = 1;
                        }
                    }
                } catch (Exception e) {
                    state = -3;
                }

                if (stopRequest) break;

                //STEP 2: DASHBOARD AND ON-OFF UI UPDATE
                //NOTE: if the update of the ui fails is not a problem, it means that the related fragments are not displayed
                if (state == 1) {//if step 1 ended without errors
                    canSendCommands = true; //restart sending commands
                    time1 = System.currentTimeMillis();

                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isAcceptingText()) Debug.print("On-Off UI not updated"); //I skip the update if I am entering a command with a custom value, otherwise the keyboard would close cyclically
                    else try {
                        OnOffAlgorithm.updateUI();
                    } catch (Exception e) {
                        Debug.print(e.toString());
                    }

                    if (Debug.isEnabled()) {
                        long time2 = System.currentTimeMillis();
                        Debug.print("On Off UI creation time " + (time2 - time1) + " ms");
                        time1 = time2;
                    }

                    try {
                        DashboardAlgorithm.updateUI();
                    } catch (Exception e) {
                        Debug.print(e.toString());
                    }

                    if (Debug.isEnabled()) {
                        long time2 = System.currentTimeMillis();
                        Debug.print("Dashboard UI creation time " + (time2 - time1) + " ms");
                    }

                }
                else { //if step 1 ended with errors

                    Debug.print("STEP 2 error, state = " + state);
                    try {
                        OnOffAlgorithm.updateUI();
                    } catch (Exception e) {
                        Debug.print(e.toString());
                    }

                    try {
                        DashboardAlgorithm.updateUI();
                    } catch (Exception e) {
                        Debug.print(e.toString());
                    }

                }

                if (stopRequest) break;

                //STEP 3: WAITING
                try {
                    if (state == 1) { //if step 1 ended without errors
                        final long wait = Integer.parseInt(LoadingActivity.getSettings().dataUpdate)* 1000L -(System.currentTimeMillis()-time);
                        if (wait>0 && !stopRequest) Thread.sleep(wait);
                        while (!stopRequest) {
                            while (sendingCommand && !stopRequest) {//if necessary, wait for the end of sending a command
                                Thread.sleep(5);
                            }
                            canSendCommands = false; //pause sending commands
                            if (sendingCommand) canSendCommands = true; //if another command is in send, I'll wait for that too
                            else break;
                        }
                    }
                    else {
                        if (!stopRequest) Thread.sleep(500);
                    }
                } catch (Exception e) {
                    Debug.print(e.toString());
                }

            }
            catch (Exception e) {
                Debug.print(e.toString());
            }
        }

        serviceIsRunning = false;
        canSendCommands = false;
        stopRequest = false;
        state = 0;

    }

    public static boolean isServiceRunning () {
        return reference.serviceIsRunning;
    }

    public static boolean canSendCommands () {
        return reference.canSendCommands;
    }

    public static int getAlgorithmState () {
        return reference.state;
    }

    public static void destroy (int typeOfOutput) {
        RemoteControlAlgorithm.stopService();
        if (typeOfOutput == 0) if (!LoadingActivity.getSettings().defaultSettings(reference.activity, false)) Debug.print("Warning, settings not saved on file during logout"); //I set the default values of the settings (except the language) and save them on file
        Device.resetDeviceArray();
        OnOffAlgorithm.destroy();
        DashboardAlgorithm.destroy();
        SettingsFragmentAlgorithm.destroy();
        RemoteControlActivity activity = reference.activity;
        Intent resultIntent = new Intent();
        if (typeOfOutput == 0) resultIntent.putExtra("type_of_output", "disconnection");
        else if (typeOfOutput == 1) resultIntent.putExtra("type_of_output", "application_exit");
        activity.setResult(Activity.RESULT_OK, resultIntent);
        reference.activity = null;
        reference = null;
        activity.finish();
    }

    public static boolean getStopRequest () {
        return reference.stopRequest;
    }

    //to be performed only with the visible RemoteControlActivity
    private static void stopService () {
        reference.stopRequest = true;
        reference.stopForegroundService(reference.activity);
    }

}
