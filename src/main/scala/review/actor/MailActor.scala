package review.actor

import scala.actors.Actor
import scala.actors.Actor._
import com.sun.mail.imap.IMAPFolder
import javax.mail.{Session, URLName, Folder, Message}
import javax.mail.event.{MessageCountListener, MessageCountEvent}
import org.apache.commons.io.IOUtils
import review.model.Review

object MailActor extends Actor {
  lazy val folder = {
    println("creating folder")
    val store = getStore
    store.connect
    val folder = store.getFolder("INBOX").asInstanceOf[IMAPFolder]
    folder.open(Folder.READ_ONLY)
    folder.addMessageCountListener(new MessageCountListener() {
      override def messagesAdded(e: MessageCountEvent) =
        onNewMessage(e)
      override def messagesRemoved(e: MessageCountEvent) {}
    });
    folder
  }

  def getStore = {
    val sess = Session.getDefaultInstance(System.getProperties)
    val mailConf = System.getProperty("review.url")
    if (mailConf == null) throw new RuntimeException("Missing review.url system property (example: imaps://user:pass@imap.gmail.com/INBOX)")
    val urlName = new URLName(mailConf)
    sess.getStore(urlName)
  }
  
  def onNewMessage(e: MessageCountEvent) {
    e.getMessages.foreach(handleNewMessage(_))
  }

  def handleNewMessage(m: Message) {
    println("Handling message")
    if (fromGit(m)) {
      val body  = m.getContent.toString
      val from = m.getFrom()(0).toString
      val r = Review.create.author(from).subject(m.getSubject).timestamp(new java.util.Date).diff(body)
      r.save
      PatchServer ! NewPatch(r)
    }
  }

  def fromGit(m: Message) = {
    val values = m.getHeader("X-Mailer")
    values.length == 1 && values(0).indexOf("git-send-email") != -1
  }

  def act = loop {
    folder.idle
  }
}
