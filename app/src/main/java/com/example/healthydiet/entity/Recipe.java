package com.example.healthydiet.entity;

public class Recipe {
    private String title;
    private String description;
    private int calories;

    public Recipe(String title, String description, int calories) {
        this.title = title;
        this.description = description;
        this.calories = calories;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getCalories() { return calories; }
}

