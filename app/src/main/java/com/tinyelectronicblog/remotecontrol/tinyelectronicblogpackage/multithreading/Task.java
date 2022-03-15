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

import android.os.Handler;
import android.os.Looper;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;

/*This class is designed to execute tasks (portions of code), I hope, in an easy way. A task is started via start() and, until it reaches the end, it cannot be restarted. There are four types of tasks:
- MainThreadTask executed on the main thread;
- NewThreadTask executed on a new thread in parallel;
- ServiceTaskExecutor executed on a new thread of a service;
- ForegroundServiceTask executed on a new thread of a foreground service.
To a task it is possible to bind another one of the same type; this last one is executed on the same thread at the end of the first one.
*/
abstract class Task implements Runnable {

    boolean isTaskRunning;
    Task nextTask;
    String name;

    Task (String name) {
        isTaskRunning = false;
        nextTask = null;
        this.name = name;
    }

    void setNextTask (Task task) {
        if (task != null) if (task.equals(this)) return;
        nextTask = task;
    }

    boolean start (int type) {
        if (!isTaskRunning) {
            isTaskRunning = true;
            switch (type) {
                case 1:
                    new Handler(Looper.getMainLooper()).post(this);
                    break;
                case 2:
                    new Thread(this).start();
                    break;
            }
            return true;
        }
        else return false;
    }

    public void run () {
        try {
            Task nt = this;
            while (true) {
                nt.toDo();
                nt = nt.nextTask;
                if (nt == null) break;
            }
        }
        catch (Exception e) {
            Debug.print("Task " + name + " stopped due to an exception.", e);
        }
        try {isTaskRunning = false;} catch (Exception e) {Debug.print(e.toString());}
    }

    //Instructions for the task to execute.
    public abstract void toDo ();

    //Returns true if the task and, if any, related ones are running. Use this function only on the task started with start().
    final public boolean isRunning() {
        return isTaskRunning;
    }

}