document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById('parkingForm');
    const submitButton = document.getElementById('submitButton');
    const prkLotIdA = document.getElementById('prk_lot_id_a');
    const prkLotIdB = document.getElementById('prk_lot_id_b');
    const prkLotIdC = document.getElementById('prk_lot_id_c');
    const instDt = document.getElementById('inst_dt');
    const latitude = document.getElementById('latitude');
    const longitude = document.getElementById('longitude');
    
    // 설치일자를 변경하면 prkLotIdB를 업데이트하는 함수입니다.
    function updatePrkLotIdB() {
        const instDtValue = instDt.value.replace(/-/g, '');
        prkLotIdB.value = instDtValue;
    }
    
    // 우편번호 검색을 수행하고, 주소와 좌표를 가져오는 함수입니다.
    window.findZipCode = function() {
        var layer = document.getElementById('postcodeLayer');
        layer.style.display = 'block';
        new daum.Postcode({
            oncomplete: async function(data) {
                var fullAddr = data.address;
                document.getElementById('bas_addr').value = fullAddr;
                document.getElementById('prk_lot_id_a').value = data.zonecode;

                try {
                    const coords = await getAddressCoords(fullAddr);
                    latitude.value = coords.y;
                    longitude.value = coords.x;

                    const nextSeq = await getNextPrkLotSeq();
                    prkLotIdC.value = nextSeq.toString().padStart(5, '0');
                } catch (error) {
                    console.error('Error fetching coordinates or sequence:', error);
                }

                layer.style.display = 'none';
            },
            onclose: function(state) {
                layer.style.display = 'none';
            }
        }).open();
    };

    // 다음 주차장 순번을 서버에서 가져오는 비동기 함수입니다.
    async function getNextPrkLotSeq() {
        try {
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
            const response = await fetch('/parking/getNextPrkLotSeq', {
                method: 'GET',
                headers: {
                    [csrfHeader]: csrfToken
                }
            });
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();
            return data.nextSeq;
        } catch (error) {
            console.error('Error fetching next parking lot sequence:', error);
            throw error;
        }
    }

    // 주소로부터 좌표를 가져오는 비동기 함수입니다.
    window.getAddressCoords = async function(address) {
        const config = { headers: { Authorization: `KakaoAK dbbc39caa6e4e9e78338914a288a093f` } };
        const url = `https://dapi.kakao.com/v2/local/search/address.json?query=${encodeURIComponent(address)}`;
        try {
            const result = await axios.get(url, config);
            if (result.data.documents.length > 0) {
                return { y: result.data.documents[0].y, x: result.data.documents[0].x };
            } else {
                throw new Error('No results found');
            }
        } catch (error) {
            throw new Error('Error fetching coordinates');
        }
    };

    // 설치일자가 변경될 때 updatePrkLotIdB 함수를 호출합니다.
    instDt.addEventListener('change', updatePrkLotIdB);
    
    // 폼 제출 시 handleFormSubmit 함수를 호출합니다.
    form.addEventListener('submit', handleFormSubmit);

    async function handleFormSubmit(event) {
        event.preventDefault();
        submitButton.disabled = true;

        // 폼 제출을 처리하는 비동기 함수입니다. 주차장 ID를 생성하고, 운영일자를 설정한 후 폼 데이터를 서버에 제출합니다.
        if (confirm('등록하시겠습니까?')) {
            const prkLotId = `${prkLotIdA.value}-${prkLotIdB.value}-${prkLotIdC.value}`;
            document.getElementById('prk_lot_id').value = prkLotId;

            const opertnDayCheckboxes = document.querySelectorAll('input[name="opertn_day"]:checked');
            const opertnDays = Array.from(opertnDayCheckboxes).map(checkbox => checkbox.value);
            
            // 중복 제거
            const uniqueOpertnDays = [...new Set(opertnDays)];
            
            // 문자열 길이 제한 (최대 24자)
            let opertnDaysStr = uniqueOpertnDays.join(',');
            if (opertnDaysStr.length > 24) {
                opertnDaysStr = opertnDaysStr.substring(0, 24);
            }
            
            document.getElementById('opertn_day').value = opertnDaysStr;

            if (instDt.value.trim() === '') {
                alert('설치일자를 입력해주세요.');
                submitButton.disabled = false;
                return;
            }

            try {
                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                const response = await fetch('/parking/register', {
                    method: 'POST',
                    headers: {
                        [csrfHeader]: csrfToken
                    },
                    body: new FormData(form)
                });

                if (response.ok) {
                    const data = await response.json();
                    alert('정상적으로 등록되었습니다.');
                    window.location.href = `/parking/details/${data.parkingLotId}`;
                } else {
                    const error = await response.text();
                    alert(`등록에 실패했습니다: ${error}`);
                }
            } catch (error) {
                console.error('Error submitting form:', error);
                alert('등록 중 오류가 발생했습니다.');
            } finally {
                submitButton.disabled = false;
            }
        } else {
            submitButton.disabled = false;
        }
    }
    
    // 페이지 로드 시 초기 주차장 순번을 설정하는 즉시 실행 함수입니다.
    (async function setPrkLotIdC() {
        try {
            const nextSeq = await getNextPrkLotSeq();
            prkLotIdC.value = nextSeq.toString().padStart(5, '0');
        } catch (error) {
            console.error('Error setting initial parking lot sequence:', error);
        }
    })();
});
