package com.jeelpatel.qnachat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jeelpatel.qnachat.databinding.ActivityAuthenticationBinding;

import java.util.Objects;

public class AuthenticationActivity extends AppCompatActivity {

    ActivityAuthenticationBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient gsc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeAuthAndGoogleSignIn();    // Initialize Google Sign in Client and Auth
        initializeTermsAndPrivacyButtons();  // Initialize Terms and Privacy Policy buttons

        // Button Click for Google Sign in And Sign up
        binding.buttonSignUp.setOnClickListener(view -> {
            if (binding.checkPrivacyPolicy.isChecked()) {
                if (binding.checkTermsAndConditions.isChecked()) {
                    signinGoogle();
                } else {
                    initializeSnackbarForCheck(binding.getRoot(), "CHECK TERMS AND CONDITIONS", "CHECK", binding.checkTermsAndConditions);
                }
            } else {
                initializeSnackbarForCheck(binding.getRoot(), "CHECK PRIVACY POLICY", "CHECK", binding.checkPrivacyPolicy);
            }
        });
    }

    private void initializeSnackbarForCheck(View view, String title, String actionTitle, MaterialSwitch checkBox) {
        Snackbar.make(this, view, title, Snackbar.LENGTH_LONG)
                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                .setAction(actionTitle, v -> checkBox.setChecked(true)).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
            overridePendingTransition(1, 1);
            finish();
        }
    }

    @SuppressWarnings("deprecation")
    private void signinGoogle() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.pleaseWait.setVisibility(View.VISIBLE);
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, 100);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account;
            try {
                account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.pleaseWait.setVisibility(View.VISIBLE);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                binding.progressBar.setVisibility(View.VISIBLE);
                                binding.pleaseWait.setVisibility(View.VISIBLE);
                                startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
                                overridePendingTransition(0, 0);
                                finish();
                            } else {
                                binding.progressBar.setVisibility(View.VISIBLE);
                                binding.pleaseWait.setVisibility(View.VISIBLE);
                                initializeSnackbarForCheck(binding.getRoot(), Objects.requireNonNull(task1.getException()).getLocalizedMessage(), null, null);
                                Log.d("TAG", "onActivityResult: " + task1.getException().getLocalizedMessage());
                            }
                        });
            } catch (ApiException e) {
                binding.progressBar.setVisibility(View.GONE);
                binding.pleaseWait.setVisibility(View.GONE);
                Toast.makeText(this, "TRY AGAIN", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeAuthAndGoogleSignIn() {
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
    }

    private void initializeTermsAndPrivacyButtons() {
        binding.privacyPolicyLink.setOnClickListener(view -> {
            String url1 = "https://htmlxprism.blogspot.com/2023/04/qna-chat-app-terms-conditions.html";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url1));
            startActivity(i);
        });

        binding.termsAndConditionsLink.setOnClickListener(view -> {
            String url2 = "https://htmlxprism.blogspot.com/2023/04/qna-chat-app-privacy-policy.html";
            Intent i1 = new Intent(Intent.ACTION_VIEW);
            i1.setData(Uri.parse(url2));
            startActivity(i1);
        });
    }

}