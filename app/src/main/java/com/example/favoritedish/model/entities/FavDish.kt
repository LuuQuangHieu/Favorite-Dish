package com.example.favoritedish.model.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


/**
    *    An entity represents an object or a concept, along with its properties, to store in the
    database. An entity class that defines a table, and each instance of that class represents a
    row in that table. The entity class has mappings to tell Room how it intends to present and
    interact with the information in the database.

    *    Room does all the hard work to get from Kotlin data classes to entities that can be stored in
    SQLite tables, and from function declarations to SQL queries.

    *    Define each entity as an annotated data class, and the interactions with that entity as an
    annotated interface, called a data access object (DAO)
     */

    @Parcelize
    //Define your tables as data classes annotated with @Entity
    @Entity(tableName = "Fav_dishes_table")
    data class FavDish(
        //Define properties annotated with @ColumnInfo as columns in the tables
        @ColumnInfo val image: String,
        @ColumnInfo val imageSource: String,    //local or online
        @ColumnInfo val title: String,
        @ColumnInfo val type: String,
        @ColumnInfo val category: String,
        @ColumnInfo val ingredients: String,

        // Specifies the name of the column in the table if want it to be different from the name of the member variable
        @ColumnInfo(name = "cooking_time") val cookingTime: String,
        @ColumnInfo(name = "instructions") val directionToCook: String,
        @ColumnInfo(name = "favorite_dish") var favoriteDish: Boolean = false,
        @PrimaryKey(autoGenerate = true) val id: Int = 0
    ): Parcelable
