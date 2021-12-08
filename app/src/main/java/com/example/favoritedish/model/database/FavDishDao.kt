package com.example.favoritedish.model.database

import androidx.room.*
import com.example.favoritedish.model.entities.FavDish
import kotlinx.coroutines.flow.Flow


/**
    * When you use a Room database, you query the database by defining and calling Kotlin functions in
    your code. These Kotlin functions map to SQL queries. You define those mappings in a DAO using
    annotations, and Room creates the necessary code.

    * Think of a DAO as defining a custom interface for accessing your database.
 */
@Dao
interface FavDishDao {


    /**
     * All queries must be executed on a separate thread. They cannot be executed from Main Thread
       or it will cause an crash.

     * Room has Kotlin coroutines support.
     * This allows your queries to be annotated with the suspend modifier and then called from a
       coroutine or from another suspension function.
     */

    //function to insert favorite dish details to the local database using Room
    @Insert
    suspend fun insertFavDishDetails(favDish: FavDish)

    /**
     * When data changes, you usually want to take some action, such as displaying the updated data in the UI.
     * This means you have to observe the data so when it changes, you can react.
     *
     * To observe data changes we will use Flow.
     * Use a return value of type Flow in your method description,
     * and Room generates all necessary code to update the Flow when the database is updated.
     *
     * A Flow is an async sequence of values
     * Flow produces values one at a time (instead of all at once) that can generate values from async operations
     * like network requests, database calls, or other async code.
     * It supports coroutines throughout its API, so you can transform a flow using coroutines as well!
     */
    @Query("SELECT * FROM FAV_DISHES_TABLE ORDER BY ID")
    fun getAllDishesList(): Flow<List<FavDish>>

    @Update
    suspend fun updateFavDishDetails(favDish: FavDish)

    /**
     * SQLite does not have a boolean data type. Room maps it to an INTEGER column, mapping true to 1 and false to 0.
     */
    @Query("SELECT * FROM FAV_DISHES_TABLE WHERE favorite_dish = 1")
    fun getFavoriteDishesList(): Flow<List<FavDish>>

    /**
     * Delete favorite dish details from the local database using room
     */
    @Delete
    suspend fun deleteFavDishDetails(favDish: FavDish)

    @Query("SELECT * FROM FAV_DISHES_TABLE WHERE type = :filterType")   //use ':' because filtertype is String
    fun getFilterDishesLise(filterType: String): Flow<List<FavDish>>

    //search feature
//    @Query("SELECT * FROM Fav_dishes_table WHERE title LIKE :searchQuery")
//    fun searchDatabase(searchQuery: String): Flow<List<FavDish>>
}