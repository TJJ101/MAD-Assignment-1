package sg.edu.np.madassignment1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddFragment extends Fragment {
    int stepNumber = 0;
    String key;
    TextInputLayout cuisinesTxt;
    TextView recipeName, descText, ingredientNameTxt, ingredientQtyTxt, addStepsTxt, addTimerTxt;
    Button uploadBtn, addIngBtn, addStepsBtn, createRecipeBtn;
    ImageView recipeImg;
    Uri filePath;
    String[] cuisine = {"Turkish", "Thai", "Japanese", "Indian", "French", "Chinese", "Western"};
    String[] units = {"n/a", "g", "kg", "ml", "l", "tsp", "tbsp", "cup"};
    AutoCompleteTextView cuisineTxt, ingredientUnitTxt;
    ArrayList<Ingredient> ingredientList = new ArrayList<Ingredient>();
    ArrayList<Steps> stepsList = new ArrayList<Steps>();
    ListView ingredientListView, stepsListView;
    StepsAdapter stepsAdapter;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://mad-asg-6df37-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference mDatabase = firebaseDatabase.getReference();
    Recipe mRecipe;
    Global global = new Global();
    User mUser = new User();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        //Initialise data
        recipeName = view.findViewById(R.id.recipeNameTxt);
        descText = view.findViewById(R.id.descTxt);
        cuisineTxt = view.findViewById(R.id.cuisineDropdown);
        uploadBtn = view.findViewById(R.id.uploadBtn);
        recipeImg = view.findViewById(R.id.recipeImg);
        recipeImg = view.findViewById(R.id.recipeImg);
        cuisinesTxt = view.findViewById(R.id.textInputDropdownLayout);
        ingredientListView = view.findViewById(R.id.ingredientListView);
        addIngBtn = view.findViewById(R.id.addIngredientBtn);
        ingredientNameTxt = view.findViewById(R.id.addIngredientTxt);
        ingredientQtyTxt = view.findViewById(R.id.addQtyTxt);
        ingredientUnitTxt = view.findViewById(R.id.addUnitTxt);
        stepsListView = view.findViewById(R.id.stepsListView);
        addStepsBtn = view.findViewById(R.id.addStepsBtn);
        addStepsTxt = view.findViewById(R.id.addStepsTxt);
        addTimerTxt = view.findViewById(R.id.addTimerTxt);
        createRecipeBtn = view.findViewById(R.id.createRecipeBtn);

        //Initialise Firebase db
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Set autoCompleteTextView input type to text
        cuisineTxt.setInputType(InputType.TYPE_CLASS_TEXT);

        //Add on click to upload image function
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        //Long click on Image to change image
        recipeImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openDialog();
                return true;
            }
        });

        //setup the cuisine dropdown text
        ArrayAdapter<String> cuisineAdapter = new ArrayAdapter<String>(getActivity(), R.layout.dropdown, cuisine);
        AutoCompleteTextView cuisineTxt = view.findViewById(R.id.cuisineDropdown);
        cuisineTxt.setThreshold(1);//start when u type first char
        cuisineTxt.setAdapter(cuisineAdapter);

        //setup the unit dropdown text
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<String>(getActivity(), R.layout.dropdown, units);
        ingredientUnitTxt.setThreshold(1);//start when u type first char
        ingredientUnitTxt.setAdapter(unitAdapter);

        //for adding new ingredients to ingredients array and list view
        LinearLayout.LayoutParams ingredientListViewLP = (LinearLayout.LayoutParams) ingredientListView.getLayoutParams();
        addIngBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ingredients = ingredientNameTxt.getText().toString();
                String qty = ingredientQtyTxt.getText().toString();
                String unit = ingredientUnitTxt.getText().toString();
                //Not able to add when enter invalid inputs
                if (validateIngredientInput(ingredients, qty, unit)){
                    ingredientList.add(new Ingredient(
                            ingredients,
                            Double.parseDouble(qty),
                            unit));
                    ingredientListViewLP.height += DipToPixels(60);
                    ingredientListView.setLayoutParams(ingredientListViewLP);
                    ingredientListView.setAdapter(new IngredientAdapter(ingredientList, getContext()));
                    ingredientNameTxt.setText("");
                    ingredientQtyTxt.setText("");
                    ingredientUnitTxt.setText("");
                }

                //hide the keyboard
                global.hideKeyboard(getActivity());
            }
        });

        //Add Step Timer Picker
        addTimerTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimerPicker();
            }
        });

        //for adding new steps to steps array and list view
        LinearLayout.LayoutParams stepsListViewLP = (LinearLayout.LayoutParams) stepsListView.getLayoutParams();
        addStepsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stepsDesc = addStepsTxt.getText().toString();
                String stepsTimer = addTimerTxt.getText().toString();
                if (validateStepsInput(stepsDesc, stepsTimer)){
                    stepNumber = stepsList.size() + 1;
                    stepsList.add(new Steps(
                            stepNumber,
                            stepsDesc,
                            stepsTimer
                    ));
                    //Add 125 which is height of one Steps view holder
                    stepsListViewLP.height += DipToPixels(125);
                    stepsListView.setLayoutParams(stepsListViewLP);
                    stepsAdapter = new StepsAdapter(stepsList, getContext());
                    stepsListView.setAdapter(stepsAdapter);
                    addStepsTxt.setText("");
                    addTimerTxt.setText("");
                }

                //hide the keyboard
                global.hideKeyboard(getActivity());
            }
        });

        createRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newRecipeName = recipeName.getText().toString();
                String newRecipeDesc = descText.getText().toString();
                String newRecipeCui = cuisineTxt.getText().toString();

                if(validateRecipeInput(recipeName.getText().toString(), descText.getText().toString(), cuisineTxt.getText().toString(), recipeImg)){
                    mUser = MainActivity.mUser;
                    key = mDatabase.child("Recipe").push().getKey();
                    mRecipe = new Recipe(newRecipeName, newRecipeCui, newRecipeDesc, ingredientList, stepsList);
                    mRecipe.setRecipeId(key);
                    mRecipe.setCreatorId(mUser.getUsername());
                    mRecipe.setRatings(new Ratings());
                    mDatabase.child("Recipe").child(key).setValue(mRecipe);
                    uploadImage();
                    ClearAllText();
                }
            }
        });

        return view;
    }

    public void ClearAllText(){
        recipeName.setText("");
        descText.setText("");
        cuisineTxt.setText("");
        ingredientNameTxt.setText("");
        ingredientQtyTxt.setText("");
        ingredientUnitTxt.setText("");
        addStepsTxt.setText("");
        addTimerTxt.setText("");
    }

    public boolean validateRecipeInput(String name, String desc, String cuisine, ImageView recipeImg){
        if(name.isEmpty() || name.trim().length() == 0 ||
                desc.isEmpty() || desc.trim().length() == 0 ||
                cuisine.isEmpty() || cuisine.trim().length() == 0 ||
                recipeImg.getVisibility() == View.GONE ||
                ingredientList.size() == 0 || stepsList.size() == 0){
            Toast.makeText(getContext(), "Please enter in all the fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //  Check if fields for steps section are empty
    public boolean validateStepsInput(String stepsDesc, String stepsTimer){
        if (stepsDesc.isEmpty() || stepsDesc.trim().length() == 0 ||
                stepsTimer.isEmpty() || stepsTimer.trim().length() == 0){
            Toast.makeText(getContext(), "Please enter both step description and time to add steps", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    //  Check if fields for ingredients section are empty
    public boolean validateIngredientInput(String ingredients, String qty, String unit){
        if (ingredients.isEmpty() || ingredients.trim().length() == 0 ||
                qty.isEmpty() || qty.trim().length() == 0 ||
                !global.isNumeric(qty) || Double.parseDouble(qty) <= 0 ||
                unit.isEmpty() || unit.trim().length() == 0) {
            Toast.makeText(getContext(), "Please enter valid values for ingredient", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public float convertPixelsToDp(float px){
        return px / ((float) getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    //Convert px to dp
    public float DipToPixels(float dp){
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    //Select image from device function
    private void SelectImage()
    {
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(intent);
    }
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        //Get the Uri of data
                        filePath = data.getData();
                        Glide.with(getContext()).load(filePath).into(recipeImg);
                        recipeImg.setVisibility(View.VISIBLE);
                        uploadBtn.setVisibility(View.GONE);
                    }
                }
            });

    //Open alert
    private void openDialog() {
        //build an AlertDialog
        final AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setMessage("Upload new image")
                .setPositiveButton("Upload", null)
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .create();
        builder.show();
        //Setting up OnClickListener on positive button of AlertDialog
        builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
                builder.dismiss();
            }
        });
    }

    //Dialog for Time picker
    public void openTimerPicker(){
        LayoutInflater l = getLayoutInflater();
        final View v = l.inflate(R.layout.select_time_dialog, null);
        //final LinearLayout linearLayout = (LinearLayout) ((LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.select_time_dialog, null);

        NumberPicker selectHour = v.findViewById(R.id.selectHour);
        NumberPicker selectMin = v.findViewById(R.id.selectMinutes);
        NumberPicker selectSec = v.findViewById(R.id.selectSeconds);

        selectHour.setMinValue(0);
        selectHour.setMaxValue(24);
        selectHour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        selectMin.setMinValue(0);
        selectMin.setMaxValue(59);
        selectMin.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        selectSec.setMinValue(0);
        selectSec.setMaxValue(59);
        selectSec.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        final AlertDialog builder = new AlertDialog.Builder(getContext())
                .setPositiveButton("Submit", null)
                .setNegativeButton("Cancel", null)
                .setView(v)
                .setCancelable(false)
                .create();
        builder.show();
        //Setting up OnClickListener on positive button of AlertDialog
        builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = "";
                if(selectHour.getValue() > 9){
                    time += selectHour.getValue()+ ":";
                }
                else{
                    time += "0"+ selectHour.getValue() + ":";
                }
                if(selectMin.getValue() > 9){
                    time += selectMin.getValue() + ":";
                }
                else{
                    time += "0" + selectMin.getValue() + ":";
                }
                if (selectSec.getValue() > 9){
                    time += selectSec.getValue();
                }
                else{
                    time += "0" + selectSec.getValue();
                }
                addTimerTxt.setText(time);
                builder.dismiss();
            }
        });
    }

    // UploadImage method
    private void uploadImage() {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + key  + ".jpeg");

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot) {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(getActivity(),
                                                    "Recipe Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    Bundle extras = new Bundle();
                                    extras.putString("image", key + ".jpeg");
                                    extras.putSerializable("Recipe", (Serializable) mRecipe);
                                    Intent in = new Intent(getContext(), DetailsActivity.class);
                                    in.putExtras(extras);
                                    startActivity(in);
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(getActivity(),
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int) progress + "%");
                                }
                            });
        }
    }
}
