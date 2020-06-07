package com.retrox.aodmod.proxy.view.custom.word

import android.content.Context
import androidx.lifecycle.Observer
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.retrox.aodmod.MainHook
import com.retrox.aodmod.R
import com.retrox.aodmod.SmaliImports
import com.retrox.aodmod.extensions.ResourceUtils
import com.retrox.aodmod.extensions.setGoogleSans
import com.retrox.aodmod.extensions.setGradientTest
import com.retrox.aodmod.extensions.toCNString
import com.retrox.aodmod.pref.XPref
import com.retrox.aodmod.proxy.AbsDreamView
import com.retrox.aodmod.proxy.DreamProxy
import com.retrox.aodmod.proxy.view.SharedIds
import com.retrox.aodmod.proxy.view.theme.ThemeManager
import com.retrox.aodmod.state.AodClockTick
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.constraintLayout
import java.text.SimpleDateFormat
import java.util.*

class WordDream(dreamProxy: DreamProxy) : AbsDreamView(dreamProxy) {
    override val layoutTheme: String
        get() = "Word"


    override fun onCreateView(): View {
        return context.wordClockView(this)
    }

    override fun onAvoidScreenBurnt(mainView: View, lastTime: Long) {
        val horizontal = 0 // 这个模式避免左右移动
        val vertical = Random().nextInt(400) - 300 // 更大的移动范围 (-300, 100)

        mainView.animate()
            .translationX(horizontal.toFloat())
            .translationY(-vertical.toFloat())
            .setDuration(if (lastTime == 0L) /*加入初始位移 避免烧屏*/ 0L else 800L)
            .start()
    }

}

fun Context.wordClockView(lifecycleOwner: LifecycleOwner): ConstraintLayout {
    return constraintLayout {

        verticalLayout {

            val timeTextView = textView {
                text = ""
                textSize = 30f
                setLineSpacing(8f, 1f)
                textColor = Color.WHITE
                setGoogleSans()
            }.lparams(wrapContent, wrapContent) {
                bottomMargin = dip(16)
            }

            val dateTextView = textView {
                text = ""
                textSize = 16f
                textColor = Color.WHITE
                setGoogleSans()
            }.lparams(wrapContent, wrapContent)

            AodClockTick.tickLiveData.observe(lifecycleOwner, Observer {
                val cal = Calendar.getInstance()
                val hour = cal.get(Calendar.HOUR) % 12
                val minute = cal.get(Calendar.MINUTE) % 60
                val month = cal.get(Calendar.MONTH) + 1
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val weekDay = cal.get(Calendar.DAY_OF_WEEK)

                val hoursArray = ResourceUtils.getInstance(this)
                    .resources.getStringArray(R.array.type_clock_hours)
                val minutesArray = ResourceUtils.getInstance(this)
                    .resources.getStringArray(R.array.type_clock_minutes)

                // 系统语言是中文 并且关闭了强制英文时钟时候才使用中文时钟
                if (Locale.getDefault().language == Locale.CHINESE.language && !XPref.getForceEnglishWordClock()) {
                    timeTextView.text = "${hour.toCNString()}時\n${minute.toCNString()}分\nです"
                    val weekDayStr = if (weekDay == 1) "日" else ((weekDay - 1).toCNString())
                    val text = "${month.toCNString()}月${day.toCNString()}日 周${weekDayStr}" + " "
                    dateTextView.text =
                        "${month.toCNString()}月${day.toCNString()}日 周${weekDayStr}" + " "
                } else {
                    val engStr = ResourceUtils.getInstance(this).resources.getString(R.string.clock_its) + "\n${hoursArray.getOrNull(hour)}\n${minutesArray.getOrNull(minute)}" + " "
                    timeTextView.text = engStr
                    dateTextView.text = SimpleDateFormat(SmaliImports.systemDateFormat, Locale.getDefault()).format(Date())
                }

            })

            textView {
                // 备忘
                textColor = Color.WHITE
                setGoogleSans()
                letterSpacing = 0.02f
                textSize = 16f

                maxWidth = dip(260)
                visibility = View.GONE
                if (XPref.getAodShowNote() && !XPref.getAodNoteContent().isNullOrBlank()) {
                    visibility = View.VISIBLE
                    text = XPref.getAodNoteContent()
                }
            }.lparams(wrapContent, wrapContent) {
                bottomMargin = dip(6)
            }


        }.lparams(width = wrapContent, height = wrapContent) {
            startToStart = PARENT_ID
            topToTop = PARENT_ID
            topMargin = getTopMargin()
            marginStart = dip(40)
        }

        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            applyRecursively {
                when (it) {
                    is TextView -> it.setGradientTest()
                    is ImageView -> it.imageTintList =
                        ColorStateList.valueOf(Color.parseColor(ThemeManager.getCurrentColorPack().tintColor))
                }
            }
        }

        if(!XPref.isSettings()) {
            val filterView = View(context).apply {
                id = SharedIds.filterView
                backgroundColor = Color.BLACK
                alpha = 0f
                layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                )
                elevation = 8f
            }

            addView(filterView)
        }
    }
}

private fun View.getTopMargin() : Int {
    return if(XPref.isSettings()){
        0
    }else{
        dip(200)
    }
}