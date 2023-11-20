/*package com.example.opencvproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }
}*/

package com.example.opencvproject

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import com.example.opencvproject.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

private const val TAG = "TEST_OPEN_CV_ANDROID"
private const val REQUEST_IMAGE_CAPTURE = 1

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var textViewRGB: TextView
    private lateinit var textViewColor: TextView
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)

        // OpenCV 라이브러리 초기화
        OpenCVLoader.initDebug()

        imageView = findViewById(R.id.imageView)
        textViewRGB = findViewById(R.id.textViewRGB)
        textViewColor = findViewById(R.id.textViewColor)
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.candies)

        imageView.setImageBitmap(bitmap)
        imageView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()

                val location = IntArray(2)
                v.getLocationOnScreen(location)
                val viewX = location[0]
                val viewY = location[1]

                val imageX = x - viewX
                val imageY = y - viewY

                val imageWidth = v.width
                val imageHeight = v.height

                val relativeX = (imageX.toFloat() / imageWidth * bitmap.width).toInt()
                val relativeY = (imageY.toFloat() / imageHeight * bitmap.height).toInt()

                val hsv = getHSV(relativeX, relativeY)
                val rgb = HSVtoRGB(hsv)
                showRGB(rgb)
                showColorName(hsv)

                // 클릭한 부분을 표시할 네모 이미지를 생성합니다.
                val rectBitmap =
                    Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(rectBitmap)
                val rectPaint = Paint().apply {
                    color = Color.WHITE // 네모 이미지의 색상을 설정합니다.
                    style = Paint.Style.STROKE
                    strokeWidth = 20f
                }

                // 클릭한 부분의 좌표를 기준으로 사각형의 좌표값을 계산합니다.
                val rectLeft = relativeX - 50
                val rectTop = relativeY - 50
                val rectRight = relativeX + 50
                val rectBottom = relativeY + 50

                canvas.drawRect(
                    rectLeft.toFloat(),
                    rectTop.toFloat(),
                    rectRight.toFloat(),
                    rectBottom.toFloat(),
                    rectPaint
                )

                // 클릭한 부분의 이미지와 네모 이미지를 결합합니다.
                val combinedBitmap =
                    Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                val combinedCanvas = Canvas(combinedBitmap)
                combinedCanvas.drawBitmap(bitmap, 0f, 0f, null)
                combinedCanvas.drawBitmap(rectBitmap, 0f, 0f, null)

                // 이미지뷰에 결합된 이미지를 설정합니다.
                imageView.setImageBitmap(combinedBitmap)
            }
            true
        }

        val cameraIcon = findViewById<ImageView>(R.id.cameraIcon)
        cameraIcon.setOnClickListener {
            openCamera()
        }
    }

    private fun openCamera() {
        takePicture.launch(null)
    }

    private fun getHSV(x: Int, y: Int): FloatArray {
        val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
        Utils.bitmapToMat(bitmap, mat)

        val pixel = mat.get(y, x)
        val rgb = IntArray(3)
        rgb[0] = pixel[0].toInt() and 0xFF // Red
        rgb[1] = pixel[1].toInt() and 0xFF // Green
        rgb[2] = pixel[2].toInt() and 0xFF // Blue

        val hsv = FloatArray(3)
        Color.RGBToHSV(rgb[0], rgb[1], rgb[2], hsv)

        return hsv
    }

    private fun HSVtoRGB(hsv: FloatArray): IntArray {
        val rgb = IntArray(3)
        val color = Color.HSVToColor(hsv)
        rgb[0] = (color shr 16) and 0xFF
        rgb[1] = (color shr 8) and 0xFF
        rgb[2] = color and 0xFF

        return rgb
    }

    private fun showRGB(rgb: IntArray) {
        val (red, green, blue) = rgb
        val rgbText = String.format("#%02X%02X%02X", red, green, blue)
        textViewRGB.text = "$rgbText"
    }

    private fun getColorName(hsv: FloatArray): String {
        val rgb = HSVtoRGB(hsv)
        val (red, green, blue) = rgb
        val colorMap = mapOf(
            "White(흰색)" to intArrayOf(255, 255, 255),
            "Black(검정색)" to intArrayOf(0, 0, 0),
            "Red(빨간색)" to intArrayOf(255, 0, 0),
            "Blue(파랑색)" to intArrayOf(0, 0, 255),
            "Yellow(노란색)" to intArrayOf(255, 255, 0),
            "Green(초록색)" to intArrayOf(0, 255, 0),
            "Viridian(짙은녹색)" to intArrayOf(0, 100, 0),
            "Olive Green(짙은 녹두색)" to intArrayOf(0, 128, 0),
            "Brown(갈색)" to intArrayOf(165, 42, 42),
            "Yellow Ochre(황토색)" to intArrayOf(198, 138, 18),
            "Vandyke Brown(고동색)" to intArrayOf(88, 70, 48),
            "Purple(자주색)" to intArrayOf(128, 0, 128), //rgb값 수정
            "Orange(주황색)" to intArrayOf(255, 165, 0),
            "Pale Orange(연주황색)" to intArrayOf(255, 178, 102),
            "Vermilion(다홍색)" to intArrayOf(227, 66, 52),
            "Pink(분홍색)" to intArrayOf(255, 192, 203),
            "Sky Blue(하늘색)" to intArrayOf(135, 206, 235),
            "Yellow Green(연두)" to intArrayOf(129, 193, 71),
            "Gray(회색)" to intArrayOf(128, 128, 128),
            "Ultramarine(군청색)" to intArrayOf(0, 0, 128),
            "Beige(베이지색)" to intArrayOf(245, 245, 220),
            "Ivory(아이보리색)" to intArrayOf(255, 255, 240),
            "Blue Green(청록색)" to intArrayOf(0, 255, 255),
            "lime(라임색)" to intArrayOf(191, 255, 0),
            "Lemon Yellow(레몬색)" to intArrayOf(255, 247, 0),
            "Prussian Blue(남색)" to intArrayOf(0, 0, 139),
            "Violet(보라색)" to intArrayOf(83, 32, 161),
        )

        var ColorName = ""
        var minDistance = Double.MAX_VALUE

        for ((colorName, colorValue) in colorMap) {
            val distance = Math.sqrt(
                Math.pow(red - colorValue[0].toDouble(), 2.0) +
                        Math.pow(green - colorValue[1].toDouble(), 2.0) +
                        Math.pow(blue - colorValue[2].toDouble(), 2.0)
            )
            if (distance < minDistance) {
                minDistance = distance
                ColorName = colorName
            }
        }

        return ColorName
    }

    private fun showColorName(hsv: FloatArray) {
        val rgb = HSVtoRGB(hsv)
        val (red, green, blue) = rgb
        val colorName = getColorName(hsv)
        val colorText = " $colorName"
        textViewColor.text = colorText

        val colorBox = findViewById<TextView>(R.id.colorBox)
        colorBox.setBackgroundColor(android.graphics.Color.rgb(red, green, blue))
    }


    private fun getHSVFromBitmap(imageBitmap: Bitmap): FloatArray {
        val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
        Utils.bitmapToMat(bitmap, mat)

        val hsvImage = Mat()
        Imgproc.cvtColor(mat, hsvImage, Imgproc.COLOR_RGB2HSV)

        val pixel = hsvImage.get(bitmap.height / 2, bitmap.width / 2)
        val hsv = FloatArray(3)
        hsv[0] = pixel[0].toFloat() // Hue
        hsv[1] = pixel[1].toFloat() // Saturation
        hsv[2] = pixel[2].toFloat() // Value

        return hsv
    }

    // 새로운 결과 처리 변수를 추가합니다.
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { result ->
            result?.let { bitmap ->
                // 이미지를 처리하고 색상을 추출하는 로직
                val centerX = bitmap.width / 2
                val centerY = bitmap.height / 2
                val hsv = getHSV(centerX, centerY)
                val rgb = HSVtoRGB(hsv)
                showRGB(rgb)
                showColorName(hsv)

                // 클릭한 부분을 표시할 네모 이미지를 생성합니다.
                val rectBitmap = Bitmap.createBitmap(
                    bitmap.width,
                    bitmap.height,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(rectBitmap)
                val rectPaint = Paint().apply {
                    color = Color.WHITE // 네모 이미지의 색상을 설정합니다.
                    style = Paint.Style.STROKE
                    strokeWidth = 20f
                }

                // 클릭한 부분의 좌표를 기준으로 사각형의 좌표값을 계산합니다.
                val rectLeft = bitmap.width / 2 - 50
                val rectTop = bitmap.height / 2 - 50
                val rectRight = bitmap.width / 2 + 50
                val rectBottom = bitmap.height / 2 + 50

                canvas.drawRect(
                    rectLeft.toFloat(),
                    rectTop.toFloat(),
                    rectRight.toFloat(),
                    rectBottom.toFloat(),
                    rectPaint
                )

                // 클릭한 부분의 이미지와 네모 이미지를 결합합니다.
                val combinedBitmap = Bitmap.createBitmap(
                    bitmap.width,
                    bitmap.height,
                    Bitmap.Config.ARGB_8888
                )
                val combinedCanvas = Canvas(combinedBitmap)
                combinedCanvas.drawBitmap(bitmap, 0f, 0f, null)
                combinedCanvas.drawBitmap(rectBitmap, 0f, 0f, null)

                // 이미지뷰에 결합된 이미지를 설정합니다.
                imageView.setImageBitmap(combinedBitmap)
            }
        }
}

//rgb
/*class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var textViewRGB: TextView
    private lateinit var textViewColor: TextView
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)

        // OpenCV 초기화
        OpenCVLoader.initDebug()

        imageView = findViewById(R.id.imageView)
        textViewRGB = findViewById(R.id.textViewRGB)
        textViewColor = findViewById(R.id.textViewColor)
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.candies)

        imageView.setImageBitmap(bitmap)
        imageView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()

                val location = IntArray(2)
                v.getLocationOnScreen(location)
                val viewX = location[0]
                val viewY = location[1]

                val imageX = x - viewX
                val imageY = y - viewY

                val imageWidth = v.width
                val imageHeight = v.height

                val relativeX = (imageX.toFloat() / imageWidth * bitmap.width).toInt()
                val relativeY = (imageY.toFloat() / imageHeight * bitmap.height).toInt()

                val rgb = getRGB(relativeX, relativeY)
                showRGB(rgb)
                showColorName(rgb)

                // 클릭한 부분을 표시할 네모 이미지를 생성합니다.
                val rectBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(rectBitmap)
                val rectPaint = Paint().apply {
                    color = Color.WHITE // 네모 이미지의 색상을 설정합니다.
                    style = Paint.Style.STROKE
                    strokeWidth = 20f
                }

                // 클릭한 부분의 좌표를 기준으로 사각형의 좌표값을 계산합니다.
                val rectLeft = relativeX - 50
                val rectTop = relativeY - 50
                val rectRight = relativeX + 50
                val rectBottom = relativeY + 50

                canvas.drawRect(rectLeft.toFloat(), rectTop.toFloat(), rectRight.toFloat(), rectBottom.toFloat(), rectPaint)

                // 클릭한 부분의 이미지와 네모 이미지를 결합합니다.
                val combinedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                val combinedCanvas = Canvas(combinedBitmap)
                combinedCanvas.drawBitmap(bitmap, 0f, 0f, null)
                combinedCanvas.drawBitmap(rectBitmap, 0f, 0f, null)

                // 이미지뷰에 결합된 이미지를 설정합니다.
                imageView.setImageBitmap(combinedBitmap)
            }
            true
        }
    }

    private fun getRGB(x: Int, y: Int): IntArray {
        val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
        Utils.bitmapToMat(bitmap, mat)

        val pixel = mat.get(y, x)
        val rgb = IntArray(3)
        rgb[0] = pixel[0].toInt() and 0xFF // Red
        rgb[1] = pixel[1].toInt() and 0xFF // Green
        rgb[2] = pixel[2].toInt() and 0xFF // Blue

        return rgb
    }

    private fun showRGB(rgb: IntArray) {
        val (red, green, blue) = rgb
        val rgbText = String.format("#%02X%02X%02X", red, green, blue)
        textViewRGB.text = "$rgbText"
    }

    private fun getColorName(red: Int, green: Int, blue: Int): String {
        val colorMap = mapOf(
            "White(흰색)" to intArrayOf(255, 255, 255),
            "Ivory(아이보리색)" to intArrayOf(255, 255, 240),
            "Lemon Yellow(레몬색)" to intArrayOf(255, 247, 0),
            "Yellow(노란색)" to intArrayOf(255, 255, 0),
            "Pale Orange(연주황색)" to intArrayOf(255, 178, 102),
            "Orange(주황색)" to intArrayOf(255, 165, 0),
            "Vermilion(다홍색)" to intArrayOf(227, 66, 52),
            "Red(빨간색)" to intArrayOf(255, 0, 0),
            "Pink(분홍색)" to intArrayOf(255, 192, 203),
            "Yellow Ochre(황토색)" to intArrayOf(227, 168, 87),
            "Brown(갈색)" to intArrayOf(165, 42, 42),
            "Vandyke Brown(고동색)" to intArrayOf(112, 48, 160),
            "Yellow Green(연두)" to intArrayOf(204, 255, 0),
            "Green(초록색)" to intArrayOf(0, 255, 0),
            "Viridian(짙은녹색)" to intArrayOf(0, 100, 0),
            "Blue Green(청록색)" to intArrayOf(0, 255, 255),
            "Olive Green(짙은 녹두색)" to intArrayOf(0, 128, 0),
            "Sky Blue(하늘색)" to intArrayOf(135, 206, 235),
            "Cobalt Blue(파랑색)" to intArrayOf(0, 0, 255),
            "Ultramarine(군청색)" to intArrayOf(0, 0, 128),
            "Red Purple(자주색)" to intArrayOf(128, 0, 128),
            "Prussian Blue(남색색)" to intArrayOf(0, 0, 139),
            "Violet(남보라색)" to intArrayOf(83, 32, 161),
            "Black(검정색)" to intArrayOf(0, 0, 0),
        )

        var ColorName = ""
        var minDistance = Double.MAX_VALUE

        for ((colorName, colorValue) in colorMap) {
            val distance = Math.sqrt(
                Math.pow(red - colorValue[0].toDouble(), 2.0) +
                        Math.pow(green - colorValue[1].toDouble(), 2.0) +
                        Math.pow(blue - colorValue[2].toDouble(), 2.0)
            )
            if (distance < minDistance) {
                minDistance = distance
                ColorName = colorName
            }
        }

        return ColorName
    }

    private fun showColorName(rgb: IntArray) {
        val (red, green, blue) = rgb
        val colorName = getColorName(red, green, blue)
        val colorText = " $colorName"
        textViewColor.text = colorText
        //textViewColor.setBackgroundColor(android.graphics.Color.rgb(red, green, blue))
        val colorBox = findViewById<TextView>(R.id.colorBox)
        colorBox.setBackgroundColor(android.graphics.Color.rgb(red, green, blue))
    }
}*/
