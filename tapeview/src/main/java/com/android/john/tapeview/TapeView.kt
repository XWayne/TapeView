package com.android.john.tapeview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.Scroller
import java.text.DecimalFormat
import kotlin.math.abs

typealias UpdateListen = (value:Float)->Unit

class TapeView :View{

    //刻度线颜色
    private var tickMarkColor = Color.WHITE

    private var textSize = context.sp2px(20f)

    //短刻度线高度
    private var tickMarkHeightShort = context.dp2px(18f)

    //长刻度线高度
    private var tickMarkHeightLong = context.dp2px(30f)

    //刻度线间隔
    private var gap = context.dp2px(15f)

    //最小值
    private var minValue = 4

    //当前值
    private var currentValue = 5f

    //最大值
    private var maxValue = 10

    //短刻度线宽度
    private val marginBottom = context.dp2px(4f)

    //短刻度线宽度
    private val tickMarkWidthShort = context.dp2px(2f)

    //长刻度线宽度
    private val tickMarkWidthLong = context.dp2px(3f)

    //最小刻度线距离当前值的距离
    private var startOffset = 0f

    //最小刻度线距离当前值的距离的最大值，即当前值为最大值的时候
    private var maxOffset = 0f

    //刻度线总数
    private var totalCount = 0

    //三角形线路
    private val path = Path()

    //画笔
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var lastX = 0f

    //fling 速度检测
    private lateinit var velocityTracker:VelocityTracker

    //滑动计算
    private val scroller: Scroller

    private var listener:UpdateListen? = null

    private val decimalFormat = DecimalFormat("#.0")

    constructor(context:Context):this(context,null)
    constructor(context: Context,attrs:AttributeSet?):super(context,attrs){
        //TODO 引入自定义属性

        scroller = Scroller(context)
    }

    fun setUpdateListener(listener:UpdateListen){
        this.listener = listener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        velocityTracker = VelocityTracker.obtain()

        startOffset = (minValue - currentValue)*10 * gap
        maxOffset = (minValue - maxValue) *10 * gap
        totalCount = (maxValue - minValue)*10 + 1
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        velocityTracker.recycle()
        listener = null
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {

            paint.color = tickMarkColor
            paint.textSize = textSize

            drawBackground(it)
            drawTickMarks(it)
            drawTriangle(it)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let{
            velocityTracker.addMovement(event)
            when(it.action){
                MotionEvent.ACTION_DOWN ->{
                    scroller.forceFinished(true)
                }
                MotionEvent.ACTION_MOVE ->{
                    validateScroll(it.x - lastX )
                }
                MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL ->{
                    scrollToTickMark()
                    parseFling()
                }
            }
            lastX = it.x
        }


        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (scroller.computeScrollOffset()){
            val currX= scroller.currX
            when(currX) {
                scroller.startX ->{
                    postInvalidate()
                }
                scroller.finalX -> {
                    scrollToTickMark()
                }
                else ->{

                    validateScroll(currX - lastX )
                }
            }
            lastX = currX.toFloat()
        }
    }

    private fun parseFling(){
        velocityTracker.computeCurrentVelocity(1000)
        val xVelocity = velocityTracker.xVelocity
        if (abs(xVelocity)>ViewConfiguration.get(context).scaledMinimumFlingVelocity){
            scroller.fling(0,0,xVelocity.toInt(),0, Int.MIN_VALUE,Int.MAX_VALUE,0,0)
            postInvalidate()
        }
    }

    //使最终的滑动停留在刻度线上
    private fun scrollToTickMark(){
        var offset = abs(startOffset)%gap
        offset = if (offset > gap/2f) offset - gap else offset
        startOffset += offset
        postInvalidate()
    }

    //检测滑动是否有效，有效的话，则直接重绘实现滑动效果
    private fun validateScroll(dx:Float){
        if (dx >0
            && startOffset+dx > 0)
            return //滑倒最左边

        if (dx <0
            && startOffset+dx < maxOffset)
            return //滑到最右边

        startOffset += dx
        postInvalidate()

        val count = ( (abs(startOffset) + gap/2) /gap ).toInt()
        val newValue = minValue + count*0.1f
        if ( newValue != currentValue ){
            listener?.invoke(decimalFormat.format(newValue).toFloat())
            currentValue = newValue
        }
    }

    //画背景
    private fun drawBackground(canvas:Canvas){
        canvas.drawColor(Color.parseColor("#FBE40C"))
    }

    //画刻度线
    private fun drawTickMarks(canvas:Canvas){
        if (!checkValid())
            return

        for (index in 0 until totalCount){
            val px = startOffset + index *gap  + width/2f

            if ( px <=0 || px >= width ){ //不在可视范围内，不绘制
                continue
            }

            if (index % 10 == 0){ //长刻度线

                paint.strokeWidth = tickMarkWidthLong
                canvas.drawLine(px,0f,px,tickMarkHeightLong,paint)

                val text = "${minValue + (index/10f).toInt() }"
                val textWidth = paint.measureText(text)
                canvas.drawText(text,px-textWidth/2f,height-marginBottom,paint)

            }else{ //短刻度线
                paint.strokeWidth = tickMarkWidthShort
                canvas.drawLine(px,0f,px,tickMarkHeightShort,paint)

            }
        }

    }

    //画三角形
    private fun drawTriangle(canvas:Canvas){
        path.reset()
        path.moveTo(width/2f-gap,0f)
        path.rLineTo(2*gap,0f)
        path.rLineTo(-gap,gap)
        path.close()

        canvas.drawPath(path,paint)
    }

    //检查数值是否有效
    private fun checkValid() = maxValue > minValue
            &&  minValue<=currentValue
            &&  currentValue<=maxValue
}