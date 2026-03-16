let currentMonth = new Date();
let selectedDateStr = "";
let allSchedules = [];
let currentEditId = null;

async function renderCalendar() {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const monthDisplay = document.getElementById('monthDisplay');
    if(monthDisplay) monthDisplay.textContent = `${year}년 ${month + 1}월`;

    try {
        const response = await fetch('/auth/schedule/list');
        allSchedules = await response.json();

        const firstDay = new Date(year, month, 1).getDay();
        const lastDate = new Date(year, month + 1, 0).getDate();
        const grid = document.getElementById('calendarGrid');

        const labels = Array.from(grid.querySelectorAll('.day-label'));
        grid.innerHTML = '';
        labels.forEach(l => grid.appendChild(l));

        for (let i = 0; i < firstDay; i++) {
            grid.innerHTML += `<div class="day-cell" style="background:var(--bg-color); cursor:default;"></div>`;
        }

        for (let date = 1; date <= lastDate; date++) {
            const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(date).padStart(2, '0')}`;
            const cell = document.createElement('div');
            cell.className = 'day-cell';
            if (dateStr === selectedDateStr) cell.classList.add('selected');

            cell.innerHTML = `<strong>${date}</strong>`;

            // 해당 날짜의 일정들 추출 및 정렬
            const dayTasks = allSchedules.filter(s => s.date === dateStr);
            dayTasks.sort((a, b) => (a.time > b.time ? 1 : -1));

            // 💡 달력 칸 안에 일정 아이템 추가 (핵심 수정 부분)
            const scheduleContainer = document.createElement('div');
            scheduleContainer.className = 'cell-schedule-container';

            dayTasks.forEach(task => {
                const isDone = task.completed || task.isCompleted;
                const miniItem = document.createElement('div');
                miniItem.className = `mini-schedule-item ${isDone ? 'completed' : ''}`;

                // 배경색 및 대비 글자색 적용
                if (!isDone && task.color) {
                    miniItem.style.backgroundColor = task.color;
                    miniItem.style.color = getContrastColor(task.color);
                }

                const timePart = task.time ? `${task.time} ` : '';
                miniItem.textContent = `${timePart}${task.content}`;

                scheduleContainer.appendChild(miniItem);
            });

            cell.appendChild(scheduleContainer);

            // 날짜 클릭 시 목록 팝업 열기
            cell.onclick = () => showDaySchedules(dateStr);
            grid.appendChild(cell);
        }
    } catch (error) {
        console.error("데이터 로딩 실패:", error);
    }
}

function showDaySchedules(dateStr) {
    selectedDateStr = dateStr;
    const dateObj = new Date(dateStr);
    const dayName = new Intl.DateTimeFormat('ko-KR', { weekday: 'long' }).format(dateObj);
    const dayNum = dateObj.getDate();

    // 팝업 헤더 날짜 정보 업데이트
    document.getElementById('selectedDayNum').textContent = dayNum + "일";
    document.getElementById('selectedDayFull').textContent = `${dateStr} (${dayName})`;

    const list = document.getElementById('dayScheduleList');
    list.innerHTML = '';

    // 해당 날짜 일정 필터링 및 시간순 정렬
    const dayTasks = allSchedules.filter(s => s.date === dateStr);
    dayTasks.sort((a, b) => (a.time > b.time ? 1 : -1));

    if (dayTasks.length === 0) {
        list.innerHTML = '<p style="text-align:center; color:#888; margin-top:50px;">일정이 없습니다.</p>';
    } else {
        dayTasks.forEach(task => {
            const isDone = task.completed || task.isCompleted;
            const item = document.createElement('div');
            item.className = `day-schedule-item ${isDone ? 'completed' : ''}`;

            // 리스트 항목 클릭 시 수정 모달 오픈
            item.onclick = () => openEditModal(task);

            item.innerHTML = `
                <div class="color-dot" style="background:${task.color || 'var(--primary-color)'}"></div>
                <div class="item-info">
                    <div class="title">${task.content}</div>
                    <div class="time">${task.time || '시간 지정 없음'}</div>
                </div>
                <input type="checkbox" class="complete-chk" ${isDone ? 'checked' : ''} 
                       onclick="toggleCompleteFromList(event, ${task.id})">
            `;
            list.appendChild(item);
        });
    }

    document.getElementById('dayScheduleModal').style.display = 'flex';
}

/**
 * 3. 모달 제어 함수 (추가 / 수정 / 삭제)
 */

// 일정 추가 모달 열기 (+ 버튼 클릭 시)
function openAddModal() {
    openModal(selectedDateStr);
}

// 일정 수정 모달 열기 (리스트 아이템 클릭 시)
function openEditModal(task) {
    openModal(task.date, task);
}

// 공통 입력 모달 제어
function openModal(date, task = null) {
    selectedDateStr = date;
    const title = document.getElementById('targetDateTitle');
    const deleteBtn = document.getElementById('modalDeleteBtn');

    if (task) {
        // 수정 모드
        currentEditId = task.id;
        title.textContent = "일정 상세";
        document.getElementById('timeInput').value = task.time || '';
        document.getElementById('scheduleInput').value = task.content;
        document.getElementById('colorInput').value = task.color || '#6a5acd';
        deleteBtn.style.display = 'block';
    } else {
        // 추가 모드
        currentEditId = null;
        title.textContent = date + " 일정 추가";
        document.getElementById('timeInput').value = '';
        document.getElementById('scheduleInput').value = '';
        document.getElementById('colorInput').value = '#6a5acd';
        deleteBtn.style.display = 'none';
    }
    document.getElementById('scheduleModal').style.display = 'flex';
}

// 입력 모달 닫기
function closeModal() {
    document.getElementById('scheduleModal').style.display = 'none';
}

// 목록 팝업 닫기
function closeDayModal() {
    document.getElementById('dayScheduleModal').style.display = 'none';
}

/**
 * 4. 서버 통신 함수 (저장 / 삭제 / 완료토글)
 */

// 일정 저장 및 업데이트
async function saveSchedule() {
    const time = document.getElementById('timeInput').value;
    const content = document.getElementById('scheduleInput').value;
    const color = document.getElementById('colorInput').value;

    if(!content) return alert("내용을 입력해주세요!");

    const data = {
        id: currentEditId,
        date: selectedDateStr,
        time: time,
        content: content,
        color: color
    };

    try {
        const res = await fetch('/auth/schedule/save', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if(res.ok) {
            closeModal();
            // 데이터 갱신 후 화면 업데이트
            await refreshAll();
        }
    } catch (err) {
        alert("저장에 실패했습니다.");
    }
}

// 일정 삭제
async function deleteSchedule() {
    if(!currentEditId || !confirm("이 일정을 삭제할까요?")) return;

    try {
        const res = await fetch(`/auth/schedule/${currentEditId}`, { method: 'DELETE' });
        if(res.ok) {
            closeModal();
            await refreshAll();
        }
    } catch (err) {
        console.error("삭제 실패:", err);
    }
}

// 목록 팝업 내에서 완료 상태 토글
async function toggleCompleteFromList(e, scheduleId) {
    e.stopPropagation(); // 수정 모달 열림 방지
    try {
        const response = await fetch(`/auth/schedule/complete/${scheduleId}`, { method: 'POST' });
        if (response.ok) {
            await refreshAll();
        }
    } catch (err) {
        console.error("상태 변경 실패:", err);
    }
}

/**
 * 5. 유틸리티 및 기타 기능
 */

// 데이터 재로드 및 화면 동기화
async function refreshAll() {
    const response = await fetch('/auth/schedule/list');
    allSchedules = await response.json();
    renderCalendar();
    if (selectedDateStr) showDaySchedules(selectedDateStr);
}

// 월 변경
function changeMonth(diff) {
    currentMonth.setMonth(currentMonth.getMonth() + diff);
    selectedDateStr = ""; // 월 변경 시 선택 해제
    closeDayModal();
    renderCalendar();
}

// 배경색 대비 글자색 결정 함수
function getContrastColor(hexColor) {
    if (!hexColor) return 'white';
    const hex = hexColor.replace('#', '');
    const r = parseInt(hex.substring(0, 2), 16);
    const g = parseInt(hex.substring(2, 4), 16);
    const b = parseInt(hex.substring(4, 6), 16);
    const brightness = (r * 299 + g * 587 + b * 114) / 1000;
    return brightness > 128 ? 'black' : 'white';
}

// 초기 로드
document.addEventListener('DOMContentLoaded', renderCalendar);