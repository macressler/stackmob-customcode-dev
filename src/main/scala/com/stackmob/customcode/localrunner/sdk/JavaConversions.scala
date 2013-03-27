/**
 * Copyright 2011 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.customcode.localrunner.sdk

import collection.JavaConversions._
import java.util.{ArrayList => JArrayList, HashMap => JHashMap, List => JList, Map => JMap}

object JavaConversions {

  implicit def mapWithListAsScala[A, B](jm:JMap[A, JList[B]]):Map[A, List[B]] = {
    var scala = Map[A, List[B]]()
    for((k, v) <- mapAsScalaMap(jm)) {
      scala = scala + ((k, asScalaBuffer(v).toList))
    }
    scala
  }

  implicit def mapWithListAsJava[A, B](m:Map[A, List[B]]):JMap[A, JList[B]] = {
    val java = new JHashMap[A, JList[B]]()
    for((k, v) <- m) {
      java.put(k, seqAsJavaList(v))
    }
    java
  }

  implicit def listWithMapAsScala[A, B](l:JList[JMap[A, B]]):List[Map[A, B]] = {
    var scala = List[Map[A, B]]()
    for(elt:JMap[A, B] <- asScalaBuffer(l).toList) {
      scala = scala ++ List(mapAsScalaMap(elt).toMap)
    }
    scala
  }

  implicit def listWithMapAsJava[A, B](l:List[Map[A, B]]):JList[JMap[A, B]] = {
    val java = new JArrayList[JMap[A, B]]()
    for(map <- l) {
      java.add(mapAsJavaMap(map))
    }
    java
  }

  implicit def mapAsScalaImmutable[A, B](m:JMap[A, B]):Map[A, B] = mapAsScalaMap(m).toMap

  implicit def listAsScalaList[T](l:JList[T]):List[T] = asScalaBuffer(l).toList
}