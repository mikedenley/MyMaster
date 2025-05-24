package com.example.swoop.ui.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat

object TooltipHelper{
    fun show(view:View,msg:String){
        TooltipCompat.setTooltipText(view,msg)
        view.postDelayed({Toast.makeText(view.context,msg,Toast.LENGTH_SHORT).show()},100)
    }
    fun show(ctx:Context,msg:String)=Toast.makeText(ctx,msg,Toast.LENGTH_SHORT).show()
}