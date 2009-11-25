package com.mufora

import java.net.InetAddress
import org.mortbay.jetty.servlet.{Context => JettyContext, ServletHolder}
import com.sun.jersey.api.core.ClassNamesResourceConfig
import com.sun.jersey.spi.container.servlet.ServletContainer
import org.mortbay.jetty.{Server => JettyServer}
import javax.ws.rs.{Path, GET}
import java.util.{Map, HashMap}
import java.io.{File, OutputStream, IOException, OutputStreamWriter}
import com.sun.jersey.spi.template.TemplateProcessor
import freemarker.cache.FileTemplateLoader
import freemarker.template.{Configuration, Template, ObjectWrapper, SimpleObjectWrapper,
                            TemplateHashModel, TemplateModel, TemplateException,
                            TemplateModelException}
import javax.ws.rs.ext.Provider
import javax.servlet.ServletContext
import javax.ws.rs.core.Context
import com.sun.jersey.api.view.Viewable
import scala.collection.{ mutable }
import org.scala_tools.javautils.Imports._


@Path("/")
class TestResource {
  @GET
  def handleIndex:Viewable = {
    new Viewable("/index.ftl", Database.listForums)
  }
}

object Server {
  def main(args:Array[String]) = {
    Database.init

    var sh = new ServletHolder(classOf[ServletContainer])
    sh.setInitParameter(ServletContainer.RESOURCE_CONFIG_CLASS,
                        classOf[ClassNamesResourceConfig].getName)

    var classNames = List[String](classOf[TestResource].getName,
                                  classOf[FreemarkerTemplateProvider].getName)
    sh.setInitParameter(ClassNamesResourceConfig.PROPERTY_CLASSNAMES,
                        classNames.reduceLeft {(a,b) => a + ";" + b} )

    var port = 8080
    var jettyServer = new JettyServer(port)
    var context = new JettyContext(jettyServer, "/", true, false)

    context.addServlet(sh, "/*")
    jettyServer.start();

    var host = InetAddress.getLocalHost.getHostName
    println("Server started on http://" + host + ":" + port)
  }
}

/**
* Basic ideas taken from http://github.com/cwinters/jersey-freemarker
* Modified heavily for my purposes.
*
* @author Chris Winters <chris@cwinters.com>
* @author Toby Jungen
*/
@Provider
class FreemarkerTemplateProvider extends TemplateProcessor {
  val default_ext:String = ".ftl"
  var freemarkerConfig:Configuration = _

  def resolve(path:String):String = {
    // accept both '/path/to/template' and '/path/to/template.ftl'
    if (path.endsWith(default_ext)) {
      path
    }
    else {
      path + default_ext
    }
  }

  override def writeTo(resolvedPath:String, model:Any, out:OutputStream) = {
    out.flush // send status + headers

    var template = freemarkerConfig.getTemplate(resolvedPath)
    var vars = new HashMap[String, Any]()

    if (model.isInstanceOf[Map[String, Any]]) {
      vars.putAll(model.asInstanceOf[Map[String, Any]])
    }
    else {
      vars.put("it", model)
    }

    template.process(vars, new OutputStreamWriter(out))
  }

  @Context
  def setServletContext(context:ServletContext) = {
    freemarkerConfig = new Configuration()
    var rootPath = new File("src/main/templates");
    freemarkerConfig.setTemplateLoader(new FileTemplateLoader(rootPath))
    freemarkerConfig.setNumberFormat("0") // don't always put a ',' in numbers
    freemarkerConfig.setLocalizedLookup(false) // don't look for list.en.ftl when list.ftl requested
    freemarkerConfig.setTemplateUpdateDelay(0) // don't cache
    freemarkerConfig.setObjectWrapper(new ScalaBeansWrapper)
  }
}

class ScalaBeansWrapper extends SimpleObjectWrapper {
  override def wrap(obj: Object): TemplateModel = {
    obj match {
      case scol: scala.Collection[_] => super.wrap(scol.asJava)
      case sobj: ScalaObject => new ScalaHashModel(this, sobj)
      case _ => super.wrap(obj)
    }
  }
}

/** A model that will expose all Scala getters that has zero parameters
 * to the FM Hash#get method so can retrieve it without calling with parenthesis.

 This stuff is still kinda magic to me... -TJ */
class ScalaHashModel(wrapper: ObjectWrapper, sobj: ScalaObject) extends TemplateHashModel {
  type Getter = () => AnyRef

  val gettersCache = new mutable.HashMap[Class[_], mutable.HashMap[String, Getter]]

  val getters = {
    val cls = sobj.getClass
    gettersCache.synchronized{
      gettersCache.get(cls) match {
        case Some(cachedGetters) => cachedGetters
        case None =>{
          val map = new mutable.HashMap[String, Getter]
          cls.getMethods.foreach { m =>
            val n = m.getName
            if(!n.endsWith("_$eq") && m.getParameterTypes.length==0){
              map += Pair(n, (() => m.invoke(sobj, Array[AnyRef]():_*)))
            }
          }
          gettersCache.put(cls, map)
          map
        }
      }
    }
  }

  def get(key: String) : TemplateModel = getters.get(key) match {
    case Some(getter) => wrapper.wrap(getter())
    case None => throw new TemplateModelException(key+" not found in object "+sobj)
  }

  def isEmpty = false
}