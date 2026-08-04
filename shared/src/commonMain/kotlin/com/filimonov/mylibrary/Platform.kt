package com.filimonov.mylibrary

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform