package com.mufora

import org.hibernate.cfg.AnnotationConfiguration
import org.hibernate.{Session, Transaction}
import java.util.{List => JavaList}

object Database {
  var hibernateCfg = new AnnotationConfiguration()
                      .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                      .setProperty("hibernate.connection.url", "jdbc:h2:mem:")
                      .setProperty("hibernate.hbm2ddl.auto", "create")
                      .setProperty("hibernate.current_session_context_class", "thread")
  //                    .setProperty("hibernate.show_sql", "true")

  ForumMeta.entityClasses.foreach{ cls => hibernateCfg.addAnnotatedClass(cls) }

  var sessionFactory = hibernateCfg.buildSessionFactory

  def init() = {
    var session = sessionFactory.getCurrentSession
    var tx = session.beginTransaction
    
    var forum1 = new Forum
    forum1.name = "Test forum 1"
    session.save(forum1)
    
    var thread1 = new Thread
    thread1.name = "Test thread 1"
    forum1 + thread1
    session.save(thread1)

    var post1 = new Post
    post1.content = "Test post 1 content"
    thread1 + post1
    session.save(post1)

    var post2 = new Post
    post2.content = "Test post 2 content"
    thread1 + post2
    session.save(post2)

    var thread2 = new Thread
    thread2.name = "Test thread 2"
    forum1 + thread2
    session.save(thread2)

    var post3 = new Post
    post3.content = "Test post 3 content"
    thread2 + post3
    session.save(post3)

    var forum2 = new Forum
    forum2.name = "Test forum 2"
    session.save(forum2)

    var thread3 = new Thread
    thread3.name = "Test thread 3"
    forum2 + thread3
    session.save(thread3)

    var post4 = new Post
    post4.content = "Test post 4 content"
    thread3 + post4
    session.save(post4)
    
    tx.commit
  }

  def select[T](func: Session => T):T = {
    var session:Session = null
    var tx:Transaction = null
    var result:T = null.asInstanceOf[T] // <neo>whoa</neo>

    try {
      session = sessionFactory.getCurrentSession
      tx = session.beginTransaction
      result = func(session)
      tx.commit
    }
    catch {
      case e: Exception => {
        println("Error: " + e.getMessage + " [" + e.getClass.getName + "]")
        if (tx != null) tx.rollback
      }
    }

    result
  }

  def update[T](func: Session => _) = {
    var session:Session = null
    var tx:Transaction = null

    try {
      session = sessionFactory.getCurrentSession
      tx = session.beginTransaction
      func(session)
      tx.commit
    }
    catch {
      case e: Exception => if (tx != null) tx.rollback
    }
  }
}