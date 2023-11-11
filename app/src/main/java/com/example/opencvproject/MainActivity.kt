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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

private const val TAG = "TEST_OPEN_CV_ANDROID"

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var textViewRGB: TextView
    private lateinit var textViewColor: TextView
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                val rectLeft = relativeX - 100
                val rectTop = relativeY - 100
                val rectRight = relativeX + 100
                val rectBottom = relativeY + 100

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
            "White" to intArrayOf(255, 255, 255),
            "Black" to intArrayOf(0, 0, 0),
            "Red" to intArrayOf(255, 0, 0),
            "Orange" to intArrayOf(255, 165, 0),
            "Yellow" to intArrayOf(255, 255, 0),
            "Light Green" to intArrayOf(192, 255, 0),
            "Green" to intArrayOf(0, 255, 0),
            "Mint" to intArrayOf(0, 255, 192),
            "Sky Blue" to intArrayOf(0, 255, 255),
            "Baby Blue" to intArrayOf(0, 192, 255),
            "Blue" to intArrayOf(0, 0, 255),
            "Purple" to intArrayOf(192, 0, 255),
            "Pink" to intArrayOf(255, 0, 192),
            "Light Red" to intArrayOf(255, 192, 192),
            "Peach" to intArrayOf(255, 218, 185),
            "Pale Yellow" to intArrayOf(255, 255, 192),
            "Pale Green" to intArrayOf(192, 255, 192),
            "Pale Sky Blue" to intArrayOf(192, 255, 255),
            "Pale Blue" to intArrayOf(192, 192, 255),
            "Pale Purple" to intArrayOf(255, 192, 255),
            "Brown" to intArrayOf(165, 42, 42),
            "Gray" to intArrayOf(128, 128, 128),
            "Dark Red" to intArrayOf(128, 0, 0),
            "Dark Orange" to intArrayOf(128, 85, 0),
            "Dark Yellow" to intArrayOf(128, 128, 0),
            "Dark Green" to intArrayOf(0, 128, 0),
            "Dark Sky Blue" to intArrayOf(0, 128, 128),
            "Dark Blue" to intArrayOf(0, 0, 128),
            "Dark Purple" to intArrayOf(85, 0, 128),
            "Dark Pink" to intArrayOf(128, 0, 85)
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
}
