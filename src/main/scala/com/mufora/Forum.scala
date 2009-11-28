package com.mufora

import javax.persistence.{Entity, Id, GeneratedValue, OneToMany, ManyToOne, FetchType}
import java.io.Serializable
import javax.ws.rs.{Path, GET, PathParam}
import com.sun.jersey.api.view.Viewable
import java.util.{List => JList, ArrayList}
import scala.reflect.BeanProperty

// this is a little ugly but still better than spreading this out over multiple files
object ForumMeta {
  val resourceClasses = List(classOf[ForumIndex], classOf[Forum], classOf[Thread])
  val entityClasses = List(classOf[Forum], classOf[Thread], classOf[Post])
}

@Path("/")
class ForumIndex {
  @GET
  def handleIndex:Viewable = new Viewable("/index.ftl", Database.listForums)

  @Path("/forum/{id}")
  def forum(@PathParam("id") id:int):Forum = Database.getForum(id)
}

@Entity
class Forum extends Serializable {
  @Id @GeneratedValue @BeanProperty
  var id:int = _
  @BeanProperty
  var name:String = _

  // unfortunately this needs to be of type java.util.List for hibernate to work
  @OneToMany{ val mappedBy = "forum" } @BeanProperty
  var threads:JList[Thread] = new ArrayList[Thread]

  def +(thread:Thread) = {
    thread.forum = this
    threads.add(thread)
  }

  @GET
  def get():Viewable = new Viewable("/forum.ftl", this)

  @Path("thread/{id}")
  def thread(@PathParam("id") id:int):Thread = Database.getThread(id)
}

@Entity
class Thread extends Serializable {
  @Id @GeneratedValue @BeanProperty
  var id:int = _
  @BeanProperty
  var name:String = _
  @ManyToOne @BeanProperty
  var forum:Forum = _
  @OneToMany{ val mappedBy = "thread" } @BeanProperty
  var posts:JList[Post] = new ArrayList[Post]

  def +(post:Post) = {
    post.thread = this
    posts.add(post)
  }

  @GET
  def get():Viewable = new Viewable("/thread.ftl", this)
}

@Entity
class Post extends Serializable {
  @Id @GeneratedValue @BeanProperty
  var id:int = _
  @BeanProperty
  var content:String = _
  @ManyToOne @BeanProperty
  var thread:Thread = _
}