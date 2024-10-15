package com.newapp.realwearapp

import android.content.Context
import org.webrtc.*
import org.webrtc.PeerConnection
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class WebRTCManager {
    private lateinit var peerConnectionFactory: PeerConnectionFactory // PeerConnectionFactory 객체 초기화 준비
    private lateinit var rootEglBase: EglBase // EglBase 객체 초기화 준비 (비디오 처리를 위한 EGL 컨텍스트 관리)
    private lateinit var videoTrack: VideoTrack // 로컬 비디오 트랙 초기화 준비
    private lateinit var audioTrack: AudioTrack // 로컬 오디오 트랙 초기화 준비
    private lateinit var localMediaStream: MediaStream // 로컬 미디어 스트림 초기화 준비
    private var peerConnection: PeerConnection? = null // PeerConnection 객체 초기화 준비

    fun initialize(context: Context) { // WebRTC 초기화 작업
        // EGL 초기화 (비디오 처리용)
        rootEglBase = EglBase.create() // EglBase 객체 생성

        // PeerConnectionFactory 초기화
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions() // 초기화 옵션 생성
        PeerConnectionFactory.initialize(initializationOptions) // PeerConnectionFactory 초기화

        // PeerConnectionFactory 빌드
        val options = PeerConnectionFactory.Options() // 옵션 설정
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options) // 옵션 적용
            .createPeerConnectionFactory() // PeerConnectionFactory 생성
    }

    fun createLocalMediaStream(context: Context): MediaStream { // 로컬 미디어 스트림 생성 작업
        // 카메라와 마이크를 사용해 로컬 미디어 스트림 생성
        val videoCapturer = createCameraCapturer(Camera2Enumerator(context)) // 카메라 캡처 객체 생성
        val videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast) // 비디오 소스 생성
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext) // SurfaceTextureHelper 생성
        videoCapturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver) // 비디오 캡처 초기화
        videoCapturer.startCapture(1280, 720, 30) // 비디오 캡처 시작 (해상도: 1280x720, 프레임 속도: 30fps)

        videoTrack = peerConnectionFactory.createVideoTrack("VIDEO_TRACK_ID", videoSource) // 비디오 트랙 생성

        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints()) // 오디오 소스 생성
        audioTrack = peerConnectionFactory.createAudioTrack("AUDIO_TRACK_ID", audioSource) // 오디오 트랙 생성

        localMediaStream = peerConnectionFactory.createLocalMediaStream("LOCAL_STREAM") // 로컬 미디어 스트림 생성
        localMediaStream.addTrack(videoTrack) // 로컬 미디어 스트림에 비디오 트랙 추가
        localMediaStream.addTrack(audioTrack) // 로컬 미디어 스트림에 오디오 트랙 추가

        return localMediaStream // 로컬 미디어 스트림 반환
    }

    private fun createCameraCapturer(enumerator: Camera2Enumerator): VideoCapturer { // 카메라 캡처 객체 생성 작업
        val deviceNames = enumerator.deviceNames // 사용 가능한 카메라 장치 이름 목록 가져오기
        for (deviceName in deviceNames) { // 모든 카메라 장치 이름 순회
            if (enumerator.isBackFacing(deviceName)) { // 후면 카메라 확인
                val videoCapturer = enumerator.createCapturer(deviceName, null) // 후면 카메라 캡처 객체 생성
                if (videoCapturer != null) { // 캡처 객체가 생성되었는지 확인
                    return videoCapturer // 캡처 객체 반환
                }
            }
        }
        throw RuntimeException("No front-facing camera found") // 전면 카메라가 없는 경우 예외 발생
    }

    fun getEglBaseContext(): EglBase.Context = rootEglBase.eglBaseContext // EglBase의 컨텍스트 반환

    fun createPeerConnection(iceServers: List<PeerConnection.IceServer>, onIceCandidate: (IceCandidate) -> Unit) { // PeerConnection 생성 작업
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers) // RTC 구성 설정
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer { // PeerConnection 생성 및 Observer 설정
            override fun onIceCandidate(candidate: IceCandidate) { // ICE 후보가 발견되었을 때 호출
                onIceCandidate(candidate) // ICE 후보 콜백 호출
            }

            override fun onAddStream(stream: MediaStream) { // 원격 스트림이 추가될 때 호출
                // 원격 스트림 처리
            }

            override fun onRemoveStream(stream: MediaStream) { // 원격 스트림이 제거될 때 호출
                // 원격 스트림 제거 처리
            }

            override fun onSignalingChange(state: PeerConnection.SignalingState) {} // 시그널링 상태 변경 처리
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {} // ICE 연결 상태 변경 처리
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {} // ICE 수집 상태 변경 처리
            override fun onDataChannel(channel: DataChannel) {} // 데이터 채널 이벤트 처리
            override fun onRenegotiationNeeded() {} // 재협상 필요 시 처리

            override fun onIceConnectionReceivingChange(receiving: Boolean) { // ICE 연결 수신 상태 변경 처리
                // ICE 연결 수신 상태 처리
            }

            override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) { // ICE 후보가 제거될 때 호출
                // ICE 후보 제거 처리
            }
        })
    }

    fun setRemoteDescription(description: SessionDescription) { // 원격 설명 설정 작업
        peerConnection?.setRemoteDescription(object : SdpObserver { // 원격 설명 설정
            override fun onSetSuccess() {} // 원격 설명 설정 성공 시 처리
            override fun onSetFailure(error: String) {} // 원격 설명 설정 실패 시 처리

            override fun onCreateSuccess(description: SessionDescription) { // SDP 생성 성공 시 처리
                // SDP 생성 성공 처리
            }

            override fun onCreateFailure(error: String) { // SDP 생성 실패 시 처리
                // SDP 생성 실패 처리
            }
        }, description)
    }

    fun addIceCandidate(candidate: IceCandidate) { // ICE 후보 추가 작업
        peerConnection?.addIceCandidate(candidate) // ICE 후보 추가
    }

    fun getLocalVideoTrack(): VideoTrack? { // 로컬 비디오 트랙 가져오기 작업
        return if (::videoTrack.isInitialized) { // 비디오 트랙 초기화 여부 확인
            videoTrack // 초기화된 경우 비디오 트랙 반환
        } else {
            null // 초기화되지 않은 경우 null 반환
        }
    }
}
