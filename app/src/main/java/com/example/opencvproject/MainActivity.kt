package com.example.opencvproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.opencvproject.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils

private const val TAG = "TEST_OPEN_CV_ANDROID"
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding // binding을 클래스 전체에서 사용하기 위해 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed.")
        } else {
            Log.d(TAG, "OpenCV initialization succeeded.")
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // binding.root를 통해 메인 레이아웃에 접근

        fun updateImage() {
            Log.d(TAG, "updateImage() called")
            val candies: Mat = Imgcodecs.imread("drawable/candies.png")
            val dst = Mat()

            val h = binding.rangeH.values
            val s = binding.rangeS.values
            val v = binding.rangeV.values

            Log.d(TAG, "h: $h, s: $s, v: $v")

            runOnUiThread {
                if (h.size >= 2 && s.size >= 2 && v.size >= 2) {
                    Log.d(TAG, "Valid array sizes")
                    val lower = Scalar(h[0].toDouble(), s[0].toDouble(), v[0].toDouble())
                    val upper = Scalar(h[1].toDouble(), s[1].toDouble(), v[1].toDouble())
                    Core.inRange(candies, lower, upper, dst)
                    val bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
                    Utils.matToBitmap(dst, bitmap)
                    binding.dst.setImageBitmap(bitmap)
                } else {
                    Log.e(TAG, "Invalid array sizes")
                }
            }
        }
        binding.rangeH.addOnChangeListener { slider, value, fromUser ->
            updateImage()
        }

        binding.rangeS.addOnChangeListener { slider, value, fromUser ->
            updateImage()
        }

        binding.rangeV.addOnChangeListener { slider, value, fromUser ->
            updateImage()
        }
    }
}