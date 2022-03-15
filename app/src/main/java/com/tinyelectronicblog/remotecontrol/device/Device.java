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

import static com.tinyelectronicblog.remotecontrol.Strings.dashboard_unreadable_value;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import com.tinyelectronicblog.remotecontrol.ui.loading.LoadingActivity;
import com.tinyelectronicblog.remotecontrol.ui.remotecontrol.settings.SettingsFragment;
import java.util.ArrayList;
import java.util.List;

/*Each instance identifies an device in the workspace. Each device is associated with an online file,
 containing buttons for OnOffFragment and values to be displayed in DashboardFragment.*/
public class Device {

    //Values displayed in DashboardFragment
    public static final String VALUE_SEPARATOR1 = " is ";
    public static final String VALUE_SEPARATOR2 = ":   ";

    //Values displayed in OnOffFragment
    public static final String DEVICE_NAME = "Device ";

    //Values used for managing files on Google Drive
    public static final String COMMAND_TITLE1 = "To dev. ";
    public static final String tCOMMAND_TITLE2 = " command ";
    public static final String CUS_VALUE = "Cus. value ";
    public static final String MESSAGE = "From Remote control ";
    public static final String TITLE_ACK1 = "From dev. ";
    public static final String TITLE_ACK2 = " ACK ";
    public static final String CUSTOMIZABLE_VALUE = "customizable value";
    public static final String VALUES = "values:";
    public static final String BUTTONS = "buttons:";
    public static final String DEVICE_FILE = "File of device ";
    public static final String FILE_TO_DELETE = "File to delete";

    public static ArrayList<Device> deviceOnline = new ArrayList<>();//All the devices of the workspace
    private static boolean resetWorkspaceRequest = false;

    public ArrayList<DeviceButton> buttons; //Buttons defined in the online file
    String fileID; //Online file ID
    String name; //Device name contained in the title of the online file (excluding DEVICE_FILE)
    String message; //Online file message
    String[] values; //Values defined in the online file
    DateTime createdTime; //Date of creation of the online file

    public Device() {
        fileID = null;
        values = null;
        buttons = null;
        name = null;
        message = null;
        createdTime = null;
    }

    private void onDestroy () {
        name = null;
        fileID = null;
        message = null;
        values = null;
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).onDestroy();
        }
        buttons = null;
        createdTime = null;
    }

    //For each online file of an device it generates an instance of this class, which it stores in deviceOnline.
    public static int updateDeviceFiles() {
        if (LoadingActivity.getGoogleDrive() == null) {
            return -1;
        }
        String q = "trashed = false and mimeType = 'text/plain' and name contains '" + DEVICE_FILE + "' and '" + SettingsFragment.getWorkspaceFolderID() + "' in parents";
        long time1 = 0;
        if (Debug.isEnabled()) time1 = Debug.millis();
        FileList filelist = LoadingActivity.getGoogleDrive().queryFiles(q, null, null, 1000);
        if (filelist == null) {
            return -2;
        }
        if (Debug.isEnabled()) {
            long time2 = System.currentTimeMillis();
            Debug.print("Execution time googleDrive.queryFiles step 1-1-1: "+ (time2-time1) +" ms");
        }
        List<File> folderList = filelist.getFiles();
        if (folderList == null) {
            return -3;
        }
        // I have successfully obtained the id of the searched files, now I recover the contents of the files
        Device[] deviceUpdated = new Device[folderList.size()];
        int i = 0;
        while (i < deviceUpdated.length) {
            if (Debug.isEnabled()) time1 = Debug.millis();
            Pair<File, String> pair = LoadingActivity.getGoogleDrive().readFile(folderList.get(i).getId());
            if (pair == null) {
                return -4;
            }
            deviceUpdated[i] = new Device();
            deviceUpdated[i].fileID = folderList.get(i).getId();
            deviceUpdated[i].createdTime = pair.first.getCreatedTime();
            deviceUpdated[i].name = pair.first.getName().substring(DEVICE_FILE.length());
            deviceUpdated[i].message = pair.second;
            if (Debug.isEnabled()) {
                long time2 = System.currentTimeMillis();
                Debug.print("Execution time googleDrive.readFile step 1-1-2: "+ (time2-time1)+ " ms, " + deviceUpdated[i].name);
            }
            i++;
        }
        //Now I got the updated device file list online
        i = deviceOnline.size();
        while (i > 0) {//Remove from deviceOnline the devices no longer present online
            Device dev = deviceOnline.get(i-1);
            int i2;
            for (i2 = 0; i2 < deviceUpdated.length; i2++) {
                if (dev.name.equals(deviceUpdated[i2].name)) break;
            }
            if (i2 == deviceUpdated.length) { //deviceOnline.get(i) is no longer online, so I'm removing it
                removeDevice(i-1);
            }
            i--;
        }
        // now in deviceOnline there are only those still online
        for (i = 0; i < deviceUpdated.length; i++) { //I add the new ones and update the old ones contained in deviceOnline
            Device devUpdate = deviceUpdated[i];
            String mes = null;
            //since values and buttons, if present, must be contained in the first two lines of the online file, I discard the other lines
            int i3 = deviceUpdated[i].message.indexOf("\n");
            int i4 = -1;
            if (i3 != -1) {//detected first line
                if (i3 != deviceUpdated[i].message.length()-1) {
                    i4 = deviceUpdated[i].message.indexOf("\n", i3 + 1);
                    if (i4 != -1) {//detected second line
                        mes = deviceUpdated[i].message.substring(0, i4); //I consider only the first two lines
                    }
                }
            }
            if (i4 == -1) mes = deviceUpdated[i].message; //consider the entire file
            //reading the values and buttons fields of the device
            devUpdate.values = new String[0];
            devUpdate.buttons = new ArrayList<>();
            i3 = mes.indexOf(VALUES);
            if (i3 != -1) { //if the values line is present
                i3 = i3 + VALUES.length();
                i4 = mes.indexOf(';', i3);
                if (i4 != -1) { //if the values line has been detected correctly
                    String temp = mes.substring(i3, i4).trim();
                    i3 = 0;
                    int i5 = 0;
                    while (i3 < temp.length()) { //save the number of commas in i5
                        if (temp.charAt(i3) == ',') i5++;
                        i3++;
                    }
                    String[] valuest = new String[i5 + 1];
                    i5 = 0;
                    i4 = 0;
                    if (valuest.length > 1) {
                        while (true) {
                            i3 = temp.indexOf(',', i5);
                            if (i3 == -1) break;
                            if (i3 != i5) {//discard values ""
                                String t = temp.substring(i5, i3).trim();
                                if (!t.equals("")) {
                                    valuest[i4] = t;
                                    i4++;
                                }
                            }
                            i5 = i3 + 1;
                        }
                    }
                    //store the last value
                    if (temp.length() - i5 > 0) { //discard values ""
                        String t = temp.substring(i5).trim();
                        if (!t.equals("")) {
                            valuest[i4] = t;
                            i4++;
                        }
                    }
                    if (i4 != 0) { //if there is at least one valid value
                        devUpdate.values = new String[i4];
                        i3 = 0;
                        while (i3 < i4) {
                            devUpdate.values[i3] = valuest[i3];
                            i3++;
                        }
                    }
                }
            }
            i3 = mes.indexOf(BUTTONS);
            if (i3 != -1) { //if the buttons line is present, repeat the same thing done above for the values
                i3 = i3 + BUTTONS.length();
                i4 = mes.indexOf(';', i3);
                if (i4 != -1) {
                    String temp = mes.substring(i3, i4).trim();
                    i3 = 0;
                    int i5 = 0;
                    while (i3 < temp.length()) {
                        if (temp.charAt(i3) == ',') i5++;
                        i3++;
                    }
                    String[] buttonst = new String[i5+1];
                    i5 = 0;
                    i4 = 0;
                    if (buttonst.length > 1) {
                        while (true) {
                            i3 = temp.indexOf(',', i5);
                            if (i3 == -1) break;
                            if (i3 != i5) { //scrap ""
                                String t = temp.substring(i5, i3).trim();
                                if (!t.equals("")) {
                                    buttonst[i4] = t;
                                    i4++;
                                }
                            }
                            i5 = i3+1;
                        }
                    }
                    if (temp.length()-i5 > 0) {
                        String t = temp.substring(i5).trim();
                        if (!t.equals("")) {
                            buttonst[i4] = t;
                            i4++;
                        }
                    }
                    if (i4 != 0) { //if there is at least one valid button
                        i3 = 0;
                        boolean cv = false;
                        while (i3 < i4) {
                            if (buttonst[i3].equals(CUSTOMIZABLE_VALUE)) {
                                if (!cv) {//each device can contain max one button of this type
                                    devUpdate.buttons.add(new DeviceButton(null,true, devUpdate));
                                    cv = true;
                                }
                            }
                            else devUpdate.buttons.add(new DeviceButton(buttonst[i3],false, devUpdate));
                            i3++;
                        }
                    }
                }
            }
            int i2;
            //find devUpdate in deviceOnline
            for (i2 = 0; i2 < deviceOnline.size(); i2++) {
                if (devUpdate.fileID.equals(deviceOnline.get(i2).fileID)) break;
            }
            if (i2 == deviceOnline.size()) { //case devUpdate not yet present in deviceOnline
                addDevice(devUpdate);
            }
            else {//case devUpdate already present in deviceOnline with index i2. i need to update messages, values and buttons
                Device oldDevice = deviceOnline.get(i2);
                //delete buttons no longer present
                i3 = 0;
                while (i3 < oldDevice.buttons.size()) {//for each button of the old device
                    for (i4 = 0; i4 < devUpdate.buttons.size(); i4++) { //for each button of the new device
                        if (oldDevice.buttons.get(i3).name.equals(devUpdate.buttons.get(i4).name)) break;
                    }
                    if (i4 == devUpdate.buttons.size()) {//button no longer present
                        removeButton(i2, i3);
                        continue;
                    }
                    else devUpdate.buttons.remove(i4); //the button is still present, can I not consider the new one read
                    i3++;
                }
                //oldDevice.buttons contains those still online, while devUpdate.buttons contains the new ones to be added
                for (i3 = 0; i3 < devUpdate.buttons.size(); i3++) {
                    addButton(i2, devUpdate.buttons.get(i3));
                }
                //finally I update values and messages
                oldDevice.values = devUpdate.values;
                oldDevice.message = devUpdate.message;
            }
        }
        return 0;
    }

    private static void addDevice(Device device) {
        deviceOnline.add(device);
    }

    private static void removeDevice(int index) {
        (deviceOnline.remove(index)).onDestroy();
    }

    private static void addButton(int indexDevice, DeviceButton button) {
        Device dev = deviceOnline.get(indexDevice);
        button.deviceReference = dev;
        dev.buttons.add(button);
    }

    private static void removeButton(int indexDevice, int indexButton) {
        (deviceOnline.get(indexDevice)).buttons.remove(indexButton).onDestroy();
    }

    //Handles command and ack online files
    public static int updateAckCommandFiles () {
        class file {
            String id, valueName;
            boolean isCommand;
        }
        if (LoadingActivity.getGoogleDrive() == null) {
            return -1;
        }
        String q = "trashed = false and mimeType = 'text/plain' and name contains '" + COMMAND_TITLE1 + "' and name contains '" + tCOMMAND_TITLE2 + "' and '" + SettingsFragment.getWorkspaceFolderID() + "' in parents or trashed = false and mimeType = 'text/plain' and name contains '" + TITLE_ACK1 + "' and name contains '" + TITLE_ACK2 + "' and '" + SettingsFragment.getWorkspaceFolderID() + "' in parents or trashed = false and mimeType = 'text/plain' and name = '"+ FILE_TO_DELETE +"'";
        long time1 = 0;
        if (Debug.isEnabled()) time1 = Debug.millis();
        FileList fileList = LoadingActivity.getGoogleDrive().queryFiles(q, "createdTime", null, 1000);
        if (fileList == null) {
            return -2;
        }
        if (Debug.isEnabled()) {
            long time2 = System.currentTimeMillis();
            Debug.print("Execution time googleDrive.queryFiles step 1-2-1: "+ (time2-time1)+" ms");
        }
        List<File> folderList = fileList.getFiles();
        if (folderList == null) {
            return -3;
        }
        //I got the ids of the files sorted by creation date correctly
        //Each file element is an array containing the commands and acks of an device. The last item contains the files to be removed.
        ArrayList<ArrayList<file>> files = new ArrayList<>();
        for (int i = 0; i <= deviceOnline.size(); i++) {
            files.add(new ArrayList<>());
        }
        // I retrieve the online commands and acks and store them in their dynamic array
        int i = 0;
        while (i < folderList.size()) {// for each command or ack to be read online
            file f = new file();
            f.id= folderList.get(i).getId();
            if (Debug.isEnabled()) time1 = Debug.millis();
            Pair<File, String> pair = LoadingActivity.getGoogleDrive().readFile(f.id);// recover data from a file
            if (pair == null) {
                return -4;
            }
            f.valueName = pair.first.getName();  //file title
            if (Debug.isEnabled()) {
                long time2 = System.currentTimeMillis();
                Debug.print("Execution time googleDrive.readFile step 1-2-2: "+ (time2-time1)+" ms, "+f.valueName);
            }
            if (resetWorkspaceRequest) files.get(deviceOnline.size()).add(f); //in this case all files must be deleted
            else if (f.valueName.equals(FILE_TO_DELETE)) files.get(deviceOnline.size()).add(f); //file to delete
            else {
                // get the name of the related device
                String devName;
                int k1 = f.valueName.indexOf(COMMAND_TITLE1), k2 = f.valueName.indexOf(tCOMMAND_TITLE2);
                if (k1 == -1 || k2 == -1) {// if the file is not a command
                    k1 = f.valueName.indexOf(TITLE_ACK1);
                    k2 = f.valueName.indexOf(TITLE_ACK2);
                    if (k1 == -1 || k2 == -1) {// if the file is not an ack
                        return -5;
                    }
                    else {// if the file is an ack
                        f.isCommand = false;
                        devName = f.valueName.substring(k1 + TITLE_ACK1.length(), k2); //device name
                        f.valueName = f.valueName.substring(k2 + TITLE_ACK2.length()); //ack name
                    }
                }
                else { // if the file is not a command
                    f.isCommand = true;
                    devName = f.valueName.substring(k1 + COMMAND_TITLE1.length(), k2); //device name
                    f.valueName = f.valueName.substring(k2 + tCOMMAND_TITLE2.length()); //command name
                    //if the command is customizable
                    if (f.valueName.indexOf(CUS_VALUE) != -1) f.valueName = CUS_VALUE;
                }
                int i2;
                for (i2 = 0; i2 < deviceOnline.size(); i2++) {
                    if (deviceOnline.get(i2).name.equals(devName)) break;
                }
                ArrayList<file> array = files.get(i2);//array in which to save the file
                //I sort the files by the name of the ack or command (customizable commands are not considered). If the related device is not found, the file is deleted.
                if (i2 != deviceOnline.size()) {//device found
                    if (!f.valueName.equals(CUS_VALUE)) { //not a customizable value
                        int i3 = 0;
                        ArrayList<DeviceButton> buttons = deviceOnline.get(i2).buttons;
                        while (i3 < buttons.size()) {
                            if (f.valueName.equals(buttons.get(i3).name)) break;
                            i3++;
                        }
                        if (i3 == buttons.size()) files.get(deviceOnline.size()).add(f);  //file to be deleted because the corresponding button is no longer there
                        else {//files to be added in an orderly way
                            i3 = array.size();
                            while (i3 > 0) {
                                if (f.valueName.compareTo(array.get(i3 - 1).valueName) >= 0)
                                    break;
                                i3--;
                            }
                            array.add(i3, f);
                        }
                    }
                }
                else array.add(f);
            }
            i++;
        }
        if (resetWorkspaceRequest) {//in this case all files must be deleted
            i = 0;
            ArrayList<file> toDel = files.get(deviceOnline.size());
            while (i < deviceOnline.size()) {
                ArrayList<file> array = files.get(i);
                int i2 = array.size() - 1;
                while (i2 >= 0) {
                    toDel.add(array.remove(i2));
                    i2--;
                }
                i++;
            }
            resetWorkspaceRequest = false;
        }
        /*In each array (except "to delete") the elements are in alphabetical order. The cases of homonymy are further ordered according to the creation date. In the latter cases, if the youngest element is:
            - a command, then I will delete all the other elements (they refer to already verified commands);
            - an ack, then I will delete all elements of the group (they refer to already verified commands).*/
        ArrayList<file> toDelete = files.get(deviceOnline.size());
        i = 0;
        while (i < deviceOnline.size()) {
            ArrayList<file> array = files.get(i);
            int i2 = array.size()-1;
            while (i2 >= 0) {// for each file in the array
                file f = array.get(i2);
                int i3 = i2 - 1;
                while (i3 >= 0) {
                    if (!f.valueName.equals(array.get(i3).valueName)) break;
                    i3--;
                }
                i3++;
                while (i3 < i2) {
                    toDelete.add(array.remove(i2));
                    i2--;
                }
                if (!f.isCommand) toDelete.add(array.remove(i2));
                i2--;
            }
            i++;
        }
        if (Debug.isEnabled()) {
            Debug.print("Commands and ack detected:");
            i = 0;
            while (i < files.size() - 1) {
                ArrayList<file> f = files.get(i);
                int i1 = 0;
                while (i1 < f.size()) {
                    file f2 = f.get(i1);
                    if (f2.isCommand) Debug.print(DEVICE_NAME + deviceOnline.get(i).name + " command: " + f2.valueName);
                    else Debug.print(DEVICE_NAME + deviceOnline.get(i).name + " ack: " + f2.valueName);
                    i1++;
                }
                i++;
            }
            ArrayList<file> f = files.get(i);
            i = 0;
            while (i < f.size()) {
                file f2 = f.get(i);
                if (f2.valueName.equals(FILE_TO_DELETE)) Debug.print("Deleted files with name: " + f2.valueName);
                else if (f2.isCommand) Debug.print("Deleted command: " + f2.valueName);
                else Debug.print("Deleted ack: " + f2.valueName);
                i++;
            }
        }
        // remove unnecessary files from Google Drive
        i = toDelete.size() - 1;
        while (i >= 0) {
            if (Debug.isEnabled()) time1 = Debug.millis();
            if (!LoadingActivity.getGoogleDrive().deleteFile(toDelete.remove(i).id)) {
                return -6;
            }
            if (Debug.isEnabled()) {
                long time2 = System.currentTimeMillis();
                Debug.print("Execution time googleDrive.deleteFile step 1-2-3: "+ (time2-time1)+" ms");
            }
            i--;
        }
        //I update the "enable" property of the buttons
        i = 0;
        while (i < deviceOnline.size()) {
            ArrayList<file> array = files.get(i);
            ArrayList<DeviceButton> buttons = deviceOnline.get(i).buttons;
            for (int i2 = 0; i2 < buttons.size(); i2++) {// for each button of the device
                DeviceButton b = buttons.get(i2);
                if (b.sendingThisCommand) continue; //the state of the button is handled elsewhere
                if (b.isCustomizableValue()) continue; //the state of the button is handled elsewhere
                int i3;
                for (i3 = 0; i3 < array.size(); i3++) {// for each device command
                    if (b.name.equals(array.get(i3).valueName)) break;
                }
                if (i3 == array.size()) {// the button must be reactivated because the related command no longer exists on Google Drive
                    b.statusEnable = true;
                }
                else { // the button must be disabled because the related command on Google Drive exists
                    b.statusEnable = false;
                }
            }
            i++;
        }
        return 0;
    }

    //Returns the device textview to be used in OnOffFragment
    public static TextView textViewOnOffFragment (Context context, Device device) {
        TextView tv = new TextView(context);
        tv.setPadding(0,95,0,15);
        tv.setTextSize(28);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setTypeface(null, Typeface.BOLD);
        final String t = DEVICE_NAME +device.name;
        tv.setText(t);
        return tv;
    }

    //Returns the textview of the device name to be used in DashboardFragment
    public static TextView textViewNameDashboardFragment (Context context, Device device) {
        TextView nametv = new TextView(context);
        nametv.setPadding(0,95,0,0);
        nametv.setTextSize(28);
        nametv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        nametv.setTypeface(null, Typeface.BOLD);
        final String t = DEVICE_NAME +device.name;
        nametv.setText(t);
        return nametv;
    }

    //Returns the textview of the device values to be used in DashboardFragment
    public static TextView textViewValuesDashboardFragment (Context context, Device device) {
        TextView valuestv = new TextView(context);
        valuestv.setTextSize(23);
        valuestv.setTypeface(null, Typeface.BOLD);
        int i = 0;
        StringBuilder v = new StringBuilder();
        String[] values = device.values;
        while (i < values.length) {
            int i1 = values[i].indexOf(VALUE_SEPARATOR1);
            if (i1 == -1) {
                v.append(dashboard_unreadable_value.get(LoadingActivity.getSettings().languageID));
            }
            else {
                v.append(values[i].substring(0, i1).trim());
                v.append(VALUE_SEPARATOR2);
                v.append(values[i].substring(i1+ VALUE_SEPARATOR1.length()).trim());
            }
            v.append("\n");
            i++;
        }
        valuestv.setText(v.toString());
        return valuestv;
    }

    //Deletion of all command and ack files
    public static void resetWorkspaceRequest () {
        resetWorkspaceRequest = true;
    }

    public static void resetDeviceArray() {
        int i = deviceOnline.size() -1;
        while (i >= 0) {
            removeDevice(i);
            i--;
        }
        deviceOnline = new ArrayList<>();
    }

}