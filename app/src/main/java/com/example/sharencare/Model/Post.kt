package com.example.sharencare.Model

class Post {
    private var postID : String = ""
    private var description : String = ""
    private var postImage : String = ""
    private var postPdf : String = ""
    private var publisher : String = ""
    private var postPdfName : String = ""
    private var publisherUsername : String = ""
    private var publisherImage : String  = ""

    constructor()

    constructor(description: String ,postID :  String,postImage: String,postPdf: String,postPdfName: String,publisher: String){
        this.postID = postID
        this.description = description
        this.postImage = postImage
        this.postPdf = postPdf
        this.postPdfName = postPdfName
        this.publisher = publisher
    }

    fun getPublisherUsername() : String{
        return this.publisherUsername
    }

    fun setPublisherUsername(publisherUsername : String){
        this.publisherUsername = publisherUsername
    }

    fun getPublisherImage() : String{
        return this.publisherImage
    }

    fun setPublisherImage(publisherImage : String){
        this.publisherImage = publisherImage
    }

    fun getPostID() : String{
        return this.postID
    }

    fun getDescription() : String{
        return this.description
    }

    fun getPostImage() : String{
        return this.postImage
    }

    fun getPostPdf() : String{
        return this.postPdf
    }

    fun getPostPdfName() : String{
        return this.postPdfName
    }

    fun getPublisher() : String{
        return this.publisher
    }

    fun setPostID(postID : String){
        this.postID = postID
    }

    fun setDescription(description : String){
        this.description = description
    }

    fun setPostImage(postImage : String){
        this.postImage = postImage
    }

    fun setPostPdf(postPdf : String){
        this.postPdf = postPdf
    }

    fun setPostPdfName(postPdfName : String){
        this.postPdfName = postPdfName
    }

    fun setPublisher(publisher : String){
        this.publisher = publisher
    }
}