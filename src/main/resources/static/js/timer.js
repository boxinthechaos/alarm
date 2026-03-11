const minutesInput = document.getElementById('minutesInput');
const secondsInput = document.getElementById('secondsInput');
const display = document.getElementById('display');
const startButton = document.getElementById('startButton');
const stopButton = document.getElementById('stopButton');
const resetButton = document.getElementById('resetButton');

// 백엔드 API를 통해 DB의 1번 오디오 파일을 가져옵니다.
const alarmSound = new Audio('/alarm/audio/1');

let originalSeconds;
let countdown;
let totalSeconds;

function updateDisplay(seconds) {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    display.textContent = `${String(minutes).padStart(2, '0')}:${String(remainingSeconds).padStart(2, '0')}`;
}

// 오디오 정지 함수
function stopAlarm() {
    alarmSound.pause();
    alarmSound.currentTime = 0;
}

startButton.addEventListener('click', () => {
    stopAlarm();

    let minutes = parseInt(minutesInput.value, 10) || 0;
    let seconds = parseInt(secondsInput.value, 10) || 0;
    totalSeconds = (minutes * 60) + seconds;

    // 1️⃣ [추가] 시작할 때 입력된 전체 시간을 변수에 따로 저장합니다.
    originalSeconds = totalSeconds;

    if (totalSeconds <= 0) {
        alert("유효한 시간을 입력해 주세요.");
        return;
    }

    clearInterval(countdown);

    countdown = setInterval(() => {
        if (totalSeconds <= 0) {
            clearInterval(countdown);

            // 2️⃣ [추가] originalSeconds가 600(10분) 이상일 때만 fetch 실행
            if (originalSeconds >= 6) {
                fetch('/auth/timer/complete', {
                    method: 'POST'
                })
                    .then(response => response.text())
                    .then(message => {
                        console.log("서버 응답:", message);
                    })
                    .catch(error => console.error('포인트 적립 실패:', error));

                alert("시간이 종료되었습니다! 포인트가 적립되었습니다. 🎉");
            } else {
                // 10분 미만일 때는 포인트 없이 알림만
                alert("시간이 종료되었습니다! (10분 미만은 포인트가 지급되지 않습니다)");
            }

            alarmSound.play();
            return;
        }
        totalSeconds--;
        updateDisplay(totalSeconds)
    }, 1000);
});

stopButton.addEventListener('click', () => {
    clearInterval(countdown);
    stopAlarm();
});

resetButton.addEventListener('click', () => {
    clearInterval(countdown);
    totalSeconds = 0;
    updateDisplay(totalSeconds);
    minutesInput.value = '';
    secondsInput.value = '';
    stopAlarm();
});