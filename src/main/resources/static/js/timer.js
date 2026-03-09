const minutesInput = document.getElementById('minutesInput');
const secondsInput = document.getElementById('secondsInput');
const display = document.getElementById('display');
const startButton = document.getElementById('startButton');
const stopButton = document.getElementById('stopButton');
const resetButton = document.getElementById('resetButton');

// 백엔드 API를 통해 DB의 1번 오디오 파일을 가져옵니다.
const alarmSound = new Audio('/alarm/audio/1');

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

    if (totalSeconds <= 0) {
        alert("유효한 시간을 입력해 주세요.");
        return;
    }

    clearInterval(countdown);

    countdown = setInterval(() => {
        if (totalSeconds <= 0) {
            clearInterval(countdown);
            alarmSound.play();
            alert("시간이 종료되었습니다!");
            return;
        }
        totalSeconds--;
        updateDisplay(totalSeconds);
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