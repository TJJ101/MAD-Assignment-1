package sg.edu.np.madassignment1;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SavedFragment extends Fragment {
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://mad-asg-6df37-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private DatabaseReference mDatabase = firebaseDatabase.getReference();
    private RecipeAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList <Recipe> recipeList = new ArrayList<>();
    private ArrayList <Recipe> savedList = new ArrayList<>();
    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);
        adapter = new RecipeAdapter(getContext(), recipeList, getActivity());

        //put saved recipes data into saved list
        recyclerView = view.findViewById(R.id.savedRecipeRecycler);
        mDatabase.child("Recipe").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recipeList = new ArrayList<>();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    recipeList.add(eventSnapshot.getValue(Recipe.class));
                }
                for (Recipe r : recipeList){
                    if(MainActivity.mUser.getSavedRecipes().contains(r.getRecipeId())){
                        savedList.add(r);
                    }
                }

                adapter = new RecipeAdapter(getContext(), savedList, getActivity());
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 1));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return view;
    }
}