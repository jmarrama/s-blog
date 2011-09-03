package controllers

import play._
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
    
}
