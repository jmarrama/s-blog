package controllers

import play._
import cache._
import data.validation.{Valid, Validation}
import libs.Codec
import play.mvc._
import models._
import java.lang.Boolean


object Application extends Controller {
    
    import views.Application._

    def index = {

        val allPosts = Post.allWithAuthorAndComments

//        val sid = session.getId
//        println(sid)

        html.index(
            allPosts.headOption,
            allPosts.take(5),
            allPosts.drop(5)
        )


    }

//    def isAdmin() : scala.Boolean = {
//        val cstore = Cache.get(session.getId)
//        if(cstore == None) return false
//        else{
//            return (cstore.get == "admin")
//        }
//    }

    def isAdmin() : scala.Boolean = {
        if(!session.contains("isadmin")) return false
        else return (session.get("isadmin") == "true")
    }


    def show(id: Long) = {
        Post.byIdWithAuthorAndComments(id).map { post =>
            val isad = isAdmin()
            html.show(post, post._1.prevNext, Codec.UUID(), isad)
        } getOrElse {
            NotFound("No such Post")
        }
    }

    def postComment(postId:Long) = {
        val author = params.get("author")
        val content = params.get("content")
        val code = params.get("code")
        val randomID = params.get("randomID")
        Validation.required("author", author).message("Author is required")
        Validation.required("content", content).message("Content is required")

//        println(code)
//        println(Cache.get(randomID).orNull)

        Validation.equals("code", code, "code", Cache.get(randomID).orNull).
            message("Invalid code. Please type it again")



        if(Validation.hasErrors){
            show(postId)
        }
        else{
            Comment.create(Comment(postId, author, content))
            flash += "success" -> ("Thanks for posting " + author)
            Action(show(postId))
        }
    }

    def captcha(id: String) = {

        def captcha = "What is the first name of the author of this blog?"
        def answer = "joseph"
        Cache.set(id, answer, "10mn")
        captcha
    }

    def login() = {
        html.loginPage()
    }

    def authenticate = {
        val username = params.get("username")
        val pass = params.get("password")
        Validation.required("username", username).message("Username is required")
        Validation.required("password", pass).message("Password is required")

        //get the user
        User.connect(username, pass).map { founduser =>

            //store the username in the session cookie
            val userid = founduser.id.get.get
            session.put("userid", userid.toString)
            session.put("username", founduser.email)
            session.put("isadmin", founduser.isAdmin.toString)

            //if the user is an admin
            //get session id, and then store seshid => "admin" into cache
            val seshid = session.getId
            val isadmin = founduser.isAdmin
            if(isadmin) Cache.set(seshid, "admin", "100mn")

        } getOrElse {
            //add error if username wasn't found
            Validation.addError("valid-login", "Invalid User/Password Combination")
        }


        //if there are errors, flash them at the login screen using validation
        if(Validation.hasErrors)
            login()
        else{
            //otherwise, return to home
            Action(index)
        }
    }

    def logout() = {
        session.remove("userid")
        session.remove("username")
        session.remove("isadmin")
        Cache.delete(session.getId)
        Action(index)
    }

    def deleteComment(id: Long, postid: Long) = {
        //check if its admin, delete if so
        if(isAdmin()){
            Comment.deleteById(id)
        }
        Action(show(postid))
    }

    def newPost() = {
        //check if its admin first
        if(isAdmin()){
            html.newPostPage()
        }
        else{
            Action(index)
        }
    }

    def createPost() = {
        //check if its admin first
        if(isAdmin()){
            val ptitle = params.get("post-name")
            val content = params.get("content")
            Validation.required("title", ptitle).message("Post title is required")
            Validation.required("content", content).message("Content is required, durrrr")

            if(Validation.hasErrors){
                newPost()
            }
            else{
                val author_id = session.get("userid").toLong
                val author = session.get("username")
                Post.create(Post(author_id, ptitle, content))
                Action(index)
            }
        }
    }

    def deletePost(id: Long) = {
        //check if its admin, delete if so
        if(isAdmin()){
            Post.deleteById(id)
        }
        Action(index)
    }
}
