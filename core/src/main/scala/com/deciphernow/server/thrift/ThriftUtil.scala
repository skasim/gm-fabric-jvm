package com.deciphernow.server.thrift

import java.lang.reflect.{Constructor, Method}

import com.twitter.finagle.stats.{LoadedStatsReceiver, StatsReceiver}
import com.twitter.finagle.thrift.ThriftClientRequest
import com.twitter.finagle.{Service, Thrift}
import com.twitter.util.NonFatal
import org.apache.thrift.protocol.TProtocolFactory

/**
  * modified by ghershfield on 6/1/16.
  *
  * copied from: finagle-thrift/src/main/scala/com/twitter/finagle/rich.scala
  *
  * This is Twitter code and the private modifier has been removed. This
  * piece of code is covered under licensing that Twitter used
  * as of 2016-06-01. At the time it is Apache 2.0 License.
  *
  */
object ThriftUtil {
  private type BinaryService = Service[Array[Byte], Array[Byte]]

  private val thriftFinagleClientParamTypes =
    Seq(classOf[Service[_, _]], classOf[TProtocolFactory])

  private val scrooge2FinagleClientParamTypes =
    Seq(
      classOf[Service[_, _]],
      classOf[TProtocolFactory],
      classOf[Option[_]],
      classOf[StatsReceiver])

  private val scrooge3FinagleClientParamTypes =
    Seq(
      classOf[Service[_, _]],
      classOf[TProtocolFactory],
      classOf[String],
      classOf[StatsReceiver])

  def findClass1(name: String): Option[Class[_]] =
    try Some(Class.forName(name)) catch {
      case _: ClassNotFoundException => None
    }

  def findClass[A](name: String): Option[Class[A]] =
    for {
      cls <- findClass1(name)
    } yield cls.asInstanceOf[Class[A]]

  def findConstructor[A](clz: Class[A], paramTypes: Class[_]*): Option[Constructor[A]] =
    try {
      Some(clz.getConstructor(paramTypes: _*))
    } catch {
      case _: NoSuchMethodException => None
    }

  def findMethod(clz: Class[_], name: String, params: Class[_]*): Option[Method] =
    try Some(clz.getMethod(name, params:_*)) catch {
      case _: NoSuchMethodException => None
    }

  def findRootWithSuffix(str: String, suffix: String): Option[String] =
    if (str.endsWith(suffix)) Some(str.dropRight(suffix.length)) else None

  lazy val findSwiftClass: Class[_] => Option[Class[_]] = {
    val f = for {
      serviceSym <- findClass1("com.twitter.finagle.exp.swift.ServiceSym")
      meth <- findMethod(serviceSym, "isService", classOf[Class[_]])
    } yield {
      k: Class[_] =>
        try {
          if (meth.invoke(null, k).asInstanceOf[Boolean]) Some(k)
          else None
        } catch {
          case NonFatal(_) => None
        }
    }

    f getOrElse Function.const(None)
  }

  /**
    * Construct an `Iface` based on an underlying [[com.twitter.finagle.Service]]
    * using whichever Thrift code-generation toolchain is available.
    */
  def constructIface[Iface](
                             underlying: Service[ThriftClientRequest, Array[Byte]],
                             cls: Class[_],
                             protocolFactory: TProtocolFactory,
                             sr: StatsReceiver
                           ): Iface = {
    val clsName = cls.getName

    def tryThriftFinagleClient: Option[Iface] =
      for {
        baseName   <- findRootWithSuffix(clsName, "$ServiceIface")
        clientCls  <- findClass[Iface](baseName + "$ServiceToClient")
        cons       <- findConstructor(clientCls, thriftFinagleClientParamTypes: _*)
      } yield cons.newInstance(underlying, protocolFactory)

    def tryScrooge3FinagleClient: Option[Iface] =
      for {
        clientCls  <- findClass[Iface](clsName + "$FinagleClient")
        cons       <- findConstructor(clientCls, scrooge3FinagleClientParamTypes: _*)
      } yield cons.newInstance(underlying, protocolFactory, "", sr)

    def tryScrooge3FinagledClient: Option[Iface] =
      for {
        baseName   <- findRootWithSuffix(clsName, "$FutureIface")
        clientCls  <- findClass[Iface](baseName + "$FinagledClient")
        cons       <- findConstructor(clientCls, scrooge3FinagleClientParamTypes: _*)
      } yield cons.newInstance(underlying, protocolFactory, "", sr)

    def tryScrooge2Client: Option[Iface] =
      for {
        baseName   <- findRootWithSuffix(clsName, "$FutureIface")
        clientCls  <- findClass[Iface](baseName + "$FinagledClient")
        cons       <- findConstructor(clientCls, scrooge2FinagleClientParamTypes: _*)
      } yield cons.newInstance(underlying, protocolFactory, None, sr)

    def trySwiftClient: Option[Iface] =
      for {
        swiftClass <- findSwiftClass(cls)
        proxy <- findClass1("com.twitter.finagle.exp.swift.SwiftProxy")
        meth <- findMethod(proxy, "newClient",
          classOf[Service[_, _]], classOf[ClassManifest[_]])
      } yield {
        val manifest = ClassManifest.fromClass(swiftClass)
          .asInstanceOf[ClassManifest[Iface]]
        meth.invoke(null, underlying, manifest).asInstanceOf[Iface]
      }

    val iface =
      tryThriftFinagleClient orElse
        tryScrooge3FinagleClient orElse
        tryScrooge3FinagledClient orElse
        tryScrooge2Client orElse
        trySwiftClient

    iface getOrElse {
      throw new IllegalArgumentException("Iface %s is not a valid thrift iface".format(clsName))
    }
  }

  /**
    * Construct a binary [[com.twitter.finagle.Service]] for a given Thrift
    * interface using whichever Thrift code-generation toolchain is available.
    */
  def serverFromIface(
                       impl: AnyRef,
                       protocolFactory: TProtocolFactory,
                       stats: StatsReceiver,
                       maxThriftBufferSize: Int
                     ): BinaryService = {
    def tryThriftFinagleService(iface: Class[_]): Option[BinaryService] =
      for {
        baseName   <- findRootWithSuffix(iface.getName, "$ServiceIface")
        serviceCls <- findClass[BinaryService](baseName + "$Service")
        cons       <- findConstructor(serviceCls, iface, classOf[TProtocolFactory])
      } yield cons.newInstance(impl, protocolFactory)

    def tryScroogeFinagleService(iface: Class[_]): Option[BinaryService] =
      for {
        baseName   <- findRootWithSuffix(iface.getName, "$FutureIface") orElse
          Some(iface.getName)
        serviceCls <- findClass[BinaryService](baseName + "$FinagleService") orElse
          findClass[BinaryService](baseName + "$FinagledService")
        cons       <- findConstructor(serviceCls, iface, classOf[TProtocolFactory], classOf[StatsReceiver], Integer.TYPE)
      } yield cons.newInstance(impl, protocolFactory, stats, Int.box(maxThriftBufferSize))

    // The legacy $FinagleService that doesn't take stats.
    def tryLegacyScroogeFinagleService(iface: Class[_]): Option[BinaryService] =
      for {
        baseName   <- findRootWithSuffix(iface.getName, "$FutureIface") orElse
          Some(iface.getName)
        serviceCls <- findClass[BinaryService](baseName + "$FinagleService") orElse
          findClass[BinaryService](baseName + "$FinagledService")
        cons       <- findConstructor(serviceCls, iface, classOf[TProtocolFactory])
      } yield cons.newInstance(impl, protocolFactory)

    def trySwiftService(iface: Class[_]): Option[BinaryService] =
      for {
        _ <- findSwiftClass(iface)
        swiftServiceCls <- findClass1("com.twitter.finagle.exp.swift.SwiftService")
        const <- findConstructor(swiftServiceCls, classOf[Object])
      } yield const.newInstance(impl).asInstanceOf[BinaryService]

    def tryClass(cls: Class[_]): Option[BinaryService] =
      tryThriftFinagleService(cls) orElse
        tryScroogeFinagleService(cls) orElse
        tryLegacyScroogeFinagleService(cls) orElse
        trySwiftService(cls) orElse
        (Option(cls.getSuperclass) ++ cls.getInterfaces).view.flatMap(tryClass).headOption

    tryClass(impl.getClass).getOrElse {
      throw new IllegalArgumentException("argument implements no candidate ifaces")
    }
  }

  /**
    * Construct a binary [[com.twitter.finagle.Service]] for a given Thrift
    * interface using whichever Thrift code-generation toolchain is available.
    * (Legacy version for backward-compatibility).
    */
  def serverFromIface(impl: AnyRef, protocolFactory: TProtocolFactory): BinaryService = {
    serverFromIface(impl, protocolFactory, LoadedStatsReceiver, Thrift.maxThriftBufferSize)
  }
}
