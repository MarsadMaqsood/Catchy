package com.marsad.catchy.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marsad.catchy.R;
import com.marsad.catchy.model.PostImageModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.marsad.catchy.MainActivity.IS_SEARCHED_USER;
import static com.marsad.catchy.MainActivity.USER_ID;
import static com.marsad.catchy.utils.Constants.PREF_DIRECTORY;
import static com.marsad.catchy.utils.Constants.PREF_NAME;
import static com.marsad.catchy.utils.Constants.PREF_STORED;
import static com.marsad.catchy.utils.Constants.PREF_URL;

public class Profile extends Fragment {


    boolean isMyProfile = true;
    String userUID;
    FirestoreRecyclerAdapter<PostImageModel, PostImageHolder> adapter;
    List<Object> followersList, followingList, followingList_2;
    boolean isFollowed;
    DocumentReference userRef, myRef;
    int count;
    private TextView nameTv, toolbarNameTv, statusTv, followingCountTv, followersCountTv, postCountTv;
    private CircleImageView profileImage;
    private Button followBtn;
    private RecyclerView recyclerView;
    private LinearLayout countLayout;
    private FirebaseUser user;
    private ImageButton editProfileBtn;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);


        myRef = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());


        if (IS_SEARCHED_USER) {
            isMyProfile = false;
            userUID = USER_ID;

            loadData();

        } else {
            isMyProfile = true;
            userUID = user.getUid();
        }

        if (isMyProfile) {
            editProfileBtn.setVisibility(View.VISIBLE);
            followBtn.setVisibility(View.GONE);
            countLayout.setVisibility(View.VISIBLE);
        } else {
            editProfileBtn.setVisibility(View.GONE);
            followBtn.setVisibility(View.VISIBLE);
//            countLayout.setVisibility(View.GONE);
        }
        userRef = FirebaseFirestore.getInstance().collection("Users").document(userUID);

        loadBasicData();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        loadPostImages();

        recyclerView.setAdapter(adapter);

        clickListener();

    }

    private void loadData() {

        myRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.e("Tag_b", error.getMessage());
                    return;
                }

                if (value == null || !value.exists()) {
                    return;
                }

                followingList_2 = (List<Object>) value.get("following");


            }
        });

    }

    private void clickListener() {


        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isFollowed) {

                    followersList.remove(user.getUid()); //opposite user

                    followingList_2.remove(userUID); //us

                    final Map<String, Object> map_2 = new HashMap<>();
                    map_2.put("following", followingList_2);


                    Map<String, Object> map = new HashMap<>();
                    map.put("followers", followersList);


                    userRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                followBtn.setText("Follow");

                                myRef.update(map_2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "UnFollowed", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.e("Tag_3", task.getException().getMessage());
                                        }
                                    }
                                });

                            } else {
                                Log.e("Tag", "" + task.getException().getMessage());
                            }
                        }
                    });


                } else {

                    followersList.add(user.getUid()); //opposite user

                    followingList_2.add(userUID); //us

                    final Map<String, Object> map_2 = new HashMap<>();
                    map_2.put("following", followingList_2);


                    Map<String, Object> map = new HashMap<>();
                    map.put("followers", followersList);


                    userRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                followBtn.setText("UnFollow");

                                myRef.update(map_2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Followed", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.e("tag_3_1", task.getException().getMessage());
                                        }
                                    }
                                });


                            } else {
                                Log.e("Tag", "" + task.getException().getMessage());
                            }
                        }
                    });


                }

            }
        });


        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(getContext(), Profile.this);

            }
        });

    }

    private void init(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        assert getActivity() != null;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        nameTv = view.findViewById(R.id.nameTv);
        statusTv = view.findViewById(R.id.statusTV);
        toolbarNameTv = view.findViewById(R.id.toolbarNameTV);
        followersCountTv = view.findViewById(R.id.followersCountTv);
        followingCountTv = view.findViewById(R.id.followingCountTv);
        postCountTv = view.findViewById(R.id.postCountTv);
        profileImage = view.findViewById(R.id.profileImage);
        followBtn = view.findViewById(R.id.followBtn);
        recyclerView = view.findViewById(R.id.recyclerView);
        countLayout = view.findViewById(R.id.countLayout);
        editProfileBtn = view.findViewById(R.id.edit_profileImage);


        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

    }

    private void loadBasicData() {

        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.e("Tag_0", error.getMessage());
                    return;
                }

                assert value != null;
                if (value.exists()) {

                    String name = value.getString("name");
                    String status = value.getString("status");

                    final String profileURL = value.getString("profileImage");

                    nameTv.setText(name);
                    toolbarNameTv.setText(name);
                    statusTv.setText(status);

                    followersList = (List<Object>) value.get("followers");
                    followingList = (List<Object>) value.get("following");


                    followersCountTv.setText("" + followersList.size());
                    followingCountTv.setText("" + followingList.size());

                    try {

                        Glide.with(getContext().getApplicationContext())
                                .load(profileURL)
                                .placeholder(R.drawable.ic_person)
                                .circleCrop()
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                        storeProfileImage(bitmap, profileURL);
                                        return false;
                                    }
                                })
                                .timeout(6500)
                                .into(profileImage);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    if (followersList.contains(user.getUid())) {
                        followBtn.setText("UnFollow");
                        isFollowed = true;
                    } else {
                        isFollowed = false;
                        followBtn.setText("Follow");
                    }


                }

            }
        });

    }

    private void storeProfileImage(Bitmap bitmap, String url) {

        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isStored = preferences.getBoolean(PREF_STORED, false);
        String urlString = preferences.getString(PREF_URL, "");

        SharedPreferences.Editor editor = preferences.edit();

        if (isStored && urlString.equals(url))
            return;

        if (IS_SEARCHED_USER)
            return;

        ContextWrapper contextWrapper = new ContextWrapper(getContext().getApplicationContext());

        File directory = contextWrapper.getDir("image_data", Context.MODE_PRIVATE);

        if (!directory.exists())
            directory.mkdirs();

        File path = new File(directory, "profile.png");

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(path);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {

            try {
                assert outputStream != null;
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        editor.putBoolean(PREF_STORED, true);
        editor.putString(PREF_URL, url);
        editor.putString(PREF_DIRECTORY, directory.getAbsolutePath());
        editor.apply();


    }

    private void loadPostImages() {

        DocumentReference reference = FirebaseFirestore.getInstance().collection("Users").document(userUID);

        Query query = reference.collection("Post Images");

        FirestoreRecyclerOptions<PostImageModel> options = new FirestoreRecyclerOptions.Builder<PostImageModel>()
                .setQuery(query, PostImageModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<PostImageModel, PostImageHolder>(options) {
            @NonNull
            @Override
            public PostImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_items, parent, false);
                return new PostImageHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PostImageHolder holder, int position, @NonNull PostImageModel model) {

                Glide.with(holder.itemView.getContext().getApplicationContext())
                        .load(model.getImageUrl())
                        .timeout(6500)
                        .into(holder.imageView);
                count = getItemCount();
                postCountTv.setText("" + count);

            }

            @Override
            public int getItemCount() {

                return super.getItemCount();
            }
        };


    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            Uri uri = result.getUri();

            uploadImage(uri);

        }

    }

    private void uploadImage(Uri uri) {

        final StorageReference reference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        reference.putFile(uri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            reference.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String imageURL = uri.toString();

                                            UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
                                            request.setPhotoUri(uri);

                                            user.updateProfile(request.build());

                                            Map<String, Object> map = new HashMap<>();
                                            map.put("profileImage", imageURL);

                                            FirebaseFirestore.getInstance().collection("Users")
                                                    .document(user.getUid())
                                                    .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful())
                                                        Toast.makeText(getContext(), "Updated Successful", Toast.LENGTH_SHORT).show();
                                                    else
                                                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(),
                                                                Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                        }
                                    });

                        } else {
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }

    private static class PostImageHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public PostImageHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);

        }

    }

}