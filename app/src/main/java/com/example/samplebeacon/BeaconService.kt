package com.example.samplebeacon

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.util.Log
import android.widget.Toast
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.BeaconTransmitter
import org.altbeacon.beacon.RangeNotifier
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.RegionViewModel

class BeaconService(val context: Activity) {
//    private val beaconParser = BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT)
    private val parser = BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
    private val beaconTransmitter: BeaconTransmitter
    private val beaconManager: BeaconManager
    private val uuid: String = "2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6"
    private val major:Int = 1
    private val minor:Int = 2
    private val region = Region("all-beacons", null, null, null)
    private val checkPermission:CheckPermission = CheckPermission(context)

    private val TAG = "Beacon Service"

    init {
        beaconTransmitter = BeaconTransmitter(context, parser)
        beaconManager = BeaconManager.getInstanceForApplication(context)
        parser.setHardwareAssistManufacturerCodes(arrayOf(0x004c).toIntArray())
        beaconManager.beaconParsers.add(parser)
    }

    fun getRegionViewModel():RegionViewModel{
        return beaconManager.getRegionViewModel(region)
    }

    fun startScanBeacons(onScanResult: (List<Beacon>) -> Unit) {
        // Thiết lập thời gian quét foreground (foreground scan) là 1 giây (1000ms)
        beaconManager.foregroundBetweenScanPeriod = 1000L
        beaconManager.addRangeNotifier(object : RangeNotifier {
            override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region) {
                beacons?.let {
                    Toast.makeText(context, "Quét được ${it.size}", Toast.LENGTH_SHORT).show()
                    // Xử lý danh sách beacon quét được ở đây
                    for (beacon in it) {
                        // Thực hiện các thao tác với beacon
                        val uuid = beacon.id1
                        val major = beacon.id2
                        val minor = beacon.id3
                        val distance = beacon.distance
                        println("Beacon: UUID=$uuid, Major=$major, Minor=$minor, Distance=$distance")
                    }
                    onScanResult(it.toList())
                }
            }

        })
        beaconManager.startRangingBeacons(region)
    }

    fun startAdvertising(beacon: Beacon) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
         bluetoothAdapter?.isEnabled == true
        val checkPermission = CheckPermission(context = context)
        Log.d(TAG, checkPermission.CheckBluetoothPermission().toString())
        stopAdvertising()
        if (CheckPermission(context = context).CheckBluetoothPermission() == false){
            CheckPermission(context = context).requestPermission(1)
        }
        // Xác thực và phát tín hiệu Beacon
        beaconTransmitter.startAdvertising(beacon, object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d(TAG, "phát thành công ${settingsInEffect}")
                Toast.makeText(context, "Start advertising successfully", Toast.LENGTH_SHORT).show()
            }

            override fun onStartFailure(errorCode: Int) {
                Log.d(TAG, "phát thất bại, mã lỗi: ${errorCode}")
                Toast.makeText(context, "Start advertising fail", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun startInBackground(timeout: Long) {
        beaconManager.backgroundBetweenScanPeriod = timeout
    }
    fun stopAdvertising() {
        beaconTransmitter.stopAdvertising()
    }

    fun stopScanning() {
        beaconManager.stopRangingBeacons(region)
    }
}