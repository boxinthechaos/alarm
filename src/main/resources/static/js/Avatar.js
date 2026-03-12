// 장착 함수
function equipItem(itemId) {
    fetch(`/auth/shop/inventory/equip/${itemId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                location.reload(); // 상태 갱신을 위해 새로고침
            }
        })
        .catch(err => alert("장착 실패!"));
}

// 해제 함수
function unequipItem(category) {
    fetch(`/auth/shop/inventory/unequip?category=${category}`, {
        method: 'POST'
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                if (category === 'CAP') {
                    const hatImg = document.getElementById('mainHat');
                    if (hatImg) hatImg.src = '';
                } else if (category === 'CLOTHES') {
                    const clothesImg = document.getElementById('mainClothes');
                    if (clothesImg) clothesImg.src = '';
                }
                location.reload();
            }
        });
}

// 인벤토리 탭 필터링
document.addEventListener('DOMContentLoaded', function() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const itemSlots = document.querySelectorAll('.item-slot');

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const filter = button.getAttribute('data-filter');

            // 1. 버튼 활성화 상태 변경
            tabButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active'); // classList.add로 수정

            // 2. 아이템 필터링
            itemSlots.forEach(slot => {
                // 빈 슬롯 처리
                if (slot.classList.contains('empty')) {
                    slot.style.display = (filter === 'ALL') ? 'block' : 'none';
                    return;
                }

                // 카테고리 비교
                const itemCategory = slot.getAttribute('data-category');

                if (filter === 'ALL' || itemCategory === filter) {
                    slot.style.display = 'block';
                } else {
                    slot.style.display = 'none';
                }
            });
        });
    });
});