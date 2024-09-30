package com.pro.shopfee.model

import java.io.Serializable

class News : Serializable {
    var id: String? = null
    var title: String? = null
    var description: String? = null
    var image: String? = null
    var timestamp: String? = null

    constructor()
    constructor(
         id: String? ,
         title: String? ,
         description: String? ,
         image: String?,
         timestamp: String?
    ) {
        this.id = id
        this.title = title
        this.description = description
        this.image = image
        this.timestamp = timestamp
    }
}
