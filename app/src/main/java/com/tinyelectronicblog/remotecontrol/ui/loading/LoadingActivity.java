package com.tinyelectronicblog.remotecontrol.ui.loading;

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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.Task;
import com.tinyelectronicblog.remotecontrol.R;
import com.tinyelectronicblog.remotecontrol.Settings;
import com.tinyelectronicblog.remotecontrol.databinding.ActivityLoadingBinding;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.google.drive.DriveServiceHelper;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.google.login.GoogleLoginHelper;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.gui.dialog.SimpleDialog;

public class LoadingActivity extends AppCompatActivity implements View.OnClickListener {

    TextView loading;
    Button restartButton;
    SignInButton signInButton;
    GoogleLoginHelper googleLoginHelper;
    SimpleDialog dialogueIncorrectSettings;
    SimpleDialog exitDialogue;

    static Settings settings;
    static GoogleSignInAccount googleAccout;
    static DriveServiceHelper googleDrive;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoadingBinding binding = ActivityLoadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loading = binding.Loading;
        loading.setVisibility(View.INVISIBLE);

        signInButton = binding.SignInButton;
        signInButton.setOnClickListener(this);
        signInButton.setVisibility(View.INVISIBLE);

        restartButton = binding.RestartButton;
        restartButton.setOnClickListener(this);
        restartButton.setVisibility(View.INVISIBLE);

        LoadingActivity ref = this;
        googleLoginHelper = new GoogleLoginHelper(this) {
            @Override
            public void onSuccessGoogleSignIn() {
                LoadingActivityAlgorithm.getReference().startRemoteControlActivity();
            }
            @Override
            public void onFailureGoogleSignIn() {
                try {
                    updateUI(false, true);
                    Toast.makeText(ref, "Authentication failed", Toast.LENGTH_LONG);
                }
                catch (Exception e) {Debug.print(e.toString());}
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadingActivityAlgorithm.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LoadingActivityAlgorithm.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loading = null;
        restartButton = null;
        signInButton = null;
        googleLoginHelper = null;
        dialogueIncorrectSettings = null;
        exitDialogue = null;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(restartButton)) { //I pressed the button to restart the application
            LoadingActivityAlgorithm.getReference().startRemoteControlActivity();
        }
        else if (v.equals(signInButton)){ //I pressed the button to log in
            googleLoginHelper.newSignIn();
        }
    }

    void updateUI (boolean LoAdInGVisible, boolean SignInButtonVisible) {
        if (LoAdInGVisible && loading.getVisibility() != View.VISIBLE) loading.setVisibility(View.VISIBLE);
        else if (!LoAdInGVisible && loading.getVisibility() != View.INVISIBLE) loading.setVisibility(View.INVISIBLE);
        if (SignInButtonVisible && signInButton.getVisibility() != View.VISIBLE) signInButton.setVisibility(View.VISIBLE);
        else if (!SignInButtonVisible && signInButton.getVisibility() != View.INVISIBLE) signInButton.setVisibility(View.INVISIBLE);
    }

    void setLoadingState (int state) {
        switch (state) {
            case 0:
                loading.setText(R.string.loading_loading0);
                break;
            case 1:
                loading.setText(R.string.loading_loading1);
                break;
            case 2:
                loading.setText(R.string.loading_loading2);
                break;
            case 3:
                loading.setText(R.string.loading_loading3);
                break;
            case 4:
                loading.setText(R.string.loading_loading4);
                break;
            case 5:
                loading.setText(R.string.loading_loading5);
                break;
            case 6:
                loading.setText(R.string.loading_loading6);
                break;
        }
    }

    public static Settings getSettings () {
        return settings;
    }

    public static GoogleSignInAccount getGoogleAccout () {
        return googleAccout;
    }

    public static DriveServiceHelper getGoogleDrive () {
        return googleDrive;
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            String returnValue = result.getData().getStringExtra("type_of_output");
                            if (returnValue.equals("disconnection")) {
                                googleLoginHelper.revokeAccess();
                                googleAccout = null;
                                googleDrive = null;
                                LoadingActivityAlgorithm.start(3);
                            }
                            else if (returnValue.equals("application_exit")) {
                                settings = null;
                                googleAccout = null;
                                googleDrive = null;
                                LoadingActivityAlgorithm.destroy();
                                finishAndRemoveTask();
                            }
                        }
                        catch (Exception e) {
                            Debug.print(e.toString());
                            settings = null;
                            googleAccout = null;
                            googleDrive = null;
                            LoadingActivityAlgorithm.destroy();
                            finishAndRemoveTask();
                        }
                    //}
                }
            });

}