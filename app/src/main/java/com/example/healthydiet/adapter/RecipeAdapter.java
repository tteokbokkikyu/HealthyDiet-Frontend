//package com.example.healthydiet.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.healthydiet.R;
//import com.example.healthydiet.entity.Recipe;
//
//import java.util.List;
//
//public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
//
//    private final List<Recipe> recipes;
//
//    public RecipeAdapter(List<Recipe> recipes) {
//        this.recipes = recipes;
//    }
//
//    @Override
//    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
//        return new RecipeViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(RecipeViewHolder holder, int position) {
//        Recipe recipe = recipes.get(position);
//        holder.tvTitle.setText("• " + recipe.getTitle() + " - " + recipe.getCalories() + " kcal");
//        holder.tvDesc.setText(recipe.getDescription());
//    }
//
//    @Override
//    public int getItemCount() {
//        return recipes.size();
//    }
//
//    static class RecipeViewHolder extends RecyclerView.ViewHolder {
//        TextView tvTitle, tvDesc;
//
//        public RecipeViewHolder(View itemView) {
//            super(itemView);
//            tvTitle = itemView.findViewById(R.id.tvRecipeTitle);
//            tvDesc = itemView.findViewById(R.id.tvRecipeDesc);
//        }
//    }
//}
