package com.example.favoritedish.view.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.favoritedish.R
import com.example.favoritedish.databinding.ActivityAddUpdateDishesBinding
import com.example.favoritedish.databinding.DialogCustomImageSelectionBinding
import com.karumi.dexter.Dexter
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.audiofx.BassBoost
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.favoritedish.application.FavDishApplication
import com.example.favoritedish.databinding.DialogCustomListBinding
import com.example.favoritedish.model.entities.FavDish
import com.example.favoritedish.utils.Constants
import com.example.favoritedish.view.adapters.CustomListItemAdapter
import com.example.favoritedish.viewmodel.FavDishViewModel
import com.example.favoritedish.viewmodel.FavDishViewModelFactory
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


class AddUpdateDishesActivity : AppCompatActivity(), View.OnClickListener { //Implement the View.OnClickListener

    private lateinit var mBinding: ActivityAddUpdateDishesBinding

    private var mImagePath: String = ""

    private lateinit var mCustomListDialog: Dialog

    private var mFavDishDetails: FavDish? = null

    /**
     * Create an instance of the ViewModel class so that we can access its methods in our View class

     * To create the ViewModel we used the viewModels delegate, passing in an instance of our FavDishViewModelFactory.
     * This is constructed based on the repository retrieved from the FavDishApplication.
     */
    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityAddUpdateDishesBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        //Get the dish details from intent extra and initialize the mFavDishDetails variable
        if(intent.hasExtra(Constants.EXTRA_DISH_DETAILS)){
            mFavDishDetails = intent.getParcelableExtra(Constants.EXTRA_DISH_DETAILS)
        }

        setupActionBar()

        mFavDishDetails?.let {
            if (it.id != 0){
                mImagePath = it.image

                //load the dish image in the iv
                Glide.with(this@AddUpdateDishesActivity)
                    .load(mImagePath)
                    .centerCrop()
                    .into(mBinding.ivDishImage)

                mBinding.etTitle.setText(it.title)
                mBinding.etType.setText(it.type)
                mBinding.etCategory.setText(it.category)
                mBinding.etIngredients.setText(it.ingredients)
                mBinding.etCookingTime.setText(it.cookingTime)
                mBinding.etDirectionToCook.setText(it.directionToCook)

                mBinding.btnAddDish.text = resources.getString(R.string.lbl_update_dish)
            }
        }

        // Assign the click event to the image button.
        mBinding.ivAddDishImage.setOnClickListener(this@AddUpdateDishesActivity)

        mBinding.etType.setOnClickListener(this@AddUpdateDishesActivity)
        mBinding.etCategory.setOnClickListener(this@AddUpdateDishesActivity)
        mBinding.etCookingTime.setOnClickListener(this@AddUpdateDishesActivity)

        mBinding.btnAddDish.setOnClickListener(this@AddUpdateDishesActivity)
    }

    private fun setupActionBar() {
        setSupportActionBar(mBinding.toolbarAddDishActivity)

        //Update the title accordingly "ADD" or "UPDATE"
        if (mFavDishDetails != null && mFavDishDetails!!.id != 0){
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_edit_dish)
            }
        } else{
            supportActionBar?.let {
                it.title = resources.getString(R.string.title_add_dish)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        mBinding.toolbarAddDishActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.iv_add_dish_image -> {
                customImageSelectionDialog()
                return  //get out of this execution
            }

            R.id.et_type ->{
                customItemsListDialog(
                    resources.getString(R.string.title_select_dish_type),
                    Constants.dishType(),
                    Constants.DISH_TYPE
                )
                return
            }

            R.id.et_category -> {
                customItemsListDialog(
                    resources.getString(R.string.title_select_dish_category),
                    Constants.dishCategories(),
                    Constants.DISH_CATEGORY
                )
                return
            }

            R.id.et_cooking_time -> {
                customItemsListDialog(
                    resources.getString(R.string.title_select_dish_cooking_time),
                    Constants.dishCookTime(),
                    Constants.DISH_COOKING_TIME
                )
                return
            }

            R.id.btn_add_dish -> {

                // Define the local variables and get the EditText values.
                // For Dish Image we have the global variable defined already.

                val title = mBinding.etTitle.text.toString().trim { it <= ' ' }
                val type = mBinding.etType.text.toString().trim { it <= ' ' }
                val category = mBinding.etCategory.text.toString().trim { it <= ' ' }
                val ingredients = mBinding.etIngredients.text.toString().trim { it <= ' ' }
                val cookingTimeInMinutes = mBinding.etCookingTime.text.toString().trim { it <= ' ' }
                val cookingDirection = mBinding.etDirectionToCook.text.toString().trim { it <= ' ' }

                when {

                    TextUtils.isEmpty(mImagePath) -> {
                        Toast.makeText(
                            this@AddUpdateDishesActivity,
                            resources.getString(R.string.err_msg_select_dish_image),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    TextUtils.isEmpty(title) -> {
                        Toast.makeText(
                            this@AddUpdateDishesActivity,
                            resources.getString(R.string.err_msg_enter_dish_title),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    TextUtils.isEmpty(type) -> {
                        Toast.makeText(
                            this@AddUpdateDishesActivity,
                            resources.getString(R.string.err_msg_select_dish_type),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    TextUtils.isEmpty(category) -> {
                        Toast.makeText(
                            this@AddUpdateDishesActivity,
                            resources.getString(R.string.err_msg_select_dish_category),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(ingredients) -> {
                        Toast.makeText(
                            this@AddUpdateDishesActivity,
                            resources.getString(R.string.err_msg_enter_dish_ingredients),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(cookingTimeInMinutes) -> {
                        Toast.makeText(
                            this@AddUpdateDishesActivity,
                            resources.getString(R.string.err_msg_select_dish_cooking_time),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    TextUtils.isEmpty(cookingDirection) -> {
                        Toast.makeText(
                            this@AddUpdateDishesActivity,
                            resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        //Update the data and pass the details to ViewModel to Insert or Update
                        var dishID =0
                        var imageSource = Constants.DISH_IMAGE_SOURCE_LOCAL
                        var favoriteDish = false

                        mFavDishDetails?.let {
                            if(it.id != 0){
                                dishID = it.id
                                imageSource = it.imageSource
                                favoriteDish = it.favoriteDish
                            }
                        }

                        val favDishDetails: FavDish = FavDish(
                            mImagePath,
                            Constants.DISH_IMAGE_SOURCE_LOCAL,
                            title,
                            type,
                            category,
                            ingredients,
                            cookingTimeInMinutes,
                            cookingDirection,
                            favoriteDish,
                            dishID
                        )

                        if (dishID == 0 ){
                            //pass the value to the ViewModelClass
                            mFavDishViewModel.insert(favDishDetails)

                            Toast.makeText(
                                this@AddUpdateDishesActivity,
                                "You successfully added your favorite dish details.",
                                Toast.LENGTH_LONG
                            ).show()

                            Log.e("Insertion", "Success")
                        } else {
                            mFavDishViewModel.update(favDishDetails)

                            Toast.makeText(
                                this@AddUpdateDishesActivity,
                                "You successfully updated your favorite dish details.",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.e("Updating", "Success")
                        }


                        finish()    //finish activity
                    }
                }
            }
        }
    }

    private fun customImageSelectionDialog() {
        val dialog = Dialog(this@AddUpdateDishesActivity)
        val binding : DialogCustomImageSelectionBinding = DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {

            Dexter.withContext(this@AddUpdateDishesActivity)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
                .withListener(object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        //all the permission are granted, launch the CAMERA to capture an image.
                        if(report!!.areAllPermissionsGranted()){
                            //Start camera using the Image capture action. Get the result in the onActivityResult method
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(intent, CAMERA)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permisons: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        //show alert dialog
                        showRationalDialogForPermissions()
                    }

                }).onSameThread()
                .check()

            dialog.dismiss()
        }

        binding.tvGallery.setOnClickListener{

            Dexter.withContext(this@AddUpdateDishesActivity)
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        //all the permission are granted, launch the gallery to select and image
                        if (report!!.areAllPermissionsGranted()){
                            //Launch the gallery for Image selection using the constant
                            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            startActivityForResult(galleryIntent, GALLERY)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()

            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /**
         * Receive the result from a previous call to
         * {@link #startActivityForResult(Intent, int)}.  This follows the
         * related Activity API as described there in
         * {@link Activity#onActivityResult(int, int, Intent)}.
         *
         * @param requestCode The integer request code originally supplied to
         *                    startActivityForResult(), allowing you to identify who this
         *                    result came from.
         * @param resultCode The integer result code returned by the child activity
         *                   through its setResult().
         * @param data An Intent, which can return result data to the caller
         *               (various data can be attached to Intent "extras").
         */
        if(resultCode == RESULT_OK){
            if (requestCode == CAMERA){
                data?.extras?.let {
                    val thumbnail: Bitmap = data.extras!!.get("data") as Bitmap //Bitmap from data

                    // Set Capture Image bitmap to the imageView using Glide
                    Glide.with(this@AddUpdateDishesActivity)
                        .load(thumbnail)
                        .centerCrop()
                        .into(mBinding.ivDishImage)

                    //Save the captured image via Camera to the app directory and get back the image path
                    mImagePath = saveImageToInternalStorage(thumbnail)

                    // Replace the add icon with edit icon once the image is selected
                    mBinding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@AddUpdateDishesActivity,
                            R.drawable.ic_vector_edit
                        )
                    )
                }

            } else if (requestCode == GALLERY){
                data?.let{
                    //get the select image URI
                    val selectedPhotoUri = data.data

                    Glide.with(this@AddUpdateDishesActivity)
                        .load(selectedPhotoUri)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable>{
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.e("TAG", "Error loading image", e)
                                return false // important to return false so the error placeholder can be placed
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                //get the bitmap and save it to the local storage and get the image path
                                val bitmap: Bitmap = resource!!.toBitmap()
                                mImagePath = saveImageToInternalStorage(bitmap)
                                Log.i("ImagePath", mImagePath)
                                return false
                            }

                        })
                        .into(mBinding.ivDishImage)

                    mBinding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@AddUpdateDishesActivity,
                            R.drawable.ic_vector_edit
                        )
                    )
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED){
            Log.e("Cancelled", "Cancelled")
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature." +
                                                    " It can be enabled under Application Settings")
            .setPositiveButton(
                "GO SETTINGS"
            ){ _,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName,null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel   "){
                dialog,_ -> dialog.dismiss()
            }.show()
    }

    //Create to save a copy of an image to internal storage for app to use. Return a file absolute path.
    private fun saveImageToInternalStorage(bitmap: Bitmap): String{
        val wrapper = ContextWrapper(applicationContext)    //get the context wrapper instance

        /**
         * File creation mode: the default mode, where the created file can only
         * be accessed by the calling application (or all applications sharing the
         * same user ID).
         */
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)    //initialize new file and get directory
        file = File(file,"${UUID.randomUUID()}.jpg")    //Mention a file name to save the image, random file name

        try {
            val stream: OutputStream = FileOutputStream(file)   //get the file output stream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)    //compress bitmap
            stream.flush()  //flush the stream
            stream.close()  //close the stream
        } catch (e: IOException){
            e.printStackTrace()
        }
        return file.absolutePath    //return the saved image absolute path
    }

    /**
     * A function to launch the custom list dialog.
     *
     * @param title - Define the title at runtime according to the list items.
     * @param itemsList - List of items to be selected.
     * @param selection - By passing this param you can identify the list item selection.
     */
    private fun customItemsListDialog(title: String, itemsList: List<String>, selection: String){
        mCustomListDialog = Dialog(this@AddUpdateDishesActivity)

        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        mCustomListDialog.setContentView(binding.root)

        binding.tvTitle.text = title

        //set the layoutManager that this  RecycleView will use
        binding.rvList.layoutManager = LinearLayoutManager(this@AddUpdateDishesActivity)

        //adapter class is initialized and list is passed in the param
        val adapter = CustomListItemAdapter(this@AddUpdateDishesActivity, null, itemsList, selection)

        //adapter instance is set to the recycleview to inflate the items
        binding.rvList.adapter = adapter

        mCustomListDialog.show()
    }

    /**
     * A function to set the selected item to the view.
     *
     * @param item - Selected Item.
     * @param selection - Identify the selection and set it to the view accordingly.
     */
    fun selectedListItem(item: String, selection: String){
        when(selection){
            Constants.DISH_TYPE ->{
                mCustomListDialog.dismiss()
                mBinding.etType.setText(item)
            }

            Constants.DISH_CATEGORY ->{
                mCustomListDialog.dismiss()
                mBinding.etCategory.setText(item)
            }
            else ->{
                mCustomListDialog.dismiss()
                mBinding.etCookingTime.setText(item)
            }
        }
    }

    companion object{
        private const val CAMERA = 1
        private const val GALLERY = 2
        //Declare a constant variable for directory name to store the images
        private const val IMAGE_DIRECTORY = "FavDishImages"
    }
}