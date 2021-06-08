package com.marsad.catchy.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.marsad.catchy.R;
import com.marsad.catchy.adapter.HomeAdapter;
import com.marsad.catchy.model.HomeModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends Fragment {

    HomeAdapter adapter;
    private RecyclerView recyclerView;
    private List<HomeModel> list;
    private FirebaseUser user;
    private final MutableLiveData<Integer> commentCount = new MutableLiveData<>();

    public Home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);

        loadDataFromFirestore();

        adapter.OnPressed(new HomeAdapter.OnPressed() {
            @Override
            public void onLiked(int position, String id, String uid, List<String> likeList, boolean isChecked) {

                DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                        .document(uid)
                        .collection("Post Images")
                        .document(id);

                if (likeList.contains(user.getUid())) {
                    likeList.remove(user.getUid()); //unlike
                } else {
                    likeList.add(user.getUid()); //like
                }

                Map<String, Object> map = new HashMap<>();
                map.put("likes", likeList);

                reference.update(map);

            }

            @Override
            public void setCommentCount(final TextView textView) {

                Activity activity = getActivity();

                commentCount.observe((LifecycleOwner) activity, new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {

                        if (commentCount.getValue() == 0) {
                            textView.setVisibility(View.GONE);
                        } else
                            textView.setVisibility(View.VISIBLE);

                        textView.setText("See all " + commentCount.getValue() + " comments");

                    }
                });




            }
        });

    }

    private void init(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

    }

    private void loadDataFromFirestore() {

        final DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());

        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");

        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.d("Error: ", error.getMessage());
                    return;
                }

                if (value == null)
                    return;

                List<String> uidList = (List<String>) value.get("following");

                if (uidList == null || uidList.isEmpty())
                    return;

                collectionReference.whereIn("uid", uidList)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                if (error != null) {
                                    Log.d("Error: ", error.getMessage());
                                }

                                if (value == null)
                                    return;

                                for (QueryDocumentSnapshot snapshot : value) {


                                    snapshot.getReference().collection("Post Images")
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                                    if (error != null) {
                                                        Log.d("Error: ", error.getMessage());
                                                    }

                                                    if (value == null)
                                                        return;

                                                    list.clear();

                                                    for (final QueryDocumentSnapshot snapshot : value) {

                                                        if (!snapshot.exists())
                                                            return;

                                                        HomeModel model = snapshot.toObject(HomeModel.class);

                                                        list.add(new HomeModel(
                                                                model.getName(),
                                                                model.getProfileImage(),
                                                                model.getImageUrl(),
                                                                model.getUid(),
                                                                model.getDescription(),
                                                                model.getId(),
                                                                model.getTimestamp(),
                                                                model.getLikes()
                                                        ));

                                                        snapshot.getReference().collection("Comments").get()
                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                                        if (task.isSuccessful()) {

                                                                            int count = 0;

                                                                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                                                                count++;
                                                                            }
                                                                            commentCount.setValue(count);
                                                                            //comment count

                                                                        }

                                                                    }
                                                                });


                                                    }
                                                    adapter.notifyDataSetChanged();


                                                }
                                            });


                                }

                            }
                        });


            }
        });


    }


}