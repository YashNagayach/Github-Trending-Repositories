package com.yash.trending.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.yash.trending.data.model.Repo
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RepoSearchResponse(

    @SerializedName("total_count")
    val total: Int = 0,

    @SerializedName("items")
    val items: List<Repo> = emptyList(),
) : Parcelable