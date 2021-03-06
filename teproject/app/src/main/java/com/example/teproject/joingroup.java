package com.example.teproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.google.firebase.firestore.FieldValue.arrayUnion;

public class joingroup extends AppCompatActivity {

    private TextView mCreateGroupText;
    private Button mCreateGrpBtn, mJoinGrpBtn, mSkipBtn;
    private Random rand;
    private EditText mJoinGroupTxt;
    private int year, codeint;
    final ArrayList<Integer> codeArr = new ArrayList<>();
    private String fireRegID, userUid, code, fireGrp;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    private DocumentReference docRef, docRef2;
    private boolean status, exist, fireRole, firegroupid;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joingroup);

        mToolbar = findViewById(R.id.back_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // Your code
                finish();
            }
        });

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        mCreateGrpBtn = findViewById(R.id.creategroupbtn);
        mJoinGrpBtn = findViewById(R.id.joingroupbtn);
        mCreateGroupText = findViewById(R.id.groupCodetext);
        mJoinGroupTxt = findViewById(R.id.joingrptext);
        mSkipBtn = findViewById(R.id.skipbtn);
        rand = new Random();

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();
        userUid =  user.getUid();

        String userEmail = user.getEmail();

        getIDS();
        docRef2 = fStore.document("year/"+year+"- "+(year+1)+"/Users/"+fireRegID);


        mCreateGrpBtn. setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeint = rand.nextInt((9999 - 100) + 1) + 10;
                code = Integer.toString(codeint);
                docRef = fStore.document("year/"+year+"- "+(year+1)+"/Groups/"+code);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        while(documentSnapshot.exists()){
                            codeint = rand.nextInt((9999 - 100) + 1) + 10;
                            code = Integer.toString(codeint);
                            Log.d("TAG", "Code: "+ code);
                        }
                        if(!documentSnapshot.exists()){
                            mCreateGrpBtn.setEnabled(false);
                            mJoinGrpBtn.setEnabled(false);
                            Log.d("TAG", "Reg is: "+ fireRegID);

                            mCreateGroupText.setVisibility(View.VISIBLE);
                            mCreateGroupText.setText("Group code: "+code + "\n" +
                                    "Share it with group members only!!");
                            Log.d("TAG", "Code "+ code);
                            addGroupID();
                        }
                    }
                });
            }
        });

        mSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        mJoinGrpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = mJoinGroupTxt.getText().toString().trim();
                if(TextUtils.isEmpty(code)){
                    mJoinGroupTxt.setError("Enter code");
                }
                docRef = fStore.document("year/"+year+"- "+(year+1)+"/Groups/"+code);
                docRef2 = fStore.document("year/"+year+"- "+(year+1)+"/Users/"+fireRegID);
                Log.d("TAG", "code is: "+ code);
                checkIfgroupExists();
            }
        });
    }

//    void checkforbtnenable(){
//        docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if(task.isSuccessful()){
//                    DocumentSnapshot document = task.getResult();
//                    if(document.exists()){
//                        if(!document.getString("GroupID").equals("N.A.")){
//                            mCreateGrpBtn.setEnabled(false);
//                            mJoinGrpBtn.setEnabled(false);
//                            Log.d("TAG", "In btnenable");
//                            mJoinGroupTxt.setHint("Already a member");
//                        }
//
//                    }
//                }
//
//            }
//        });
//    }


    void addMemberToGrp(){

        Map<String, Object> datatosave2 = new HashMap<>();
        if(fireRole){
            datatosave2.put("Members", arrayUnion(fireRegID));
        } else {
            datatosave2.put("MentorID", fireRegID);
        }

        docRef.set(datatosave2, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("TAG", "Member successfully added");
                    Toast.makeText(joingroup.this, "You're a member of team: "+code, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    mSkipBtn.setText("Next");
                } else{
                    Log.d("TAG", "Member not added!");
                    mJoinGrpBtn.setEnabled(false);
                    Toast.makeText(joingroup.this, "You're in another team", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void addGroupID(){

        docRef2 = fStore.document("year/"+year+"- "+(year+1)+"/Users/"+fireRegID);
        docRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    Log.d("TAG", "Exissttsssss");
                    String groupid = document.getString("GroupID");
                    if(groupid.equals("N.A.")){
                        status = true;
                        Map<String, Object> datatosave = new HashMap<>();
                        datatosave.put("GroupID", code);
                        Log.d("TAG","groupid: "+ code);

                        docRef2.update(datatosave).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d("Hooray", "GroupID added successfully");
                                    addMemberToGrp();
                                }else{
                                    Log.d("Error", "There was an error!");
                                }
                            }
                        });

                    } else{
                        status = false;
                        mJoinGrpBtn.setEnabled(false);
                        Log.d("TAG", "Already a group member!");
                        Toast.makeText(joingroup.this, "Already a member", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
    void checkIfgroupExists(){
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    exist = true;
                    mCreateGrpBtn.setEnabled(false);
                        addGroupID();
                } else{
                    exist = false;
                    mJoinGroupTxt.setError("Invalid code");
                    mCreateGrpBtn.setEnabled(true);
                }
            }
        });
    }

    void getRegID(){
        DocumentReference dRef = fStore.document("year/"+year+"- "+(year+1)+"/Users/"+fireRegID);

        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    fireRegID = document.getString("RegistrationID");
                    fireRole = document.getBoolean("Role");
                    fireGrp = document.getString("GroupID");
                    if(fireRole){
                        mCreateGrpBtn.setEnabled(true);
                    } else {
                        mCreateGrpBtn.setEnabled(false);
                    }
                    if(fireGrp.equals("N.A.")){
                        mCreateGrpBtn.setEnabled(true);
                        mJoinGrpBtn.setEnabled(true);
                    } else {
                        mCreateGrpBtn.setEnabled(false);
                        mJoinGrpBtn.setEnabled(false);
                    }

                    Log.d("TAG", "data => " + document.getData());
                    Log.d("TAG", "ROle is: "+ fireRole);
                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }

            }
        });
    }
    void getIDS(){
        DocumentReference dRef_1 = fStore.document("year/"+year+"- "+(year+1)+"/IDS/"+userUid);
        dRef_1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    fireRegID = document.getString("RegID");
                    Log.d("TAG", "RegID: "+ fireRegID);
                    getRegID();

                } else{
                    Log.d("TAG", "Error fetching the document");
                }
            }
        });
    }
}


