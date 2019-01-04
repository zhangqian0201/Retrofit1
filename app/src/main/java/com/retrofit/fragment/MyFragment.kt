package com.retrofit.fragment

import android.Manifest
import android.content.Intent
import android.support.design.widget.BottomSheetDialog
import android.view.LayoutInflater
import com.ljq.mvpframework.factory.CreatePresenter
import com.retrofit.R
import com.retrofit.activity.TabActivity
import com.retrofit.model.GirlsBean
import com.retrofit.presenter.GirlsPresenter
import com.retrofit.view.BaseView
import kotlinx.android.synthetic.main.fragment_my.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import zxing.activity.CaptureActivity


/**
 * Created by zq on 2018/8/4
 */
@Suppress("DEPRECATION")
@CreatePresenter(GirlsPresenter::class)
class MyFragment : BaseFragment<BaseView<GirlsBean>, GirlsPresenter>(), BaseView<GirlsBean>, EasyPermissions.PermissionCallbacks {
    private val SCAN_CODE = 10010
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private val cameraPerm = Manifest.permission.CAMERA
    private val storagePerm = Manifest.permission.READ_EXTERNAL_STORAGE

    override fun getLayoutId(): Int = R.layout.fragment_my

    override fun initObject() {
        bottomSheetDialog = BottomSheetDialog(context!!)
        val view = LayoutInflater.from(context!!).inflate(R.layout.layout_bottom_dialog, null)
        bottomSheetDialog.setContentView(view)
    }

    override fun initData() {

    }

    override fun initListener() {
        item_collection_me.setOnClickListener { startActivity(Intent(context, TabActivity::class.java)) }
        item_like_me.setOnClickListener { bottomSheetDialog.show() }
        iv_two_code.setOnClickListener {
            if (EasyPermissions.hasPermissions(context!!, storagePerm, cameraPerm)) {
                startActivityForResult(Intent(context, CaptureActivity::class.java), SCAN_CODE)
            } else {
                EasyPermissions.requestPermissions(this@MyFragment, "我们需要获取您的相册权限以正常使用名片拍摄功能。", 3, storagePerm, cameraPerm)
            }
        }
    }

    override fun loadSuccess(model: GirlsBean, isFirstLoad: Boolean) {

    }

    override fun loadFail(msg: String?) {

    }

    companion object {
        val instance: MyFragment by lazy {
            MyFragment()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCAN_CODE) {
            data?.let {
                userName.text = it.getStringExtra("result")
//                Toast.makeText(context, it.getStringExtra("result"), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {
        if (requestCode == 3) {
            AppSettingsDialog.Builder(this).setTitle("权限申请")
                    .setRationale("缺少相应的权限，请到到设置页面，找到权限管理，允许相应的权限！").setRequestCode(1000)
                    .setNegativeButton("取消")
                    { _, _ -> }.build().show()
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {
        if (requestCode == 3) {
            if (EasyPermissions.hasPermissions(context!!, cameraPerm, storagePerm)) {
                startActivityForResult(Intent(context, CaptureActivity::class.java), SCAN_CODE)
            }
        }
    }
}