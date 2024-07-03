package abhishekh.diseasedetector

import abhishekh.diseasedetector.ml.PlantModel
import abhishekh.diseasedetector.ml.SkinModel
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.InputStream

class HomePage : AppCompatActivity() {

    private lateinit var framePlant: FrameLayout
    private lateinit var frameSkin: FrameLayout
    private lateinit var imagePlant: ImageView
    private lateinit var imageSkin: ImageView
    private lateinit var imageSelected: ImageView
    private lateinit var btnCamera: Button
    private lateinit var btnGallery: Button
    private lateinit var btnPredict: Button
    private lateinit var tvResult: TextView
    private var category: String = "Plant"
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageProcessor: ImageProcessor
    private var imageSize = 225

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        framePlant = findViewById(R.id.framePlant)
        frameSkin = findViewById(R.id.frameSkin)
        imagePlant = findViewById(R.id.imagePlant)
        imageSkin = findViewById(R.id.imageSkin)
        imageSelected = findViewById(R.id.imageSelected)
        btnCamera = findViewById(R.id.btnCamera)
        btnGallery = findViewById(R.id.btnGallery)
        btnPredict = findViewById(R.id.btnPredict)
        tvResult = findViewById(R.id.tvResult)

        imageProcessor = ImageProcessor.Builder()
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        imagePlant.setOnClickListener {

            if (category == "Skin") {

                category = "Plant"
                imageSize = 225
                framePlant.setBackgroundResource(R.color.colorSecondary)
                frameSkin.setBackgroundResource(R.color.viewBlack)
            }
        }

        imageSkin.setOnClickListener {

            if (category == "Plant") {

                category = "Skin"
                imageSize = 224
                frameSkin.setBackgroundResource(R.color.colorSecondary)
                framePlant.setBackgroundResource(R.color.viewBlack)
            }
        }

        btnCamera.setOnClickListener {

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (cameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(cameraIntent, 321)
            } else {
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
            }
        }

        btnGallery.setOnClickListener {

            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

            if (galleryIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(galleryIntent, 123)
            } else {

                Toast.makeText(this, "No gallery app available", Toast.LENGTH_SHORT).show()
            }
        }

        btnPredict.setOnClickListener {

            if (category == "Plant") {

                tvResult.text = predictPlant()
            }
            else if (category == "Skin") {

                tvResult.text = predictSkin()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let {

                val inputStream: InputStream? = contentResolver.openInputStream(it)
                imageBitmap = BitmapFactory.decodeStream(inputStream)
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageSize, imageSize, true)
                imageSelected.setImageBitmap(imageBitmap)
                inputStream?.close()
            }
        }
        else if (requestCode == 321 && resultCode == Activity.RESULT_OK) {

            imageBitmap = data?.extras?.get("data") as Bitmap
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageSize, imageSize, true)
            imageSelected.setImageBitmap(imageBitmap)
        }
    }

    fun predictPlant(): String {

        tvResult.text = ""

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(imageBitmap)
        tensorImage = imageProcessor.process(tensorImage)
        val model = PlantModel.newInstance(applicationContext)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 225, 225, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(tensorImage.buffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        var maxIdx = 0

        outputFeature0.forEachIndexed { index, fl ->

            if (outputFeature0[maxIdx] < fl) {

                maxIdx = index
            }
        }

        var result = ""

        if (maxIdx == 0) {

            result = "Healthy"
        }
        else if (maxIdx == 1) {

            result = "Powdery"
        }
        else if (maxIdx == 2) {

            result = "Rust"
        }

        model.close()

        return result
    }

    fun predictSkin(): String {

        tvResult.text = ""

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(imageBitmap)
        tensorImage = imageProcessor.process(tensorImage)
        val model = SkinModel.newInstance(applicationContext)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(tensorImage.buffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        var maxIdx = 0

        outputFeature0.forEachIndexed { index, fl ->

            if (outputFeature0[maxIdx] < fl) {

                maxIdx = index
            }
        }

        Log.e("FindError", maxIdx.toString())
        var result = arrayOf<String>("Actinic keratosis", "Atopic Dermatitis", "Benign keratosis", "Dermatofibroma", "Melanocytic nevus", "Melanoma", "Squamous cell carcinoma", "Tinea Ringworm Candidiasis", "Vascular lesion")

        model.close()

        return result[maxIdx]
    }
}


