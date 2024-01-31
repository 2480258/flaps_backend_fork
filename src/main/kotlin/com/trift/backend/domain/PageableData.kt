package com.trift.backend.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.ArrayList

data class PageableData<T> @JsonCreator constructor(
    @JsonProperty("data") val data: List<T>,
    @JsonProperty("total") val total: Long
)