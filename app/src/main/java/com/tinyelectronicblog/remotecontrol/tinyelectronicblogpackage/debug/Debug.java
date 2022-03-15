package com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug;

import android.util.Log;

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

public final class Debug {

    private final static String TAG = "Debug";

    private static boolean debug_is_enabled = false;

    private Debug () {}

    public static void enable () {
        debug_is_enabled = true;
    }

    public static void disable () {
        debug_is_enabled = false;
    }

    public static boolean isEnabled () {return debug_is_enabled;}

    public static void print (String text) {
        if (debug_is_enabled) Log.i(TAG, text);
    }

    public static void print (String text, Exception e) {
        if (debug_is_enabled) Log.i(TAG, text + "\n" + e.toString());
    }

    public static void print (CharSequence text) {
        if (debug_is_enabled) Log.i(TAG, text.toString());
    }

    public static void print (CharSequence text, Exception e) {
        if (debug_is_enabled) Log.i(TAG, text.toString() + "\n" + e.toString());
    }

    public static long millis () {
        return System.currentTimeMillis();
    }

}