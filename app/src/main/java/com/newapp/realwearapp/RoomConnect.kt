package com.newapp.realwearapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import fi.vtt.nubomedia.kurentoroomclientandroid.KurentoRoomAPI
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomError
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomListener
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomNotification
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomResponse
import fi.vtt.nubomedia.utilitiesandroid.LooperExecutor
import timber.log.Timber

class RoomConnect : Activity(), RoomListener {
    private var executor: LooperExecutor? = null

    override fun onCreate(savedInstanceState: Bundle?) { //액티비티가 최초로 시작될 때 필요한 초기화 작업
        super.onCreate(savedInstanceState)

        // LooperExecutor 초기화: WebSocket 작업을 백그라운드에서 처리하는 스레드 시작
        executor = LooperExecutor() // LooperExecutor 객체 생성
        executor!!.requestStart() // LooperExecutor 시작 요청

        // WebSocket을 통한 KurentoRoomAPI 연결 설정
        // KurentoRoomAPI 객체를 생성하고 WebSocket 서버에 연결을 시도
        val wsRoomUri = "wss://mykurentoserver:1234/room" // Kurento 미디어 서버의 WebSocket 주소
        kurentoRoomAPI = KurentoRoomAPI(executor, wsRoomUri, this) // KurentoRoomAPI 객체 초기화
        kurentoRoomAPI!!.connectWebSocket() // WebSocket 연결 요청
    }

    // 서버에 연결되었을 때 호출되는 메소드
    override fun onRoomConnected() {
        // WebSocket 연결이 성공적으로 완료되었음을 로그로 출력
        Timber.tag(TAG).d("WebSocket 연결 완료!")

        // 방에 참여 요청: 특정 방에 사용자 이름과 함께 참여 요청
        kurentoRoomAPI!!.sendJoinRoom("myUserName", "myRoomName", true, 123) // 방 참여 요청 전송
    }

    // 방 참여 응답 처리: 서버로부터 받은 방 참여 요청에 대한 응답을 처리
    override fun onRoomResponse(response: RoomResponse) {
        Timber.tag(TAG).d("RoomResponse: " + response) // 응답 로그 출력

        // 참여 요청에 대한 응답 ID가 일치하는지 확인
        if (response.id == 123) {
            // 방에 성공적으로 연결되었음을 로그로 출력
            Timber.tag(TAG).d("방에 성공적으로 연결되었습니다!")

            // 방에 있는 모든 사용자에게 메시지 전송 요청
            kurentoRoomAPI!!.sendMessage("myRoomName", "myUserName", "안녕하세요, 방!", 125) // 메시지 전송 요청
        } else if (response.id == 125) {
            // 메시지 전송 요청에 대한 응답이 수신되었을 때
            Timber.tag(TAG).d("서버가 메시지를 수신했습니다!") // 메시지 수신 확인 로그 출력
        }
    }

    // 방 알림 처리 (메시지 수신): 방에서 발생하는 이벤트(메시지 수신 등)를 처리합니다.
    override fun onRoomNotification(notification: RoomNotification) {
        // 메시지 수신 이벤트를 처리
        if (notification.method == RoomListener.METHOD_SEND_MESSAGE) { // 메시지 수신 메소드 확인
            // 메시지를 보낸 사용자 이름과 메시지 내용을 추출
            val username = notification.getParam("user").toString() // 사용자 이름 추출
            val message = notification.getParam("message").toString() // 메시지 내용 추출
            Timber.tag(TAG).d(username + "로부터 메시지를 받았습니다: " + message) // 수신 메시지 로그 출력
        }
    }

    // 오류 처리: 서버와의 통신 중 발생한 오류를 처리
    override fun onRoomError(error: RoomError) {
        Timber.tag(TAG).d("RoomError: " + error) // 오류 로그 출력

        // 특정 오류 코드에 대한 추가 처리
        if (error.code == RoomError.Code.EXISTING_USER_IN_ROOM_ERROR_CODE.value) { // 중복 사용자 오류 확인
            Timber.tag(TAG).d("이미 같은 이름의 사용자가 방에 있습니다!") // 중복 사용자 오류 로그 출력
        }
    }

    // 방에서 연결이 끊어졌을 때 호출되는 메소드: 방 연결이 끊어졌을 때 처리
    override fun onRoomDisconnected() {
        Timber.tag(TAG).d("연결이 끊겼습니다.") // 연결 끊김 로그 출력
    }

    override fun onDestroy() {
        // 방에서 나가기 요청을 전송: 액티비티가 종료될 때 방에서 나가는 요청
        kurentoRoomAPI!!.sendLeaveRoom(131) // 방 나가기 요청 전송
        super.onDestroy() // 상위 클래스의 onDestroy 호출
    }

    companion object {
        private const val TAG = "MainActivity" // 로그 태그로 사용할 문자열
        private var kurentoRoomAPI: KurentoRoomAPI? = null // KurentoRoomAPI 인스턴스
    }
}
