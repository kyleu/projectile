package models.input

abstract class Input() extends Ordered[Input] {
  def t: InputTemplate
  def key: String
  def title: String
  def description: String

  override def compare(that: Input) = title.compare(that.title)
}
