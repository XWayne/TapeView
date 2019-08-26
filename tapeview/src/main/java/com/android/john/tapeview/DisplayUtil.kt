package com.android.john.tapeview

import android.content.Context
import android.util.TypedValue

object DisplayUtil {
    fun dp2px(context:Context,dp:Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,context.resources.displayMetrics)

    fun sp2px(context:Context,sp:Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,sp,context.resources.displayMetrics)
}