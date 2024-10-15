package com.newapp.realwearapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import org.webrtc.SurfaceViewRenderer

class VideoActivity : AppCompatActivity() {
    private lateinit var localView: SurfaceViewRenderer // 로컬 비디오 출력을 위한 SurfaceViewRenderer 객체 초기화 준비
    private lateinit var webRTCManager: WebRTCManager // WebRTCManager 객체 초기화 준비
    private lateinit var signalingClient: SignalingClient // SignalingClient 객체 초기화 준비

    override fun onCreate(savedInstanceState: Bundle?) { // 액티비티가 최초로 생성될 때 호출되는 함수
        super.onCreate(savedInstanceState) // 상위 클래스의 onCreate 호출
        setContentView(R.layout.activity_video) // 레이아웃 파일 설정

        // UI 설정
        localView = findViewById(R.id.local_view) // 레이아웃에서 로컬 비디오 뷰 초기화
        webRTCManager = WebRTCManager() // WebRTCManager 객체 생성
        webRTCManager.initialize(this) // WebRTCManager 초기화

        // Signaling 서버 연결
        signalingClient = SignalingClient("ws://your-sfu-server", ::onMessageReceived) // SignalingClient 객체 생성 및 콜백 설정
        signalingClient.connect() // Signaling 서버에 연결

        // 비디오 출력 설정
        localView.init(webRTCManager.getEglBaseContext(), null) // 비디오 출력을 위한 EGL 컨텍스트 초기화
        localView.setMirror(true) // 로컬 비디오 뷰를 미러링 모드로 설정

        // 로컬 미디어 설정
        val localMediaStream = webRTCManager.createLocalMediaStream(this) // 로컬 미디어 스트림 생성
        val videoTrack = webRTCManager.getLocalVideoTrack() // 로컬 비디오 트랙 가져오기
        videoTrack?.addSink(localView) // 로컬 비디오 트랙을 로컬 비디오 뷰에 연결
    }

    private fun onMessageReceived(data: JSONObject) { // 메시지를 수신했을 때 호출되는 콜백 함수
        // Offer/Answer 처리
    }

    override fun onDestroy() { // 액티비티가 종료될 때 호출되는 함수
        super.onDestroy() // 상위 클래스의 onDestroy 호출
        localView.release() // SurfaceViewRenderer 자원 해제
        signalingClient.disconnect() // Signaling 서버와의 연결 종료
    }
}
