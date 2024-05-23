package com.chimple.parentalcontrol.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.chimple.parentalcontrol.R;
import com.chimple.parentalcontrol.databinding.ActivityAuthBinding;
import com.chimple.parentalcontrol.databinding.ClForgotPasswordLayoutBinding;
import com.chimple.parentalcontrol.databinding.ClPinLayoutBinding;
import com.chimple.parentalcontrol.firebase.Constant;
import com.chimple.parentalcontrol.model.UserModel;
import com.chimple.parentalcontrol.services.PersistentForegroundService;
import com.chimple.parentalcontrol.util.CProgressDialog;
import com.chimple.parentalcontrol.util.LocalPreference;
import com.chimple.parentalcontrol.util.MyDialog;
import com.chimple.parentalcontrol.util.VUtil;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());





        auth = FirebaseAuth.getInstance();


        if (auth.getCurrentUser() != null || !LocalPreference.getPin().equals("")) {
            startActivity(new Intent(AuthActivity.this, MainActivity.class));
            finish();
        }


        binding.tvSkip.setOnClickListener(v -> {
            startActivity(new Intent(AuthActivity.this, SettingActivity.class));

        });


        binding.dontHaveAccountBtn.setOnClickListener(v -> {
            binding.loginLayout.setVisibility(View.GONE);
            binding.registrationLayout.setVisibility(View.VISIBLE);
        });

        binding.alreadyHaveAccountBtn.setOnClickListener(v -> {
            binding.loginLayout.setVisibility(View.VISIBLE);
            binding.registrationLayout.setVisibility(View.GONE);
        });


        binding.loginBtn.setOnClickListener(v -> {

            CProgressDialog.mShow(AuthActivity.this);

            String email = binding.userEmail.getText().toString();
            String password = Objects.requireNonNull(binding.userPassword.getText()).toString();

            if (email.isEmpty()) {
                CProgressDialog.mDismiss();
                binding.userEmail.setError("Please enter email");
                return;
            }
            if (password.isEmpty()) {
                CProgressDialog.mDismiss();
                binding.userPassword.setError("Please enter password");
                return;
            }
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                CProgressDialog.mDismiss();


                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    VUtil.showSuccessToast(getApplicationContext(), "Dear " + auth.getCurrentUser().getDisplayName() + ", Login Successful");

                }
            }).addOnFailureListener(e -> {
                CProgressDialog.mDismiss();
                VUtil.showErrorToast(getApplicationContext(), e.getMessage());
            });
        });

        binding.websiteBtn.setOnClickListener(v -> openWebsite("https://chimple.com"));


        binding.instagramBtn.setOnClickListener(v -> openWebsite("https://www.instagram.com/chimple_learning/"));


        binding.facebookBtn.setOnClickListener(v -> openWebsite("https://www.facebook.com/chimple"));

        binding.tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        binding.createAccountBtn.setOnClickListener(v -> {
            CProgressDialog.mShow(AuthActivity.this);
            String name = binding.newName.getText().toString();
            String email = binding.newEmail.getText().toString();
            String password = Objects.requireNonNull(binding.newPassword.getText()).toString();
            String confirmPassword = Objects.requireNonNull(binding.newConfirmPassword.getText()).toString();

            if (name.isEmpty()) {
                CProgressDialog.mDismiss();
                binding.userEmail.setError("Please enter name");
                return;
            }

            if (email.isEmpty()) {
                CProgressDialog.mDismiss();
                binding.userEmail.setError("Please enter email");
                return;
            }
            if (password.isEmpty()) {
                CProgressDialog.mDismiss();
                binding.userPassword.setError("Please enter password");
                return;
            }

            if (!password.equals(confirmPassword)) {
                CProgressDialog.mDismiss();
                VUtil.showErrorToast(getApplicationContext(), "Password does not match");
                return;
            }
            Constant.mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    UserModel model = new UserModel();
                    model.setUid(Objects.requireNonNull(task.getResult().getUser()).getUid());
                    model.setName(name);
                    model.setEmail(email);
                    model.setPassword(password);

                    Constant.userDb.child(model.getUid()).setValue(model).addOnCompleteListener(task1 -> {
                        CProgressDialog.mDismiss();
                    }).addOnFailureListener(e -> {
                        CProgressDialog.mDismiss();
                        VUtil.showErrorToast(getApplicationContext(), e.getMessage());
                    });


                }
            }).addOnFailureListener(e -> {
                CProgressDialog.mDismiss();
                VUtil.showErrorToast(getApplicationContext(), e.getMessage());
            });
        });

    }

    public void openWebsite(String websiteURL) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteURL));
        startActivity(intent);
    }


    private void showForgotPasswordDialog() {
        final MyDialog dialog = new MyDialog(AuthActivity.this, R.layout.cl_forgot_password_layout);
        View dialogView = dialog.getView();
        final ClForgotPasswordLayoutBinding forgotPasswordLayoutBinding = ClForgotPasswordLayoutBinding.bind(dialogView);

        forgotPasswordLayoutBinding.resetBtn.setOnClickListener(v -> {
            CProgressDialog.mShow(AuthActivity.this);
            String email = forgotPasswordLayoutBinding.userEmail.getText().toString();
            if (email.isEmpty()) {
                CProgressDialog.mDismiss();
                forgotPasswordLayoutBinding.userEmail.setError("Please enter email");
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    CProgressDialog.mDismiss();
                    if (task.isSuccessful()) {
                        VUtil.showSuccessToast(getApplicationContext(), "If the email is registered, a password reset link has been sent.");
                    } else {
                        VUtil.showErrorToast(getApplicationContext(), "Failed to send reset link. Please try again.");
                    }
                    dialog.dismiss(); // Dismiss dialog after showing the toast
                }).addOnFailureListener(e -> {
                    CProgressDialog.mDismiss();
                    VUtil.showErrorToast(getApplicationContext(), e.getMessage());
                    dialog.dismiss(); // Ensure dialog is dismissed even on failure
                });
            }
        });

        dialog.setCancelable(true);
        dialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (CProgressDialog.isDialogShown) {
            CProgressDialog.mDismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (CProgressDialog.isDialogShown) {
            CProgressDialog.mDismiss();
        }
    }
}
