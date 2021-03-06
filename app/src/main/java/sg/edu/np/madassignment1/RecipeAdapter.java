package sg.edu.np.madassignment1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.MyViewHolder> implements Filterable {

    private List<Recipe> recipeList;
    private List<Recipe> recipeListFull;
    Activity rActivity;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    Context context;

    RecipeAdapter(Context context, ArrayList<Recipe> recipeList, Activity rActivity){
        this.recipeList = recipeList;
        this.context = context;
        this.rActivity = rActivity;
        recipeListFull = new ArrayList<>(recipeList);
    }

    @NonNull
    @Override
    public RecipeAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.recipe_viewholder,
                parent,
                false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Recipe selectedRecipe = recipeList.get(position);
        holder.myRecipeName.setText(selectedRecipe.getName());
        holder.myRecipeCuisine.setText(selectedRecipe.getCuisine());
        String ratings = String.format("%.1f", selectedRecipe.getRatings().calculateOverallRatings()) ;
        holder.myRecipeRating.setRating(Float.parseFloat(ratings));
        //for getting image
        String imgName = selectedRecipe.getRecipeId() + ".jpeg";
        StorageReference imageRef = storage.getReference().child("images").child(imgName);
        Glide.with(context).load(imageRef).diskCacheStrategy(DiskCacheStrategy.DATA).centerCrop().into(holder.myRecipeImg);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString("image", imgName);
                extras.putSerializable("Recipe", (Serializable)selectedRecipe);
                Intent in = new Intent(holder.itemView.getContext(), DetailsActivity.class);
                in.putExtras(extras);
                holder.itemView.getContext().startActivity(in);
                //Slide from Right to Left Transition
                rActivity.overridePendingTransition(R.transition.slide_in_right, R.transition.slide_out_left);
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView myRecipeName, myRecipeCuisine;
        ImageView myRecipeImg;
        RatingBar myRecipeRating;
        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            myRecipeName = itemView.findViewById(R.id.myRecipeName);
            myRecipeCuisine = itemView.findViewById(R.id.myRecipeCuisine);
            myRecipeRating = itemView.findViewById(R.id.myRecipeRating);
            myRecipeImg = itemView.findViewById(R.id.myRecipeImg);
        }
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    @Override
    public Filter getFilter() { return recipeFilter; }

    public Filter getCuisineFilter() {return cuisineFilter; }

    private Filter recipeFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Recipe> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(recipeListFull);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Recipe r : recipeListFull){
                    if (r.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(r);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if(recipeList!=null){
                recipeList.clear();
                recipeList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        }
    };

    private Filter cuisineFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Recipe> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                filteredList.addAll(recipeListFull);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Recipe r : recipeListFull){
                    if (r.getCuisine().toLowerCase().contains(filterPattern)){
                        filteredList.add(r);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if(recipeList != null){
                recipeList.clear();
                recipeList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        }
    };
}
