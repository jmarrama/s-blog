/**
 * Created by IntelliJ IDEA.
 * User: jmarrama
 * Date: 8/25/11
 * Time: 8:36 PM
 * To change this template use File | Settings | File Templates.
 */

package models

import play.db.anorm._
import play.db.anorm.SqlParser._
import play.db.anorm.defaults._
import play.data.validation._
import java.sql.{ Timestamp, SQLException }
import java.text.SimpleDateFormat
import java.util.Date


// User

case class User(
    id: Pk[Long],
    email: String,
    password: String,
    fullname: String,
    isAdmin: Boolean
)

object User extends Magic[User] {

  def connect(email: String, password: String) = {
    User.find("email = {email} and password = {password}")
      .on("email" -> email, "password" -> password)
      .first()
  }

}

case class Post(
    id: Pk[Long],
    title: String,
    content: String,
    postedAt: Long,
    dateStr: String,
    author_id: Long
) {
    def prevNext = {
        SQL(
            """
                (
                    select *, 'next' as pos from post
                    where postedAt < {date} order by postedAt desc limit 1
                )
                    union
                (
                    select *, 'prev' as pos from post
                    where postedAt > {date} order by postedAt asc limit 1
                )

                order by postedAt desc

            """
        ).on("date" -> postedAt).as(
            opt('pos.is("prev")~>Post.on("")) ~ opt('pos.is("next")~>Post.on(""))
            ^^ flatten
        )
    }


}


object Post extends Magic[Post] {
    val dateFormatter = new SimpleDateFormat("MM-dd HH:mm:ss")

    def apply(author_id: Long, title: String, content: String) = {
        val curtime = new Date()
        new Post(NotAssigned, title, content, curtime.getTime, dateFormatter.format(curtime), author_id)
    }

    def allWithAuthorAndComments:List[(Post,User,List[Comment])] = {
        SQL(
            """
                select * from Post p
                join User u on p.author_id = u.id
                left join Comment c on c.post_id = p.id
                order by p.postedAt desc
            """
        ).as( Post ~< User ~< Post.spanM( Comment ) ^^ flatten * )
    }

    def byIdWithAuthorAndComments(id: Long):Option[(Post,User,List[Comment])] = {
        SQL(
            """
                select * from Post p
                join User u on p.author_id = u.id
                left join Comment c on c.post_id = p.id
                where p.id = {id}
            """
        ).on("id" -> id).as( Post ~< User ~< Post.spanM( Comment ) ^^ flatten ? )
    }

    def deleteById(id: Long) = {
        SQL(
            """
             delete from Post where id = {id}
            """
        ).on("id" -> id).executeUpdate().fold(
            e => "Oops, there was an error" ,
            c => c + " rows were updated!"
        )
    }

}

case class Comment(
    id: Pk[Long],
    author: String,
    content: String,
    postedAt: Long,
    dateStr: String,
    post_id: Long
)

object Comment extends Magic[Comment] {
    val dateFormatter = new SimpleDateFormat("MM-dd HH:mm:ss")

    def apply(post_id: Long, author: String, content: String) = {
        val curtime = new Date()
        new Comment(NotAssigned, author, content, curtime.getTime, dateFormatter.format(curtime), post_id)
    }

    def deleteById(id: Long) = {
        SQL(
            """
             delete from Comment where id = {id}
            """
        ).on("id" -> id).executeUpdate().fold(
            e => "Oops, there was an error" ,
            c => c + " rows were updated!"
        )
    }

}