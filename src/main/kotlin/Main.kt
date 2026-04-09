package me.chillywilly

import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.LightstreamerClient
import com.lightstreamer.client.Subscription
import com.lightstreamer.client.SubscriptionListener
import io.javalin.Javalin
import org.slf4j.LoggerFactory

var value = -1.0F
var LOG = LoggerFactory.getLogger("ISSNormalizerServer")

fun main() {
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
                ctx.result(value.toDouble().toString())
            }
        }
    }.start(7000)
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