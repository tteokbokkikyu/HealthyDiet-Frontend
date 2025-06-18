package com.example.healthydiet.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.healthydiet.R;
import com.example.healthydiet.entity.ChatMessage;
import com.example.healthydiet.entity.Recipe;
import com.example.healthydiet.manager.UserManager;
import com.example.healthydiet.entity.FoodItem;
import com.example.healthydiet.entity.FoodRecord;
import com.example.healthydiet.entity.User;
import com.example.healthydiet.websocket.WebSocketManager;
import com.example.healthydiet.websocket.WebSocketMessageType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class DietAnalysisActivity extends AppCompatActivity {
    private List<FoodRecord> TodayRecordList;
    private int total_calories;
    private double total_fat=0;
    private double total_protein=0;
    private double total_carbohydrates=0;
    private double total_sodium=0;
    private double total_potassium=0;
    private double total_dietaryFiber=0;
    private double generation;
    private ProgressBar progressBar;
    private TextView caloriesTextView;  // 用来显示摄入千卡数
    private TextView hintTextView;
    private TableLayout tableLayout;

    private TableLayout tableLayoutfood;

    private Button recipeRecommendationButton;

    private WebSocketManager webSocketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dietanalysis);

        // 初始化 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置返回按钮的点击事件
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 显示返回按钮
            getSupportActionBar().setDisplayShowHomeEnabled(true);  // 启用返回按钮图标
        }

        // 返回按钮的点击监听器
        toolbar.setNavigationOnClickListener(v -> {
            // 处理返回操作
            onBackPressed();  // 执行返回操作
        });

        // 获取传递过来的 List
        Intent intent = getIntent();
        TodayRecordList = (List<FoodRecord>) intent.getSerializableExtra("TodayRecordList");
        total_calories=getIntent().getIntExtra("total_calories", 0);
        total_fat=getIntent().getDoubleExtra("total_fat", 0);
        total_protein=getIntent().getDoubleExtra("total_protein", 0);
        total_carbohydrates=getIntent().getDoubleExtra("total_carbohydrates", 0);
        total_sodium=getIntent().getDoubleExtra("total_sodium", 0);
        total_potassium=getIntent().getDoubleExtra("total_potassium", 0);
        total_dietaryFiber=getIntent().getDoubleExtra("total_dietaryFiber", 0);
        generation=getIntent().getDoubleExtra("generation", 0);


        // 初始化进度条
        progressBar = findViewById(R.id.progress_bar);
        // 设置进度条的最大值和当前进度
        progressBar.setMax((int)generation);  // 设置最大进度为 2000
        progressBar.setProgress(total_calories);  // 将 total_calories 作为当前进度

        caloriesTextView = findViewById(R.id.TextView2);
        String caloriesText = "今日已摄入 " + total_calories + " 千卡";
        caloriesTextView.setText(caloriesText);

        hintTextView = findViewById(R.id.TextView1);
        String hintText = "根据毛德倩公式，每日应摄入热量 " + (int)generation + " 千卡";
        hintTextView.setText(hintText);

        tableLayout = findViewById(R.id.table_layout);  // 获取 TableLayout
        addTableRow("项目","摄入量");
        addTableRow("脂肪",total_fat+"克");
        addTableRow("蛋白质",total_protein+"克");
        addTableRow("碳水化合物",total_carbohydrates+"克");
        addTableRow("膳食纤维",total_dietaryFiber+"克");
        addTableRow("钠",total_sodium+"毫克");
        addTableRow("钾",total_potassium+"毫克");

        User user = UserManager.getInstance().getUser();

        double[] userProfile = {2000-total_calories, 2000*0.45f/4.0-total_carbohydrates, 25.0-total_dietaryFiber, 4700.0-total_potassium,1500.0-total_sodium, 2000*0.2f/9.0-total_fat, user.getWeight()*0.8-total_protein};

        webSocketManager = WebSocketManager.getInstance();
        webSocketManager.logConnectionStatus();  // 记录连接状态
        // 注册食物列表回调
        webSocketManager.registerCallback(WebSocketMessageType.FOOD_LIST, message -> {
            Log.d("FoodList", "Received food list response: " + message);
            try {
                JSONArray foodItems = new JSONArray(message);
                List<FoodItem> foodItemList = new ArrayList<>();

                for (int i = 0; i < foodItems.length(); i++) {
                    JSONObject foodJson = foodItems.getJSONObject(i);
                    FoodItem foodItem = new FoodItem(
                            foodJson.getString("name"),
                            foodJson.getString("type"),
                            foodJson.getInt("calories"),
                            foodJson.getDouble("carbohydrates"),
                            foodJson.getDouble("dietaryFiber"),
                            foodJson.getDouble("potassium"),
                            foodJson.getDouble("sodium"),
                            foodJson.getDouble("fat"),
                            foodJson.getDouble("protein")
                    );
                    int id = foodJson.getInt("foodid");
                    foodItem.setFoodid(id);
                    foodItemList.add(foodItem);
                }
                List<FoodItem> recommendedFoods = recommendTopNFoods(foodItemList, userProfile, 6);

                displayRecommendedFoods(recommendedFoods);
            } catch (Exception e) {
                Log.e("FoodList", "Error processing food list: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // 确保WebSocket已连接后再发送请求
        if (!webSocketManager.isConnected()) {
            Log.d("FoodList", "WebSocket not connected, attempting to reconnect...");
            webSocketManager.reconnect();
        }
        webSocketManager.sendMessage("getAllFood");
        webSocketManager.registerCallback(WebSocketMessageType.RECIPE_LIST, message -> {
            try {
                JSONObject json = new JSONObject(message);
                JSONObject data = json.getJSONObject("data");  // 先取data对象
                String foodName = data.getString("foodName"); // 从data中取foodName
                JSONArray recipeArray = data.getJSONArray("data"); // 从data中取食谱数组


                List<Recipe> recipeList = new ArrayList<>();
                for (int i = 0; i < recipeArray.length(); i++) {
                    JSONObject recipeJson = recipeArray.getJSONObject(i);
                    Recipe recipe = new Recipe(
                            recipeJson.getString("title"),
                            recipeJson.getString("description"),
                            recipeJson.getInt("calories")
                    );
                    recipeList.add(recipe);
                }

                // 缓存这个食物对应的食谱（你可以用 Map 缓存后面优化）
                runOnUiThread(() -> {
                    updateRecipesForFood(foodName, recipeList);
                });

            } catch (Exception e) {
                Log.e("RecipeList", "解析食谱出错：" + e.getMessage());
            }
        });


    }
    private void addTableRow(String item, String calories) {
        // 创建新的 TableRow
        TableRow tableRow = new TableRow(this);

        // 创建 TextView 显示项目名称
        TextView itemTextView = new TextView(this);
        itemTextView.setText(item);
        itemTextView.setPadding(20, 0, 50, 0);  // 设置左边和右边的内边距（增加列间距）
        tableRow.addView(itemTextView);  // 将项目添加到表格行中
        itemTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);  // 设置字体大小为 20sp

        // 创建 TextView 显示摄入量
        TextView caloriesTextView = new TextView(this);
        caloriesTextView.setText(calories);
        caloriesTextView.setPadding(50, 0, 20, 0);  // 设置左边和右边的内边距（增加列间距）
        tableRow.addView(caloriesTextView);  // 将摄入量添加到表格行中
        caloriesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);  // 设置字体大小为 20sp


        // 将新创建的 TableRow 添加到 TableLayout 中
        tableLayout.addView(tableRow);
    }

    private void displayRecommendedFoods(List<FoodItem> recommendedFoods) {
        LinearLayout container = findViewById(R.id.food_recommend_container);
        container.removeAllViews();

        for (FoodItem food : recommendedFoods) {
            // 卡片外层 CardView
            CardView cardView = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(32, 32, 32, 0);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(24f);
            cardView.setCardElevation(10f);
            cardView.setUseCompatPadding(true);

            // 卡片内部 LinearLayout
            LinearLayout cardLayout = new LinearLayout(this);
            cardLayout.setOrientation(LinearLayout.VERTICAL);

            // ===== 食物标题部分（有背景色） =====
            TextView foodTitle = new TextView(this);
            foodTitle.setText("🍽 推荐食物：" + food.getName() + "（" + food.getCalories() + " 千卡）");
            foodTitle.setTextSize(20);
            foodTitle.setTypeface(Typeface.DEFAULT_BOLD);
            foodTitle.setTextColor(Color.WHITE);
            foodTitle.setBackgroundColor(Color.parseColor("#66BB6A")); // 清新绿色
            foodTitle.setPadding(32, 24, 32, 24);
            cardLayout.addView(foodTitle);

            // ===== 推荐食谱区域 =====
            LinearLayout recipeListLayout = new LinearLayout(this);
            recipeListLayout.setOrientation(LinearLayout.VERTICAL);
            recipeListLayout.setBackgroundColor(Color.parseColor("#F5F5F5"));
            recipeListLayout.setPadding(32, 24, 32, 24);

            List<Recipe> recipes = getRecipesForFood(food);
            for (int i = 0; i < recipes.size(); i++) {
                Recipe recipe = recipes.get(i);

                if (i > 0) {
                    View divider = new View(this);
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 1);
                    dividerParams.setMargins(0, 16, 0, 16);
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundColor(Color.parseColor("#CCCCCC"));
                    recipeListLayout.addView(divider);
                }

                TextView recipeTitle = new TextView(this);
                recipeTitle.setText("🍴 " + recipe.getTitle() + " - " + recipe.getCalories() + " 千卡");
                recipeTitle.setTextSize(17);
                recipeTitle.setTypeface(Typeface.DEFAULT_BOLD);
                recipeTitle.setTextColor(Color.parseColor("#333333"));
                recipeListLayout.addView(recipeTitle);

                TextView recipeDesc = new TextView(this);
                recipeDesc.setText(recipe.getDescription());
                recipeDesc.setTextSize(15);
                recipeDesc.setTextColor(Color.parseColor("#666666"));
                recipeDesc.setPadding(16, 4, 0, 8);
                recipeListLayout.addView(recipeDesc);
            }

            // 合并内容进卡片
            cardLayout.addView(recipeListLayout);
            cardView.addView(cardLayout);
            container.addView(cardView);
        }
    }



    private double calculateSimilarity(double[] userPreferences, FoodItem food) {
        double dotProduct = 0;
        double userMagnitude = 0;
        double foodMagnitude = 0;
        double[] foodAttributes = {food.getCalories(), food.getCarbohydrates(), food.getDietaryFiber(), food.getPotassium(),food.getSodium(), food.getFat(), food.getProtein()};

        for (int i = 0; i < userPreferences.length; i++) {
            dotProduct += userPreferences[i] * foodAttributes[i];
            userMagnitude += Math.pow(userPreferences[i], 2);
            foodMagnitude += Math.pow(foodAttributes[i], 2);
        }

        return dotProduct / (Math.sqrt(userMagnitude) * Math.sqrt(foodMagnitude));
    }
    public List<FoodItem> recommendTopNFoods(List<FoodItem> foods, double[] userPreferences, int N) {
        // 创建一个优先队列，按相似度降序排列
        PriorityQueue<FoodItem> queue = new PriorityQueue<>(new Comparator<FoodItem>() {
            @Override
            public int compare(FoodItem f1, FoodItem f2) {
                // 计算食物与用户偏好之间的相似度
                double similarity1 = calculateSimilarity(userPreferences, f1);
                double similarity2 = calculateSimilarity(userPreferences, f2);

                // 按相似度降序排列
                return Double.compare(similarity2, similarity1);
            }
        });

        // 将所有食物添加到优先队列中
        for (FoodItem food : foods) {
            queue.add(food);
        }

        // 取出前N个食物
        List<FoodItem> recommendedFoods = new ArrayList<>();
        for (int i = 0; i < N && !queue.isEmpty(); i++) {
            recommendedFoods.add(queue.poll());
        }

        return recommendedFoods;
    }
    private List<Recipe> getRecipesForFood(FoodItem foodItem) {
        // 发送食物名到后端请求推荐食谱

            try{
                String llmRequest = "getRecipes:{" +
                        "\"foodName\": \"" + foodItem.getName()+ "\"," + "}";
                Log.d("LLM", "Sending getRecipes message: " + llmRequest);

                if (!webSocketManager.isConnected()) {
                    Log.d("LLM", "WebSocket not connected, attempting to reconnect...");
                    webSocketManager.reconnect();
                }
            webSocketManager.sendMessage(llmRequest);
        } catch (Exception e) {
            Log.e("WebSocket", "发送推荐食谱请求失败：" + e.getMessage());
        }

        // 这里可以返回一个空列表或占位数据，实际推荐结果由上面的 callback 异步回填
        return new ArrayList<>();
    }
    private void updateRecipesForFood(String foodName, List<Recipe> recipes) {
        LinearLayout container = findViewById(R.id.food_recommend_container);

        for (int i = 0; i < container.getChildCount(); i++) {
            View cardView = container.getChildAt(i);
            if (cardView instanceof CardView) {
                CardView card = (CardView) cardView;
                LinearLayout cardLayout = (LinearLayout) card.getChildAt(0);
                TextView titleView = (TextView) cardLayout.getChildAt(0);
                if (titleView.getText().toString().contains(foodName)) {
                    // 找到对应的卡片，更新其食谱
                    LinearLayout recipeListLayout = (LinearLayout) cardLayout.getChildAt(1);
                    recipeListLayout.removeAllViews();

                    for (int j = 0; j < recipes.size(); j++) {
                        Recipe recipe = recipes.get(j);

                        if (j > 0) {
                            View divider = new View(this);
                            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, 1);
                            dividerParams.setMargins(0, 16, 0, 16);
                            divider.setLayoutParams(dividerParams);
                            divider.setBackgroundColor(Color.parseColor("#CCCCCC"));
                            recipeListLayout.addView(divider);
                        }

                        TextView recipeTitle = new TextView(this);
                        recipeTitle.setText("🍴 " + recipe.getTitle() + " - " + recipe.getCalories() + " 千卡");
                        recipeTitle.setTextSize(17);
                        recipeTitle.setTypeface(Typeface.DEFAULT_BOLD);
                        recipeTitle.setTextColor(Color.parseColor("#333333"));
                        recipeListLayout.addView(recipeTitle);

                        TextView recipeDesc = new TextView(this);
                        recipeDesc.setText(recipe.getDescription());
                        recipeDesc.setTextSize(15);
                        recipeDesc.setTextColor(Color.parseColor("#666666"));
                        recipeDesc.setPadding(16, 4, 0, 8);
                        recipeListLayout.addView(recipeDesc);
                    }

                    break;
                }
            }
        }
    }



}
