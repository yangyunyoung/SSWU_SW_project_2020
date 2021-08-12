package com.example.sw_project.adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sw_project.Activity.WriterPopUpActivity;
import com.example.sw_project.CommentInfo;
import com.example.sw_project.R;
import com.example.sw_project.StudentInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.CommentViewHolder> {

    private static final String TAG = "CommentAdapter";
    private ArrayList<CommentInfo> mComments;
    private Activity activity;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser firebaseUser;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;


    public CommentListAdapter(ArrayList<CommentInfo> mComments,Activity activity) {

        this.mComments = mComments;
        this.activity=activity;
        firebaseFirestore=FirebaseFirestore.getInstance();
    }


    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_list,parent, false);

        final CommentViewHolder holder = new CommentViewHolder(view);

        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull CommentListAdapter.CommentViewHolder holder, int position) {

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        holder.itemView.setTag(position);
        holder.commentView.setText(mComments.get(position).getContents());

        SpannableString content = new SpannableString(mComments.get(position).getCommentwriter());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        holder.CommentwriterView.setText(content);
        holder.createdAtView.setText(mComments.get(position).getCreatedAt());

        holder.CommentwriterView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                DocumentReference docRef = firebaseFirestore.collection("users").document(mComments.get(position).getUserUid());
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        StudentInfo studentInfo = documentSnapshot.toObject(StudentInfo.class);

                        try {
                            Intent intent = new Intent(activity, WriterPopUpActivity.class);
                            intent.putExtra("data", studentInfo.getStudentId());
                            intent.putExtra("data2", studentInfo.getCollege());
                            intent.putExtra("data3", studentInfo.getDepartment());
                            intent.putExtra("data4", studentInfo.getContestParticipate());
                            activity.startActivity(intent);
                        }catch (NullPointerException e){
                            builder = new AlertDialog.Builder(activity);
                            dialog = builder.setMessage("존재하지 않는 회원입니다.")
                                    .setPositiveButton("OK", null)
                                    .create();
                            dialog.show();
                        }
                    }
                });
            }
        });

        if(mComments.get(position).getUserUid().equals(firebaseUser.getUid())) {
            holder.menuImage.setVisibility(View.VISIBLE);
            holder.menuImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(v, holder.getAdapterPosition());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return (mComments != null ? mComments.size() : 0);
    }


    public void showPopup(View v,int position) {
        PopupMenu popup = new PopupMenu(activity, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.comment_delete:
                        firebaseFirestore.collection("comments").document(mComments.get(position).getCommentid())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(activity,"댓글을 삭제하였습니다.",Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                        Log.d(TAG,"After .delete() "+mComments.get(position).getCommentid());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(activity,"댓글을 삭제하지 못하였습니다.",Toast.LENGTH_SHORT).show();
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                                });

                        return true;
                    default:
                        return false;
                }
            }
        });

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.comments, popup.getMenu());
        popup.show();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        TextView commentView, CommentwriterView, createdAtView;
        ImageView menuImage;

        public CommentViewHolder(@NonNull View itemView) {

            super(itemView);
            this.commentView = itemView.findViewById(R.id.commentView);
            this.CommentwriterView= itemView.findViewById(R.id.CommentwriterView);
            this.createdAtView=itemView.findViewById(R.id.createdAt);
            this.menuImage = itemView.findViewById(R.id.comment_menu);

        }
    }



}


