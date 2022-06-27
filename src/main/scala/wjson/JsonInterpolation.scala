package wjson

import scala.collection.SortedMap

/**
 * Created by wangzx on 15/7/6.
 */

class JsonInterpolation(sc: StringContext) {

  def apply(args: JsValue*): JsValue =
    new JsonParser(ParserInput(sc, args), true).parseJsValue()

  def unapplySeq(input: JsValue): Option[Seq[JsValue]] = {

    val placeHolders: Seq[JsNumber] = Seq.range(0, sc.parts.length-1).map(x => JsNumber(Integer.MAX_VALUE - x) )

    val pi = ParserInput(sc, placeHolders)
    val pattern = new JsonParser(pi, true).parseJsValue()

    val results = collection.mutable.ArrayBuffer[JsValue]()
    Seq.range(0, sc.parts.length-1).foreach { x => results += null }

    try {
      patternMatch(pattern, input, placeHolders, results)
      Some(results.toSeq)
    }
    catch {
      case ex: Throwable => None
    }

  }

  // TODO report friendly
  private def patternMatch(pattern: JsValue, input: JsValue, placeHolders: Seq[JsValue],
                           results: collection.mutable.ArrayBuffer[JsValue]): Unit = {

    def isPlaceHolder(value: JsNumber) = {
      val num = value.value.toInt
      val index = Integer.MAX_VALUE - num.toInt
      num > 0 && index < placeHolders.size && placeHolders(index).eq(value)
    }

    pattern match {
      case x: JsObject =>
        assert( input.isInstanceOf[JsObject] )
        val inputObj = input.asInstanceOf[JsObject]
        x.fields.foreach {
          case (key, n @ JsNumber(num)) if isPlaceHolder(n) =>
            val index = Integer.MAX_VALUE - num.toInt
            assert(inputObj.fields contains key)
            results(index) = inputObj.fields(key)

          case (key, value) =>
            assert(inputObj.fields contains key)
            patternMatch(value, inputObj.fields(key), placeHolders, results)
        }
      case x: JsArray =>
        assert(input.isInstanceOf[JsArray])
        assert(input.asInstanceOf[JsArray].elements.size >= x.elements.size)
        val inputArr = input.asInstanceOf[JsArray]
        x.elements.zipWithIndex.foreach {
          case (x: JsNumber, y: Int) if isPlaceHolder(x) =>
            val index = Integer.MAX_VALUE - x.value.toInt
            results(index) = inputArr.elements(y)
          case (x: JsValue,y: Int)=>
            patternMatch(x, inputArr.elements.apply(y), placeHolders, results)
        }
      case x: JsString =>
        assert(x == input)
      case x: JsBoolean =>
        assert(x == input)
      case x: JsNumber =>
        assert(x == input)
      case x@ JsNull =>
        assert(x == input)
    }
  }


}