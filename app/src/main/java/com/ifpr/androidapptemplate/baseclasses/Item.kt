package com.ifpr.androidapptemplate.baseclasses

data class Item(
    var endereco: String? = null,
    val base64Image: String? = null,
    val imageUrl: String? = null,
    val fotoUrl: String? = null,
    val nome: String? = null,
    val descricao: String? = null,
    val uid: String? = null
)

