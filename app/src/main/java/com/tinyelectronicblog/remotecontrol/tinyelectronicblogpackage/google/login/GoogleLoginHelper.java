package com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.google.login;

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

import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

/**
 * This class is designed to facilitate authentication to Google services and
 * was written according to the guide at https://developers.google.com/identity/sign-in/android/start-integrating.
 * One of its objects should be instantiated in the onCreate of an activity, while updateSignedInAccounts, newSignIn and revokeAccess should be used in the activity's lifetime between onStart and onPause.
 * Example use of the class:
 * 1) execute updateLastSignedInAccount;
 * 2) if getLastSignedInAccount returns:
 * 	- an account, it is valid;
 * 	- null, you need to use newSignIn and then repeat step 2;
 * 3) use revokeAcces to log out.
 * After each successful authentication with newSignIn, onSuccessGoogleSignIn is executed, otherwise onFailureGoogleSignIn.
 * Warning, not all exceptions are handled.
 */
public abstract class GoogleLoginHelper {

    private final ComponentActivity activity;
    private final ActivityResultLauncher<Intent> onActivityResult;
    private final GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;

    public GoogleLoginHelper(ComponentActivity activity) {
        this.activity = activity;
        onActivityResult = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            handleSignInResult(task);
        });
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_FULL))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with access to the Google Sign-In API and the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    /**
     * Updates the information on the last authenticated account.
     */
    public void updateLastSignedInAccount() {
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account_t = GoogleSignIn.getLastSignedInAccount(activity);
        if (account_t != null) {
            if (GoogleSignIn.hasPermissions(account_t, new Scope(Scopes.DRIVE_FULL))) {
                account = account_t;
                return;
            }
        }
        account = null;
    }

    /**
     * Start the authentication procedure for Google services.
     */
    public void newSignIn () {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        onActivityResult.launch(signInIntent);
    }

    private void handleSignInResult(@Nullable Task<GoogleSignInAccount> completedTask) {
        if (completedTask != null) {
            if (completedTask.isSuccessful()) {
                updateLastSignedInAccount();
                if (account != null) {
                    onSuccessGoogleSignIn();
                    return;
                }
            }
        }
        account = null;
        onFailureGoogleSignIn();
    }

    /**
     * Function performed after each successful authentication procedure.
     */
    public abstract void onSuccessGoogleSignIn();

    /**
     * Function performed after each failed authentication procedure.
     */
    public abstract void onFailureGoogleSignIn();

    /**
     * Logs out.
     */
    public boolean revokeAccess () {
        mGoogleSignInClient.signOut();
        account = null;
        return mGoogleSignInClient.revokeAccess().isSuccessful();
    }

    /**
     * Returns the last correctly authenticated account, otherwise null.
     */
    public GoogleSignInAccount getLastSignedInAccount () {
        return account;
    }

}