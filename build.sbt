name := "resty"

version := "1.0"

scalaVersion := "2.12.0"

val jettyVersion = "9.2.3.v20140905"

libraryDependencies ++= Seq(
  "org.eclipse.jetty"            %  "jetty-webapp"               % jettyVersion % "container",
  "org.eclipse.jetty"            %  "jetty-plus"                 % jettyVersion % "container",
  "org.eclipse.jetty"            %  "jetty-annotations"          % jettyVersion % "container",
  "org.scala-lang"               %  "scalap"                     % scalaVersion.value,
//  "javax"                        %  "javaee-web-api"             % "7.0"        % "provided",
//  "io.swagger"                   %  "swagger-jaxrs"              % "1.5.10",
//  "org.jboss.resteasy"           %  "resteasy-jaxrs"             % "3.1.0.CR3",
//  "org.jboss.resteasy"           %  "resteasy-jackson2-provider" % "3.1.0.CR3",
  "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.8.4",
//  "io.swagger"                   % "swagger-core"                % "1.5.10",
  "io.swagger"                   % "swagger-models"              % "1.5.10",
  "commons-io"                   % "commons-io"                  % "2.5",
  "javax.servlet"                %  "javax.servlet-api"          % "3.0.1"      % "provided"
)

enablePlugins(JettyPlugin)

javaOptions in Jetty ++= Seq(
  "-Xdebug",
  "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"
)