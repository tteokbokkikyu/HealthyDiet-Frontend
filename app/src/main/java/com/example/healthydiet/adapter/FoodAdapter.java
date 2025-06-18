//package com.example.healthydiet.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.healthydiet.R;
//import com.example.healthydiet.entity.FoodItem;
//import com.example.healthydiet.entity.Recipe;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
//
//    private final List<FoodItem> foodList;
//
//    public FoodAdapter(List<FoodItem> foodList) {
//        this.foodList = foodList;
//    }
//
//    @Override
//    public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_card, parent, false);
//        return new FoodViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(FoodViewHolder holder, int position) {
//        FoodItem food = foodList.get(position);
//        holder.tvFoodName.setText(food.getName() + " (" + food.getCalories() + " kcal)");
//
//        // 获取对应食谱（这里调用你原先的方法）
//        List<Recipe> recipes = getRecipesForFood(food);
//        RecipeAdapter recipeAdapter = new RecipeAdapter(recipes);
//        holder.rvRecipes.setAdapter(recipeAdapter);
//        holder.rvRecipes.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
//    }
//
//    @Override
//    public int getItemCount() {
//        return foodList.size();
//    }
//
//    static class FoodViewHolder extends RecyclerView.ViewHolder {
//        TextView tvFoodName;
//        RecyclerView rvRecipes;
//
//        public FoodViewHolder(View itemView) {
//            super(itemView);
//            tvFoodName = itemView.findViewById(R.id.tvFoodName);
//            rvRecipes = itemView.findViewById(R.id.rvRecipes);
//        }
//    }
//
//    // 这里可复用你已有方法
//    private List<Recipe> getRecipesForFood(FoodItem foodItem) {
//        List<Recipe> recipes = new ArrayList<>();
//        recipes.add(new Recipe(foodItem.getName() + " 食谱A", "适合早餐", 250));
//        recipes.add(new Recipe(foodItem.getName() + " 食谱B", "营养均衡", 300));
//        recipes.add(new Recipe(foodItem.getName() + " 食谱C", "适合运动后食用", 280));
//        return recipes;
//    }
//}
