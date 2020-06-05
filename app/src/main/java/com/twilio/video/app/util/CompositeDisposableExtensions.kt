package com.twilio.video.app.util

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

operator fun CompositeDisposable.plus(newDisposable: Disposable) = add(newDisposable)
