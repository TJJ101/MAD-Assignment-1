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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyRecipeFragment extends Fragment {
    public FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://mad-asg-6df37-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private DatabaseReference mDatabase = firebaseDatabase.getReference();
    private RecipeAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList <Recipe> recipeList = new ArrayList<>();

    ArrayList<String> myRecipeList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_recipe, container, false);
        adapter = new RecipeAdapter(getContext(), recipeList, getActivity());
        MainActivity mainActivity = (MainActivity)getActivity();

        if (getArguments() != null){
            myRecipeList = getArguments().getStringArrayList("createdRecipes");
        }

        //get the list of recipe id that the user has created
        mDatabase.child("Recipe").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recipeList = new ArrayList<>();
                Recipe recipe;
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    recipe = eventSnapshot.getValue(Recipe.class);
                    if(recipe.getCreatorId().toUpperCase().equals(mainActivity.mUser.getUsername().toUpperCase())){
                        recipeList.add(eventSnapshot.getValue(Recipe.class));
                    }
                }

                recyclerView = view.findViewById(R.id.myRecipeRecycler);
                adapter = new RecipeAdapter(getContext(), recipeList, getActivity());
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 1));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                adapter.notifyDataSetChanged();
                Log.d("SIZE", "" + recipeList.size());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return view;
    }
}