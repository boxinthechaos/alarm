document.addEventListener('DOMContentLoaded', () => {
    updateNavUserInfo();
    initTheme();
    handleActiveNavHighlight();
});

function updateNavUserInfo() {
    fetch('/auth/user-info')
        .then(response => response.ok ? response.json() : Promise.reject())
        .then(user => {
            document.getElementById('navNickname').textContent = user.nickname;
            document.getElementById('navPoint').textContent = user.point.toLocaleString();
        })
        .catch(() => console.log('비로그인 상태'));
}

function initTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.onclick = () => {
            const current = document.documentElement.getAttribute('data-theme');
            const next = current === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', next);
            localStorage.setItem('theme', next);
        };
    }
}

function handleActiveNavHighlight() {
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-item').forEach(item => {
        if (item.getAttribute('href') === currentPath) {
            item.style.color = 'var(--primary-color)';
            item.style.fontWeight = '700';
        }
    });
}