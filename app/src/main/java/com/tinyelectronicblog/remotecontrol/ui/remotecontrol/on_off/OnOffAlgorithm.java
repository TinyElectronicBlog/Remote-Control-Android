package com.tinyelectronicblog.remotecontrol.ui.remotecontrol.on_off;

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

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.tinyelectronicblog.remotecontrol.Strings.remotecontrol_drive_failed;
import static com.tinyelectronicblog.remotecontrol.device.Device.deviceOnline;
import static com.tinyelectronicblog.remotecontrol.Strings.dashboard_no_device;
import static com.tinyelectronicblog.remotecontrol.Strings.on_off_command_sent;
import static com.tinyelectronicblog.remotecontrol.Strings.on_off_ready;
import static com.tinyelectronicblog.remotecontrol.Strings.on_off_send_command;
import static com.tinyelectronicblog.remotecontrol.Strings.on_off_send_failed;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_loading;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tinyelectronicblog.remotecontrol.device.Device;
import com.tinyelectronicblog.remotecontrol.device.DeviceButton;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.multithreading.MainThreadTask;
import com.tinyelectronicblog.remotecontrol.ui.loading.LoadingActivity;
import com.tinyelectronicblog.remotecontrol.ui.remotecontrol.RemoteControlAlgorithm;
import com.tinyelectronicblog.remotecontrol.ui.remotecontrol.settings.SettingsFragmentAlgorithm;

public class OnOffAlgorithm extends MainThreadTask {

    private static OnOffAlgorithm reference = null;

    private OnOffFragment fragment;

    private int checkboxText;
    private boolean checkboxSelected;

    private OnOffAlgorithm(String name) {
        super(name);
        checkboxText = 0;
        checkboxSelected = true;
    }

    static void onResume(OnOffFragment fragment) {
        if (reference == null) {
            reference = new OnOffAlgorithm("OnOffAlgorithm");
        }

        reference.fragment = fragment;

        reference.toDo();
    }

    static void onPause() {
        if (reference != null) reference.fragment = null;
    }

    @Override
    public void toDo() {
        //if fragment becomes null everything is ok, this means that it is not displayed anymore
        try {
            LinearLayout layout = fragment.binding.layout;
            if (RemoteControlAlgorithm.getAlgorithmState() == 1 && deviceOnline.size() != 0) {//if there are device files online
                int i = 0;
                TextView[] names = new TextView[deviceOnline.size()];
                Button[][] buttons = new Button[deviceOnline.size()][];
                EditText[] etcv = new EditText[deviceOnline.size()];
                Button[] buttoncv = new Button[deviceOnline.size()];
                while (i < deviceOnline.size()) {
                    Device device = deviceOnline.get(i);
                    names[i] = Device.textViewOnOffFragment(fragment.getContext(), device);
                    int i2 = 0;
                    buttons[i] = new Button[device.buttons.size()];
                    boolean cv = false;
                    while (i2 < device.buttons.size()) {
                        DeviceButton button = device.buttons.get(i2);
                        if (button.isCustomizableValue()) {
                            if (!cv) {
                                cv = true;
                                buttoncv[i] = button.getButton(fragment.getContext());
                                buttoncv[i].setEnabled(button.statusEnable);
                                etcv[i] = button.getEditText(fragment.getContext());
                            }
                            buttons[i][i2] = null;
                        }
                        else {
                            buttons[i][i2] = button.getButton(fragment.getContext());
                            buttons[i][i2].setEnabled(button.statusEnable);
                        }
                        i2++;
                    }
                    if (!cv) buttoncv[i] = null;
                    i++;
                }
                layout.removeAllViews(); //if present I eliminate the old elements
                setCheckboxStatus(checkboxSelected, checkboxText, true);
                i = 0;
                while (i < deviceOnline.size()) {
                    layout.addView(names[i]);
                    int i2 = 0;
                    while (i2 < buttons[i].length) {
                        if (buttons[i][i2] != null) {
                            layout.addView(buttons[i][i2]);
                        }
                        i2++;
                    }
                    if (buttoncv[i] != null) {
                        layout.addView(etcv[i]);
                        layout.addView(buttoncv[i]);
                    }
                    i++;
                }
            }
            else {
                setCheckboxStatus(true, 0, false);
                TextView tv = new TextView(fragment.getContext());
                tv.setPadding(0, 95, 0, 15);
                tv.setTextSize(28);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tv.setTypeface(null, Typeface.BOLD);
                if (RemoteControlAlgorithm.getAlgorithmState() == 1 && deviceOnline.size() == 0) tv.setText(dashboard_no_device.get(LoadingActivity.getSettings().languageID));
                else {
                    if (RemoteControlAlgorithm.getAlgorithmState() == 0 && !SettingsFragmentAlgorithm.isDriveReady()) {
                        tv.setText(settings_drive_loading.get(LoadingActivity.getSettings().languageID));
                    }
                    else tv.setText(remotecontrol_drive_failed.get(LoadingActivity.getSettings().languageID));
                }
                layout.removeAllViews(); //if present I eliminate the old elements
                layout.addView(tv);
            }
        }
        catch (Exception e) {
            Debug.print(e.toString());
        }
    }

    // update the ui
    public static void updateUI () {
        reference.start();
    }

    public static Context getContext () {
        return reference.fragment.getContext();
    }

    public static void setCheckboxStatus (boolean state, int text, boolean visibile) {
        final CharSequence t;
        switch (text) {
            case 0:
                t = on_off_ready.get(LoadingActivity.getSettings().languageID);
                break;
            case 1:
                t = on_off_send_command.get(LoadingActivity.getSettings().languageID);
                break;
            case 2:
                t = on_off_command_sent.get(LoadingActivity.getSettings().languageID);
                break;
            case 3:
                t = on_off_send_failed.get(LoadingActivity.getSettings().languageID).toString();
                break;
            default:
                t = "";
        }
        reference.checkboxText = text;
        reference.checkboxSelected = state;
        CheckBox b = reference.fragment.checkBox;
        b.setText(t);
        b.setChecked(reference.checkboxSelected);
        if (visibile) b.setVisibility(VISIBLE);
        else b.setVisibility(INVISIBLE);
    }

    public static OnOffFragment getFragmentReference () {
        return reference.fragment;
    }

    public static void destroy () {
        reference = null;
    }

}