package wdta.widgt.waprogressview

import android.content.Context
import android.graphics.*
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat


/**
 * @author 吳東承
 * @create 2020-6-30
 * @Describe 建立錄音時的時間進展
 */
class WARecordVoiceProgressView : View {

    private var isDebug: Boolean = true

    //region listener
    var recordVoiceProgressListener: RecordVoiceProgressListener? = null
    //endregion

    //region 繪製相關
    private var drawGradientPaint: Paint? = null
    private var gradientPath = Path()
    private var drawProgressViewBottomPaint: Paint? = null
    private var mSweepGradient: SweepGradient? = null
    private var countDownTimer: CountDownTimer? = null
    //endregion

    //region attr
    private var maxTime: Int = 0
    private var bottomColor: Int = 0
    private var startColor: Int = 0
    private var endColor: Int = 0
    private var gradientColor: IntArray? = null
    //endregion

    //region parameter
    private var centerX = 0F
    private var centerY = 0F
    private var viewHeight = 0F
    private var viewWidth = 0F
    private var radius = 0F
    private var circleLineStroke = 0F
    private var gradientRectF: RectF? = null
    private var gradientSweepAngel = 0F
    //endregion

    constructor(context: Context?) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.WARecordVoiceProgressView)
        maxTime = resources.getInteger(
            typeArray.getResourceId(
                R.styleable.WARecordVoiceProgressView_default_voice_progress_max,
                R.integer.default_voice_progress_max
            )
        )
        bottomColor = typeArray.getResourceId(
            R.styleable.WARecordVoiceProgressView_default_voice_progress_background_color,
            R.color.default_voice_progress_background_color
        )
        startColor = typeArray.getResourceId(
            R.styleable.WARecordVoiceProgressView_default_voice_progress_start_color,
            R.color.default_voice_progress_start_color
        )
        endColor = typeArray.getResourceId(
            R.styleable.WARecordVoiceProgressView_default_voice_progress_end_color,
            R.color.default_voice_progress_end_color
        )

        //漸層初始
        val gradientId = typeArray.getResourceId(
            R.styleable.WARecordVoiceProgressView_default_voice_progress_gradient_color,
            -1
        )
        gradientColor = if (gradientId == -1) {
            createDefaultGradient()
        } else {
            resources.getIntArray(gradientId)
        }

        typeArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (isDebug) Log.d("DEBUG", "onMeasure-> widthMode:$widthMode,heightMode:$heightMode")
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (isDebug) Log.d(
            "onLayout",
            "changed:$changed,left:$left,top:$top,right:$right,bottom:$bottom"
        )
        centerX = (right - left) / 2F
        centerY = (bottom - top) / 2F
        viewHeight = (bottom - top).toFloat()
        viewWidth = (right - left).toFloat()
        if (isDebug) Log.d(
            "onLayout",
            "centerX:$centerX,centerY:$centerY,viewHeight:$viewHeight,viewWidth:$viewWidth"
        )

        circleLineStroke = initCircleStroke() //圓邊線的寬度
        radius = (if (viewWidth > viewHeight) viewHeight else viewWidth) / 2 - circleLineStroke
        initPaint() //初始化畫筆
        initRectF()//初始化範圍
        initCountDownTimer()//初始化倒數計時
//        test()//測試
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawGradientPaint?.let {
            if (isDebug) Log.d("DEBUG", "onDraw gradientSweepAngel:$gradientSweepAngel")
            gradientPath.reset()
            gradientPath.addArc(gradientRectF!!, 270F, gradientSweepAngel)
            canvas?.drawPath(gradientPath, it)
        }
        drawProgressViewBottomPaint?.let {
            canvas?.drawCircle(centerX, centerY, radius, it)
        }
    }

    //region Init

    //初始化畫筆
    private fun initPaint() {
        if (isDebug) Log.d("DEBUG", "initPaint")
        //底部
        drawProgressViewBottomPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = circleLineStroke
            color = ActivityCompat.getColor(context, bottomColor)
        }
        //畫圓
        drawGradientPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = circleLineStroke
        }

        mSweepGradient = SweepGradient(centerX, centerY, gradientColor!!, null)
        drawGradientPaint?.shader = mSweepGradient
    }

    //計算圓邊線的寬度
    private fun initCircleStroke(): Float {
        if (isDebug) Log.d("DEBUG", "initCircleStroke")
        return viewWidth * 0.045f
    }

    //引用預設色彩漸層
    private fun createDefaultGradient(): IntArray {
        if (isDebug) Log.d("DEBUG", "createDefaultGradient")
        return intArrayOf(
            ActivityCompat.getColor(context, endColor),
            ActivityCompat.getColor(context, startColor),
            ActivityCompat.getColor(context, endColor),
            ActivityCompat.getColor(context, endColor)
        )
    }

    //初始化繪製漸層圓範圍
    private fun initRectF() {
        if (isDebug) Log.d("DEBUG", "initRectF")
        gradientRectF = RectF().apply {
            left = centerX - radius
            top = centerY - radius
            right = centerX + radius
            bottom = centerY + radius
        }
    }

    private fun initCountDownTimer() {
        if (isDebug) Log.d("DEBUG", "initCountDownTimer")
        gradientSweepAngel = 0F
        val maxTimeLong = maxTime * 1000
        countDownTimer = object : CountDownTimer(maxTimeLong.toLong(), 10L) {
            override fun onFinish() {
                gradientSweepAngel = 360F
                invalidate()
                recordVoiceProgressListener?.recordFinish()
            }

            override fun onTick(millisUntilFinished: Long) {
                gradientSweepAngel =
                    (1F - (millisUntilFinished.toFloat() / maxTimeLong.toFloat())) * 360F
                invalidate()
            }
        }
    }

    //測試
//    private fun test() {
        //測試
//        start()
//        GlobalScope.launch {
//            delay(7500)
//            stop()
//        }
//    }
    //endregion

    //region 控制
    fun start() {
        if (isDebug) Log.d("DEBUG", "start")
        countDownTimer?.start()
    }

    fun stop() {
        if (isDebug) Log.d("DEBUG", "stop")
        countDownTimer?.cancel()
        gradientSweepAngel = 0F
    }
    //endregion

}