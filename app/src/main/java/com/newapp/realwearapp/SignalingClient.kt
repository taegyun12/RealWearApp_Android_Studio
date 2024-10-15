package com.newapp.realwearapp

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class SignalingClient(private val signalingUrl: String, private val onMessage: (JSONObject) -> Unit) {
    private lateinit var socket: Socket // Socket 객체 초기화 준비

    fun connect() { // 서버에 연결하는 함수
        socket = IO.socket(signalingUrl) // signalingUrl을 사용하여 Socket 객체 생성
        socket.connect() // 서버에 WebSocket 연결 요청

        socket.on("offer") { args -> // "offer" 이벤트 수신 처리
            val data = args[0] as JSONObject // 수신된 데이터를 JSONObject로 변환
            onMessage(data) // 수신된 데이터를 콜백 함수로 전달
        }

        socket.on("answer") { args -> // "answer" 이벤트 수신 처리
            val data = args[0] as JSONObject // 수신된 데이터를 JSONObject로 변환
            onMessage(data) // 수신된 데이터를 콜백 함수로 전달
        }

        socket.on("ice-candidate") { args -> // "ice-candidate" 이벤트 수신 처리
            val data = args[0] as JSONObject // 수신된 데이터를 JSONObject로 변환
            onMessage(data) // 수신된 데이터를 콜백 함수로 전달
        }
    }

    fun sendMessage(event: String, message: JSONObject) { // 메시지를 서버로 전송하는 함수
        socket.emit(event, message) // 지정된 이벤트와 메시지를 서버로 전송
    }

    fun disconnect() { // 서버와의 연결을 종료하는 함수
        socket.disconnect() // WebSocket 연결 종료
    }
}
