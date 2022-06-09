package com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.file;

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

import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * A simple class for reading and writing files in the device memory.
 */
public final class FileIO {

    private FileIO() {}

    /**
     * Writes the file. Returns true on success.
     */
    public static boolean write (Context context, String filename, String text) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(text.getBytes(StandardCharsets.UTF_8));
            fos.close();
            return true;
        }
        catch (Exception e) {
            final String msg = "Error writing file with name " + filename + ".";
            Debug.print(msg, e);
            try {if (fos != null) fos.close();} catch (Exception err) {Debug.print(msg, err);}
        }
        return false;
    }

    /**
     * Returns the contents of the read file if successful, otherwise null.
     */
    public static String read (Context context, String filename) {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
            fis.close();
            return stringBuilder.toString();
        }
        catch (Exception e) {
            final String msg = "Error reading file with name " + filename + ".";
            Debug.print(msg, e);
            try {if (fis != null) fis.close();} catch (Exception err) {Debug.print(msg, err);}
        }
        return null;
    }

}