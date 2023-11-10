package com.example.opencvproject

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.opencvproject.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.android.Utils
import org.opencv.imgproc.Imgproc

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var candies: Mat
    private lateinit var dst: Mat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 레이아웃 설정
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // OpenCV 초기화
        OpenCVLoader.initDebug()

        // 이미지 로드
        candies = loadCandiesImage()

        // BGR 색 공간을 HSV로 변환
        Imgproc.cvtColor(candies, candies, Imgproc.COLOR_BGR2HSV)

        dst = Mat()

        // 이미지 업데이트 함수
        fun updateImage() {
            // HSV 범위 설정
            val h = binding.rangeH.values
            val s = binding.rangeS.values
            val v = binding.rangeV.values

            // 최소 및 최대 HSV 값 생성
            val lower = Scalar(h[0].toDouble(), s[0].toDouble(), v[0].toDouble())
            val upper = Scalar(h[1].toDouble(), s[1].toDouble(), v[1].toDouble())

            // Core.inRange 함수를 사용하여 필터링
            Core.inRange(candies, lower, upper, dst)

            // 필터링된 결과를 binding.dst에 표시
            val bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.RGB_565)
            Utils.matToBitmap(dst, bitmap)
            binding.dst.setImageBitmap(bitmap)
        }

        // Slider 값 변경 이벤트 처리
        binding.rangeH.addOnChangeListener { _, _, _ -> updateImage() }
        binding.rangeS.addOnChangeListener { _, _, _ -> updateImage() }
        binding.rangeV.addOnChangeListener { _, _, _ -> updateImage() }
    }

    private fun loadCandiesImage(): Mat {
        // 이미지 리소스 로드
        val resourceId = resources.getIdentifier("candies", "drawable", packageName)
        val bitmap = BitmapFactory.decodeResource(resources, resourceId)

        // Bitmap을 Mat으로 변환
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
    }
}
