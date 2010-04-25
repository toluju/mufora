package com.mufora

class Ipsum {
  val LOREM_IPSUM = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
                    "sed diam nonumy eirmod tempor invidunt ut labore et " +
                    "dolore magna aliquyam erat, sed diam voluptua. At vero " +
                    "eos et accusam et justo duo dolores et ea rebum. Stet " +
                    "clita kasd gubergren, no sea takimata sanctus est Lorem " +
                    "ipsum dolor sit amet."

  var loremIpsumWords = LOREM_IPSUM.split("\\s")

  def words:String = { words(50) }

  def words(num:int):String = { words(num, 0) }

  def words(num:int, offset:int):String = {
    if (offset < 0 || offset > 49) {
      throw new IndexOutOfBoundsException("offset must be >= 0 and < 50")
    }

    var word = offset
    var lorem = new StringBuilder()

    for (i <- 0 until num - 1) {
      lorem.append(loremIpsumWords(word) + " ")
      word = (word + 1) % 50
    }

    lorem.append(loremIpsumWords(word))
    return lorem.toString
  }

  def paragraphs:String = { paragraphs(2) }

  def paragraphs(num:int):String = {
    var lorem = new StringBuilder()

    for (i <- 0 until num - 1) {
      lorem.append(LOREM_IPSUM + "\n\n")
    }

    lorem.append(LOREM_IPSUM)
    return lorem.toString
  }
}