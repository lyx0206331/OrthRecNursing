package com.chwishay.orthrecnursing

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
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
import com.github.mikephil.charting.utils.ColorTemplate
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.*
import org.jetbrains.anko.find
import org.w3c.dom.Text
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {

    private lateinit var tvTargetTrainingDuration: TextView
    private lateinit var tvTrainingDuration: TextView
    private lateinit var tvTargetTrainingNum: TextView
    private lateinit var tvTargetJointAngle: TextView
    private lateinit var tvCurrentTrainingNum: TextView
    private lateinit var tvCompleteTrainingNum: TextView
    private lateinit var tvJointAngle: TextView
    private lateinit var tvJointAngleVelocity: TextView
    private lateinit var tvLateralFemoralMuscleContractionStrength: TextView
    private lateinit var tvMedialFemoralMuscleContractionStrength: TextView
    private lateinit var tvBicepsFemoralContractionStrength: TextView
    private lateinit var tvSemitendinosusFemoralContractionStrength: TextView
    private lateinit var tvAnteriorTibialTendonContractionStrength: TextView
    private lateinit var tvPeronealMuscleContractionStrength: TextView
    private lateinit var lineChart: LineChart

    private lateinit var tvLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLog = findViewById(R.id.tvLog)
        tvTrainingDuration = findViewById(R.id.tvTrainingDuration)
        tvTargetTrainingDuration = findViewById<TextView>(R.id.tvTargetTrainingDuration)
        tvTargetTrainingNum = findViewById<TextView>(R.id.tvTargetTrainingNum)
        tvTargetJointAngle = findViewById<TextView>(R.id.tvTargetJointAngle)
        tvCurrentTrainingNum = findViewById<TextView>(R.id.tvCurrentTrainingNum)
        tvCompleteTrainingNum = findViewById<TextView>(R.id.tvCompleteTrainingNum)
        tvJointAngle = findViewById<TextView>(R.id.tvJointAngle)
        tvJointAngleVelocity = findViewById<TextView>(R.id.tvJointAngleVelocity)
        tvLateralFemoralMuscleContractionStrength = findViewById<TextView>(R.id.tvLateralFemoralMuscleContractionStrength)
        tvMedialFemoralMuscleContractionStrength = findViewById<TextView>(R.id.tvMedialFemoralMuscleContractionStrength)
        tvBicepsFemoralContractionStrength = findViewById<TextView>(R.id.tvBicepsFemoralContractionStrength)
        tvSemitendinosusFemoralContractionStrength = findViewById<TextView>(R.id.tvSemitendinosusFemoralContractionStrength)
        tvAnteriorTibialTendonContractionStrength = findViewById<TextView>(R.id.tvAnteriorTibialTendonContractionStrength)
        tvPeronealMuscleContractionStrength = findViewById<TextView>(R.id.tvPeronealMuscleContractionStrength)
        lineChart = find<LineChart>(R.id.chart).also {
            it.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                }

                override fun onNothingSelected() {
                }
            })
            it.description.isEnabled = true
            it.setTouchEnabled(true)
            it.isDragEnabled = true
            it.setScaleEnabled(true)
            it.setDrawGridBackground(false)
            it.setPinchZoom(true)
            it.setBackgroundColor(Color.LTGRAY)
            it.data = LineData().apply { setValueTextColor(Color.WHITE) }
            it.legend.apply {
                form = Legend.LegendForm.LINE
                textColor = Color.WHITE
            }
            it.xAxis.apply {
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
            it.isAutoScaleMinMaxEnabled = true
            it.axisRight.isEnabled = false
            it.description.isEnabled = false
        }

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
            tvTrainingDuration.text = it
        }

        DispatchUtil.onResultObservable().observeOn(AndroidSchedulers.mainThread()).subscribe() {
            if (DispatchUtil.isTimerStart) {
//                "BYTES_VALUE".logE("${it}")
//                lineChart.addEntry(it)
//                tvTargetTrainingDuration.text =
//                    "${it.targetTrainingDuration}min"
//                tvTargetTrainingNum.text = "${it.targetTrainingNum}次"
//                tvTargetJointAngle.text = "${it.targetJointAngle}°"
//                tvCurrentTrainingNum.text = "${it.currentTrainingNum}次"
//                tvCompleteTrainingNum.text =
//                    "${it.completeTrainingNum}次"
//                tvJointAngle.text = "${it.jointAngle}°"
//                tvJointAngleVelocity.text =
//                    "${it.jointAngleVelocity}°/s"
//                tvLateralFemoralMuscleContractionStrength.text =
//                    "${it.lateralFemoralMuscleContractionStrength}"
//                tvMedialFemoralMuscleContractionStrength.text =
//                    "${it.medialFemoralMuscleContractionStrength}"
//                tvBicepsFemoralContractionStrength.text =
//                    "${it.bicepsFemoralContractionStrength}"
//                tvSemitendinosusFemoralContractionStrength.text =
//                    "${it.semitendinosusFemoralContractionStrength}"
//                tvAnteriorTibialTendonContractionStrength.text =
//                    "${it.anteriorTibialTendonContractionStrength}"
//                tvPeronealMuscleContractionStrength.text =
//                    "${it.peronealMuscleContractionStrength}"
            }
        }

        BluetoothServer.onBtConnStateChange().observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it != BluetoothServer.STATE_BT_CONNECT_SUCCESS) {
                    DispatchUtil.isTimerStart = false
                }
            }

        find<ExpandableEditText>(R.id.eet).onCenterTextChangedListener = { text, start, before, count ->
            "EET".logE("text:$text, start:$start, before:$before, count:$count")
        }
    }

    fun LineChart.addEntry(dataInfo: DataInfo) = this.data?.let { d ->
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