package com.xingyang.data.websocket

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatWebSocket(serverUri: URI, private val token: String) : WebSocketClient(serverUri) {
    
    private val _messageFlow = MutableStateFlow<ChatMessage?>(null)
    val messageFlow = _messageFlow.asStateFlow()

    override fun onOpen(handshakedata: ServerHandshake?) {
        // Send authentication
        send("""{"type":"auth","token":"$token"}""")
    }

    override fun onMessage(message: String) {
        // Parse message and emit to flow
        try {
            val chatMessage = parseChatMessage(message)
            _messageFlow.value = chatMessage
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        // Handle reconnection logic
    }

    override fun onError(ex: Exception?) {
        ex?.printStackTrace()
    }

    fun sendMessage(message: ChatMessage) {
        val json = message.toJson()
        send(json)
    }

    private fun parseChatMessage(json: String): ChatMessage {
        // TODO: Implement JSON parsing
        return ChatMessage("", "", "", "", emptyList())
    }
}

data class ChatMessage(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val mediaUrls: List<String>,
    val type: String = "text",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): String {
        // TODO: Implement JSON serialization
        return ""
    }
}
