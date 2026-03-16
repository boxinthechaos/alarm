const historyList = document.getElementById('historyList');
const settingsSection = document.getElementById('settingsSection');
const playerSection = document.getElementById('playerSection');
const alarmForm = document.getElementById('alarmForm');
const audio = document.getElementById('myAudio');
const displayTime = document.getElementById('displayTime');
const submitBtn = document.getElementById('submitBtn');
const fileInput = document.getElementById('file-input');
const fileNameDisplay = document.getElementById('fileName');
const playerTitle = document.getElementById('playerTitle');
const alarmClockCard = document.getElementById('alarmClockCard');
const alarmBubble = document.getElementById('alarmBubble');
const bubbleText = document.getElementById('bubbleText');
const cancelAlarmBtn = document.getElementById('cancelAlarmBtn');

// --- 상태 관리 변수 ---
let isRinging = false;
let activeAlarms = []; // 여러 알람을 담는 배열
let masterInterval = null; // 1초마다 검사하는 단 하나의 심장
let currentRingingAlarmId = null; // 현재 울리고 있는 알람의 Id 저장
const activeAlarmList = document.getElementById('activeAlarmList');

fileInput.addEventListener('change', () => {
    fileNameDisplay.textContent = fileInput.files.length > 0 ? fileInput.files[0].name : '선택된 파일 없음';
});

// 알람 설정 (Submit)
alarmForm.addEventListener('submit', function(event) {
    event.preventDefault();
    submitBtn.disabled = true;
    submitBtn.textContent = '설정 중...';

    const formData = new FormData(alarmForm);
    const targetTime = formData.get('time');

    fetch('/alarm/set', {
        method: 'POST',
        body: formData,
    })
        .then(response => {
            if (!response.ok) return response.text().then(text => { throw new Error(text) });
            return response.json();
        })
        .then(data => {
            alert(`${targetTime} 알람이 추가되었습니다!`);
            console.log("서버 응답 확인:", data);
            console.log("data 전체:", JSON.stringify(data)); // 구조 확인용
            armAlarm(targetTime, data.audioId.audioId, data.audioId.alarmId);

            loadHistory();
            submitBtn.disabled = false;
            submitBtn.textContent = '설정 완료';
            alarmForm.reset();
            fileNameDisplay.textContent = '선택된 파일 없음';
        })
        .catch(error => {
            alert(error.message || '알람 설정 중 오류가 발생했습니다.');
            submitBtn.disabled = false;
            submitBtn.textContent = '설정 완료';
        });
});

// 알람을 배열에 넣고 감시 시작
function armAlarm(targetTime, audioId, alarmId) {
    const newAlarm = {
        time: targetTime,
        audioId: audioId,
        alarmId: alarmId,
        triggered: false
    };

    activeAlarms.push(newAlarm);
    renderActiveAlarms();
    console.log("현재 대기 중인 알람 목록:", activeAlarms);

    // 마스터 인터벌이 없으면 여기서 생성 (단 한 번만 실행됨)
    if (!masterInterval) {
        masterInterval = setInterval(checkAllAlarms, 1000);
    }
}

// 1초마다 모든 알람 확인
function checkAllAlarms() {
    const now = new Date();
    const currentH = now.getHours();
    const currentM = now.getMinutes();
    const currentS = now.getSeconds();

    // 매 분 0초에 한 번만 실행되도록 하거나, triggered로 방어
    activeAlarms.forEach(alarm => {
        if (!alarm.triggered) {
            const [targetH, targetM] = alarm.time.split(":").map(Number);

            if (currentH === targetH && currentM === targetM) {
                alarm.triggered = true;
                playSpecificAlarm(alarm);
            }
        }
    });
}

// 실제로 알람이 울리는 시점
function playSpecificAlarm(alarm) {
    cancelAlarm(alarm.alarmId);
    // UI 전환: 알람이 울릴 때만 플레이어 섹션을 보여줌
    settingsSection.style.display = 'none';
    playerSection.style.display = 'block';
    displayTime.textContent = alarm.time;
    playerTitle.textContent = "⏰ 알람 울리는 중! ⏰";
    alarmClockCard.classList.add('ringing');
    currentRingingAlarmId = alarm.alarmId;

    // 오디오 재생
    console.log("재생할 오디오 ID:", alarm.audioId);
    audio.src = `/alarm/audio/${alarm.audioId}`;
    audio.play().catch(e => console.error("오디오 재생 실패:", e));

    isRinging = true;
    currentRingingAlarmId = alarm.alarmId;
}

// 알람 끄기 (포인트 지급 로직 포함)
function stopAudio() {
    audio.pause();
    audio.currentTime = 0;
    playerTitle.textContent = "알람이 정지되었습니다.";
    alarmClockCard.classList.remove('ringing');

    if (isRinging) {
        console.log("보내는 alarmId:", currentRingingAlarmId);
        fetch(`/alarm/stop?alarmId=${currentRingingAlarmId}`, { method: 'POST' })
            .then(response => {
                console.log("서버 응답 상태:", response.status);

                // ✅ 이 부분 추가
                if (response.status === 401) {
                    alert("로그인이 필요합니다.");
                    window.location.href = '/login';
                    return null;
                }

                if (!response.ok) throw new Error("서버 응답 오류");
                return response.json();
            })
            .then(data => {
                if (!data) return; // ✅ null 체크
                console.log("서버 데이터:", data);
                alert(data.message);
                isRinging = false;
                currentRingingAlarmId = null;
                if (typeof updateNavUserInfo === 'function') {
                    updateNavUserInfo();
                }
                resetAlarm();
            })
            .catch(err => {
                console.error("상세 에러 내용:", err);
                alert("알람 정지 처리 중 오류가 발생했습니다.");
            });
    }
}

function renderActiveAlarms() {
    if (alarmBubble) {
        if (activeAlarms.length === 0) {
            alarmBubble.style.display = 'none';
            return;
        }
        alarmBubble.style.display = 'flex';
        bubbleText.textContent = `⏰ ${activeAlarms.map(a => a.time).join(', ')}`;
    }
}

cancelAlarmBtn.addEventListener('click', () => {
    if (activeAlarms.length > 0) {
        cancelAlarm(activeAlarms[activeAlarms.length - 1].alarmId);
    }
});

// 수동 리셋 버튼
function resetAlarm() {
    audio.pause();
    audio.currentTime = 0;
    playerSection.style.display = 'none';
    settingsSection.style.display = 'block';
    alarmForm.reset();
}

// 기록에서 알람 설정하기
function setAlarmFromHistory(time, audioId, songName) {
    document.getElementById('time-input').value = time;
    fileNameDisplay.textContent = `(기록 선택됨) ${songName}`;

    // hidden input 처리 (파일 업로드 대신 기존 ID 사용 시)
    let hiddenInput = alarmForm.querySelector('input[name="audioId"]');
    if (!hiddenInput) {
        hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'audioId';
        alarmForm.appendChild(hiddenInput);
    }
    hiddenInput.value = audioId;
}

// 기록 삭제
function deleteHistoryItem(alarmId) {
    if (!confirm('정말로 이 기록을 삭제하시겠습니까?')) return;
    fetch('/alarm/history/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ 'alarmId': alarmId })
    })
        .then(response => { if (response.ok) loadHistory(); })
        .catch(error => console.error('기록 삭제 중 오류 발생:', error));
}

// 기록 불러오기
function loadHistory() {
    fetch('/alarm/history')
        .then(response => (response.status === 401 ? null : response.json()))
        .then(data => {
            if (!data) { historyList.innerHTML = '<li>로그인 후 이용해주세요.</li>'; return; }
            historyList.innerHTML = '';
            if (data.length === 0) {
                historyList.innerHTML = '<li>기록이 없습니다</li>';
            } else {
                data.forEach(item => {
                    const li = document.createElement('li');
                    li.innerHTML = `
                        <span class="history-item-text">[${item.time}] - ${item.song}</span>
                        <button class="delete-btn">x</button>
                    `;
                    li.querySelector('.history-item-text').onclick = () => setAlarmFromHistory(item.time, item.audioId, item.song);
                    li.querySelector('.delete-btn').onclick = (e) => {
                        e.stopPropagation();
                        deleteHistoryItem(item.alarmId);
                    };
                    historyList.appendChild(li);
                });
            }
        });
}

document.addEventListener('DOMContentLoaded', loadHistory);

function cancelAlarm(alarmId) {
    activeAlarms = activeAlarms.filter(alarm => alarm.alarmId !== alarmId);

    if (activeAlarms.length === 0) {
        clearInterval(masterInterval);
        masterInterval = null;
    }

    renderActiveAlarms(); // UI 갱신
}