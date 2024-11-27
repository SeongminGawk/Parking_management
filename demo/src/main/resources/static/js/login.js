document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM fully loaded and parsed');

    // 로컬 스토리지에서 저장된 아이디 불러오기
    const savedUserid = localStorage.getItem('savedUserid');
    if (savedUserid) {
        document.getElementById('userid').value = savedUserid;
        document.getElementById('remember-me').checked = true;
    }

    const loginButton = document.getElementById('loginButton');
    if (loginButton) {
        loginButton.addEventListener("click", function(event) {
            event.preventDefault();
            encryptLoginForm();
        });
    }

    function encryptLoginForm() {
        const rememberMeCheckbox = document.getElementById('remember-me');
        const userid = document.getElementById('userid').value;

        // "아이디 기억하기" 체크박스 상태에 따라 로컬 스토리지에 아이디 저장/삭제
        if (rememberMeCheckbox.checked) {
            localStorage.setItem('savedUserid', userid);
        } else {
            localStorage.removeItem('savedUserid');
        }

        // 암호화 로직 시작
        var encrypt = new JSEncrypt();
        encrypt.setPublicKey(publicKey);

        var encryptedUserid = encrypt.encrypt(userid);
        var encryptedPassword = encrypt.encrypt(document.getElementById("password").value);

        const params = new URLSearchParams({
            encryptedUserid: encryptedUserid,
            encryptedPassword: encryptedPassword,
            [csrfParameterName]: csrfToken
        });

        fetch("/perform_login", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: params.toString()
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                window.location.href = "/map";
            } else {
                alert(data.message);
                loginButton.disabled = false;
            }
        })
        .catch(error => {
            console.error("Login failed:", error);
            alert("Login failed: " + error.message);
            loginButton.disabled = false;
        });

        loginButton.disabled = true;
    }
});
