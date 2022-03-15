package com.tinyelectronicblog.remotecontrol.ui.remotecontrol.settings;

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

import static com.tinyelectronicblog.remotecontrol.Settings.updateDataDEFAULT;
import static com.tinyelectronicblog.remotecontrol.Settings.updateDataMAX;
import static com.tinyelectronicblog.remotecontrol.Settings.updateDataMIN;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_authenticated_as;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_disconnect;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_exit;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_id;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_languages;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_reset_workspace;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_set_language;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_set_language_toast;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_set_values;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_set_values_toast1;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_set_values_toast2;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_set_values_toast3;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_set_values_toast5;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_update_interval;
import static com.tinyelectronicblog.remotecontrol.Strings.settings_workspace_folder;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tinyelectronicblog.remotecontrol.Settings;
import com.tinyelectronicblog.remotecontrol.device.Device;
import com.tinyelectronicblog.remotecontrol.databinding.FragmentSettingsBinding;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import com.tinyelectronicblog.remotecontrol.ui.loading.LoadingActivity;
import com.tinyelectronicblog.remotecontrol.ui.remotecontrol.RemoteControlAlgorithm;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    FragmentSettingsBinding binding;
    ImageView logo;
    TextView textViewlogo;
    Button disconnectButton, exitButton;
    EditText workspaceFolder;
    EditText RemoteID;
    EditText updateData;
    TextView languagesText;
    Spinner spinnerLanguages;
    ArrayAdapter<CharSequence> spinnerLanguagesAdapter;
    Button setValuesButton;
    Button setLanguageButton;
    Button resetWorkspace;

    static String TEBFolderID;
    static String remoteControlConfigFileID; //remote control configuration file ID
    static String workspaceFolderID;
    static Bitmap profilePhoto;
    static String profileText;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textViewlogo = binding.ProfileLabel;
        textViewlogo.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        disconnectButton = binding.DisconnectButton;
        disconnectButton.setOnClickListener(this);

        exitButton = binding.ExitButton;
        exitButton.setOnClickListener(this);

        setLanguageButton = binding.SetLanguageButton;
        setLanguageButton.setOnClickListener(this);

        workspaceFolder = binding.WorkspaceFolder;

        RemoteID = binding.RemoteID;

        updateData = binding.UpdateInterval;

        languagesText = binding.LanguagesLabel;

        setValuesButton = binding.SetValuesButton;
        setValuesButton.setOnClickListener(this);

        resetWorkspace = binding.ResetWorkspaceButton;
        resetWorkspace.setOnClickListener(this);

        spinnerLanguages = binding.SpinnerLanguages;
        spinnerLanguagesAdapter = new ArrayAdapter<>(root.getContext(), android.R.layout.simple_spinner_item);
        spinnerLanguagesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguagesAdapter.addAll(Settings.availableLanguages);
        int i = 0;
        while (!LoadingActivity.getSettings().language.equals(Settings.availableLanguages[i])) i++;
        spinnerLanguages.setAdapter(spinnerLanguagesAdapter);
        spinnerLanguages.setSelection(i);

        logo = binding.ProfilePicture;

        this.setLanguageUI(i);

        try {
            logo.setImageBitmap(profilePhoto);
            textViewlogo.setText(profileText);
        }
        catch (Exception e) {Debug.print(e.toString());}

        // This callback will only be called when MyFragment is at least Started. It disable the back button
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        SettingsFragmentAlgorithm.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SettingsFragmentAlgorithm.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        logo = null;
        textViewlogo = null;
        disconnectButton = null;
        exitButton = null;
        workspaceFolder = null;
        RemoteID = null;
        updateData = null;
        languagesText = null;
        spinnerLanguages = null;
        spinnerLanguagesAdapter = null;
        setValuesButton = null;
        resetWorkspace = null;
        setLanguageButton = null;
        binding = null;
    }

    @Override
    public void onClick(View v) {
        if(v.equals(setValuesButton)) {
            // check the correctness of the workspaceFolder and remote control ID values and, if ok, start the task for managing the online folder of teb
            setButtonState(false); //disable buttons
            String cf = workspaceFolder.getText().toString().trim(), id = RemoteID.getText().toString().trim();
            if (cf.equals("")) {
                Toast.makeText(binding.getRoot().getContext(), settings_set_values_toast1.get(LoadingActivity.getSettings().languageID), Toast.LENGTH_LONG).show();
                setButtonState(true);
                return;
            }
            else if (id.equals("")) {
                Toast.makeText(binding.getRoot().getContext(), settings_set_values_toast2.get(LoadingActivity.getSettings().languageID), Toast.LENGTH_LONG).show();
                setButtonState(true);
                return;
            }
            else {
                LoadingActivity.getSettings().workspaceFolder = cf;
                LoadingActivity.getSettings().controllerID = id;
                int ud;
                try { ud = Integer.parseInt(updateData.getText().toString()); }
                catch (NumberFormatException e) {ud = updateDataDEFAULT;}
                if (ud < updateDataMIN) ud = updateDataMIN;
                else if (ud > updateDataMAX) ud = updateDataMAX;
                LoadingActivity.getSettings().dataUpdate = ""+ud;
                if (!updateData.getText().toString().equals(LoadingActivity.getSettings().dataUpdate)) updateData.setText(LoadingActivity.getSettings().dataUpdate);
                if (!LoadingActivity.getSettings().saveSettings(binding.getRoot().getContext())) {
                    Debug.print("Warning, settings not saved on file");
                    Toast.makeText(binding.getRoot().getContext(), settings_set_values_toast5.get(LoadingActivity.getSettings().languageID).toString(), Toast.LENGTH_LONG).show();
                }
                else Toast.makeText(binding.getRoot().getContext(), settings_set_values_toast3.get(LoadingActivity.getSettings().languageID).toString(), Toast.LENGTH_LONG).show();
                SettingsFragmentAlgorithm.start(2);
            }
        }
        else if (v.equals(disconnectButton)) {
            setButtonState(false); //disable buttons for security
            RemoteControlAlgorithm.destroy(0);
        }
        else if (v.equals(exitButton)) {
            setButtonState(false); //disable buttons for security
            RemoteControlAlgorithm.destroy(1);
        }
        else if (v.equals(setLanguageButton)) { //change ui language
            int pos = spinnerLanguages.getSelectedItemPosition();
            if (LoadingActivity.getSettings().languageID != pos) {
                LoadingActivity.getSettings().language = Settings.availableLanguages[pos];
                LoadingActivity.getSettings().languageID = pos;
                setLanguageUI(pos);
                if (LoadingActivity.getSettings().saveSettings(binding.getRoot().getContext())) Toast.makeText(binding.getRoot().getContext(), settings_set_language_toast.get(pos), Toast.LENGTH_LONG).show();
                else Debug.print("Warning, settings not saved on file during language switching");
            }
        }
        else if (v.equals(resetWorkspace)) {
            Device.resetWorkspaceRequest();
        }
    }

    void setLanguageUI(int language) {
        disconnectButton.setText(settings_disconnect.get(language));
        exitButton.setText(settings_exit.get(language));
        workspaceFolder.setHint(settings_workspace_folder.get(language));
        if (LoadingActivity.getSettings().workspaceFolder != null) workspaceFolder.setText(LoadingActivity.getSettings().workspaceFolder);
        else workspaceFolder.setText("");
        RemoteID.setHint(settings_id.get(language));
        if (LoadingActivity.getSettings().controllerID != null) RemoteID.setText(LoadingActivity.getSettings().controllerID);
        else RemoteID.setText("");
        updateData.setHint(settings_update_interval.get(language));
        if (LoadingActivity.getSettings().dataUpdate != null) updateData.setText(LoadingActivity.getSettings().dataUpdate);
        else updateData.setText("");
        languagesText.setText(settings_languages.get(language));
        setValuesButton.setText(settings_set_values.get(language));
        resetWorkspace.setText(settings_reset_workspace.get(language));
        setLanguageButton.setText(settings_set_language.get(language));
        profileText = createProfileText(LoadingActivity.getSettings().languageID);
        textViewlogo.setText(profileText);
    }

    void setButtonState(boolean state) {
        if (setValuesButton.isEnabled() != state) setValuesButton.setEnabled(state);
        if (resetWorkspace.isEnabled() != state) resetWorkspace.setEnabled(state);
        if (disconnectButton.isEnabled() != state) disconnectButton.setEnabled(state);
        if (exitButton.isEnabled() != state) exitButton.setEnabled(state);
    }

    String createProfileText(int language) {
        StringBuilder t = new StringBuilder();
        t.append(settings_authenticated_as.get(language));
        String t2 = LoadingActivity.getGoogleAccout().getDisplayName();
        if (t2 != null) {
            t.append("\n");
            t.append(t2);
        }
        t2 = LoadingActivity.getGoogleAccout().getEmail();
        if (t2 != null) {
            t.append("\n");
            t.append(t2);
        }
        return t.toString();
    }
    public static String getWorkspaceFolderID () {
        return workspaceFolderID;
    }

    public static String getRemoteControlConfigFileID () {
        return remoteControlConfigFileID;
    }

    public static String getTEBFolderID () {
        return TEBFolderID;
    }

}