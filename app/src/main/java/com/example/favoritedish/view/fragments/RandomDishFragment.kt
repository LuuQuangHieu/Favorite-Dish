package com.example.favoritedish.view.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.favoritedish.R
import com.example.favoritedish.application.FavDishApplication
import com.example.favoritedish.databinding.FragmentRandomDishBinding
import com.example.favoritedish.model.entities.FavDish
import com.example.favoritedish.model.entities.RandomDish
import com.example.favoritedish.utils.Constants
import com.example.favoritedish.viewmodel.FavDishViewModel
import com.example.favoritedish.viewmodel.FavDishViewModelFactory
import com.example.favoritedish.viewmodel.RandomDishViewModel

class RandomDishFragment : Fragment() {

    private var mBinding: FragmentRandomDishBinding? = null

    private lateinit var mRandomDishViewModel: RandomDishViewModel

    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentRandomDishBinding.inflate(inflater, container, false)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initialize the ViewModel variable
        mRandomDishViewModel = ViewModelProvider(this).get(RandomDishViewModel::class.java)

        //call the function to get the response from API
        mRandomDishViewModel.getRandomRecipeFromAPI()

        //call the observer function
        randomDishViewModelObserver()

        /**
         * Set the setOnRefreshListener of SwipeRefreshLayout as below and call the
         * getRandomDishFromAPI function to get the new dish details on the same screen.
         */
        mBinding!!.srlRandomDish.setOnRefreshListener {
            // This method performs the actual data-refresh operation.
            // The method calls setRefreshing(false) when it's finished.
            mRandomDishViewModel.getRandomRecipeFromAPI()
        }
    }

    private fun randomDishViewModelObserver() {

        mRandomDishViewModel.randomDishResponse.observe(
            viewLifecycleOwner, Observer { randomDishResponse ->
                randomDishResponse?.let {
                    Log.i("Random Dish Response", "$randomDishResponse.recipes[0]")

                    //Hide the Loading ProgressBar of SwipeRefreshLayout once the response is success
                    if (mBinding!!.srlRandomDish.isRefreshing){
                        mBinding!!.srlRandomDish.isRefreshing = false
                    }

                    setRandomDishResponseInUI(randomDishResponse.recipes[0])
                }
            }
        )

        mRandomDishViewModel.randomDishLoadingError.observe(
            viewLifecycleOwner, Observer { dataError ->
                dataError?.let {
                    Log.i("Random Dish API Error", "$dataError")

                    //Hide the Loading ProgressBar of SwipeRefreshLayout when there is an error from API
                    if (mBinding!!.srlRandomDish.isRefreshing){
                        mBinding!!.srlRandomDish.isRefreshing = false
                    }
                }
            }
        )

        mRandomDishViewModel.loadRandomDish.observe(
            viewLifecycleOwner, Observer { loadRandomDish ->
                loadRandomDish?.let {
                    Log.i("Random Dish Loading", "$loadRandomDish")

                    //Show the progress dialog if the SwipeRefreshLayout is not visible and hide when the usage is completed
                    if (loadRandomDish && !mBinding!!.srlRandomDish.isRefreshing){
                        showCustomProgressDialog()
                    } else{
                        hideProgressDialog()
                    }
                }
            }
        )
    }

    //Create a method to populate the API response in the UI
    private fun setRandomDishResponseInUI(recipe: RandomDish.Recipe){

        //load the dish image in the iv
        Glide.with(requireActivity())
            .load(recipe.image)
            .centerCrop()
            .into(mBinding!!.ivDishImage)

        mBinding!!.tvTitle.text = recipe.title

        //default dish type
        var dishType: String = "other"

        if(recipe.dishTypes.isNotEmpty()){
            dishType = recipe.dishTypes[0]
            mBinding!!.tvType.text = dishType
        }

        var ingredients = ""
        for(value in recipe.extendedIngredients){
            if (ingredients.isEmpty()){
                ingredients = value.original
            } else{
                ingredients = ingredients + ", \n" + value.original
            }
        }
        mBinding!!.tvIngredients.text = ingredients

        /**
         *  The instruction or you can say the Cooking direction text is in the HTML format so we
         *  will you the fromHtml to populate it in the TextView.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            mBinding!!.tvCookingDirection.text = Html.fromHtml(
                recipe.instructions,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else{
            @Suppress("DEPRECATION")
            mBinding!!.tvCookingDirection.text = Html.fromHtml(recipe.instructions)
        }

        mBinding!!.tvCookingTime.text =
            resources.getString(
                R.string.lbl_estimate_cooking_time,
                recipe.readyInMinutes.toString())

        //By default load the favorite image button as unselected
        mBinding!!.ivFavoriteDish.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ic_favorite_unselected
            )
        )

        /**
         * Create a variable to avoid the duplication of items that is added by click on the
         * Favorite image to add the dish details to local database.
         */
        var addedToFavorite = false

        /**
         * Assign the click event to the Favorite Button and add the dish details to the local
         * database if user click on it
         */
        mBinding!!.ivFavoriteDish.setOnClickListener {

            if (addedToFavorite){
                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_already_added_to_favorites),
                    Toast.LENGTH_SHORT
                ).show()
            } else{
                //Create a instance of FavDish data model class and fill it with required information from the API response
                val randomDishDetails = FavDish(
                    recipe.image,
                    Constants.DISH_IMAGE_SOURCE_ONLINE,
                    recipe.title,
                    dishType,
                    "Other",
                    ingredients,
                    recipe.readyInMinutes.toString(),
                    recipe.instructions,
                    true
                )

                //Create an instance of FavDishViewModel class and call insert function and pass the required details
                val mFavDishViewModel: FavDishViewModel by viewModels{
                    FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
                }

                mFavDishViewModel.insert(randomDishDetails)

                addedToFavorite = true

                mBinding!!.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_selected
                    )
                )

                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_added_to_favorites),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    //function to show the custom progress dialog
    private fun showCustomProgressDialog(){
        mProgressDialog = Dialog(requireActivity())
        mProgressDialog?.let {
            it.setContentView(R.layout.dialog_custom_progress)
            it.show()
        }
    }

    //function to hide the custom progress dialog
    private fun hideProgressDialog(){
        mProgressDialog?.let {
            it.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

}