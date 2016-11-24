package io.github.resty.servlet

import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import io.github.resty.RestyKernel

@WebServlet(name="RestyServlet", urlPatterns=Array("/*"))
class RestyServlet extends HttpServlet with RestyKernel {

  protected override def doGet(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    processAction(request, response, "get")
  }

  protected override def doPost(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    processAction(request, response, "post")
  }

  protected override def doPut(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    processAction(request, response, "put")
  }

  protected override def doDelete(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    processAction(request, response, "delete")
  }

}

