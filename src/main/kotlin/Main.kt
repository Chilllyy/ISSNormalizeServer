package me.chillywilly

import com.electronwill.nightconfig.core.file.CommentedFileConfig
import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.LightstreamerClient
import com.lightstreamer.client.Subscription
import com.lightstreamer.client.SubscriptionListener
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread.sleep
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.util.Scanner
import kotlin.system.exitProcess

var value = -1.0F
var LOG: Logger = LoggerFactory.getLogger("ISSNormalizerServer")
var connection: Connection? = null

fun main() {
    if (!File("./config.toml").exists()) {
        val inputStream = object {}.javaClass.getResourceAsStream("/config.toml")
        ?: throw IllegalArgumentException("Resource not found: /config.toml")

        val destFile = File("./config.toml")

        inputStream.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
    }
    var config = CommentedFileConfig.builder("./config.toml").defaultResource("defaultconfig.toml").autosave().build()
    config.load()

    val DB_URL: String = config.get("db_url")
    val DB_PORT: Int = config.get("db_port")
    val DB_USERNAME: String = config.get("db_username")
    val DB_PASSWORD: String = config.get("db_password")
    val DB_DATABASE: String = config.get("db_database")
    try {
        connection = connectToDB(DB_URL, DB_PORT, DB_USERNAME, DB_PASSWORD, DB_DATABASE)
    } catch (e: Exception) {
        LOG.error("Unable to connect to DB", e);
        return;
    }

    config.close()

    val scanner = Scanner(System.`in`)
    LOG.info("Starting normalizer server")
    val client = LightstreamerClient("https://push.lightstreamer.com", "ISSLIVE")
    client.connect()
    LOG.info("Connected to Lightstreamer ${client.status}")

    val sub = Subscription("MERGE", arrayOf("NODE3000005"), arrayOf("Value"))
    sub.requestedSnapshot = "yes"
    client.subscribe(sub)
    sub.addListener(SubListener())
    LOG.info("Added Lightstreamer Subscription")

    val app = Javalin.create { config ->
        config.http.asyncTimeout = 10000L
        config.routes.get("/") { ctx ->
            run {
                LOG.info("Received Request for value")

                ctx.json(mapOf("value" to value.toDouble()))
            }
        }
        config.staticFiles.add("/static", Location.CLASSPATH)
    }.start("0.0.0.0", 7000)
    dbUpdate()
    while (true) {
        val inp = scanner.nextLine()
        if (inp.lowercase() == "stop" || inp.lowercase() == "exit") {
            break
        }
        val num = inp.toFloatOrNull()
        if (num != null) {
            value = num
            LOG.info("Changed value to $value")
        }
        sleep(10)
    }

    app.stop()
    exitProcess(0)
}

fun dbUpdate() = runBlocking {
    launch (Dispatchers.Default) {
        delay(10000) //10 second delay on startup
        while (isActive) {
            LOG.info("Inserting value into DB (" + value + ")")
            updatevalue()
            delay(60 * 1000) //Post Datapoint every minute
        }
    }
}

fun connectToDB(url: String, port: Int, username: String, password: String, database: String): Connection {
    Class.forName("com.mysql.cj.jdbc.Driver")
    var connection_url = "jdbc:mysql://" + url + ": " + port + "/" + database
    var connection: Connection = DriverManager.getConnection(
        connection_url, username, password
    )

    return connection
}

fun updatevalue() {
    var query = "INSERT INTO issdatamodels (timestamp, value) VALUES (CURRENT_TIMESTAMP(), " + value + ")"
    var statement = connection?.createStatement()
    statement?.executeUpdate(query)
    LOG.info("Inserted new value into DB")
}

class SubListener : SubscriptionListener {
    override fun onClearSnapshot(itemName: String?, itemPos: Int) {
    }

    override fun onCommandSecondLevelItemLostUpdates(lostUpdates: Int, key: String) {
    }

    override fun onCommandSecondLevelSubscriptionError(code: Int, message: String?, key: String?) {
    }

    override fun onEndOfSnapshot(itemName: String?, itemPos: Int) {
    }

    override fun onItemLostUpdates(itemName: String?, itemPos: Int, lostUpdates: Int) {
    }

    override fun onItemUpdate(itemUpdate: ItemUpdate) {
        val newValue = itemUpdate.getValue("Value")?.toFloatOrNull() ?: value
        if (newValue != value) {
            value = newValue;
            LOG.info("Received New Value from Lightstreamer: ${newValue}");
        }
    }

    override fun onListenEnd() {
    }

    override fun onListenStart() {
    }

    override fun onSubscription() {
    }

    override fun onSubscriptionError(code: Int, message: String?) {
    }

    override fun onUnsubscription() {
    }

    override fun onRealMaxFrequency(frequency: String?) {
    }
}