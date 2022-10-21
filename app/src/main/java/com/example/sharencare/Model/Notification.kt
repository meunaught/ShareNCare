package com.example.sharencare.Model

class Notification {
    private var type : String = ""
    private var sender : String = ""
    private var postID : String = ""
    private var receiver : String = ""
    private var seen : String  = ""

    fun getType(): String{
        return this.type
    }

    fun setType(type: String){
        this.type = type
    }

    fun getSender(): String{
        return this.sender
    }

    fun setSender(sender: String){
        this.sender = sender
    }

    fun getPostID(): String{
        return this.postID
    }

    fun setPostID(postID: String){
        this.postID = postID
    }


    fun getReceiver(): String{
        return this.receiver
    }

    fun setReceiver(receiver: String){
        this.receiver = receiver
    }

    fun getSeen():String{
        return this.seen
    }

    fun setSeen(seen:String){
        this.seen = seen
    }
}