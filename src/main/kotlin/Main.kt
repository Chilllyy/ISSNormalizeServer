package me.chillywilly

import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.LightstreamerClient
import com.lightstreamer.client.Subscription
import com.lightstreamer.client.SubscriptionListener
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.util.Scanner
import kotlin.system.exitProcess

var value = -1.0F
var LOG = LoggerFactory.getLogger("ISSNormalizerServer")

fun main() {
    var scanner = Scanner(System.`in`)
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

                ctx.json(mapOf("value" to value.toDouble()));
            }
        }
        config.staticFiles.add("/static", Location.CLASSPATH);
    }.start("0.0.0.0", 7000);
    while (true) {
        var inp = scanner.nextLine();
        if (inp.lowercase() == "stop" || inp.lowercase() == "exit") {
            break;
        }
        var num = inp.toFloatOrNull()
        if (num != null) {
            value = num
            LOG.info("Changed value to $value")
        }
        sleep(10)
    }

    app.stop()
    exitProcess(0)
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
        var newValue = itemUpdate.getValue("Value")?.toFloatOrNull() ?: value
        value = newValue
        LOG.info("Received New Value from Lightstreamer: ${newValue}")
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