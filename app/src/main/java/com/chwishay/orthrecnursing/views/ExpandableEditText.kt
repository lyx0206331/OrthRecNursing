package com.chwishay.orthrecnursing.views

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.InputType
import android.text.InputType.TYPE_CLASS_PHONE
import android.util.AttributeSet
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.*
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IntDef
import androidx.annotation.Nullable
import androidx.core.view.marginEnd
import androidx.core.widget.addTextChangedListener
import com.chwishay.orthrecnursing.R
import com.chwishay.orthrecnursing.orDefault
import com.chwishay.orthrecnursing.toEditable

//                       _ooOoo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                       O\ = /O
//                   ____/`---'\____
//                 .   ' \\| |// `.
//                  / \\||| : |||// \
//                / _||||| -:- |||||- \
//                  | | \\\ - /// | |
//                | \_| ''\---/'' | |
//                 \ .-\__ `-` ___/-. /
//              ______`. .' /--.--\ `. . __
//           ."" '< `.___\_<|>_/___.' >'"".
//          | | : `- \`.;`\ _ /`;.`/ - ` : | |
//            \ \ `-. \_ __\ /__ _/ .-` / /
//    ======`-.____`-.___\_____/___.-`____.-'======
//                       `=---='
//
//    .............................................
//             佛祖保佑             永无BUG
/**
 * Author:RanQing
 * Date:2021/11/26 9:33 上午
 * Description:可设置前缀及后缀，中间部分可输入数据的控件
 */
class ExpandableEditText @JvmOverloads constructor(context: Context, @Nullable attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0): LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        //常规字体
        const val TEXT_STYLE_NORMAL = Typeface.NORMAL
        //粗体
        const val TEXT_STYLE_BOLD = Typeface.BOLD
        //斜体
        const val TEXT_STYLE_ITALIC = Typeface.ITALIC
        //加粗斜体
        const val TEXT_STYLE_BOLD_ITALIC = Typeface.BOLD_ITALIC

        const val INPUT_TYPE_TEXT = InputType.TYPE_CLASS_TEXT
        const val INPUT_TYPE_NUMERIC = InputType.TYPE_CLASS_NUMBER
        const val INPUT_TYPE_PHONENUM = InputType.TYPE_CLASS_PHONE
        const val INPUT_TYPE_DATETIME = InputType.TYPE_CLASS_DATETIME
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TEXT_STYLE_NORMAL, TEXT_STYLE_BOLD, TEXT_STYLE_ITALIC, TEXT_STYLE_BOLD_ITALIC)
    annotation class TextStyle

    @Retention
    @IntDef(INPUT_TYPE_TEXT, INPUT_TYPE_NUMERIC, INPUT_TYPE_PHONENUM, INPUT_TYPE_DATETIME)
    annotation class InputableType

    private val tvPrefix by lazy { TextView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    } }
    private val tvSuffix by lazy { TextView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    } }
    private val etCenter by lazy { EditText(context).apply {
        layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        gravity = Gravity.CENTER
    } }

    @ColorInt
    var preTextColor: Int = Color.BLACK
        set(value) {
            field = value
            tvPrefix.setTextColor(field)
        }
    @Dimension
    var preTextSize: Float = 20f
        set(value) {
            field = value
            tvPrefix.textSize = field
        }
    @TextStyle
    var preTextStyle: Int = TEXT_STYLE_NORMAL
        set(value) {
            field = value
            tvPrefix.typeface = Typeface.defaultFromStyle(field)
        }
    @ColorInt
    var sufTextColor: Int = Color.BLACK
        set(value) {
            field = value
            tvSuffix.setTextColor(field)
        }
    @Dimension
    var sufTextSize: Float = 20f
        set(value) {
            field = value
            tvSuffix.textSize = field
        }
    @TextStyle
    var sufTextStyle: Int = TEXT_STYLE_NORMAL
        set(value) {
            field = value
            tvSuffix.typeface = Typeface.defaultFromStyle(field)
        }
    @ColorInt
    var centerTextColor: Int = Color.BLACK
        set(value) {
            field = value
            etCenter.setTextColor(field)
        }
    @Dimension
    var centerTextSize: Float = 20f
        set(value) {
            field = value
            etCenter.textSize = field
        }
    @TextStyle
    var centerTextStyle: Int = TEXT_STYLE_NORMAL
        set(value) {
            field = value
            etCenter.typeface = Typeface.defaultFromStyle(field)
        }

    var isCenterEditable = false
        set(value) {
            field = value
            etCenter.isEnabled = field
        }

    @Dimension
    var preToCenterSpacing = 5
        set(value) {
            field = value
            (tvPrefix.layoutParams as LayoutParams).marginEnd = field
        }

    @Dimension
    var sufToCenterSpacing = 5
        set(value) {
            field = value
            (tvSuffix.layoutParams as LayoutParams).marginStart = field
        }

    var preText: String = ""
        set(value) {
            field = value
            tvPrefix.text = field
        }

    var sufText: String = ""
        set(value) {
            field = value
            tvSuffix.text = field
        }

    var centerText: String = ""
        set(value) {
            field = value
            etCenter.text = field.toEditable()
        }

    @InputableType
    var centerInputType = INPUT_TYPE_TEXT
        set(value) {
            field = value
            etCenter.inputType = field
        }

    var centerHint: String = ""
        set(value) {
            field = value
            etCenter.hint = field
        }

    @ColorInt
    var centerHintColor: Int = Color.LTGRAY
        set(value) {
            field = value
            etCenter.setHintTextColor(field)
        }

    var onCenterTextChangedListener: ((CharSequence?, Int, Int, Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        addView(tvPrefix)
        addView(etCenter)
        addView(tvSuffix)

        context.obtainStyledAttributes(attrs, R.styleable.ExpandableEditText)?.also {
            preTextColor = it.getColor(R.styleable.ExpandableEditText_eet_pre_textColor, preTextColor)
            preTextSize = it.getDimension(R.styleable.ExpandableEditText_eet_pre_textSize, preTextSize)
            preTextStyle = it.getInt(R.styleable.ExpandableEditText_eet_pre_textStyle, preTextStyle)
            sufTextColor = it.getColor(R.styleable.ExpandableEditText_eet_suf_textColor, sufTextColor)
            sufTextSize = it.getDimension(R.styleable.ExpandableEditText_eet_suf_textSize, sufTextSize)
            sufTextStyle = it.getInt(R.styleable.ExpandableEditText_eet_suf_textStyle, sufTextStyle)
            centerTextColor = it.getColor(R.styleable.ExpandableEditText_eet_center_textColor, centerTextColor)
            centerTextSize = it.getDimension(R.styleable.ExpandableEditText_eet_center_textSize, centerTextSize)
            centerTextStyle = it.getInt(R.styleable.ExpandableEditText_eet_center_textStyle, centerTextStyle)
            isCenterEditable = it.getBoolean(R.styleable.ExpandableEditText_eet_isCenterEditable, isCenterEditable)
            preToCenterSpacing = it.getDimensionPixelSize(R.styleable.ExpandableEditText_eet_pre_to_center_spacing, preToCenterSpacing)
            sufToCenterSpacing = it.getDimensionPixelSize(R.styleable.ExpandableEditText_eet_suf_to_center_spacing, sufToCenterSpacing)
            preText = it.getString(R.styleable.ExpandableEditText_eet_pre_text) ?: preText
            sufText = it.getString(R.styleable.ExpandableEditText_eet_suf_text) ?: sufText
            centerText = it.getString(R.styleable.ExpandableEditText_eet_center_text) ?: centerText
            centerInputType = it.getInt(R.styleable.ExpandableEditText_eet_center_inputType, centerInputType)
            centerHint = it.getString(R.styleable.ExpandableEditText_eet_center_hint) ?: centerHint
            centerHintColor = it.getColor(R.styleable.ExpandableEditText_eet_center_hint_color, centerHintColor)
        }.recycle()

        etCenter.addTextChangedListener ({ text, start, count, after ->

        },{ text, start, before, count ->
            onCenterTextChangedListener?.invoke(text, start, before, count)
        },{ text ->

        })
    }
}