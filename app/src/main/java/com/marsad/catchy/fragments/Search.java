package com.marsad.catchy.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.marsad.catchy.R;
import com.marsad.catchy.adapter.UserAdapter;
import com.marsad.catchy.model.Users;

import java.util.ArrayList;
import java.util.List;

public class Search extends Fragment {

    SearchView searchView;
    RecyclerView recyclerView;
    UserAdapter adapter;
    CollectionReference reference;
    OnDataPass onDataPass;
    private List<Users> list;

    public Search() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        onDataPass = (OnDataPass) context;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        reference = FirebaseFirestore.getInstance().collection("Users");

        loadUserData();

        searchUser();

        clickListener();

    }

    private void clickListener() {

        adapter.OnUserClicked(new UserAdapter.OnUserClicked() {
            @Override
            public void onClicked(String uid) {
                onDataPass.onChange(uid);
            }
        });

    }

    private void searchUser() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                reference.orderBy("search").startAt(query).endAt(query + "\uf8ff")
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            list.clear();
                            for (DocumentSnapshot snapshot : task.getResult()) {
                                if (!snapshot.exists())
                                    return;

                                Users users = snapshot.toObject(Users.class);
                                list.add(users);

                            }
                            adapter.notifyDataSetChanged();

                        }

                    }
                });


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.equals(""))
                    loadUserData();

                return false;
            }
        });

    }

    private void loadUserData() {

        reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null)
                    return;

                if (value == null)
                    return;

                list.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    Users users = snapshot.toObject(Users.class);
                    list.add(users);
                }
                adapter.notifyDataSetChanged();

            }
        });

    }

    private void init(View view) {

        searchView = view.findViewById(R.id.searchView);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        adapter = new UserAdapter(list);
        recyclerView.setAdapter(adapter);

    }

    public interface OnDataPass {
        void onChange( String uid);
    }


}