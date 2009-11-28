package com.mufora

import java.net.InetAddress
import org.mortbay.jetty.servlet.{Context => JettyContext, FilterHolder, ServletHolder, DefaultServlet}
import com.sun.jersey.api.core.ClassNamesResourceConfig
import com.sun.jersey.spi.container.servlet.ServletContainer
import org.mortbay.jetty.{Server => JettyServer, Handler}
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

object Server {
  def main(args:Array[String]) = {
    Database.init

    var fh = new FilterHolder(classOf[StaticContentFilter])
    fh.setInitParameter(ServletContainer.RESOURCE_CONFIG_CLASS,
                        classOf[ClassNamesResourceConfig].getName)

    var classNames = new StringBuilder(classOf[FreemarkerTemplateProvider].getName)
    ForumMeta.resourceClasses.foreach{ cls => classNames.append(";" + cls.getName) }
    fh.setInitParameter(ClassNamesResourceConfig.PROPERTY_CLASSNAMES, classNames.toString)

    var sh = new ServletHolder(classOf[DefaultServlet])
    sh.setInitParameter("org.mortbay.jetty.servlet.Default.dirAllowed", "true")

    var port = 8080
    var jettyServer = new JettyServer(port)
    var context = new JettyContext(jettyServer, "/", true, false)
    context.addServlet(sh, "/*")
    context.addFilter(fh, "/*", Handler.REQUEST)
    context.setResourceBase("src/main/webapp")
    jettyServer.start();

    var host = InetAddress.getLocalHost.getHostName
    println("Server started on http://" + host + ":" + port)
  }
}

class StaticContentFilter extends ServletContainer {
  override def doFilter(request:javax.servlet.http.HttpServletRequest, response:javax.servlet.http.HttpServletResponse, chain:javax.servlet.FilterChain) = {
    var path = request.getPathInfo

    if (path.startsWith("/img/") || path.startsWith("/js/") || path.startsWith("/css/")) {
      chain.doFilter(request, response)
    }
    else {
      super.doFilter(request, response, chain)
    }
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
    if (path.endsWith(default_ext)) path else path + default_ext
  }

  override def writeTo(resolvedPath:String, model:Any, out:OutputStream) = {
    out.flush // send status + headers

    var vars = new HashMap[String, Any]()

    model match {
      case m:Map[String, _] => vars.putAll(m)
      case _ => vars.put("it", model)
    }

    freemarkerConfig.getTemplate(resolvedPath).process(vars, new OutputStreamWriter(out))
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

/**
 * Custom object wrapper for making scala object and collections freemarker-compatible
 */
class ScalaBeansWrapper extends SimpleObjectWrapper {
  override def wrap(obj:Object):TemplateModel = {
    obj match {
      case scol:scala.Collection[_] => super.wrap(scol.asJava)
      case sobj:ScalaObject => new TemplateHashModel() {
        def get(key:String):TemplateModel = wrap(sobj.getClass.getMethod(key).invoke(sobj))
        def isEmpty = false
      }
      case _ => super.wrap(obj)
    }
  }
}