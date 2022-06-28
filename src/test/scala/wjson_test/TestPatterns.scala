package wjson_test

import org.scalatest.funsuite.AnyFunSuite
import wjson.*

class TestPatterns extends AnyFunSuite {

  test("simple patterns") {
    val js = json"""{
      "a": 1,
      "b": "hello",
      "c": true
    }"""

    js match {
      case rejson"""{
        "a": ${a}@integer,
        "b": ${b}@string,
        "c": ${c}@boolean
      }""" =>
        println(s"a=$a, b=$b, c=$c")
    }

  }

}