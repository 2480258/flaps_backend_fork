package com.trift.backend.web.dto

data class NaverLoginRequest constructor(val naverState: String, val naverCode: String)