package com.example.favoritedish.model.database

import androidx.annotation.WorkerThread
import com.example.favoritedish.model.entities.FavDish
import kotlinx.coroutines.flow.Flow

/**
 * A Repository manages queries and allows you to use multiple backend

 * The DAO is passed into the repository constructor as opposed to the whole database. This is
  because it only needs access to the DAO, since the DAO contains all the read/write methods for
  the database. There's no need to expose the entire database to the repository.
 */

class FavDishRepository (private val favDishDao: FavDishDao) {

    /**
     * By default Room runs suspend queries off the main thread, therefore, we don't need to
    implement anything else to ensure we're not doing long running database work off the main thread.
     */
    @WorkerThread
    suspend fun insertFavDishData(favDish: FavDish){
      favDishDao.insertFavDishDetails(favDish)
    }

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allDishesList: Flow<List<FavDish>> = favDishDao.getAllDishesList()  //Create a variable for the dishes list to access it from ViewModel.

    //Update the details that can be called from the ViewModel class
    @WorkerThread
    suspend fun updateFavDishData(favDish: FavDish){
        favDishDao.updateFavDishDetails(favDish)
    }

    //Get the list of favorite dishes from the DAO and pass it to the ViewModel.
    val favoriteDishes: Flow<List<FavDish>> = favDishDao.getFavoriteDishesList()

    @WorkerThread
    suspend fun deleteFavDishData(favDish: FavDish){
        favDishDao.deleteFavDishDetails(favDish)
    }

    //function get the filtered list of Dishes based on the selection
    fun filteredListDishes(value: String): Flow<List<FavDish>> =
        favDishDao.getFilterDishesLise(value)   //value: dish type selection

    //search feature
//    fun searchDatabase(searchQuery: String): Flow<List<FavDish>>{
//        return favDishDao.searchDatabase(searchQuery)
//    }
}