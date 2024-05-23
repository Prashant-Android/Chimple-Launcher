package com.chimple.parentalcontrol.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chimple.parentalcontrol.R;
import com.chimple.parentalcontrol.databinding.ActivityAuthBinding;
import com.chimple.parentalcontrol.databinding.ClPinLayoutBinding;
import com.chimple.parentalcontrol.firebase.Constant;
import com.chimple.parentalcontrol.model.UserModel;
import com.chimple.parentalcontrol.util.CProgressDialog;
import com.chimple.parentalcontrol.util.LocalPreference;
import com.chimple.parentalcontrol.util.MyDialog;
import com.chimple.parentalcontrol.util.VUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.Objects;


public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Constant.mAuth.getCurrentUser() != null || LocalPreference.getPin() != null) {
            startActivity(new Intent(AuthActivity.this, MainActivity.class));
            finish();
        } else if (LocalPreference.getPin() == null) {
            showPinDialog();
        }

        binding.tvSkip.setOnClickListener(v -> {
            if (!LocalPreference.getPin().isEmpty()) {
                startActivity(new Intent(AuthActivity.this, SettingActivity.class));
            } else {
                showPinDialog();
            }
        });


        binding.dontHaveAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.loginLayout.setVisibility(View.GONE);
                binding.registrationLayout.setVisibility(View.VISIBLE);
            }
        });

        binding.alreadyHaveAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.loginLayout.setVisibility(View.VISIBLE);
                binding.registrationLayout.setVisibility(View.GONE);
            }
        });


        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CProgressDialog.mShow(AuthActivity.this);

                String email = binding.userEmail.getText().toString();
                String password = binding.userPassword.getText().toString();

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
                Constant.mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isComplete()) {
                            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            CProgressDialog.mDismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CProgressDialog.mDismiss();
                        VUtil.showErrorToast(getApplicationContext(), e.getMessage());
                    }
                });
            }
        });

        binding.createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CProgressDialog.mShow(AuthActivity.this);
                String name = binding.newName.getText().toString();
                String email = binding.newEmail.getText().toString();
                String password = binding.newPassword.getText().toString();
                String confirmPassword = binding.newConfirmPassword.getText().toString();

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
                Constant.mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isComplete()) {
                            UserModel model = new UserModel();
                            model.setUid(task.getResult().getUser().getUid());
                            model.setName(name);
                            model.setEmail(email);
                            model.setPassword(password);

                            Constant.userDb.child(model.getUid()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    CProgressDialog.mDismiss();
                                    showPinDialog();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    CProgressDialog.mDismiss();
                                    VUtil.showErrorToast(getApplicationContext(), e.getMessage());
                                }
                            });


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CProgressDialog.mDismiss();
                        VUtil.showErrorToast(getApplicationContext(), e.getMessage());
                    }
                });
            }
        });

    }

    private void showPinDialog() {
        final MyDialog dialog = new MyDialog(AuthActivity.this, R.layout.cl_pin_layout);
        View dialogView = dialog.getView();
        final ClPinLayoutBinding pinLayoutBinding = ClPinLayoutBinding.bind(dialogView);

        pinLayoutBinding.btnSetPin.setOnClickListener(v -> {
            if (Objects.requireNonNull(pinLayoutBinding.userPin.getText()).toString().isEmpty()) {
                pinLayoutBinding.userPin.setError("Please enter pin");
            } else if (pinLayoutBinding.userEmail.getText().toString().isEmpty()) {
                pinLayoutBinding.userEmail.setError("Please enter email");
            } else {
                LocalPreference.savePin(pinLayoutBinding.userPin.getText().toString());
                dialog.dismiss();
                startActivity(new Intent(AuthActivity.this, SettingActivity.class));
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
