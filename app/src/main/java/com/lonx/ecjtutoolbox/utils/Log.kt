package com.lonx.ecjtutoolbox.utils

import slimber.log.e

inline fun <T> T.log(makeMessage: (T) -> String = { it.toString() }): T =
    also { e { makeMessage(this) } }