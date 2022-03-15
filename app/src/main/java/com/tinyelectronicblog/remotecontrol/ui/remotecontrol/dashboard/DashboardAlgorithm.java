package com.tinyelectronicblog.remotecontrol.ui.remotecontrol.dashboard;

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

import static com.tinyelectronicblog.remotecontrol.Strings.remotecontrol_drive_failed;
import static com.tinyelectronicblog.remotecontrol.device.Device.deviceOnline;
import static com.tinyelectronicblog.remotecontrol.Strings.dashboard_no_device;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_drive_loading;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.tinyelectronicblog.remotecontrol.device.Device;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.multithreading.MainThreadTask;
import com.tinyelectronicblog.remotecontrol.ui.loading.LoadingActivity;
import com.tinyelectronicblog.remotecontrol.ui.remotecontrol.RemoteControlAlgorithm;

public class DashboardAlgorithm extends MainThreadTask {

    private static DashboardAlgorithm reference = null;

    private DashboardFragment fragment;

    private DashboardAlgorithm (String name) {
        super(name);
    }

    static void onResume (DashboardFragment fragment) {
        if (reference == null) {
            reference = new DashboardAlgorithm("DashboardAlgorithm");
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
            if (RemoteControlAlgorithm.getAlgorithmState() == 1 && deviceOnline.size() != 0) { //if there are device files online
                int i = 0;
                TextView[] names = new TextView[deviceOnline.size()];
                TextView[] values = new TextView[deviceOnline.size()];
                while (i < deviceOnline.size()) {
                    Device device = deviceOnline.get(i);
                    names[i] = Device.textViewNameDashboardFragment(fragment.getContext(), device);
                    values[i] = Device.textViewValuesDashboardFragment(fragment.getContext(), device);
                    i++;
                }
                i = 0;
                layout.removeAllViews(); //if present I eliminate the old elements
                while (i < deviceOnline.size()) {
                    layout.addView(names[i]);
                    layout.addView(values[i]);
                    i++;
                }
            }
            else {
                TextView tv = new TextView(fragment.getContext());
                tv.setPadding(0, 95, 0, 15);
                tv.setTextSize(28);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tv.setTypeface(null, Typeface.BOLD);
                if (RemoteControlAlgorithm.getAlgorithmState() == 1 && deviceOnline.size() == 0) tv.setText(dashboard_no_device.get(LoadingActivity.getSettings().languageID));
                else {
                    if (RemoteControlAlgorithm.getAlgorithmState() == 0) {
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

    public static void destroy () {
        reference = null;
    }

}
