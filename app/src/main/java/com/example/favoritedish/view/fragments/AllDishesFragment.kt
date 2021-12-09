package com.example.favoritedish.view.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.favoritedish.R
import com.example.favoritedish.application.FavDishApplication
import com.example.favoritedish.databinding.DialogCustomListBinding
import com.example.favoritedish.databinding.FragmentAllDishesBinding
import com.example.favoritedish.model.entities.FavDish
import com.example.favoritedish.utils.Constants
import com.example.favoritedish.view.activities.AddUpdateDishesActivity
import com.example.favoritedish.view.adapters.CustomListItemAdapter
import com.example.favoritedish.view.adapters.FavDishAdapter
import com.example.favoritedish.viewmodel.FavDishViewModel
import com.example.favoritedish.viewmodel.FavDishViewModelFactory



class AllDishesFragment : Fragment() {

    private lateinit var mBinding: FragmentAllDishesBinding

    private lateinit var mFavDishAdapter: FavDishAdapter

    private lateinit var mCustomListDialog: Dialog

    /**
     * To create the ViewModel we used the viewModels delegate, passing in an instance of our FavDishViewModelFactory.
     * This is constructed based on the repository retrieved from the FavDishApplication.
     */
    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPause() {
        super.onPause()
        Log.e("onPause", "Pause")
    }

    override fun onStop() {
        super.onStop()
        Log.e("onPause", "Stop")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)

        /*
        //search feature
        val search = menu?.findItem(R.id.action_search_dish)
        val searchView = search?.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
         */
    }

//    override fun onQueryTextSubmit(query: String?): Boolean {
//        return true
//    }
//
//    override fun onQueryTextChange(query: String?): Boolean {
//        if(query != null){
//            searchDatabase(query)
//        }
//        return true
//    }
//
//    private fun searchDatabase(query: String) {
//        val searchQuery = "%$query%"
//
//        mFavDishViewModel.searchDatabase(searchQuery).observe(this, { list ->
//            list.let {
//                mFavDishAdapter.dishesList(it)
//            }
//        })
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentAllDishesBinding.inflate(inflater, container,false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //set the LayoutManager that this RecycleView will use
        mBinding.rvDishesList.layoutManager = GridLayoutManager(requireActivity(), 1)

        //adapter class is initialized and list is passed in the param
        mFavDishAdapter = FavDishAdapter(this@AllDishesFragment)
        mBinding.rvDishesList.adapter = mFavDishAdapter

        /**
         * Add an observer on the LiveData returned by getAllDishesList.
         * The onChanged() method fires when the observed data changes and the activity is in the foreground.
         */
        mFavDishViewModel.allDishesList.observe(viewLifecycleOwner){
            dishes ->
                dishes.let {
                    if (it.isNotEmpty()) {

                        mBinding.rvDishesList.visibility = View.VISIBLE
                        mBinding.tvNoDishesAddedYet.visibility = View.GONE

                        mFavDishAdapter.dishesList(it)
                    } else {

                        mBinding.rvDishesList.visibility = View.GONE
                        mBinding.tvNoDishesAddedYet.visibility = View.VISIBLE
                    }
                }
        }
    }

    fun dishDetails(favDish: FavDish){
        findNavController().navigate(AllDishesFragmentDirections.actionNavigationAllDishesToNavigationDishDetails(favDish))
    }

    fun deleteDish(dish: FavDish){
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(resources.getString(R.string.title_delete_dish))
        builder.setMessage(resources.getString(R.string.msg_delete_dish_dialog, dish.title))
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(resources.getString(R.string.lbl_yes)) {
            dialogInterface, _ ->
                mFavDishViewModel.delete(dish)
                dialogInterface.dismiss()   //
        }
        builder.setNegativeButton(resources.getString(R.string.lbl_no)){
            dialogInterface, which ->
                dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)    // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()
    }

    private fun filterDishesDialog(){
        mCustomListDialog = Dialog(requireActivity())

        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)

        mCustomListDialog.setContentView(binding.root)

        binding.tvTitle.text = resources.getString(R.string.title_select_item_to_filter)

        val dishType = Constants.dishType()
        dishType.add(0, Constants.ALL_ITEMS)

        //set the layoutmanager that this recycleview will use
        binding.rvList.layoutManager = LinearLayoutManager(requireActivity())

        //adapter class is initialized and list is passed in the param
        val adapter = CustomListItemAdapter(
            requireActivity(),
            this@AllDishesFragment,
            dishType,
            Constants.FILTER_SELECTION
        )

        //adapter instance is set to the recycleview to inflate the items
        binding.rvList.adapter = adapter

        //start the dialog
        mCustomListDialog.show()
    }

    //function to get the filter item selection and get the list from database accordingly
    fun filterSelection(filterItemSelection: String){
        mCustomListDialog.dismiss()

        Log.i("Filter Selection", filterItemSelection)

        if (filterItemSelection == Constants.ALL_ITEMS){
            mFavDishViewModel.allDishesList.observe(viewLifecycleOwner){
                dishes ->
                    dishes.let {
                        if (it.isNotEmpty()){

                            mBinding.rvDishesList.visibility = View.VISIBLE
                            mBinding.tvNoDishesAddedYet.visibility = View.GONE

                            mFavDishAdapter.dishesList(it)
                        } else{

                            mBinding.rvDishesList.visibility = View.GONE
                            mBinding.tvNoDishesAddedYet.visibility = View.VISIBLE
                        }
                    }
            }
        } else{
            mFavDishViewModel.getFilteredList(filterItemSelection).observe(viewLifecycleOwner){
                dishes ->
                    dishes.let {
                        if(it.isNotEmpty()){

                            mBinding.rvDishesList.visibility = View.VISIBLE
                            mBinding.tvNoDishesAddedYet.visibility = View.GONE

                            mFavDishAdapter.dishesList(it)
                        } else{
                            mBinding.rvDishesList.visibility = View.GONE
                            mBinding.tvNoDishesAddedYet.visibility = View.VISIBLE
                        }
                    }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

//            R.id.action_add_dish ->{
//                startActivity(Intent(requireActivity(), AddUpdateDishesActivity::class.java))
//                return true
//            }

            R.id.action_filter_dishes -> {
                filterDishesDialog()
                return true
            }

            R.id.action_about -> {
                findNavController().navigate(AllDishesFragmentDirections.actionNavigationAllDishesToNavigationAbout())
                return true
            }

        }

        return super.onOptionsItemSelected(item)
    }

}