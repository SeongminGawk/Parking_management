document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM fully loaded and parsed');

    if (typeof JSEncrypt === 'undefined') {
        console.error('JSEncrypt is not loaded');
        return;
    }

    console.log('JSEncrypt is loaded');
});

function encryptForm() {
    if (typeof JSEncrypt === 'undefined') {
        console.error('JSEncrypt is not loaded');
        return false;
    }

    var password = document.getElementById("password").value;
    var confirmPassword = document.getElementById("confirmPassword").value;

    if (password !== confirmPassword) {
        alert("비밀번호가 일치하지 않습니다.");
        return false;
    }

    var encrypt = new JSEncrypt();
    encrypt.setPublicKey(publicKey);
    
    //입력값 가져오기
    var userid = document.getElementById("userid").value;
    var name = document.getElementById("name").value;
    var phoneNumber = document.getElementById("phoneNumber").value;
    var email = document.getElementById("email").value;

    //가져온 입력값 RSA 공개키로 암호화
    var encryptedUserid = encrypt.encrypt(userid);
    var encryptedUsername = encrypt.encrypt(name);
    var encryptedPassword = encrypt.encrypt(password);
    var encryptedPhoneNumber = encrypt.encrypt(phoneNumber);
    var encryptedEmail = encrypt.encrypt(email);

    // 암호화된 값을 숨겨진 입력 필드에 설정
    document.getElementById("encryptedUserid").value = encryptedUserid;
    document.getElementById("encryptedUsername").value = encryptedUsername;
    document.getElementById("encryptedPassword").value = encryptedPassword;
    document.getElementById("encryptedPhoneNumber").value = encryptedPhoneNumber;
    document.getElementById("encryptedEmail").value = encryptedEmail;

    // 입력값 비우기
    document.getElementById("userid").value = "";
    document.getElementById("name").value = "";
    document.getElementById("password").value = "";
    document.getElementById("confirmPassword").value = "";
    document.getElementById("phoneNumber").value = "";
    document.getElementById("email").value = "";

    return true;
}

function checkDuplicateUserid() {
    var userid = document.getElementById("userid").value;
    console.log("Checking userid: " + userid); 
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/checkUserid?userid=" + encodeURIComponent(userid), true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            console.log(xhr.responseText); 
            if (xhr.status === 200) {
                try {
                    var response = JSON.parse(xhr.responseText);
                    var message = response.message;
                    alert(message); 
                } catch (e) {
                    console.error("Parsing error:", e); 
                    alert("서버 응답을 처리하는 중 오류가 발생했습니다.");
                }
            } else {
                console.error("Error: " + xhr.status); 
                alert("서버와 통신하는 중 오류가 발생했습니다.");
            }
        }
    };
    xhr.send();
}
