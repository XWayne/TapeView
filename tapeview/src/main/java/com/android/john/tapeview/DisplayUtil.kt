package com.android.john.tapeview

import android.content.Context
import android.util.TypedValue

fun Context.dp2px(dp:Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,resources.displayMetrics)

fun Context.sp2px(sp:Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,sp,resources.displayMetrics)
