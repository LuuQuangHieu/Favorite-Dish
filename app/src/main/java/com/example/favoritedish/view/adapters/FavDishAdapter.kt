package com.example.favoritedish.view.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.favoritedish.R
import com.example.favoritedish.databinding.ItemDishLayoutBinding
import com.example.favoritedish.model.entities.FavDish
import com.example.favoritedish.utils.Constants
import com.example.favoritedish.view.activities.AddUpdateDishesActivity
import com.example.favoritedish.view.fragments.AllDishesFragment
import com.example.favoritedish.view.fragments.FavoriteDishesFragment

class FavDishAdapter(private val fragment: Fragment): RecyclerView.Adapter<FavDishAdapter.ViewHolder>() {

    private var dishes: List<FavDish> = listOf()

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDishLayoutBinding =
            ItemDishLayoutBinding.inflate(LayoutInflater.from(fragment.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dish = dishes[position]

        //load the dish image in the imageview
        Glide.with(fragment)
            .load(dish.image)
            .centerCrop()
            .into(holder.ivDishImage)

        holder.tvTitle.text = dish.title

        //open DishDetailsFragment
        holder.itemView.setOnClickListener {
            if (fragment is AllDishesFragment){
                fragment.dishDetails(dish)
            } else if (fragment is FavoriteDishesFragment){
                fragment.dishDetails(dish)
            }
        }

        if (fragment is AllDishesFragment) {
            holder.ibMore.visibility = View.VISIBLE
        } else if (fragment is FavoriteDishesFragment) {
            holder.ibMore.visibility = View.GONE
        }

        holder.ibMore.setOnClickListener {
            val popup = PopupMenu(fragment.context, holder.ibMore)
            popup.menuInflater.inflate(R.menu.menu_adapter, popup.menu)

            popup.setOnMenuItemClickListener {
                if (it.itemId == R.id.action_edit_dish) {

                    //pass the dishdetails to addupdateactivity
                    val intent = Intent(fragment.requireActivity(), AddUpdateDishesActivity::class.java)
                    intent.putExtra(Constants.EXTRA_DISH_DETAILS, dish)
                    fragment.requireActivity().startActivity(intent)

                } else if (it.itemId == R.id.action_delete_dish) {
                    if (fragment is AllDishesFragment){
                        fragment.deleteDish(dish)
                    }
                }

                true
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int {
        return dishes.size
    }

    //Create a function that will have the updated list of dishes that we will bind it to the adapter class.
    fun dishesList(list: List<FavDish>){
        dishes = list
        notifyDataSetChanged()
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class ViewHolder(view: ItemDishLayoutBinding): RecyclerView.ViewHolder(view.root) {
        //Holds the textview that will add each item to
        val ivDishImage = view.ivDishImage
        val tvTitle = view.tvDishTitle

        val ibMore = view.ibMore
    }
}