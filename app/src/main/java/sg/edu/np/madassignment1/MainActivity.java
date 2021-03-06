 package sg.edu.np.madassignment1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
/*import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;*/

import java.util.ArrayList;

 public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    public static User mUser;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://mad-asg-6df37-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference mDatabase = firebaseDatabase.getReference();
    Fragment selectedFragment;
    ArrayList<String> myRecipeList = new ArrayList<>();
    TextView recipeName, recipeDesc, cuisine;
    ArrayList<Ingredient> ingredientList;
    ArrayList<Steps> stepsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Hide the top title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            //get the logged in user
            mDatabase.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUser = dataSnapshot.getValue(User.class);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        // init bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        //this is to open the home fragment when creating
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragment_container, new HomeFragment()).commit();
    }

    //to tell the nav to be selected when switching
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //If add fragment input field not empty, prompt alert
            if(item.getItemId() == R.id.nav_add && selectedFragment instanceof AddFragment){
                return false;
            }
            else if(selectedFragment!= null && addFragmentHasInput(selectedFragment)){
                final AlertDialog builder = new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Discard draft?")
                        .setPositiveButton("Discard", null)
                        .setNegativeButton("Cancel", null)
                        .setCancelable(false)
                        .create();
                builder.show();
                //Setting up OnClickListener on positive button of AlertDialog
                builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fragmentSwitch(item);
                        builder.dismiss();
                    }
                });
                return false;
            }
            fragmentSwitch(item);
            return true;
        }
    };

    private void fragmentSwitch(MenuItem item){
        selectedFragment = null;
        Bundle bundle = new Bundle();
        switch (item.getItemId()){
            case R.id.nav_home:
                selectedFragment = new HomeFragment();
                break;
            case R.id.nav_account:
                selectedFragment = new AccountFragment();
                bundle.putStringArrayList("createdRecipes", myRecipeList);
                selectedFragment.setArguments(bundle);
                break;
            case R.id.nav_add:
                selectedFragment = new AddFragment();
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragment_container, selectedFragment).commit();
    }

    private boolean addFragmentHasInput(Fragment addFragment){
        if(!(addFragment instanceof AddFragment)) {
            return false;
        }
        ImageView recipeImg = addFragment.getView().findViewById(R.id.recipeImg);
        recipeName = ((AddFragment) addFragment).recipeName;
        recipeDesc = ((AddFragment) addFragment).descText;
        cuisine = ((AddFragment) addFragment).descText;
        ingredientList = ((AddFragment) addFragment).ingredientList;
        stepsList = ((AddFragment) addFragment).stepsList;

        //Condition to check if any of these fields are not empty, return false
        if(recipeImg.getVisibility() == View.VISIBLE ||
            !recipeName.getText().toString().equals("") ||
            !recipeDesc.getText().toString().equals("") ||
            !cuisine.getText().toString().equals("") ||
            ingredientList.size() > 0 ||
            stepsList.size() > 0){
            return true;
        }
        return false;
    };

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            //Slide right to Left Transition
            overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
        }
    }

     //Method for Android's back button animation
     @Override
     public void onBackPressed(){
         super.onBackPressed();
         MainActivity.this.overridePendingTransition(R.transition.slide_in_left, R.transition.slide_out_right);
     }

}