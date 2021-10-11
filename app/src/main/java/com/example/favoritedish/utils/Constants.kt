package com.example.favoritedish.utils

object Constants {

    const val DISH_TYPE: String = "DishType"
    const val DISH_CATEGORY: String = "DishCategory"
    const val DISH_COOKING_TIME: String = "DishCookingTime"

    const val DISH_IMAGE_SOURCE_LOCAL: String = "Local"
    const val DISH_IMAGE_SOURCE_ONLINE: String = "Online"

    //for passing the DishDetails to AddUpdateDishActivity
    const val EXTRA_DISH_DETAILS: String = "DishDetails"

    const val ALL_ITEMS: String = "All"
    const val FILTER_SELECTION: String = "FilterSelection"

    //https://api.spoonacular.com/recipes/random?apiKey=ba990d639a0d4bd8966a51b0a63e9bd9&limitLicense=true&tags=vegetarian,%20dessert&number=1

    const val BASE_URL = "https://api.spoonacular.com/"
    const val API_ENDPOINT: String = "recipes/random"

    //api key value from spoonacular console
    const val API_KEY_VALUE: String = "ba990d639a0d4bd8966a51b0a63e9bd9"

    //key params
    const val API_KEY: String = "apiKey"
    const val LIMIT_LICENSE: String = "limitLicense"
    const val TAGS: String = "tags"
    const val NUMBER: String = "number"

    //Add the default values to the constants
    // KEY PARAMS VALUES ==> CAN CHANGE AS PER REQUIREMENT FROM HERE TO MAKE THE DIFFERENCE IN THE API RESPONSE
    const val LIMIT_LICENSE_VALUE: Boolean = true
    const val TAG_VALUE: String = "vegetarian, dessert"
    const val NUMBER_VALUE: Int = 1     //random 1 dish


    fun dishType(): ArrayList<String>{
        val list = ArrayList<String>()
        list.add("breakfast")
        list.add("lunch")
        list.add("snacks")
        list.add("dinner")
        list.add("salad")
        list.add("side dish")
        list.add("dessert")
        list.add("other")
        return list
    }

    fun dishCategories(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add("Pizza")
        list.add("BBQ")
        list.add("Bakery")
        list.add("Burger")
        list.add("Cafe")
        list.add("Chicken")
        list.add("Dessert")
        list.add("Drinks")
        list.add("Hot Dogs")
        list.add("Juices")
        list.add("Sandwich")
        list.add("Tea & Coffee")
        list.add("Wraps")
        list.add("Other")
        return list
    }

    fun dishCookTime(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add("10")
        list.add("15")
        list.add("20")
        list.add("30")
        list.add("45")
        list.add("50")
        list.add("60")
        list.add("90")
        list.add("120")
        list.add("150")
        list.add("180")
        return list
    }
}