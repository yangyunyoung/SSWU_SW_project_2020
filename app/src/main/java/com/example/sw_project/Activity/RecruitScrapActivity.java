package com.example.sw_project.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sw_project.R;
import com.example.sw_project.ScrapInfo;
import com.example.sw_project.WriteInfo;
import com.example.sw_project.adapter.PostListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RecruitScrapActivity extends Fragment {

    View view;

    private final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
    private FirebaseFirestore db;
    private DatabaseReference mDatabase;
    private String TAG = "RecruitActivity";
    private FirebaseUser user;

    public static RecruitScrapActivity newInstance() {
        return new RecruitScrapActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_postlist, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        db.collection("scrapsPost")
                .whereEqualTo("scrapUserUid",user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<ScrapInfo> scrapArray = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                scrapArray.add(document.toObject(ScrapInfo.class));
                            }

                            // postId에 해당하는 post 불러오기
                            ArrayList<WriteInfo> writeArray = new ArrayList<>();
                            for(ScrapInfo findPost : scrapArray){
                                db.collection("posts")
                                        .whereEqualTo("postid", findPost.getPostId())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                                        writeArray.add(document.toObject(WriteInfo.class));
                                                    }
                                                    RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
                                                    recyclerView.setHasFixedSize(true);
                                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                                    RecyclerView.Adapter madapter = new PostListAdapter(writeArray, getActivity());
                                                    recyclerView.setAdapter(madapter);
                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                            }

                        }else {
                            Log.w(TAG, "Error => ", task.getException());
                        }
                    }
                });

        return view;
    }

}