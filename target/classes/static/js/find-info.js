// CSRF 토큰을 쿠키에서 추출하는 함수
function getCsrfTokenFromCookie() {
    const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : null;
}

// 서버 요청 함수
async function sendRequest(url, payload, csrfToken) {
    try {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": csrfToken,
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            console.error(`Server returned status ${response.status}: ${errorMsg}`);
            throw new Error(errorMsg || "Request failed.");
        }

        return await response.json();
    } catch (error) {
        console.error("Error during server request:", error.message);
        throw error;
    }
}

// 버튼 활성/비활성화 함수
function toggleButtonState(button, isDisabled) {
    if (button) button.disabled = isDisabled;
}

// 페이지가 로드되었을 때 실행
document.addEventListener("DOMContentLoaded", () => {
    const csrfToken = getCsrfTokenFromCookie();
    if (!csrfToken) {
        alert("CSRF 토큰이 누락되었습니다. 새로고침 후 다시 시도해 주세요.");
        console.error("CSRF token is missing in the cookies.");
        return;
    }

    console.log("CSRF Token:", csrfToken);

    // 버튼 요소 가져오기
    const findIdBtn = document.getElementById("findIdBtn");
    const findPasswordBtn = document.getElementById("findPasswordBtn");

    // 섹션 전환 버튼
    document.getElementById("findIdButton").addEventListener("click", () => {
        document.getElementById("findIdSection").classList.remove("hidden");
        document.getElementById("findPasswordSection").classList.add("hidden");
    });

    document.getElementById("findPasswordButton").addEventListener("click", () => {
        document.getElementById("findPasswordSection").classList.remove("hidden");
        document.getElementById("findIdSection").classList.add("hidden");
    });

    // 아이디 찾기
    findIdBtn.addEventListener("click", async () => {
        const name = document.getElementById("name")?.value.trim();

        if (!name) {
            alert("이름을 입력해 주세요.");
            return;
        }

        toggleButtonState(findIdBtn, true);
        try {
            const payload = { name };
            console.log("Sending payload for ID retrieval:", payload);

            const response = await sendRequest("/find-info/find-id", payload, csrfToken);

            if (response.success) {
                const userIds = response.userIds.join("\n"); // 여러 아이디를 줄바꿈으로 연결
                alert(`등록된 아이디:\n${userIds}`);
            } else {
                alert(response.message);
            }
        } catch (error) {
            alert(`오류: 아이디를 찾을 수 없습니다. ${error.message}`);
        } finally {
            toggleButtonState(findIdBtn, false);
        }
    });

    // 비밀번호 재설정
    findPasswordBtn.addEventListener("click", async () => {
    const userId = document.getElementById("userId")?.value.trim();
    const name = document.getElementById("nameForPassword")?.value.trim();
    const newPassword = document.getElementById("newPassword")?.value.trim();

    if (!userId || !name || !newPassword) {
        alert("아이디, 이름, 새 비밀번호를 모두 입력해 주세요.");
        return;
    }

    toggleButtonState(findPasswordBtn, true);
    try {
        const payload = { userId, name, newPassword };
        console.log("Sending payload for password reset:", payload);

        const response = await sendRequest("/find-info/reset-password", payload, csrfToken);

        if (response.success) {
            alert(response.message); // 성공 메시지 출력
            window.location.href = "/login"; // 로그인 페이지로 이동
        } else {
            alert(`오류: ${response.message}`);
        }
    } catch (error) {
        alert(`오류: 비밀번호를 재설정할 수 없습니다. ${error.message}`);
    } finally {
        toggleButtonState(findPasswordBtn, false);
    }
});
});
