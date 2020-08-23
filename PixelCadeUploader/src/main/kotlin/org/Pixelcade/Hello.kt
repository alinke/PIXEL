package org.Pixelcade

import com.jcraft.jsch.*
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.jar.JarFile
import kotlin.system.exitProcess


var retries = 0
val host = "pixelcadedx.local"
val user = "pi"
val args = arrayOf<String>()
val password = "flynn-lives"
val command = "killall java && unzip -qqoXK package.pxl && rm package.pxl && sudo chmod +x pixelcade/pixelweb && pixelcade/pixelweb & exit\n"

fun main(args:Array<String>?) {
    /* if (args != null && args.count() == 0) {
        println("Usage:")
        println("PixelcadeLCDUploader <path to folder of jpeg marquee images>")
        exitProcess(0)
    } */

    if (retries > 4){
        println("Your PixelcadeLCD could not be found.")
        println("Please check your connections and try again.")
        exitProcess(-1)
    }

    retries += 1
    try {
        val jsch = JSch()
        val session = jsch.getSession(user, host, 22)
        session.userInfo = org.Pixelcade.SSHRemoteExampleUserInfo(user, password)
        var progress : progressMonitor
        session.connect()
        val channel = session.openChannel("shell")
        if (args != null && args.count() > 0) {
            println("PixelcadeLCD Artwork Uploader")
        }else{
            println("PixelcadeLCD Firmware Updater 1.0.25")
        }

        println("Updating Your PixelcadeLCD, Please Wait...")

        try {

            if (args != null) {
                val stuff = args!!
                if (stuff.count() > 0){
                    println("Uploader Mode")
                    println("Instead of updating your PixelcadeLCD, the Updater is checking")
                    println("if the option(s) supplied are folders of jpegs, and if so, updating")
                    println("your PixelcadeLCD with them, then will quit.")
                    for (arg in stuff) {
                        val file = File(arg)
                        val filter = FilenameFilter { f, name -> // We want to find only .c files
                            name.endsWith(".jpg")
                        }

                        if (file.exists() && file.isDirectory){
                            val sftpIChannel = session.openChannel("sftp") as ChannelSftp
                            sftpIChannel.connect()

                            for (marquee in file.listFiles(filter)){
                                if (marquee.isFile ){ //!marquee.absolutePath.contains(" ")
                                    println(marquee.absolutePath)
                                    progress = progressMonitor(marquee.totalSpace)
                                    try {
                                        sftpIChannel.put(marquee.inputStream(), "/home/pi/pixelcade/lcdmarquees/${marquee.name}", progress)
                                    }catch (sx: SftpException){
                                        println(sx.localizedMessage)
                                    }

                                }
                            }
                            sftpIChannel.disconnect()
                            exitProcess(0)
                        }
                    }
                }
            }



            val jarFile = JarFile(File(progressMonitor::class.java.protectionDomain.codeSource.location
                    .toURI()).path);
            val size = jarFile.getEntry("package.pxl").size;
            println("Size: $size")
            progress = progressMonitor(size)

            val sftpChannel = session.openChannel("sftp") as ChannelSftp
            sftpChannel.connect()
            sftpChannel.put(object {}.javaClass.getResource("/package.pxl").openStream(), "/home/pi/package.pxl", progress)

            sftpChannel.disconnect()
            print("\nFinishing...")
        } catch (ioEx: FileNotFoundException) {
            print("Your PixelcadeLCD did not connect...retrying...\n")
            Thread.sleep(500)
            session.disconnect()
            main(null)
        } catch (illEx: IllegalStateException){
            println("No Firmware Update!")
            println("Usage:")
            println("PixelcadeLCDUploader <path to folder of jpeg marquee images>")
            exitProcess(-1)
        }
        channel.inputStream = ByteArrayInputStream(command.toByteArray(StandardCharsets.UTF_8))
        channel.outputStream = System.out
        val `in` = channel.inputStream
        val outBuff = StringBuilder()
        var exitStatus: Int;
        channel.connect()
        while (true) {
            var c: Int
            while (`in`.read().also { c = it } >= 0) {
                outBuff.append(c.toChar())
            }
            if (channel.isClosed) {
                if (`in`.available() > 0) continue
                exitStatus = channel.exitStatus
                break
            }
        }

        channel.disconnect()
        session.disconnect()
        if (exitStatus == 0) {
            println("Update Complete.")
        } else {
            print("Your PixelcadeLCD did not connect...retrying...\n")
            main(null)
        }


    } catch (ioEx: IOException) {
        print("Your PixelcadeLCD did not connect...retrying...\n")
        main(null)
    } catch (ioEx: JSchException) {
        print("Your PixelcadeLCD did not connect...retrying...\n")
        main(null)
    }
}

internal class SSHRemoteExampleUserInfo(userName: String?, private val pwd: String) : UserInfo {
    //private val userName = userName
    override fun getPassphrase(): String {
        throw UnsupportedOperationException("getPassphrase Not supported yet.")
    }

    override fun getPassword(): String {
      // println("$userName")
        return pwd
    }

    override fun promptPassword(string: String): Boolean {
        /*mod*/
        return true
    }

    override fun promptPassphrase(string: String): Boolean {
        throw UnsupportedOperationException("promptPassphrase Not supported yet.")
    }

    override fun promptYesNo(string: String): Boolean {
        /*mod*/
        return true
    }

    override fun showMessage(string: String) {}
}

class progressMonitor(maxSize: Long)  // If you need send something to the constructor, change this method
    : SftpProgressMonitor {
    private var max: Long = maxSize
    private var count: Long = 0
    private var percent: Long = 0

    //private val callbacks: CallbackContext? = null
    override fun init(op: Int, src: String, dest: String, max: Long) {
        //this.max = max
        println("Preparing...")
    }

    override fun count(bytes: Long): Boolean {
        count += bytes
        val percentNow = count * 100 / max
        if (percentNow > percent) {
            percent = percentNow
            print("|") // Progress 0,0
        }
        return true
    }

    override fun end() {
    }
}

