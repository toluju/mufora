package com.mufora

import javax.persistence.{Entity, Id, GeneratedValue, OneToMany, ManyToOne, FetchType}
import java.io.Serializable
import javax.ws.rs.{Path, GET, PathParam}
import com.sun.jersey.api.view.Viewable
import java.util.{List => JList, ArrayList}

@Path("/")
class ForumIndex {
  @GET
  def handleIndex:Viewable = new Viewable("/index.ftl", Database.listForums)

  @Path("/forum/{id}")
  def forum(@PathParam("id") id:int):Forum = Database.getForum(id)
}

@Entity
class Forum extends Serializable {
  @Id @GeneratedValue
  var theid:int = _
  var thename:String = _

  // unfortunately this needs to be of type java.util.List for hibernate to work
  @OneToMany{ val mappedBy = "theforum" }
  var thethreads:JList[Thread] = new ArrayList[Thread]

  def id:int = theid
  def id_=(n:int) = theid = n

  def name:String = thename
  def name_=(str:String) = thename = str

  def threads:JList[Thread] = thethreads

  def +(thread:Thread) = {
    thread.forum = this
    thethreads.add(thread)
  }

  @GET
  def get():Viewable = new Viewable("/forum.ftl", this)

  @Path("thread/{id}")
  def thread(@PathParam("id") id:int):Thread = Database.getThread(id)
}

@Entity
class Thread extends Serializable {
  @Id @GeneratedValue
  var theid:int = _
  var thename:String = _
  @ManyToOne
  var theforum:Forum = _
  @OneToMany{ val mappedBy = "thethread" }
  var theposts:JList[Post] = new ArrayList[Post]

  def id:int = theid
  def id_=(n:int) = theid = n

  def name:String = thename
  def name_=(str:String) = thename = str

  def forum:Forum = theforum
  def forum_=(f:Forum) = theforum = f

  def posts:JList[Post] = theposts

  def +(post:Post) = {
    post.thread = this
    theposts.add(post)
  }

  @GET
  def get():Viewable = new Viewable("/thread.ftl", this)
}

@Entity
class Post extends Serializable {
  @Id @GeneratedValue
  var theid:int = _
  var thecontent:String = _
  @ManyToOne
  var thethread:Thread = _

  def id:int = theid
  def id_=(n:int) = theid = n

  def content:String = thecontent
  def content_=(str:String) = thecontent = str

  def thread:Thread = thethread
  def thread_=(t:Thread) = thethread = t
}