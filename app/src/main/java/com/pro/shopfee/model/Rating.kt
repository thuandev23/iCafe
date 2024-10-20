package com.pro.shopfee.model

import java.io.Serializable

class Rating : Serializable {
    var userId: String? = null
    var userName: String? = null
    var userImage: String? = null
    var review: String? = null
    var rate = 0.0

    constructor()
    constructor(userId: String?, userName: String?, userImage: String?, review: String?, rate: Double) {
        this.userId = userId
        this.userName = userName
        this.userImage = userImage
        this.review = review
        this.rate = rate
    }
}