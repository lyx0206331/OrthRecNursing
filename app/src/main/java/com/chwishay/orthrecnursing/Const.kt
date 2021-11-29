package com.chwishay.orthrecnursing

import android.os.Environment
import androidx.annotation.IntDef
import java.io.File

@Suppress("SimplifyBooleanWithConstants")
val dev = BuildConfig.DEBUG && false

const val isShowDevList = false

const val STATE_IDLE = -1

//屏蔽设备自动搜索连接功能,正常流程及发布时需修改为false
var shieldConnStateCheck = false

const val enableUploadLog = true

const val ENTRY_TYPE_KEY = "entryType"

const val ENTRY_TYPE_ELEC_STIM_DEBUG = 0
const val ENTRY_TYPE_PARAM_SET = 1
const val ENTRY_TYPE_REPORT = 2

@Retention(AnnotationRetention.SOURCE)
@IntDef(ENTRY_TYPE_ELEC_STIM_DEBUG, ENTRY_TYPE_PARAM_SET, ENTRY_TYPE_REPORT)
annotation class EntryType

@EntryType var curEntryType = ENTRY_TYPE_ELEC_STIM_DEBUG

var isElecStimDebugEntry = false
    get() = curEntryType == ENTRY_TYPE_ELEC_STIM_DEBUG

var isParamSetEntry = false
    get() = curEntryType == ENTRY_TYPE_PARAM_SET

var isReportEntry = false
    get() = curEntryType == ENTRY_TYPE_REPORT

const val SCAN_TYPE_MASTER = 0
const val SCAN_TYPE_BIND_IMU = 1
const val SCAN_TYPE_BIND_FES = 2

@Retention(AnnotationRetention.SOURCE)
@IntDef(SCAN_TYPE_MASTER, SCAN_TYPE_BIND_IMU, SCAN_TYPE_BIND_FES)
annotation class ScanType

@ScanType var curScanType = SCAN_TYPE_MASTER
const val SCAN_TYPE_KEY = "scanType"

//主控mac地址KEY
const val MASTER_MAC_KEY = "master_mac_key"
//IMU mac地址key
const val IMU_MAC_KEY = "imu_mac_key"
//FES mac地址key
const val FES_MAC_KEY = "fes_mac_key"

//主控模块
const val MODULE_MASTER = 0
//电刺激模块
const val MODULE_ELEC_STIM = 1
//IMU模块
const val MODULE_IMU = 2

@Retention(AnnotationRetention.SOURCE)
@IntDef(MODULE_MASTER, MODULE_ELEC_STIM, MODULE_IMU)
annotation class ModuleType

//电刺激Mac地址
const val MAC_E_STIM = 1
//IMU Mac地址
const val MAC_IMU = 2
@Retention(AnnotationRetention.SOURCE)
@IntDef(MAC_E_STIM, MAC_IMU)
annotation class MacType

/**
 * Root dir
 */
val rootDir = File("${Environment.getExternalStorageDirectory().absolutePath}/CHWS/D82/").also { it.mkdirs() }

/**
 * Firmware dir
 */
val firmwareDir by lazy { File(rootDir, "firmware/").also { it.mkdirs() }}

/**
 * Export dir
 */
val exportDir by lazy { File(rootDir, "export/").also { it.mkdirs() }}

/**
 * Raw dir
 */
val rawDir by lazy { File(rootDir, "raw/").also { it.mkdirs() }}

/**
 * Report dir
 */
val reportDir by lazy { File(rootDir, "report/").also { it.mkdirs() }}

/**
 * Tmp dir
 */
val tmpDir by lazy { File(rootDir, "tmp/").also { it.mkdirs() }}

/**
 * Log dir
 */
val logDir by lazy { File(rootDir, "log/").also { it.mkdirs() } }

//------------------站位训练--------------------
const val STATION_BALANCE_TRAINING = 0
const val STATION_SQUAT_TRAINING = 1
const val STATION_LUNGE_TRAINING = 2
@Retention(AnnotationRetention.SOURCE)
@IntDef(STATION_BALANCE_TRAINING, STATION_SQUAT_TRAINING, STATION_LUNGE_TRAINING)
annotation class StationTrainingType

const val STATION_MODE_TYPE_KEY = "stationType"

//------------------行走模式---------------------
const val WALK_STRIDE_TRAINING = 0
const val WALK_STAIRS_TRAINING = 1
const val WALK_WALKING_TRAINING = 2
@Retention(AnnotationRetention.SOURCE)
@IntDef(WALK_STRIDE_TRAINING, WALK_STAIRS_TRAINING, WALK_WALKING_TRAINING)
annotation class WalkTrainingType

const val WALK_MODE_TYPE_KEY = "walkType"

//--------------模式类型------------------
const val MODE_INIT = 0
const val MODE_SIT_STAND = 1
const val MODE_BALANCE = 2
const val MODE_SQUAT = 3
const val MODE_LUNGE = 4
const val MODE_STRIDE = 5
const val MODE_STARIRS = 6
const val MODE_WALK = 7
const val MODE_FIXED_RHYTHM = 8
const val MODE_MANUAL = 9
const val MODE_SINGLE_E_STIM = 10
const val MODE_PARAM_MODIFY = 11
@Retention(AnnotationRetention.SOURCE)
@IntDef(MODE_INIT, MODE_SIT_STAND, MODE_BALANCE, MODE_SQUAT, MODE_LUNGE, MODE_STRIDE, MODE_STARIRS, MODE_WALK, MODE_FIXED_RHYTHM, MODE_MANUAL, MODE_SINGLE_E_STIM, MODE_PARAM_MODIFY)
annotation class ModeType

//--------------任务标志---------------
const val TASK_COMMON = 0
const val TASK_CHECK_FES_CONN_STATE = 1
const val TASK_CHECK_IMU_CONN_STATE = 2
const val TASK_GET_POWER_OF_MODULES = 3
const val TASK_GET_MALFUNCTION_INFO = 4
const val TASK_GET_FES_MAC_INFO = 5
const val TASK_GET_IMU_MAC_INFO = 6
@Retention(AnnotationRetention.SOURCE)
@IntDef(TASK_COMMON, TASK_CHECK_FES_CONN_STATE, TASK_CHECK_IMU_CONN_STATE, TASK_GET_POWER_OF_MODULES, TASK_GET_MALFUNCTION_INFO, TASK_GET_FES_MAC_INFO, TASK_GET_IMU_MAC_INFO)
annotation class TaskType