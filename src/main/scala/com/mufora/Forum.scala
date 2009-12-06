package com.mufora

import javax.persistence.{Entity, Id, GeneratedValue, OneToMany, ManyToOne, FetchType}
import java.io.Serializable
import javax.ws.rs.{Path, GET, POST, PathParam, FormParam}
import com.sun.jersey.api.view.Viewable
import java.util.{List => JList, ArrayList}
import scala.reflect.BeanProperty
import javax.ws.rs.core.Response
import java.net.URI
import org.hibernate.Session

// this is a little ugly but still better than spreading this out over multiple files
object ForumMeta {
  val resourceClasses = List(classOf[ForumIndex], classOf[Forum], classOf[Thread])
  val entityClasses = List(classOf[Forum], classOf[Thread], classOf[Post])
}

@Path("/")
class ForumIndex {
  @GET
  def handleIndex:Viewable = new Viewable("/index.ftl", Database.select{ session =>
    session.createQuery("from Forum").list.asInstanceOf[JList[Forum]]
  })

  @Path("/forum/{id}")
  def forum(@PathParam("id") id:int):Forum = Database.select{ session =>
    var forum = session.createQuery("from Forum where id = :id").setInteger("id", id)
                       .uniqueResult.asInstanceOf[Forum]
    forum.threads.size // load threads
    forum
  }

  @GET @Path("/forum/new")
  def newForum:Viewable = new Viewable("/newforum.ftl", null)

  @POST @Path("/forum/new")
  def newForum(@FormParam("name") name:String):Response = {
    Database.update{ session:Session => session.save(new Forum(name)) }
    Response.seeOther(new URI("/")).build
  }
}

@Entity
class Forum(@BeanProperty var name:String) extends Serializable {
  def this() = this(null) // default ctor for hibernate

  @Id @GeneratedValue @BeanProperty
  var id:int = _

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
  def thread(@PathParam("id") id:int):Thread = Database.select{ session =>
    var thread = session.createQuery("from Thread where id = :id").setInteger("id", id)
                        .uniqueResult.asInstanceOf[Thread]
    thread.posts.size  // load posts
    thread
  }

  @GET @Path("thread/new")
  def newThread:Viewable = new Viewable("/newthread.ftl", this)

  @POST @Path("thread/new")
  def newThread(@FormParam("name") name:String):Response = {
    Database.update{ session:Session => session.save(new Thread(name, this)) }
    Response.seeOther(new URI("/forum/" + id)).build
  }
}

@Entity
class Thread(@BeanProperty var name:String,
             @ManyToOne @BeanProperty var forum:Forum) extends Serializable {
  def this() = this(null, null) // default ctor for hibernate

  @Id @GeneratedValue @BeanProperty
  var id:int = _
  @OneToMany{ val mappedBy = "thread" } @BeanProperty
  var posts:JList[Post] = new ArrayList[Post]

  def +(post:Post) = {
    post.thread = this
    posts.add(post)
  }

  @GET
  def get():Viewable = new Viewable("/thread.ftl", this)

  @GET @Path("post/new")
  def newPost:Viewable = new Viewable("/newpost.ftl", this)

  @POST @Path("post/new")
  def newPost(@FormParam("content") content:String):Response = {
    Database.update{ session:Session => session.save(new Post(content, this)) }
    Response.seeOther(new URI("/forum/" + forum.id + "/thread/" + id)).build
  }
}

@Entity
class Post(@BeanProperty var content:String,
           @ManyToOne @BeanProperty var thread:Thread) extends Serializable {

  def this() = this(null, null)  // default ctor for hibernate

  @Id @GeneratedValue @BeanProperty
  var id:int = _
}