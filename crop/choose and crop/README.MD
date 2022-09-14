Android Choosing Image from Camera or Gallery with Crop Functionality
===================
Android sample project demonstrating choosing an image from gallery or camera with the cropping functionality.


How to Use
===================
Follow the below simple steps to add the library into your project.

1. In [AndroidManifext.xml](https://github.com/Hardik8184/Android-Image-Choose-and-Crop/blob/master/app/src/main/AndroidManifest.xml), add **CAMERA**, **DEXTER** and **STORAGE** permissions. Add **UCrop** activity and **FileProvider** paths.
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:tools="http://schemas.android.com/tools"
  package="dev.hardik.imagepicker">

  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.CAMERA" />
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

  <application
    android:allowBackup="true"
    android:fullBackupContent="@xml/backup_descriptor"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/AppTheme"
    tools:ignore="GoogleAppIndexingWarning">
    <activity android:name=".ImagePickerActivity" />
    <activity
      android:name="dev.hardik.imagepicker.MainActivity"
      android:label="@string/app_name"
      android:screenOrientation="portrait">
      <intent-filter>
        <action android:name="android.intent.action.MAIN" />

        <category android:name="android.intent.category.LAUNCHER" />
      </intent-filter>
    </activity>

    <!-- uCrop cropping activity -->
    <activity
      android:name="com.yalantis.ucrop.UCropActivity"
      android:screenOrientation="portrait"
      android:theme="@style/AppTheme.NoActionBar" />

    <!-- cache directory file provider paths -->
    <provider
      android:name="androidx.core.content.FileProvider"
      android:authorities="${applicationId}.provider"
      android:exported="false"
      android:grantUriPermissions="true">
      <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
    </provider>
  </application>

</manifest>
```

2. Add [file_provider.xml](https://github.com/Hardik8184/Android-Image-Choose-and-Crop/blob/master/app/src/main/res/xml/file_paths.xml) to your **res -> xml** foler.
```
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-cache-path
        name="cache"
        path="camera" />
</paths>
```

uCrop
---
Thanks to [Yalantis](https://github.com/Yalantis) for providing such a beautiful cropping ([uCrop](https://github.com/Yalantis/uCrop)) library. This example uses the uCrop library for cropping functionality.

3. Add **Dexter** and **uCrop** dependencies to your **app/build.gradle**
```gradle
dependencies {
    //...

    implementation "com.karumi:dexter:5.0.0"
    implementation 'com.github.yalantis:ucrop:2.2.2'
}
```
4. Copy [ImagePickerActivity.kt](https://github.com/Hardik8184/Android-Image-Choose-and-Crop/blob/master/app/src/main/java/dev/hardik/imagepicker/ImagePickerActivity.kt) to your project.

5. Launch **ImagePickerActivity** by passing required intent data. Once the image is cropped, you can received the path of the cropped image in **onActivityResult** method.
```kt
class ImagePickerActivity : AppCompatActivity() {

  private var lockAspectRatio = false
  private var setBitmapMaxWidthHeight = false
  private var aspectRatioX = 16
  private var aspectRatioY = 9
  private var bitmapMaxWidth = 1000
  private var bitmapMaxHeight = 1000
  private var imageCompression = 80

  interface PickerOptionListener {
    fun onTakeCameraSelected()

    fun onChooseGallerySelected()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_image_picker)

    val intent = intent
    if (intent == null) {
      Toast.makeText(
          applicationContext, getString(R.string.toast_image_intent_null),
          Toast.LENGTH_LONG
      )
          .show()
      return
    }

    aspectRatioX = intent.getIntExtra(INTENT_ASPECT_RATIO_X, aspectRatioX)
    aspectRatioY = intent.getIntExtra(INTENT_ASPECT_RATIO_Y, aspectRatioY)
    imageCompression = intent.getIntExtra(INTENT_IMAGE_COMPRESSION_QUALITY, imageCompression)
    lockAspectRatio = intent.getBooleanExtra(INTENT_LOCK_ASPECT_RATIO, false)
    setBitmapMaxWidthHeight = intent.getBooleanExtra(INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, false)
    bitmapMaxWidth = intent.getIntExtra(INTENT_BITMAP_MAX_WIDTH, bitmapMaxWidth)
    bitmapMaxHeight = intent.getIntExtra(INTENT_BITMAP_MAX_HEIGHT, bitmapMaxHeight)

    val requestCode = intent.getIntExtra(INTENT_IMAGE_PICKER_OPTION, -1)
    if (requestCode == REQUEST_IMAGE_CAPTURE) {
      takeCameraImage()
    } else {
      chooseImageFromGallery()
    }
  }

  private fun takeCameraImage() {
    Dexter.withActivity(this)
        .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .withListener(object : MultiplePermissionsListener {
          override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            if (report.areAllPermissionsGranted()) {
              fileName = System.currentTimeMillis().toString() + ".jpg"
              val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
              takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getCacheImagePath(fileName))
              if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
              }
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

  private fun chooseImageFromGallery() {
    Dexter.withActivity(this)
        .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .withListener(object : MultiplePermissionsListener {
          override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            if (report.areAllPermissionsGranted()) {
              val pickPhoto = Intent(
                  Intent.ACTION_PICK,
                  Media.EXTERNAL_CONTENT_URI
              )
              startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE)
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

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQUEST_IMAGE_CAPTURE -> if (resultCode == Activity.RESULT_OK) {
        cropImage(getCacheImagePath(fileName))
      } else {
        setResultCancelled()
      }
      REQUEST_GALLERY_IMAGE -> if (resultCode == Activity.RESULT_OK) {
        val imageUri = data!!.data
        cropImage(imageUri)
      } else {
        setResultCancelled()
      }
      UCrop.REQUEST_CROP -> if (resultCode == Activity.RESULT_OK) {
        handleUCropResult(data)
      } else {
        setResultCancelled()
      }
      UCrop.RESULT_ERROR -> {
        val cropError = UCrop.getError(data!!)
        Log.e(TAG, "Crop error: " + cropError!!)
        setResultCancelled()
      }
      else -> setResultCancelled()
    }
  }

  private fun cropImage(sourceUri: Uri?) {
    val destinationUri = Uri.fromFile(File(cacheDir, queryName(contentResolver, sourceUri)))
    val options = UCrop.Options()
    options.setCompressionQuality(imageCompression)

    // applying UI theme
    options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
    options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary))
    options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary))

    if (lockAspectRatio) {
      options.withAspectRatio(aspectRatioX.toFloat(), aspectRatioY.toFloat())
    }

    if (setBitmapMaxWidthHeight) {
      options.withMaxResultSize(bitmapMaxWidth, bitmapMaxHeight)
    }

    UCrop.of(sourceUri!!, destinationUri)
        .withOptions(options)
        .start(this)
  }

  private fun handleUCropResult(data: Intent?) {
    if (data == null) {
      setResultCancelled()
      return
    }
    val resultUri = UCrop.getOutput(data)
    setResultOk(resultUri)
  }

  private fun setResultOk(imagePath: Uri?) {
    val intent = Intent()
    intent.putExtra("path", imagePath)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  private fun setResultCancelled() {
    val intent = Intent()
    setResult(Activity.RESULT_CANCELED, intent)
    finish()
  }

  private fun getCacheImagePath(fileName: String): Uri {
    val path = File(externalCacheDir, "camera")
    if (!path.exists()) path.mkdirs()
    val image = File(path, fileName)
    return getUriForFile(this@ImagePickerActivity, "$packageName.provider", image)
  }

  companion object {

    private val TAG = ImagePickerActivity::class.java.simpleName
    const val INTENT_IMAGE_PICKER_OPTION = "image_picker_option"
    const val INTENT_ASPECT_RATIO_X = "aspect_ratio_x"
    const val INTENT_ASPECT_RATIO_Y = "aspect_ratio_Y"
    const val INTENT_LOCK_ASPECT_RATIO = "lock_aspect_ratio"
    const val INTENT_IMAGE_COMPRESSION_QUALITY = "compression_quality"
    const val INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT = "set_bitmap_max_width_height"
    const val INTENT_BITMAP_MAX_WIDTH = "max_width"
    const val INTENT_BITMAP_MAX_HEIGHT = "max_height"

    const val REQUEST_IMAGE_CAPTURE = 0
    const val REQUEST_GALLERY_IMAGE = 1
    var fileName: String = ""

    fun showImagePickerOptions(
      context: Context,
      listener: PickerOptionListener
    ) {
      // setup the alert builder
      val builder = AlertDialog.Builder(context)
      builder.setTitle(context.getString(R.string.lbl_set_profile_photo))

      // add a list
      val animals = arrayOf(
          context.getString(R.string.lbl_take_camera_picture),
          context.getString(R.string.lbl_choose_from_gallery)
      )
      builder.setItems(animals) { /*dialog*/_, which ->
        when (which) {
          0 -> listener.onTakeCameraSelected()
          1 -> listener.onChooseGallerySelected()
        }
      }

      // create and show the alert dialog
      val dialog = builder.create()
      dialog.show()
    }

    @SuppressLint("Recycle")
    private fun queryName(
      resolver: ContentResolver,
      uri: Uri?
    ): String {
      val returnCursor = resolver.query(uri!!, null, null, null, null)!!
      val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      returnCursor.moveToFirst()
      val name = returnCursor.getString(nameIndex)
      returnCursor.close()
      return name
    }

    /**
     * Calling this will delete the images from cache directory
     * useful to clear some memory
     */
    fun clearCache(context: Context) {
      val path = File(context.externalCacheDir, "camera")
      if (path.exists() && path.isDirectory) {
        for (child in path.listFiles()!!) {
          child.delete()
        }
      }
    }
  }
}
```

Once the **Uri** is received, you can create a bitmap and send to your server or preview on the screen.

Intent Parameters:
==========
| Param      | Description |
| ----------- | ----------- |
| INTENT_IMAGE_PICKER_OPTION      | To define source of the image Camera or Gallery. The values could be **REQUEST_IMAGE_CAPTURE** or **REQUEST_GALLERY_IMAGE**       |
| INTENT_LOCK_ASPECT_RATIO   | Pass `true` to lock the aspect ratio. (16x9, 1x1, 3:4, 3:2)       |
| INTENT_ASPECT_RATIO_X   | Width of aspect ratio (ex: 3)        |
| INTENT_ASPECT_RATIO_Y   | Height of the aspect ratio (ex: 4)        |
| INTENT_IMAGE_COMPRESSION_QUALITY   | The image compression quality 0 - 100. The default value is 80        |
| INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT   | Pass `true` to limit the maximum width and height of the bitmap        |
| INTENT_BITMAP_MAX_WIDTH   | The maximum width of the cropped image        |
| INTENT_BITMAP_MAX_HEIGHT   | The maximum height of the cropped image        |

If you want additional options, you can customize the image picker activity.

Deleting Cached Images:
==========
While the image are taken with camera, they will stored in cached directory. You can clear the cached images once the bitmap is utilized.
```kt
// Clearing older images from cache directory
// don't call this line if you want to choose multiple images in the same activity
// call this once the bitmap(s) usage is over
ImagePickerActivity.clearCache(this)
```

