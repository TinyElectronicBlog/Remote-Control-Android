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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class ServiceTaskExecutor extends Service {

    private static final ArrayList<ServiceTask> toExecute = new ArrayList<>();

    static void addTask (ServiceTask task) {
        toExecute.add(task);
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        while (true) {
            try {
                ServiceTask task = toExecute.remove(0);
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        task.run();
                    }
                };
                t.start();
            }
            catch(IndexOutOfBoundsException e) {break;}
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }

}