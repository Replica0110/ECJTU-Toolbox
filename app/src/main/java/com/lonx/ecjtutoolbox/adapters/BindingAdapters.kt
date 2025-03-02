package com.lonx.ecjtutoolbox.adapters


import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField

@BindingAdapter("bindSrcCompat")
fun ImageView.bindSrcCompat(imageResId: ObservableField<Int>) {
    imageResId.get()?.let { resId ->
        setImageDrawable(ContextCompat.getDrawable(context, resId))
    }
}
