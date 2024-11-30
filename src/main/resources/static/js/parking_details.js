document.addEventListener("DOMContentLoaded", function() {
    console.log("페이지가 로드되었습니다.");

    // CSRF 토큰을 가져오는 함수
    function getCsrfToken() {
        return {
            token: document.querySelector('meta[name="_csrf"]').getAttribute('content'),
            header: document.querySelector('meta[name="_csrf_header"]').getAttribute('content')
        };
    }

    // CSRF 토큰을 가져와서 콘솔에 출력 (디버깅 목적)
    const csrf = getCsrfToken();
    console.log("CSRF Token:", csrf.token);
    console.log("CSRF Header:", csrf.header);

    // AJAX 요청에 CSRF 토큰을 포함시키는 설정
    document.querySelector('form').addEventListener('submit', function(event) {
        event.preventDefault();

        const form = event.target;
        const formData = new FormData(form);

        fetch(form.action, {
            method: form.method,
            headers: {
                [csrf.header]: csrf.token
            },
            body: formData
        }).then(response => {
            if (response.ok) {
                window.location.href = '/map';
            } else {
                alert('요청 실패');
            }
        }).catch(error => {
            console.error('Error:', error);
            alert('요청 중 오류 발생');
        });
    });
});
