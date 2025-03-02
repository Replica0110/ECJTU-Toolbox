package com.lonx.ecjtutoolbox.adapters


import android.widget.ImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.ObservableField

@BindingAdapter("bindSrcCompat")
fun ImageView.bindSrcCompat(imageResId: ObservableField<Int>) {
    imageResId.get()?.let { resId ->
        setImageDrawable(ContextCompat.getDrawable(context, resId))
    }
}
@BindingAdapter("android:checked")
fun setChecked(view: SwitchCompat, value: Boolean) {
    if (view.isChecked != value) {
        view.isChecked = value
    }
}

@InverseBindingAdapter(attribute = "android:checked")
fun getChecked(view: SwitchCompat): Boolean {
    return view.isChecked
}
