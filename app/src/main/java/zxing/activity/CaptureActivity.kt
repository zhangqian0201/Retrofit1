/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zxing.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceHolder
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import com.retrofit.R
import com.retrofit.utils.PictureUtils
import com.retrofit.utils.ToastUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_capture.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import zxing.camera.CameraManager
import zxing.decode.DecodeThread
import zxing.utils.BeepManager
import zxing.utils.CaptureActivityHandler
import zxing.utils.InactivityTimer
import java.io.IOException
import java.util.*

@Suppress("NAME_SHADOWING", "DEPRECATION")
/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
class CaptureActivity : AppCompatActivity(), SurfaceHolder.Callback, EasyPermissions.PermissionCallbacks {
    private val TAG = CaptureActivity::class.java.simpleName
    lateinit var cameraManager: CameraManager
        private set
    private var handler: CaptureActivityHandler? = null
    private lateinit var inactivityTimer: InactivityTimer
    private lateinit var beepManager: BeepManager

    var cropRect: Rect? = null
        private set
    private var isHasSurface = false
    private val HINTS: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)
    private val storagePerm = Manifest.permission.READ_EXTERNAL_STORAGE
    private val list = ArrayList<LocalMedia>()

    private val statusBarHeight: Int
        @SuppressLint("PrivateApi")
        get() {
            try {
                val c = Class.forName("com.android.internal.R\$dimen")
                val obj = c.newInstance()
                val field = c.getField("status_bar_height")
                val x = Integer.parseInt(field.get(obj).toString())
                return resources.getDimensionPixelSize(x)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return 0
        }

    fun getHandler(): Handler? {
        return handler
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_capture)

        initListener()

        inactivityTimer = InactivityTimer(this)
        beepManager = BeepManager(this)

        val animation = TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation
                .RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.9f)
        animation.duration = 4500
        animation.repeatCount = -1
        animation.repeatMode = Animation.RESTART
        capture_scan_line.startAnimation(animation)
    }

    private fun initListener() {
        mBack.setOnClickListener { onBackPressed() }
        mMenu.setOnClickListener { selectImg() }
    }


    private fun initHints() {
        val allFormats = ArrayList<BarcodeFormat>()
        allFormats.add(BarcodeFormat.AZTEC)
        allFormats.add(BarcodeFormat.CODABAR)
        allFormats.add(BarcodeFormat.CODE_39)
        allFormats.add(BarcodeFormat.CODE_93)
        allFormats.add(BarcodeFormat.CODE_128)
        allFormats.add(BarcodeFormat.DATA_MATRIX)
        allFormats.add(BarcodeFormat.EAN_8)
        allFormats.add(BarcodeFormat.EAN_13)
        allFormats.add(BarcodeFormat.ITF)
        allFormats.add(BarcodeFormat.MAXICODE)
        allFormats.add(BarcodeFormat.PDF_417)
        allFormats.add(BarcodeFormat.QR_CODE)
        allFormats.add(BarcodeFormat.RSS_14)
        allFormats.add(BarcodeFormat.RSS_EXPANDED)
        allFormats.add(BarcodeFormat.UPC_A)
        allFormats.add(BarcodeFormat.UPC_E)
        allFormats.add(BarcodeFormat.UPC_EAN_EXTENSION)

        HINTS[DecodeHintType.POSSIBLE_FORMATS] = allFormats
        HINTS[DecodeHintType.CHARACTER_SET] = "utf-8"
    }

    private fun selectImg() {
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            ToastUtil.showShort(this, getString(R.string.external_storage_not_exsit))
            return
        }
        if (EasyPermissions.hasPermissions(this, storagePerm)) {
            list.clear()
            PictureUtils.setImagePicker(this, list = list)
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.camera_pemmision), 1, storagePerm)
        }
    }

    @SuppressLint("Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    data?.let { it ->
                        //获取选中图片的路径
                        val list = PictureSelector.obtainMultipleResult(it)
                        val photoPath = list[0].path

                        Observable.create<String> {
                            val result = syncDecodeQRCode(getDecodeAbleBitmap(photoPath))
                            if (!TextUtils.isEmpty(result)) {
                                it.onNext(result!!)
                            } else {
                                it.onError(Throwable())
                            }
                        }.subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    val resultIntent = Intent()
                                    resultIntent.putExtra("result", it)
                                    setResult(RESULT_OK, resultIntent)
                                    finish()
                                }, {
                                    ToastUtil.showShort(this, "识别失败")
                                })
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 同步解析bitmap二维码。该方法是耗时操作，请在子线程中调用。
     *
     * @param bitmap 要解析的二维码图片
     * @return 返回二维码图片里的内容 或 null
     */
    private fun syncDecodeQRCode(bitmap: Bitmap?): String? {
        var text = ""
        return try {
            bitmap?.apply {
                val pixels = IntArray(width * height)
                getPixels(pixels, 0, width, 0, 0, width, height)
                val source = RGBLuminanceSource(width, height, pixels)
                val result = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source)), HINTS)
                text = result.text
            }
            text
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 将本地图片文件转换成可解码二维码的 Bitmap。为了避免图片太大，这里对图片进行了压缩。
     *
     * @param picturePath 本地图片文件路径
     * @return
     */
    private fun getDecodeAbleBitmap(picturePath: String): Bitmap? {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(picturePath, options)
            var sampleSize = options.outHeight / 400
            if (sampleSize <= 0)
                sampleSize = 1
            options.inSampleSize = sampleSize
            options.inJustDecodeBounds = false

            return BitmapFactory.decodeFile(picturePath, options)
        } catch (e: Exception) {
            return null
        }

    }

    override fun onResume() {
        super.onResume()

        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        cameraManager = CameraManager(application)

        handler = null

        if (isHasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(capture_preview!!.holder)
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            capture_preview!!.holder.addCallback(this)
        }

        inactivityTimer.onResume()
    }

    override fun onPause() {
        handler?.let {
            it.quitSynchronously()
            handler = null
        }
        inactivityTimer.onPause()
        beepManager.close()
        cameraManager.closeDriver()
        if (!isHasSurface) {
            capture_preview.holder.removeCallback(this)
        }
        super.onPause()
    }

    override fun onDestroy() {
        inactivityTimer.shutdown()
        super.onDestroy()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!")
        }
        if (!isHasSurface) {
            isHasSurface = true
            initCamera(holder)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isHasSurface = false
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     * @param bundle    The extras
     */
    fun handleDecode(rawResult: Result, bundle: Bundle) {
        inactivityTimer.onActivity()
        beepManager.playBeepSoundAndVibrate()

        val resultIntent = Intent()
        bundle.putInt("width", cropRect!!.width())
        bundle.putInt("height", cropRect!!.height())
        bundle.putString("result", rawResult.text)
        resultIntent.putExtras(bundle)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun initCamera(surfaceHolder: SurfaceHolder?) {
        if (surfaceHolder == null) {
            throw IllegalStateException("No SurfaceHolder provided")
        }
        if (cameraManager.isOpen) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?")
            return
        }
        try {
            cameraManager.openDriver(surfaceHolder)
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = CaptureActivityHandler(this, cameraManager, DecodeThread.ALL_MODE)
            }

            initCrop()
        } catch (ioe: IOException) {
            Log.w(TAG, ioe)
            displayFrameworkBugMessageAndExit()
        } catch (e: RuntimeException) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e)
            displayFrameworkBugMessageAndExit()
        }

    }

    private fun displayFrameworkBugMessageAndExit() {
        // camera error
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.zxing_bar_name))
        builder.setMessage("Camera error")
        builder.setPositiveButton("OK") { _, _ -> finish() }
        builder.setOnCancelListener { finish() }
        builder.show()
    }

    fun restartPreviewAfterDelay(delayMS: Long) {
        handler?.sendEmptyMessageDelayed(R.id.restart_preview, delayMS)
    }

    /**
     * 初始化截取的矩形区域
     */
    private fun initCrop() {
        val cameraWidth = cameraManager.cameraResolution.y
        val cameraHeight = cameraManager.cameraResolution.x

        /* 获取布局中扫描框的位置信息 */
        val location = IntArray(2)
        capture_crop_view!!.getLocationInWindow(location)

        val cropLeft = location[0]
        val cropTop = location[1] - statusBarHeight

        val cropWidth = capture_crop_view.width
        val cropHeight = capture_crop_view.height

        /* 获取布局容器的宽高 */
        val containerWidth = capture_container.width
        val containerHeight = capture_container.height

        /* 计算最终截取的矩形的左上角顶点x坐标 */
        val x = cropLeft * cameraWidth / containerWidth
        /* 计算最终截取的矩形的左上角顶点y坐标 */
        val y = cropTop * cameraHeight / containerHeight

        /* 计算最终截取的矩形的宽度 */
        val width = cropWidth * cameraWidth / containerWidth
        /* 计算最终截取的矩形的高度 */
        val height = cropHeight * cameraHeight / containerHeight

        /* 生成最终的截取的矩形 */
        cropRect = Rect(x, y, width + x, height + y)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {
        if (requestCode == 1) {
            AppSettingsDialog.Builder(this).setTitle(R.string.permmision_apply)
                    .setRationale(R.string.pic_denied_dsc).setRequestCode(1000)
                    .setNegativeButton(getString(R.string.cancel)
                    ) { _, _ -> ToastUtil.showShort(this, getString(R.string.pic_permmision_dsc)) }.build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {
        if (requestCode == 1 && EasyPermissions.hasPermissions(this, storagePerm)) {
            list.clear()
            PictureUtils.setImagePicker(this, list = list)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}