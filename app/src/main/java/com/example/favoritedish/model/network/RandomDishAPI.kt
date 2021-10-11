package com.example.favoritedish.model.network

import com.example.favoritedish.model.entities.RandomDish
import com.example.favoritedish.utils.Constants
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

//https://api.spoonacular.com/recipes/random?apiKey=ba990d639a0d4bd8966a51b0a63e9bd9&limitLicense=true&tags=vegetarian,%20dessert&number=1
/**
 * an interface to define the endpoint of the API
 */
interface RandomDishAPI {

    /**
     * To make GET request
     *
     * Pass the endpoint of the URL that is defined in the Constants
     */
    @GET(Constants.API_ENDPOINT)
    fun getRandomDish(
        //Query parameter appended to the URL. This is the best practice instead of appending it as have done in the browser.
        @Query(Constants.API_KEY) apiKey: String,
        @Query(Constants.LIMIT_LICENSE) limitLicense: Boolean,
        @Query(Constants.TAGS) tags: String,
        @Query(Constants.NUMBER) number: Int
    ): Single<RandomDish.Recipes>  //The Single class implements the Reactive Pattern for a single value response.

    // http://reactivex.io/documentation/single.html
    // http://reactivex.io/RxJava/javadoc/io/reactivex/Single.html
}