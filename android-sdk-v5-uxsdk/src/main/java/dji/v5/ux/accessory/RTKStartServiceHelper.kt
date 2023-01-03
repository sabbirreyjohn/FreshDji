package dji.v5.ux.accessory

import android.os.Handler
import android.text.TextUtils
import dji.rtk.CoordinateSystem
import dji.sdk.keyvalue.key.*
import dji.sdk.keyvalue.value.product.ProductType
import dji.sdk.keyvalue.value.remotecontroller.RCMode
import dji.sdk.keyvalue.value.rtkbasestation.RTKCustomNetworkSetting
import dji.sdk.keyvalue.value.rtkbasestation.RTKReferenceStationSource
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.common.utils.RxUtil
import dji.v5.et.create
import dji.v5.et.get
import dji.v5.manager.aircraft.rtk.RTKCenter
import dji.v5.manager.aircraft.rtk.RTKSystemStateListener
import dji.v5.utils.common.*
import dji.v5.ux.R
import dji.v5.ux.core.util.DataProcessor
import io.reactivex.rxjava3.core.Flowable
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Description :用于实现自动重连RTK逻辑
 *
 * @author: Byte.Cai
 *  date : 2022/8/16
 *
 * Copyright (c) 2022, DJI All Rights Reserved.
 */
object RTKStartServiceHelper {
    private const val TAG = "RTKStartServiceHelper"

    private val rtkCenter = RTKCenter.getInstance()
    private val qxRTKManager = RTKCenter.getInstance().qxrtkManager
    private val customManager = RTKCenter.getInstance().customRTKManager

    private var productType: ProductType = ProductType.UNKNOWN
    private var rtkDongleConnection = false
    private var fcConnected = false
    private val rtkModuleAvailableProcessor = DataProcessor.create(false)
    private var rtkSource: RTKReferenceStationSource = RTKReferenceStationSource.UNKNOWN
    private val isStartRTKing = AtomicBoolean(false)
    private val isHasStartRTK = AtomicBoolean(false)
    private val handle = Handler()


    private val rtkSystemStateListener = RTKSystemStateListener {
        if (rtkSource != it.rtkReferenceStationSource) {
            rtkSource = it.rtkReferenceStationSource
            LogUtils.i(TAG, "rtkSource change into:$rtkSource")
            startRtkService()
        }
    }

    init {
        //观测RTK模块的连接情况
        observerRTKNoduleAvailable()
        //观测RTKSource的变化
        rtkCenter.addRTKSystemStateListener(rtkSystemStateListener)
    }

    private fun observerRTKNoduleAvailable() {
        RxUtil.addListener(KeyTools.createKey(ProductKey.KeyProductType), this).subscribe {
            if (it != ProductType.UNRECOGNIZED && productType != it) {
                LogUtils.i(TAG, "productType=$it")
                productType = it
                updateData()
            }

        }
        RxUtil.addListener(KeyTools.createKey(RtkMobileStationKey.KeyIsRTKDongleConnect), this).subscribe {
            if (rtkDongleConnection != it) {
                LogUtils.i(TAG, "rtkDongleConnection=$it")
                rtkDongleConnection = it
                updateData()
            }

        }

        RxUtil.addListener(KeyTools.createKey(FlightControllerKey.KeyConnection), this).subscribe {
            if (fcConnected != it) {
                LogUtils.i(TAG, "fcConnected=$it")
                fcConnected = it
                updateData()
            }

        }
    }

    @Synchronized
    private fun updateData() {
        val isRtkModuleAvailable = when (productType) {
            ProductType.DJI_MAVIC_3_ENTERPRISE_SERIES ->
                // 外接RTK的判断RTK Dongle连接状态
                rtkDongleConnection && fcConnected
            else ->
                // 其他行业飞机内置RTK的飞机都是true
                fcConnected
        }
        rtkModuleAvailableProcessor.onNext(isRtkModuleAvailable)

        if (isRtkModuleAvailable && !isHasStartRTK.get()) {
            startRtkService()
        }
    }

    @Synchronized
    fun startRtkService() {
        LogUtils.i(TAG, "startRtkService")
        if (!rtkModuleAvailableProcessor.value) {
            LogUtils.e(TAG, "rtkModule is unAvailable,startRtkServiceIfNeed fail!")
            return
        }
        if (!isNeedStartRtkNetworkService()) {
            LogUtils.e(TAG, "don not need start rtk Service!")
            return
        }
        LogUtils.i(TAG, "rtkSource=$rtkSource")
        when (rtkSource) {
            RTKReferenceStationSource.CUSTOM_NETWORK_SERVICE -> startRtkCustomNetworkService()
            RTKReferenceStationSource.QX_NETWORK_SERVICE -> startQxRtkService()
            RTKReferenceStationSource.NTRIP_NETWORK_SERVICE -> startCMCCRtkService()
            RTKReferenceStationSource.BASE_STATION -> {
                LogUtils.i(TAG, "D-RTK2 固件底层已实现自动重连，不需要外部手动重连")
            }
            else -> {
                LogUtils.e(TAG, "UnKnown rtkSource:$rtkSource")
            }
        }
    }


    @Synchronized
    private fun startCMCCRtkService() {
        LogUtils.i(TAG, "startCMCCRtkService")
        setStartRTKState(true)
        isHasStartRTK.set(false)
        val rtkNetworkCoordinateSystem = RTKUtil.getNetRTKCoordinateSystem(RTKReferenceStationSource.NTRIP_NETWORK_SERVICE)
        rtkNetworkCoordinateSystem?.let {
            LogUtils.i(TAG, "startCMCCRtkService,coordinateName=$rtkNetworkCoordinateSystem")
            qxRTKManager.stopNetworkRTKService(object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    customManager.startNetworkRTKService(rtkNetworkCoordinateSystem, object : CommonCallbacks.CompletionCallback {
                        override fun onSuccess() {
                            LogUtils.i(TAG, "startCMCCRtkService success")
                            setStartRTKState(false)
                            isHasStartRTK.set(true)
                        }

                        override fun onFailure(error: IDJIError) {
                            LogUtils.e(TAG, "startCMCCRtkService fail:rtkNetworkCoordinateSystem=$rtkNetworkCoordinateSystem,error=$error")
                            setStartRTKState(false)
                            isHasStartRTK.set(false)
                            ToastUtils.showToast(StringUtils.getResStr(R.string.uxsdk_rtk_setting_menu_setting_fail))

                        }

                    })
                }

                override fun onFailure(error: IDJIError) {
                    LogUtils.e(TAG, "stopNetworkRTKService fail:$error")
                    isHasStartRTK.set(false)
                    setStartRTKState(false)


                }

            })

        }
    }

    /**
     * 启动千寻RTK
     */
    @Synchronized
    private fun startQxRtkService() {
        var rtkNetworkCoordinateSystem = RTKUtil.getNetRTKCoordinateSystem(RTKReferenceStationSource.QX_NETWORK_SERVICE)
        if (rtkNetworkCoordinateSystem != null) {
            startQxRtkService(rtkNetworkCoordinateSystem)
        } else {
            RTKCenter.getInstance().qxrtkManager.getNetworkRTKCoordinateSystem(object :
                CommonCallbacks.CompletionCallbackWithParam<CoordinateSystem> {
                override fun onSuccess(t: CoordinateSystem?) {
                    t?.let { startQxRtkService(it) }
                }

                override fun onFailure(error: IDJIError) {
                    //未实现
                }
            })
        }
    }

    private fun startQxRtkService(coordinateSystem: CoordinateSystem) {
        LogUtils.i(TAG, "startQxRtkService,rtkNetworkCoordinateSystem=$coordinateSystem")
        setStartRTKState(true)
        isHasStartRTK.set(false)

        qxRTKManager.stopNetworkRTKService(object : CommonCallbacks.CompletionCallback {
            override fun onSuccess() {
                qxRTKManager.startNetworkRTKService(coordinateSystem, object : CommonCallbacks.CompletionCallback {
                    override fun onSuccess() {
                        LogUtils.i(TAG, "startQxRtkService success")
                        setStartRTKState(false)
                        isHasStartRTK.set(true)

                    }

                    override fun onFailure(error: IDJIError) {
                        LogUtils.e(TAG, "startQxRtkService fail:$error")
                        ToastUtils.showToast(StringUtils.getResStr(R.string.uxsdk_rtk_setting_menu_setting_fail))
                        setStartRTKState(false)
                        isHasStartRTK.set(false)


                    }
                })
            }

            override fun onFailure(error: IDJIError) {
                LogUtils.e(TAG, "stopNetworkRTKService fail:$error")
                isHasStartRTK.set(false)
                setStartRTKState(false)


            }

        })
    }


    /**
     * 从本地缓存中获取自定义网络RTK配置信息启动自定义网络RTK
     */
    @Synchronized
    private fun startRtkCustomNetworkService() {
        isHasStartRTK.set(false)
        setStartRTKState(true)
        LogUtils.i(TAG, "startRtkCustomNetworkService")
        val rtkCustomNetworkSetting: RTKCustomNetworkSetting? = RTKUtil.getRtkCustomNetworkSetting()
        rtkCustomNetworkSetting?.let {
            LogUtils.i(TAG, "rtkCustomNetworkSetting=$it")
            customManager.stopNetworkRTKService(object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    customManager.customNetworkRTKSettings = it
                    customManager.startNetworkRTKService(object : CommonCallbacks.CompletionCallback {
                        override fun onSuccess() {
                            LogUtils.i(TAG, "startRtkCustomNetworkService success")
                            setStartRTKState(false)
                            isHasStartRTK.set(true)

                        }

                        override fun onFailure(error: IDJIError) {
                            LogUtils.e(TAG, "startRtkCustomNetworkService fail:$error")
                            setStartRTKState(false)
                            isHasStartRTK.set(false)

                            val errorTip = if (!TextUtils.isEmpty(error.description())) {
                                error.description()
                            } else {
                                StringUtils.getResStr(R.string.uxsdk_rtk_setting_menu_setting_fail)
                            }
                            ToastUtils.showToast(errorTip)
                        }

                    })
                }

                override fun onFailure(error: IDJIError) {
                    LogUtils.e(TAG, "stopNetworkRTKService fail:$error")
                    setStartRTKState(false)
                    isHasStartRTK.set(false)


                }

            })
        }


    }


    /**
     * 是否允许启动网络RTK （未判断网络数据模式）
     */
    private fun isNeedStartRtkNetworkService(): Boolean {
        val isConnected: Boolean = FlightControllerKey.KeyConnection.create().get(false)
        return (isConnected
                && isNetworkRTK(rtkSource)
                && NetworkUtils.isNetworkAvailable()
                && !isChannelB()
                && rtkModuleAvailableProcessor.value)
                && !isStartRTKing.get()
    }


    //后续供其他RTK相关Widget使用
    val rtkModuleAvailable: Flowable<Boolean>
        get() = rtkModuleAvailableProcessor.toFlowable()

    /**
     * 判断一个差分数据源是否是网络RTK
     */
    fun isNetworkRTK(source: RTKReferenceStationSource?): Boolean {
        return when (source) {
            RTKReferenceStationSource.QX_NETWORK_SERVICE,
            RTKReferenceStationSource.CUSTOM_NETWORK_SERVICE,
            RTKReferenceStationSource.NTRIP_NETWORK_SERVICE,
            -> true
            else -> false
        }
    }

    /**
     * 是否为B控，非双控机型返回false
     */
    fun isChannelB(): Boolean {
        return RCMode.CHANNEL_B == RemoteControllerKey.KeyRcMachineMode.create().get(RCMode.UNKNOWN)
    }

    private fun setStartRTKState(isRTKStart: Boolean) {
        if (isRTKStart) {
            isStartRTKing.set(true)
            handle.postDelayed({
                LogUtils.e(TAG, "start rtk service timeout")
                isStartRTKing.set(false)
            }, 15 * 1000)
        } else {
            isStartRTKing.set(false)
            handle.removeCallbacksAndMessages(null)
        }
    }


}