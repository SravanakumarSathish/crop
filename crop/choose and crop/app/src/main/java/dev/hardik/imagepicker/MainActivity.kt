package dev.hardik.imagepicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.techno.developer.soccer.quiz.ui.bindView
import java.io.IOException

class MainActivity : AppCompatActivity() {

  private val imgProfile by bindView<CircularImageView>(R.id.imgProfile)
  private val imgPlus by bindView<CircularImageView>(R.id.imgPlus)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    loadProfileDefault()

    imgPlus.setOnClickListener { onProfileImageClick() }
    imgProfile.setOnClickListener { onProfileImageClick() }

    // Clearing older images from cache directory
    // don't call this line if you want to choose multiple images in the same activity
    // call this once the bitmap(s) usage is over
    ImagePickerActivity.clearCache(this)
  }

  private fun loadProfile(url: String) {
    Log.d(TAG, "Image cache path: $url")

    Glide.with(this)
        .load(url)
        .into(imgProfile)
    imgProfile.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent))
  }

  private fun loadProfileDefault() {
    Glide.with(this)
        .load(R.drawable.ic_user)
        .into(imgProfile)
    imgProfile.setColorFilter(ContextCompat.getColor(this, R.color.profile_default_tint))
  }

  private fun onProfileImageClick() {
    Dexter.withActivity(this)
        .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .withListener(object : MultiplePermissionsListener {
          override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            if (report.areAllPermissionsGranted()) {
              showImagePickerOptions()
            }

            if (report.isAnyPermissionPermanentlyDenied) {
              showSettingsDialog()
            }
          }

          override fun onPermissionRationaleShouldBeShown(
            permissions: List<PermissionRequest>,
            token: PermissionToken
          ) {
            token.continuePermissionRequest()
          }
        })
        .check()
  }

  private fun showImagePickerOptions() {
    ImagePickerActivity.showImagePickerOptions(this,
        object : ImagePickerActivity.PickerOptionListener {
          override fun onTakeCameraSelected() {
            launchCameraIntent()
          }

          override fun onChooseGallerySelected() {
            launchGalleryIntent()
          }
        })
  }

  private fun launchCameraIntent() {
    val intent = Intent(this@MainActivity, ImagePickerActivity::class.java)
    intent.putExtra(
        ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION,
        ImagePickerActivity.REQUEST_IMAGE_CAPTURE
    )

    // setting aspect ratio
    intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true)
    intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1) // 16x9, 1x1, 3:4, 3:2
    intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1)

    // setting maximum bitmap width and height
    intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true)
    intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000)
    intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000)

    startActivityForResult(intent, REQUEST_IMAGE)
  }

  private fun launchGalleryIntent() {
    val intent = Intent(this@MainActivity, ImagePickerActivity::class.java)
    intent.putExtra(
        ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION,
        ImagePickerActivity.REQUEST_GALLERY_IMAGE
    )

    // setting aspect ratio
    intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true)
    intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1) // 16x9, 1x1, 3:4, 3:2
    intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1)
    startActivityForResult(intent, REQUEST_IMAGE)
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_IMAGE) {
      if (resultCode == Activity.RESULT_OK) {
        val uri = data!!.getParcelableExtra<Uri>("path")
        try {
          // You can update this bitmap to your server
          // val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

          // loading profile image from local cache
          loadProfile(uri!!.toString())
        } catch (e: IOException) {
          e.printStackTrace()
        }

      }
    }
  }

  /**
   * Showing Alert Dialog with Settings option
   * Navigates user to app settings
   * NOTE: Keep proper title and message depending on your app
   */
  private fun showSettingsDialog() {
    val builder = AlertDialog.Builder(this@MainActivity)
    builder.setTitle(getString(R.string.dialog_permission_title))
    builder.setMessage(getString(R.string.dialog_permission_message))
    builder.setPositiveButton(getString(R.string.go_to_settings)) { dialog, /*which*/_ ->
      dialog.cancel()
      openSettings()
    }
    builder.setNegativeButton(
        getString(android.R.string.cancel)
    ) { dialog, /*which*/_ -> dialog.cancel() }
    builder.show()
  }

  // navigating user to app settings
  private fun openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivityForResult(intent, 101)
  }

  companion object {
    private val TAG = MainActivity::class.java.simpleName
    const val REQUEST_IMAGE = 100
  }
}
