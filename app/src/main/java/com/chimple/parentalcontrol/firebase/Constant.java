package com.chimple.parentalcontrol.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Constant {
    public static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("users");

}
