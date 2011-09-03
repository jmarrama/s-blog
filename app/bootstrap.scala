/**
 * Created by IntelliJ IDEA.
 * User: jmarrama
 * Date: 8/27/11
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */

import play.jobs._

@OnApplicationStart class BootStrap extends Job {

    override def doJob {

        import models._
        import play.test._

        // Import initial data if the database is empty
        if(User.count().single() == 0) {
            Yaml[List[Any]]("data.yml").foreach {
                _ match {
                    case u:User => User.create(u)
                    case p:Post => Post.create(p)
                    case c:Comment => Comment.create(c)
                }
            }
        }

    }

}

