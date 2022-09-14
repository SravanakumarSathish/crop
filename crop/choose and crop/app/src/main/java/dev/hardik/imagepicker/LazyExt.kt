package com.techno.developer.soccer.quiz.ui

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes

fun <ViewT : View> Activity.bindView(@IdRes idRes: Int): Lazy<ViewT> {
  return lazyUnSynchronize { findViewById<ViewT>(idRes)  }
}

fun <ViewT : View> View.bind(@IdRes idRes: Int): Lazy<ViewT> {
  return lazyUnSynchronize { findViewById<ViewT>(idRes) }
}

fun <T> lazyUnSynchronize(initializer: () -> T): Lazy<T> =
  lazy(LazyThreadSafetyMode.NONE, initializer)