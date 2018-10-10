package models.input

abstract class Input() extends Ordered[Input] {
  def template: InputTemplate
  def key: String
  def title: String
  def description: String

  override def compare(that: Input) = title.compare(that.title)
}
