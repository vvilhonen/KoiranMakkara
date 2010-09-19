KoiranMakkara
=============

Running
-------

Set your imap server where patches are sent by email to review.url system property  
example: -Dreview.url=imaps://user:pass@imap.gmail.com/INBOX

  `sbt update`  
  `sbt run`

Stand-alone running
-------------------

KoiranMakkara has proguard configured for sbt that lets you build
stand-alone java jar to ease deployment anywhere

  `sbt proguard`

Jar is now generated to target/scala_2.8.0
