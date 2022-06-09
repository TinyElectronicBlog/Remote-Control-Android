package com.tinyelectronicblog.remotecontrol.device;

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

import static com.tinyelectronicblog.remotecontrol.device.Device.MESSAGE;
import static com.tinyelectronicblog.remotecontrol.device.Device.CUS_VALUE;
import static com.tinyelectronicblog.remotecontrol.device.Device.COMMAND_TITLE1;
import static com.tinyelectronicblog.remotecontrol.device.Device.tCOMMAND_TITLE2;
import static com.tinyelectronicblog.remotecontrol.Strings.on_off_more_command;
import static com.tinyelectronicblog.remotecontrol.Strings.on_off_null_value;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.tinyelectronicblog.remotecontrol.Strings;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.multithreading.MainThreadTask;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.multithreading.NewThreadTask;
import com.tinyelectronicblog.remotecontrol.ui.loading.LoadingActivity;
import com.tinyelectronicblog.remotecontrol.ui.remotecontrol.RemoteControlAlgorithm;
import com.tinyelectronicblog.remotecontrol.ui.remotecontrol.on_off.OnOffAlgorithm;
import com.tinyelectronicblog.remotecontrol.ui.remotecontrol.settings.SettingsFragment;

//Button used by the Device class
public class DeviceButton implements View.OnClickListener {

    //true if you are sending a command
    public static boolean sendingCommand = false;

    //true if the button is enabled
    public boolean statusEnable;
    //true if you are sending the command related to this button
    boolean sendingThisCommand;
    //true if the command for this button is customizable
    boolean isCustomizableValue;
    //button name
    String name;
    //reference to the Adevice of the button
    Device deviceReference;

    EditText editText;
    Button button;

    public DeviceButton(String name, boolean isCustomizableValue, Device deviceReference) {
        this.deviceReference = deviceReference;
        this.statusEnable = true;
        this.isCustomizableValue = isCustomizableValue;
        this.sendingThisCommand = false;
        if (isCustomizableValue) {
            this.name = Strings.on_off_send_value.get(LoadingActivity.getSettings().languageID).toString();
        } else {
            this.name = name;
        }
    }

    public void onDestroy() {
        if (button != null) button.setOnClickListener(null);
        editText = null;
        button = null;
        name = null;
        deviceReference = null;
    }

    public boolean isCustomizableValue() {
        return isCustomizableValue;
    }

    public CharSequence getText() {
        return name;
    }

    //Returns the textview to be used in OnOffFragment. Only needed with customizable commands.
    public EditText getEditText(Context context) {
        if (!isCustomizableValue) return null;
        EditText et = new EditText(context);
        et.setTextSize(20);
        et.setHint(Strings.on_off_enter_value.get(LoadingActivity.getSettings().languageID));
        editText = et;
        return et;
    }

    //Returns the button to be used in OnOffFragment.
    public Button getButton(Context context) {
        Button b = new Button(context);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setOnClickListener(this);
        if (isCustomizableValue) {
            b.setText(Strings.on_off_send_value.get(LoadingActivity.getSettings().languageID));
        } else {
            b.setText(name);
        }
        if (button != null) button.setOnClickListener(null);
        button = b;
        return b;
    }

    //Executed when pressing the Button of this instance.
    @Override
    public void onClick(View v) {
        if (sendingCommand) {//Only one command can be sent at a time
            try {
                Toast.makeText(OnOffAlgorithm.getContext(), on_off_more_command.get(LoadingActivity.getSettings().languageID), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Debug.print(e.toString());
            }
            return;
        }
        final String cvValue;
        try {
            sendingCommand = true;
            sendingThisCommand = true;
            if (isCustomizableValue) {//read the custom value to send
                cvValue = editText.getText().toString().trim();
                if (cvValue.equals("")) {
                    Toast.makeText(OnOffAlgorithm.getContext(), on_off_null_value.get(LoadingActivity.getSettings().languageID), Toast.LENGTH_LONG).show();
                    sendingThisCommand = false;
                    sendingCommand = false;
                    return;
                }
            }
            else cvValue = null;
            statusEnable = false;
            button.setEnabled(false);
            OnOffAlgorithm.setCheckboxStatus(false, 1, true);
        }
        catch (Exception e) {//if sending fails
            Debug.print(e.toString());
            try {
                statusEnable = true;
                sendingThisCommand = false;
                OnOffAlgorithm.setCheckboxStatus(false, 3, true);
            } catch (Exception err) {
                Debug.print(err.toString());
            }
            sendingCommand = false;
            return;
        }
        new NewThreadTask() {//send the value in the background when possible
            @Override
            public void toDo() {
                int sendingStatus = 0;
                try {
                    while (!RemoteControlAlgorithm.canSendCommands()) {//I wait until I can send the command
                        Thread.sleep(3);
                        if (RemoteControlAlgorithm.getStopRequest()) {
                            try {sendingThisCommand = false;} catch (Exception err) {Debug.print(err.toString());}
                            sendingCommand = false;
                            return;
                        }
                    }
                    if (LoadingActivity.getGoogleDrive() != null) {
                        String fileId = LoadingActivity.getGoogleDrive().createFile(Device.FILE_TO_DELETE, SettingsFragment.getWorkspaceFolderID(), false);
                        if (fileId != null) {
                            if (isCustomizableValue) {
                                if (!LoadingActivity.getGoogleDrive().updateFile(fileId, COMMAND_TITLE1 + deviceReference.name + tCOMMAND_TITLE2 + CUS_VALUE + cvValue, MESSAGE + LoadingActivity.getSettings().controllerID, null, null)) sendingStatus = 1;
                            }
                            else {
                                if (!LoadingActivity.getGoogleDrive().updateFile(fileId, COMMAND_TITLE1 + deviceReference.name + tCOMMAND_TITLE2 + name, MESSAGE + LoadingActivity.getSettings().controllerID, null, null)) sendingStatus = 2;
                            }
                        }
                        else sendingStatus = 3;
                    }
                    else sendingStatus = 4;
                }
                catch (Exception e) {
                    Debug.print(e.toString());
                    sendingStatus = 5;
                }
                final int finalSendingStatus = sendingStatus;
                new MainThreadTask() {
                    @Override
                    public void toDo() {
                        try {
                            switch (finalSendingStatus) {
                                case 0://command sent
                                    if (isCustomizableValue) {
                                        try {
                                            statusEnable = true;
                                            button.setEnabled(true);
                                            editText.setText("");
                                        } catch (Exception e) {
                                            Debug.print(e.toString());
                                        }
                                        Debug.print("Sent command \"" + COMMAND_TITLE1 + deviceReference.name + tCOMMAND_TITLE2 + CUS_VALUE + cvValue + "\" with the message \"" + MESSAGE + LoadingActivity.getSettings().controllerID + "\"");
                                    } else {
                                        Debug.print("Sent command \"" + COMMAND_TITLE1 + deviceReference.name + tCOMMAND_TITLE2 + name + "\" with the message \"" + MESSAGE + LoadingActivity.getSettings().controllerID + "\"");
                                        //the button will be reactivated by ack from the device
                                    }
                                    try {OnOffAlgorithm.setCheckboxStatus(true, 2, true);} catch (Exception e) {Debug.print(e.toString());}
                                    try {//hiding the keyboard
                                        InputMethodManager imm = (InputMethodManager) OnOffAlgorithm.getFragmentReference().getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                    } catch (Exception e) {Debug.print(e.toString());}
                                    try {sendingThisCommand = false;} catch (Exception e) {Debug.print(e.toString());}
                                    break;
                                default://command not send
                                    sendingThisCommand = false;
                                    statusEnable = true;
                                    button.setEnabled(true);
                                    OnOffAlgorithm.setCheckboxStatus(false, 3, true);
                            }
                        } catch (Exception e) {
                            Debug.print(e.toString());
                        }
                        sendingCommand = false;
                    }
                }.start();
            }
        }.start();
    }

}
