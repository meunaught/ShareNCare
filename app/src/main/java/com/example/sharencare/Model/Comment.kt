package com.example.sharencare.Model

class Comment {
    private var commentID : String = ""
    private var postID : String = ""
    private var message : String = ""
    private var publisher : String = ""

    fun getCommentID():String{
        return this.commentID
    }

    fun setCommentID (commentID : String){
        this.commentID = commentID
    }

    fun getPostID():String{
        return this.postID
    }

    fun setPostID (postID : String){
        this.postID = postID
    }

    fun getMessage():String{
        return this.message
    }

    fun setMessage (message : String){
        this.message = message
    }

    fun getPublisher():String{
        return this.publisher
    }

    fun setPublisher (publisher : String){
        this.publisher = publisher
    }
}