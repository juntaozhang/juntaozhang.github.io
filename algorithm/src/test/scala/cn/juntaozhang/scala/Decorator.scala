package cn.juntaozhang.scala

trait OutputStream {
  def write(b: Byte)
}

class FileOutputStream(path: String) extends OutputStream {
  override def write(b: Byte): Unit = {
    println("FileOutputStream")
  }
}

trait Buffering extends OutputStream {
  abstract override def write(b: Byte) {
    println("Buffering")
    super.write(b)
  }
}

object Decorator extends App {
  val fos = new FileOutputStream("foo.txt")
  fos.write('h')
  val fos2 = new FileOutputStream("foo.txt") with Buffering
  fos2.write('h')
}
