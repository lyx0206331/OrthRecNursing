package com.chwishay.orthrecnursing

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import com.chwishay.orthrecnursing.views.ExpandableEditText
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.find
import kotlin.math.round
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chartJointAngle.init(getString(R.string.joint_angle_curve))
        chartJointAngleVelocity.init(getString(R.string.joint_angle_velocity_curve))
        chartLateralMuscle.init(getString(R.string.lateral_muscle_caurve))
        chartBiceps.init(getString(R.string.biceps_curve))
        chartSemitendinosus.init(getString(R.string.semitendinosus_curve))
        chartMedialMuscle.init(getString(R.string.medial_muscle_curve))
        chartTibialisAnteriorMuscle.init(getString(R.string.tibialis_anterior_muscle_curve))
        chartPeroneusLongus.init(getString(R.string.peroneus_longus_curve))

        tvLog.also {
            it.isScrollable(true)
            observableLog().subscribe { spannableString ->
                if (it.lineCount > 200) {
                    it.text = null
                }
                it.append(spannableString)
                it.append("\n")
                it.scroll2LastLine()
            }.addTo(defaultCompositeDisposable)
        }

        DispatchUtil.timeCounterLiveData.observe(this) {
            tvCurrentTrainingDuration.text = it
        }

        DispatchUtil.onResultObservable().observeOn(AndroidSchedulers.mainThread()).subscribe {
            if (DispatchUtil.isTimerStart) {
                tvEverydayTrainingDuration.text = "${it.everydayTrainingDuration}m"
                tvCurrentTrainingNum.text = "${it.currentTrainingNum}次"
                tvEverydayTrainingGroups.text = "${it.eachGroupTrainingNum}组"
                tvCurrentTrainingGroups.text =
                    "${round(1f * it.currentTrainingNum / it.eachGroupTrainingNum)}组"
                tvGroupTrainingNum.text = "${it.eachGroupTrainingNum}次"
                tvTargetJointAngle.text = "${it.targetJointAngle}°"
                tvJointAngle.text = "${it.jointAngle}°"
                tvJointAngleVelocity.text = "${it.jointAngleVelocity}°/s"
                tvLateralFemoralMuscleContractionStrength.text = "${it.lateral_femoral_muscle}"
                tvMedialFemoralMuscleContractionStrength.text = "${it.medial_femoris}"
                tvBicepsFemoralContractionStrength.text = "${it.biceps_femoris}"
                tvSemitendinosusFemoralContractionStrength.text = "${it.semitendinosus_femoris}"
                tvAnteriorTibialTendonContractionStrength.text = "${it.tibialis_anterior_muscle}"
                tvPeronealMuscleContractionStrength.text = "${it.peroneus_longus}"

                chartJointAngle.addEntry(LineEntry("角度", it.jointAngle.toFloat()))
                chartJointAngleVelocity.addEntry(LineEntry("角速度", it.jointAngleVelocity.toFloat()))
                chartLateralMuscle.addEntry(LineEntry("外侧肌", it.lateral_femoral_muscle.toFloat()))
                chartBiceps.addEntry(LineEntry("二头肌", it.biceps_femoris.toFloat()))
                chartSemitendinosus.addEntry(LineEntry("半腱肌", it.semitendinosus_femoris.toFloat()))
                chartMedialMuscle.addEntry(LineEntry("内侧肌", it.medial_femoris.toFloat()))
                chartTibialisAnteriorMuscle.addEntry(
                    LineEntry(
                        "胫前肌",
                        it.tibialis_anterior_muscle.toFloat()
                    )
                )
                chartPeroneusLongus.addEntry(LineEntry("腓长肌", it.peroneus_longus.toFloat()))

//                "BYTES_VALUE".logE("${it}")
            }
        }

        BluetoothServer.onBtConnStateChange().observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it != BluetoothServer.STATE_BT_CONNECT_SUCCESS) {
                    DispatchUtil.isTimerStart = false
                }
            }

        find<ExpandableEditText>(R.id.eet).onCenterTextChangedListener =
            { text, start, before, count ->
                "EET".logE("text:$text, start:$start, before:$before, count:$count")
            }
    }

    private fun LineChart.init(desc: String) {
        setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
            }

            override fun onNothingSelected() {
            }
        })
        description.isEnabled = true
        setTouchEnabled(true)
        isDragEnabled = true
        setScaleEnabled(true)
        setDrawGridBackground(false)
        setPinchZoom(true)
        setBackgroundColor(Color.LTGRAY)
        data = LineData().apply { setValueTextColor(Color.WHITE) }
        legend.apply {
            form = Legend.LegendForm.LINE
            textColor = Color.WHITE
        }
        xAxis.apply {
            textColor = Color.WHITE
            setDrawGridLines(false)
            setAvoidFirstLastClipping(false)
            isEnabled = true
        }
//            it.axisLeft.apply {
//                textColor = Color.WHITE
//                axisMaximum = 40f
//                axisMinimum = -40f
//                setDrawGridLines(true)
//            }
        //根据数据自动缩放展示最大最小值,不能设置axisLeft,否则自动缩放无效
        isAutoScaleMinMaxEnabled = true
        axisRight.isEnabled = false
        description.text = desc
    }

    private fun LineChart.addEntry(vararg entries: LineEntry) = this.data?.let { d ->
        fun getDateSet(index: Int, @ColorInt color: Int, name: String) =
            d.getDataSetByIndex(index) ?: LineDataSet(null, name).also { lds ->
                lds.axisDependency = YAxis.AxisDependency.LEFT
                lds.color = color
                lds.setCircleColor(Color.WHITE)
                lds.lineWidth = 1f
                lds.circleRadius = 2f
                lds.fillAlpha = 65
                lds.fillColor = color
                lds.highLightColor = Color.rgb(244, 177, 177)
                lds.valueTextColor = Color.WHITE
                lds.valueTextSize = 9f
                lds.setDrawCircles(false)
                d.addDataSet(lds)
            }
        entries.forEachIndexed { index, lineEntry ->
            d.addEntry(
                Entry(
                    getDateSet(
                        index,
                        lineEntry.color,
                        lineEntry.name
                    ).entryCount.toFloat(), lineEntry.value
                ), index
            )
        }
        d.notifyDataChanged()

        this.notifyDataSetChanged()

        this.setVisibleXRangeMaximum(1000f)

        this.moveViewToX(d.entryCount.toFloat())
    }

    fun LineChart.addEntry2(dataInfo: DataInfo) = this.data?.let { d ->
        fun getDateSet(index: Int, @ColorInt color: Int, name: String) =
            d.getDataSetByIndex(index) ?: LineDataSet(null, name).also { lds ->
                lds.axisDependency = YAxis.AxisDependency.LEFT
                lds.color = color
                lds.setCircleColor(Color.WHITE)
                lds.lineWidth = 1f
                lds.circleRadius = 2f
                lds.fillAlpha = 65
                lds.fillColor = color
                lds.highLightColor = Color.rgb(244, 177, 177)
                lds.valueTextColor = Color.WHITE
                lds.valueTextSize = 9f
                lds.setDrawCircles(false)
                d.addDataSet(lds)
            }
//        d.addEntry(Entry(getDateSet(0, ColorTemplate.getHoloBlue(), "角度").entryCount.toFloat(), dataInfo.jointAngle.toFloat()), 0)
//        d.addEntry(Entry(getDateSet(1, getColor1(R.color.green01FD01), "角速度").entryCount.toFloat(), dataInfo.jointAngleVelocity.toFloat()), 1)
//        d.addEntry(Entry(getDateSet(2, getColor1(R.color.yellowFFFF00), "股外肌收缩强度").entryCount.toFloat(), dataInfo.lateralFemoralMuscleContractionStrength.toFloat()), 2)
//        d.addEntry(Entry(getDateSet(3, getColor1(R.color.purple7E2E8D), "股内肌收缩强度").entryCount.toFloat(), dataInfo.medialFemoralMuscleContractionStrength.toFloat()), 3)
//        d.addEntry(Entry(getDateSet(4, getColor1(R.color.colorPrimary), "二头肌收缩强度").entryCount.toFloat(), dataInfo.bicepsFemoralContractionStrength.toFloat()), 4)
//        d.addEntry(Entry(getDateSet(5, getColor1(R.color.redFE0000), "半腱肌收缩强度").entryCount.toFloat(), dataInfo.semitendinosusFemoralContractionStrength.toFloat()), 5)
//        d.addEntry(Entry(getDateSet(6, getColor1(R.color.brownD95218), "胫前肌收缩强度").entryCount.toFloat(), dataInfo.anteriorTibialTendonContractionStrength.toFloat()), 6)
//        d.addEntry(Entry(getDateSet(7, getColor1(R.color.blue0000FE), "腓长肌收缩强度").entryCount.toFloat(), dataInfo.peronealMuscleContractionStrength.toFloat()), 7)
        d.notifyDataChanged()

        this.notifyDataSetChanged()

        this.setVisibleXRangeMaximum(1000f)

        this.moveViewToX(d.entryCount.toFloat())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_scan -> {
                checkConnectState {
                    "CheckConn".logE("蓝牙连接检测。。。")
                }
                true
            }
            R.id.action_start -> {
                if (BluetoothServer.btConnState == BluetoothServer.STATE_BT_CONNECT_SUCCESS) {
                    DispatchUtil.isTimerStart = !DispatchUtil.isTimerStart
                } else {
                    showShortToast("请先搜索连接设备...")
                }
                true
            }
            R.id.action_report -> {
//                showShortToast("功能正在开发中，敬请期待...")
                DispatchUtil.isTimerStart = false
                ReportDialog(this, this).show()
                true
            }
            R.id.action_setting -> {
                if (BluetoothServer.btConnState == BluetoothServer.STATE_BT_CONNECT_SUCCESS) {
                    SettingDialog(this, this).show()
                } else {
                    showShortToast("请先搜索连接设备...")
                }
                true
            }
            R.id.action_log -> {
                tvLog.also {
                    it.isVisible = !it.isVisible
                    if (it.isVisible) {
                        ifCreateLog = true
                    } else {
                        it.text = null
                        ifCreateLog = false
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
//        BluetoothServer.disconnect()
//        BluetoothServer.unregisterBtReceiver()
        DispatchUtil.isTimerStart = false
        super.onDestroy()
        exitProcess(0)
    }
}

class LineEntry(val name: String, val value: Float, @ColorInt val color: Int = Color.BLACK)