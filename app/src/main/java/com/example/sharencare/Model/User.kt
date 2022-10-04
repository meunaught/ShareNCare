package com.example.sharencare.Model

class User {
    private var username : String = ""
    private var fullname : String = ""
    private var email : String = ""
    private var bio : String = ""
    private var image : String = ""
    private var uid : String = ""
    private var password : String = ""

    constructor()
    {

    }

    constructor(username : String,fullname : String,email :String,bio : String,image : String,uid : String)
    {
        this.username = username
        this.fullname = fullname
        this.email = email
        this.bio = bio
        this.image = image
        this.uid = uid
    }

    constructor(username : String,fullname : String,email :String,bio : String,image : String,uid : String,password :String)
    {
        this.username = username
        this.fullname = fullname
        this.email = email
        this.bio = bio
        this.image = image
        this.uid = uid
        this.password = password
    }



    fun getUsername() : String
    {
        return this.username
    }

    fun setUsername(username : String){
        this.username = username
    }

    fun getFullname() : String
    {
        return this.fullname
    }

    fun setFullname(fullname : String){
        this.fullname = fullname
    }

    fun getEmail() : String
    {
        return this.email
    }

    fun setEmail(email : String){
        this.email = email
    }

    fun getBio() : String
    {
        return this.bio
    }

    fun setBio(bio : String){
        this.bio = bio
    }

    fun getImage() : String
    {
        return this.image
    }

    fun setImage(image : String){
        this.image = image
    }

    fun getUid() : String
    {
        return this.uid
    }

    fun setUid(uid : String){
        this.uid = uid
    }

    fun setPassword(password: String)
    {
        this.password = password
    }

    fun getPassword() : String
    {
        return this.password
    }

}