package com.example.favoritedish.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.favoritedish.model.entities.FavDish

//This is the backend. The database. This used to be done by the OpenHelper
//https://developer.android.com/codelabs/kotlin-android-training-room-database?index=..%2F..android-kotlin-fundamentals#5

//Set the version as 1. Whenever you change the schema, you'll have to increase the version number.
//Set exportSchema to false, so as not to keep schema version history backups.
@Database(entities = [FavDish::class], version = 1)
abstract class FavDishRoomDatabase: RoomDatabase() {

    abstract fun favDishDao(): FavDishDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time

        /*
            Annotate INSTANCE with @Volatile. The value of a volatile variable will never be cached,
        and all writes and reads will be done to and from the main memory. This helps make sure the
        value of INSTANCE is always up-to-date and the same to all execution threads. It means that
        changes made by one thread to INSTANCE are visible to all other threads immediately, and you
        don't get a situation where, say, two threads each update the same entity in a cache, which
        would create a problem.

            The INSTANCE variable will keep a reference to the database, when one has been created.
        This helps you avoid repeatedly opening connections to the database, which is
        computationally expensive.
         */
        @Volatile
        private var INSTANCE: FavDishRoomDatabase? = null

        fun getDatabase(context: Context): FavDishRoomDatabase {

            /*
                If the INSTANCE is not null, then return it. If it is, then create the database

                Multiple threads can potentially ask for a database instance at the same time, resulting
            in two databases instead of one. This problem is not likely to happen in this sample
            app, but it's possible for a more complex app. Wrapping the code to get the database
            into synchronized means that only one thread of execution at a time can enter this block
            of code, which makes sure the database only gets initialized once.
             */
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(    //use the database builder to get a database
                    context.applicationContext,
                    FavDishRoomDatabase::class.java,
                    "fav_dish_database"
                )
                        /*
                            Use .fallbackToDestructiveMigration() to provide a migration object with a migration strategy for when the
                        schema changes. A migration object is an object that defines how you take
                        all rows with the old schema and convert them to rows in the new schema,
                        so that no data is lost.
                         */
                    .fallbackToDestructiveMigration()
                    .build()    //This should remove the Android Studio errors.
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}