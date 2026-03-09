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

let checkInterval;

fileInput.addEventListener('change', () => {
    fileNameDisplay.textContent = fileInput.files.length > 0 ? fileInput.files[0].name : '선택된 파일 없음';
});

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
            settingsSection.style.display = 'none';
            playerSection.style.display = 'block';
            displayTime.textContent = targetTime;
            audio.src = `/alarm/audio/${data.audioId}`;
            armAlarm(targetTime);
            loadHistory();
        })
        .catch(error => {
            alert(error.message || '알람 설정 중 오류가 발생했습니다.');
            submitBtn.disabled = false;
            submitBtn.textContent = '설정 완료';
        });
});

function armAlarm(targetTime) {
    let alarmTriggered = false;
    if(checkInterval) clearInterval(checkInterval);

    function checkTime() {
        const now = new Date();
        const [targetHour, targetMinute] = targetTime.split(":").map(Number);
        if (!alarmTriggered && now.getHours() === targetHour && now.getMinutes() === targetMinute) {
            startAudio();
            alarmTriggered = true;
        }
    }
    checkTime();
    checkInterval = setInterval(checkTime, 1000);
}

function startAudio() {
    audio.play().catch(e => console.error("오디오 재생 실패:", e));
    playerTitle.textContent = "⏰ 알람 울리는 중 ⏰";
    alarmClockCard.classList.add('ringing');
    clearInterval(checkInterval);
}

function stopAudio() {
    audio.pause();
    audio.currentTime = 0;
    playerTitle.textContent = "알람이 정지되었습니다.";
    alarmClockCard.classList.remove('ringing');
}

function resetAlarm() {
    stopAudio();
    if(checkInterval) clearInterval(checkInterval);
    playerSection.style.display = 'none';
    settingsSection.style.display = 'block';
    alarmForm.reset();
    fileNameDisplay.textContent = '선택된 파일 없음';
    submitBtn.disabled = false;
    submitBtn.textContent = '설정 완료';
}

function setAlarmFromHistory(time, audioId, songName) {
    if (playerSection.style.display === 'block') resetAlarm();
    document.getElementById('time-input').value = time;
    fileNameDisplay.textContent = `(기록) ${songName}`;
    let hiddenInput = alarmForm.querySelector('input[name="audioId"]');
    if (!hiddenInput) {
        hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'audioId';
        alarmForm.appendChild(hiddenInput);
    }
    hiddenInput.value = audioId;
}

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

function loadHistory() {
    fetch('/alarm/history')
        .then(response => {
            if(response.status === 401) return null;
            return response.json();
        })
        .then(data => {
            if (!data) { historyList.innerHTML = '<li>로그인 후 이용해주세요.</li>'; return; }
            historyList.innerHTML = '';
            if (data.length === 0) { historyList.innerHTML = '<li>기록이 없습니다</li>'; }
            else {
                data.forEach(item => {
                    const li = document.createElement('li');
                    const textSpan = document.createElement('span');
                    textSpan.textContent = `[${item.time}] - ${item.song}`;
                    textSpan.className = 'history-item-text';
                    textSpan.addEventListener('click', () => setAlarmFromHistory(item.time, item.audioId, item.song));
                    const deleteBtn = document.createElement('button');
                    deleteBtn.textContent = 'x';
                    deleteBtn.className = 'delete-btn';
                    deleteBtn.addEventListener('click', (e) => { e.stopPropagation(); deleteHistoryItem(item.alarmId); });
                    li.appendChild(textSpan);
                    li.appendChild(deleteBtn);
                    historyList.appendChild(li);
                });
            }
        });
}

document.addEventListener('DOMContentLoaded', loadHistory);