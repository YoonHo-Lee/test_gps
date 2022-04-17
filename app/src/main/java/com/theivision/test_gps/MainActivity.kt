package com.theivision.test_gps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.theivision.test_gps.databinding.ActivityMainBinding
import java.util.*

// https://fre2-dom.tistory.com/134?category=949323 참고

class MainActivity : AppCompatActivity() {
    private val TAG = "+++++MainActivity"

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val btnGps:Button by lazy { binding.btnGPS }
    private val tvLat:TextView by lazy { binding.tvLat }
    private val tvLon:TextView by lazy { binding.tvLon }
    private val tvGeocoder:TextView by lazy { binding.tvGeocoder }

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    internal lateinit var mLocationRequest: LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        mLocationRequest =  LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        btnGps.setOnClickListener {
            if (checkPermissionForLocation(this)) {
                startLocationUpdates()
            }
            Toast.makeText(this@MainActivity, "qwer", Toast.LENGTH_LONG).show()
        }
    }

    // 위치 권한이 있는지 확인하는 메서드
    private fun checkPermissionForLocation(context: Context): Boolean {
        // Android 6.0 Marshmallow 이상에서는 위치 권한에 추가 런타임 권한이 필요
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }



    private fun startLocationUpdates() {

        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // 기기의 위치에 관한 정기 업데이트를 요청하는 메서드 실행
        // 지정한 루퍼 스레드(Looper.myLooper())에서 콜백(mLocationCallback)으로 위치 업데이트를 요청

        Looper.myLooper()?.let {
            mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
                it
            )
        }
    }


    // 시스템으로 부터 위치 정보를 콜백으로 받음
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }
    // 시스템으로 부터 받은 위치정보를 화면에 갱신해주는 메소드
    fun onLocationChanged(location: Location) {
        mLastLocation = location
        tvLat.text = "위도 : ${mLastLocation.latitude}" // 갱신 된 위도
        tvLon.text = "경도 : ${mLastLocation.longitude}" // 갱신 된 경도

        val address = onGeoCoder(mLastLocation.latitude, mLastLocation.longitude)
        tvGeocoder.text = "주소 : ${address[1]}, ${address[2]}, ${address[3]}, ${address[4]}" // 주소 Geocoder

    }




    // 사용자에게 권한 요청 후 결과에 대한 처리 로직
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Log.d(TAG, "onRequestPermissionsResult : 권한 허용 거부")
                // 권한 거부시 미리 설정해놓은 위치 기준으로 출력.
                // ex) 대부분의 회사들은 강남역이나 서울역, 회사본사위치 등으로 지정
            }
        }
    }


    private fun onGeoCoder(lat:Double, lon:Double): List<String> {
        var address: List<String> = listOf("서울특별시", "성북구", "종암동")
        val geocoder = Geocoder(this@MainActivity, Locale.KOREA)

        val addrList = geocoder.getFromLocation(lat, lon, 1)
        for (addr in addrList) {
            Log.d(TAG, "Geocoder : ${addr.toString()}")
            val splitedAddr = addr.getAddressLine(0).split(" ")
            address = splitedAddr
        }

        return address
    }
}
