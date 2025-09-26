package it.unina.dietiestates.core.presentation.util

import it.unina.dietiestates.BuildConfig.BASE_URL

fun parseImageUrl(url: String?): String{
    return url?.let { url ->
        if(url.startsWith("https://") || url.startsWith("http://")){
            url
        }
        else{
            "$BASE_URL$url"
        }
    } ?: ""
}