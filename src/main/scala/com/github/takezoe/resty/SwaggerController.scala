package com.github.takezoe.resty

import java.lang.reflect.Field

import com.github.takezoe.resty.model.ParamConverter.{OptionStringConverter, SeqStringConverter}
import com.github.takezoe.resty.model.ParamDef
import com.github.takezoe.resty.util.ReflectionUtils
import io.swagger.models._
import io.swagger.models.parameters._
import io.swagger.models.properties._

import scala.collection.mutable

/**
 * Endpoint that provides Swagger 2.0 JSON.
 */
class SwaggerController {

  @Action(method = "GET", path = "/swagger.json")
  def swaggerJson() = {
    val swagger = new Swagger()

    val appInfo = Resty.appInfo
    if(appInfo.nonEmpty){
      val info = new Info()
      if(appInfo.title.nonEmpty){ info.setTitle(appInfo.title) }
      if(appInfo.version.nonEmpty){ info.setVersion(appInfo.version) }
      if(appInfo.description.nonEmpty){ info.setDescription(appInfo.description) }
      swagger.setInfo(info)
    }

    val paths = new mutable.HashMap[String, Path]()
    val models = new mutable.HashMap[String, Model]()
    val tags = new mutable.HashMap[String, Tag]()

    Resty.allActions.filterNot(_._1.instance.isInstanceOf[SwaggerController]).foreach { case (controller, action) =>
      val tag = new Tag()
      val tagName = if(controller.name.nonEmpty) controller.name else controller.instance.getClass.getSimpleName
      tag.setName(tagName)
      if(controller.description.nonEmpty){ tag.setDescription(controller.description) }
      tags.put(tag.getName, tag)

      val path = paths.getOrElseUpdate(action.path, new Path())
      val operation = new Operation()
      operation.setOperationId(action.function.getName)
      operation.addTag(tagName)

      if(action.description.nonEmpty){
        operation.setDescription(action.description)
      }
      if(action.deperecated){
        operation.setDeprecated(true)
      }

      action.params.foreach { paramDef =>
        paramDef match {
          case ParamDef.Param(name, converter) if action.path.contains(s"{${name}}") =>
            operation.addParameter(converter.parameter(new PathParameter()))
          case ParamDef.Param(name, converter) =>
            operation.addParameter(converter.parameter(new QueryParameter()))
          case ParamDef.Body(name, clazz, converter) =>
            operation.addParameter(converter.parameter(new BodyParameter()))
        }
      }

      {
        val response = new Response()
        createSimpleProperty(action.function.getReturnType, models).map { property =>
          response.setSchema(property)
        }
        operation.addResponse("200", response)
      }

      {
        val returnType = classOf[ErrorModel]
        val response = new Response()
        response.setSchema(new RefProperty(returnType.getSimpleName))
        models.put(returnType.getSimpleName, createModel(returnType, models))
        operation.addResponse("default", response)
      }

      action.method match {
        case "get"    => path.get(operation)
        case "post"   => path.post(operation)
        case "put"    => path.put(operation)
        case "delete" => path.delete(operation)
      }
    }

    tags.foreach { case (key, tag) => swagger.addTag(tag) }
    paths.foreach { case (key, path) => swagger.path(key, path) }
    models.foreach { case (key, model) => swagger.addDefinition(key, model) }

    swagger
  }

  protected def createModel(clazz: Class[_], models: mutable.HashMap[String, Model]): Model = {
    val model = new ModelImpl()
    model.setName(clazz.getSimpleName)

    clazz.getDeclaredFields.foreach { field =>
      createProperty(field, models).foreach { property =>
        model.addProperty(field.getName, property)
      }
    }

    model
  }

  protected def createProperty(field: Field, models: mutable.HashMap[String, Model]): Option[Property] = {
    val fieldType = field.getType

    // TODO Map support?
    if(fieldType == classOf[Option[_]]){
      ReflectionUtils.getWrappedTypeOfField(field).flatMap { wrappedType =>
        createSimpleProperty(wrappedType, models)
      }
    } else if(fieldType == classOf[Seq[_]]){
        ReflectionUtils.getWrappedTypeOfField(field).map { wrappedType =>
          val property = new ArrayProperty()
          createSimpleProperty(wrappedType, models).foreach { wrappedProperty =>
            property.setItems(wrappedProperty)
          }
          property
        }
    } else {
      createSimpleProperty(fieldType, models).map { property =>
        property.setRequired(true)
        property
      }
    }
  }

  protected def createSimpleProperty(clazz: Class[_], models: mutable.HashMap[String, Model]): Option[Property] = {
    if(clazz == classOf[String]){
      Some(new StringProperty())
    } else if(clazz == classOf[Int]){
      Some(new IntegerProperty())
    } else if(clazz == classOf[Long]){
      Some(new LongProperty())
    } else if(clazz == classOf[Unit]){
      None
    } else {
      models.put(clazz.getSimpleName, createModel(clazz, models))
      Some(new RefProperty(clazz.getSimpleName))
    }
  }

}
