package com.mufora

import org.hibernate.cfg.AnnotationConfiguration
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.GeneratedValue
import java.io.Serializable
import java.util.{List => JavaList}

object Database {
  var sessionFactory = new AnnotationConfiguration()
                        .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                        .setProperty("hibernate.connection.url", "jdbc:h2:mem:")
                        .setProperty("hibernate.hbm2ddl.auto", "create")
                        .setProperty("hibernate.current_session_context_class", "thread")
                        .addAnnotatedClass(classOf[Forum])
                        .buildSessionFactory

  def init() = {
    var session = sessionFactory.getCurrentSession
    var tx = session.beginTransaction
    
    var forum1 = new Forum
    forum1.name = "Test forum 1"
    session.save(forum1)
    
    var forum2 = new Forum
    forum2.name = "Test forum 2"
    session.save(forum2)
    
    tx.commit
  }

  def listForums():JavaList[Forum] = {
    var session = sessionFactory.getCurrentSession
    var tx = session.beginTransaction

    var list = session.createQuery("from Forum").list.asInstanceOf[JavaList[Forum]]

    tx.commit

    return list
  }
}

@Entity
class Forum extends Serializable {
  @Id @GeneratedValue
  var theid:int = _
  var thename:String = _

  def id:int = theid
  def id_=(n:int) = theid = n

  def name:String = thename
  def name_=(str:String) = thename = str
}