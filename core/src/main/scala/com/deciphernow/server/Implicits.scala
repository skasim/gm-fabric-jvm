package com.deciphernow.server

import java.io.File

import com.twitter.app.Flaggable


object Implicits {
  implicit val fileFlaggable: Flaggable[File] = new Flaggable[File] {
    override def default = None
    def parse(s: String) = new File(s)
    override def show(file: File) = file.toString
  }

  implicit val fileOptionFlaggable: Flaggable[Option[File]] = new Flaggable[Option[File]] {
    override def default = None
    def parse(s: String) = if (s != null && !s.trim.isEmpty) Some(new File(s)) else None
    override def show(file: Option[File]) = file.toString
  }
}
