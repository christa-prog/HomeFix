package com.project.homefix.models

data class User(

    val fullname: String="",
    val email: String="",
    val password: String="",
    val userId: String="",
    var role: String="user"
)