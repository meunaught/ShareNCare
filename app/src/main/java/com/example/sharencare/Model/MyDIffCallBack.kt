package com.example.sharencare.Model

import androidx.recyclerview.widget.DiffUtil

class MyDiffCallBack(var oldPostList : List<Post>,var newPostList : List<Post>) : DiffUtil.Callback(){
    override fun getOldListSize(): Int {
        return oldPostList.size
    }

    override fun getNewListSize(): Int {
        return newPostList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldPostList.get(oldItemPosition).getPostID() == newPostList.get(newItemPosition).getPostID()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if(oldPostList.get(oldItemPosition).getDescription() != newPostList.get(newItemPosition).getDescription())
        {
            return false
        }
        else if(oldPostList.get(oldItemPosition).getPostImage() != newPostList.get(newItemPosition).getPostImage())
        {
            return false
        }
        else if(oldPostList.get(oldItemPosition).getPostPdf() != newPostList.get(newItemPosition).getPostPdf())
        {
            return false
        }
        else if(oldPostList.get(oldItemPosition).getPostPdfName() != newPostList.get(newItemPosition).getPostPdfName())
        {
            return false
        }
        else if(oldPostList.get(oldItemPosition).getPublisher() != newPostList.get(newItemPosition).getPublisher())
        {
            return false
        }
        else
        {
            return true
        }
    }
}