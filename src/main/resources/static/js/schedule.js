let currentMonth = new Date();
let selectedDateStr = "";

// 1. 달력을 그리는 함수
async function renderCalendar() {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const monthDisplay = document.getElementById('monthDisplay');
    if(monthDisplay) {
        monthDisplay.textContent = `${year}년 ${month + 1}월`;
    }

    try {
        const response = await fetch('/auth/schedule/list');
        const mySchedules = await response.json();

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
            cell.innerHTML = `<strong>${date}</strong>`;

            const dayTasks = mySchedules.filter(s => s.date === dateStr);
            dayTasks.sort((a, b) => (a.time > b.time ? 1 : -1));

            dayTasks.forEach(task => {
                const item = document.createElement('div');
                item.className = 'schedule-item';
                const timeText = task.time ? `[${task.time}] ` : "";
                item.textContent = timeText + task.content;

                item.onclick = (e) => {
                    e.stopPropagation();
                    if(confirm(`'${task.content}' 일정을 삭제하시겠습니까?`)) {
                        deleteSchedule(task.id);
                    }
                };
                cell.appendChild(item);
            });

            cell.onclick = () => openModal(dateStr);
            grid.appendChild(cell);
        }
    } catch (error) {
        console.error("데이터 로딩 실패:", error);
    }
}

// 2. 삭제 실행 함수
function deleteSchedule(id) {
    fetch(`/auth/schedule/${id}`, { method: 'DELETE' })
        .then(res => {
            if(res.ok) {
                alert("삭제되었습니다.");
                renderCalendar();
            }
        });
}

// 3. 저장 버튼 클릭 시 실행
function saveToSpring() {
    const time = document.getElementById('timeInput').value;
    const content = document.getElementById('scheduleInput').value;

    if(!content) return alert("내용을 입력해주세요!");

    const data = {
        date: selectedDateStr,
        time: time,
        content: content
    };

    fetch('/auth/schedule/save', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => {
            if(res.ok) {
                closeModal();
                renderCalendar();
            } else {
                alert("저장에 실패했습니다.");
            }
        });
}

// 모달 및 월 변경 함수
function openModal(date) {
    selectedDateStr = date;
    const title = document.getElementById('targetDateTitle');
    if(title) title.textContent = date + " 일정 추가";
    document.getElementById('scheduleModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('scheduleModal').style.display = 'none';
    document.getElementById('scheduleInput').value = '';
    document.getElementById('timeInput').value = '';
}

function changeMonth(diff) {
    currentMonth.setMonth(currentMonth.getMonth() + diff);
    renderCalendar();
}

document.addEventListener('DOMContentLoaded', renderCalendar);