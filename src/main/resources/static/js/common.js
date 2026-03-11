document.addEventListener('DOMContentLoaded', () => {
    updateNavUserInfo();
    // 1. 테마 초기 설정
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);

    // 2. 다크모드 토글 버튼 연결
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme');
            const targetTheme = currentTheme === 'dark' ? 'light' : 'dark';

            document.documentElement.setAttribute('data-theme', targetTheme);
            localStorage.setItem('theme', targetTheme);
        });
    }
});

function updateNavUserInfo() {
    // 유저 정보를 가져오는 API (이미 구현되어 있거나 새로 만들어야 함)
    fetch('/auth/user-info')
        .then(response => {
            if (response.ok) return response.json();
            throw new Error('로그인 정보 없음');
        })
        .then(user => {
            document.getElementById('navNickname').textContent = user.nickname;
            document.getElementById('navPoint').textContent = user.point.toLocaleString(); // 숫자 콤마 표시
        })
        .catch(err => console.log('비로그인 상태 또는 에러:', err));
}

// common.js 내부에 추가
document.addEventListener('DOMContentLoaded', () => {
    const currentPath = window.location.pathname;
    const navItems = document.querySelectorAll('.nav-item');

    navItems.forEach(item => {
        if (item.getAttribute('href') === currentPath) {
            item.style.color = 'var(--primary-color)';
            item.style.fontWeight = '700';
            item.style.borderBottom = '2px solid var(--primary-color)';
        }
    });
});