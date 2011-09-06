package controllers

import play._
import data.validation.Validation
import play.mvc._
import models._

object Application extends Controller {
    
    import views.Application._
    
    def index = {

      val allPosts = Post.allWithAuthorAndComments

      html.index(
        allPosts.headOption,
        allPosts.drop(1)
      )
    }

    def show(id: Long) = {
        Post.byIdWithAuthorAndComments(id).map { post =>
            html.show(post, post._1.prevNext)
        } getOrElse {
            NotFound("No such Post")
        }
    }

    def postComment(postId:Long) = {
        val author = params.get("author")
        val content = params.get("content")
        Validation.required("author", author)
        Validation.required("content", content)
        if(Validation.hasErrors){
            show(postId)
        }
        else{
            Comment.create(Comment(postId, author, content))
            flash += "success" -> ("Thanks for posting " + author)
            Action(show(postId))
        }
    }

    
}
