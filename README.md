## 带前后缀可编辑文本输入框  [![](https://jitpack.io/v/lyx0206331/ExpandableEditText.svg)](https://jitpack.io/#lyx0206331/ExpandableEditText)

所有文本均可修改大小颜色及样式，中间可输入文本可选择输入类型。

[![osCrjK.png](https://s4.ax1x.com/2021/12/06/osCrjK.png)](https://imgtu.com/i/osCrjK)

### 使用方法:

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.lyx0206331:ExpandableEditText:Tag'
	}

### 属性说明:

| 名称  | 类型  | 说明  |
|:----------|:----------|:----------|
| eet_pre_textSize    | dimension/reference    | 前缀字体大小    |
| eet_pre_textColor    | color/reference    | 前缀字体颜色    |
| eet_pre_textStyle    | enum(normal/bold/italic/bold_italic)    |  前缀字体样式   |
| eet_suf_textSize    | dimension/reference    | 后缀字体大小    |
| eet_suf_textColor    | color/reference   | 后缀字体颜色    |
| eet_suf_textStyle    | enum(normal/bold/italic/bold_italic)    | 后缀字体样式    |
| eet_center_textSize    | dimension/reference    | 中间字体大小    |
| eet_center_textColor    | color/reference    | 中间字体颜色    |
| eet_center_textStyle    | enum(normal/bold/italic/bold_italic)    | 中间字体样式    |
| eet_isCenterEditable    | boolean    | 中间字体是否可编辑    |
| eet_pre_to_center_spacing    | dimension/reference    | 前缀到中间间隔距离    |
| eet_suf_to_center_spacing    | dimension/reference    | 后缀到中间间隔距离    |
| eet_pre_text    | string/reference    | 前缀文本    |
| eet_suf_text    | string/reference    | 后缀文本    |
| eet_center_text    | string/reference    | 中间文本    |
| eet_center_inputType    | enum(text/number/phone/date_time)    | 中间输入类型    |
| eet_center_hint    | string/reference    | 中间示意文本    |
| eet_center_hint_color    | color/reference    | 中间示意文本颜色    |

### 用例如下：

	<com.adrian.expandableedittext.ExpandableEditText
        android:id="@+id/eet"
        android:layout_width="350dp"
        android:layout_height="wrap_content"
        android:background="@color/white"
        android:padding="10dp"
        app:eet_center_hint="提示数据"
        app:eet_center_textSize="12sp"
        app:eet_pre_text="前缀"
        app:eet_pre_textSize="10sp"
        app:eet_suf_text="后缀"
        app:eet_suf_textStyle="normal"
        app:eet_suf_textSize="15sp"
        app:eet_suf_textColor="@color/teal_200"
        app:eet_center_textStyle="bold"
        app:eet_center_textColor="@color/colorPrimary"
        app:eet_pre_to_center_spacing="10dp"
        app:eet_suf_to_center_spacing="10dp"
        app:eet_isCenterEditable="true"
        app:eet_center_inputType="number"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

#### 可设置输入内容监听

	findViewById<ExpandableEditText>(R.id.eet).onCenterTextChangedListener = { text, start, before, count ->
		...
        }