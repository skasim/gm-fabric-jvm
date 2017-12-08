package com.deciphernow.server

/*
    Copyright 2018 Decipher Technology Studios LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

/**
  * @author garth.hershfield
  *
  */
object ConfigUtilities {

  /**
    *
    * @param value
    * @return
    */
  def notNullOrBlank(value: String) = (value != null && !value.trim.isEmpty())

  /**
    *
    * @param values
    * @return
    */
  def pickValue(values: List[String]) : String = values.find(notNullOrBlank(_)).getOrElse("")

  /**
    * Retrieve the value from the environment variable. Return empty String if None.
    * @param env
    * @return
    */
  def envValue(env: Option[String]) = env.flatMap{value => sys.env.get(value)}.getOrElse("")

  /**
    * Try expanding the value is if it is an env and expand it. If not env, then assume it's a static
    * defined value and return that.
    *
    * @param env
    * @return
    */
  def selectValue(env: String) : String = selectValue(Option(env))

  def selectValue(env: Option[String]) = {
    val v = envValue(env)
    if (v.trim.length == 0) env.get
    else v
  }


}
