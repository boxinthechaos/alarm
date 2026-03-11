function purchaseItem(itemId) {
    if (!confirm("이 아이템을 구매하시겠습니까?")) return;

    fetch(`/auth/shop/buy/${itemId}`, {
        method: 'POST'
    })
        .then(response => {
            if (response.ok) return response.text();
            return response.text().then(text => { throw new Error(text) });
        })
        .then(message => {
            alert(message);
            // ✅ 구매 성공 시 상단 바 포인트 즉시 갱신
            updateNavUserInfo();
            location.reload();
        })
        .catch(error => {
            alert(error.message || "구매 중 오류가 발생했습니다.");
        });
}