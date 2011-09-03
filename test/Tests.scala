import java.util.Date
import play._
import play.test._

import org.scalatest._
import org.scalatest.junit._
import org.scalatest.matchers._
import play.db.anorm._
import models._

class BasicTests extends UnitFlatSpec with ShouldMatchers with BeforeAndAfterEach{

  override def beforeEach(){
    Fixtures.deleteDatabase()
  }

  it should "create and retrieve a User" in {

    User.create(User(NotAssigned, "bob@gmail.com", "secret", "Bob", false))

    val bob = User.find(
        "email={email}").on("email" -> "bob@gmail.com"
    ).first()

    bob should not be (None)
    bob.get.fullname should be ("Bob")

  }

  it should "create a Post" in {

    User.create(User(Id(1), "bob@gmail.com", "secret", "Bob", false))
    Post.create(Post(NotAssigned, "My first post", "Hello!", new Date, 1))

    Post.count().single() should be (1)

    val posts = Post.find("author_id={id}").on("id" -> 1).as(Post*)

    posts.length should be (1)

    val firstPost = posts.headOption

    firstPost should not be (None)
    firstPost.get.author_id should be (1)
    firstPost.get.title should be ("My first post")
    firstPost.get.content should be ("Hello!")

  }

  it should "support Comments" in {

    User.create(User(Id(1), "bob@gmail.com", "secret", "Bob", false))
    Post.create(Post(Id(1), "My first post", "Hello world", new Date, 1))
    Comment.create(Comment(NotAssigned, "Jeff", "Nice post", new Date, 1))
    Comment.create(Comment(NotAssigned, "Tom", "I knew that !", new Date, 1))

    User.count().single() should be (1)
    Post.count().single() should be (1)
    Comment.count().single() should be (2)

    val Some( (post,author,comments) ) = Post.byIdWithAuthorAndComments(1)

    post.title should be ("My first post")
    author.fullname should be ("Bob")
    comments.length should be (2)
    comments(0).author should be ("Jeff")
    comments(1).author should be ("Tom")

  }

}