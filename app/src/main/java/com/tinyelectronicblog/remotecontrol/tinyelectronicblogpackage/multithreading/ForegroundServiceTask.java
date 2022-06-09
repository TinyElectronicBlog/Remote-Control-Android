package com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.multithreading;

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
import android.content.Intent;
import android.os.Build;

//Executed on a new thread of a foreground
public abstract class ForegroundServiceTask extends Task {

    Context fromContext;

    public ForegroundServiceTask () {
        super("");
    }

    public ForegroundServiceTask (String name) {
        super(name);
    }

    //it is possible to pass in input null but not the same object.
    public void setNextTask (ForegroundServiceTask task) {
        super.setNextTask(task);
    }

    public void start(Context fromContext) {
        if (super.start(0)) {
            this.fromContext = fromContext;
            ForegroundServiceTaskExecutor.addTask(this);
            Intent intent = new Intent(fromContext, ForegroundServiceTaskExecutor.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ForegroundServiceTaskExecutor.createNotificationChannel(fromContext)) {
                    if (fromContext.startForegroundService(intent) == null) isTaskRunning = false;
                }
                else isTaskRunning = false;
            }
            else if (fromContext.startService(intent) == null) isTaskRunning = false;
        }
    }

    //Stop the service
    public void stopForegroundService(Context context) {
        Intent intent = new Intent(context, ForegroundServiceTaskExecutor.class);
        context.stopService(intent);
    }

}