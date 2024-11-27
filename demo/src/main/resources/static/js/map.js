document.addEventListener('DOMContentLoaded', function() {
	const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener("click", function() {
            fetch("/logout", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            })
            .then(response => {
                if (response.ok) {
                    window.location.href = "/login"; // 로그아웃 후 로그인 페이지로 이동
                } else {
                    alert("Logout failed");
                }
            })
            .catch(error => {
                console.error("Logout failed:", error);
                alert("Logout failed: " + error.message);
            });
        });
    }
    var mapContainer = document.getElementById('map'); // 지도를 표시할 div
    var mapOption = {
        center: new kakao.maps.LatLng(37.450701, 126.570667), // 지도의 초기 중심좌표 (기본값)
        level: 3 // 지도의 확대 레벨
    };

    // 지도를 표시할 div와 지도 옵션으로 지도 생성
    var map = new kakao.maps.Map(mapContainer, mapOption);

    var mapTypeControl = new kakao.maps.MapTypeControl();
    map.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);

    var zoomControl = new kakao.maps.ZoomControl();
    map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

    var currentMarker = null;
    var currentInfowindow = null;

    // 날짜 포맷팅 함수
    //"YYYY-MM-DD HH:MM:SS" 형식
    function formatDate(dateString) {
        var date = new Date(dateString);
        var year = date.getFullYear();
        var month = String(date.getMonth() + 1).padStart(2, '0');
        var day = String(date.getDate()).padStart(2, '0');
        var hours = String(date.getHours()).padStart(2, '0');
        var minutes = String(date.getMinutes()).padStart(2, '0');
        var seconds = String(date.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }

    // 현재 위치 설정 함수
    function setCurrentLocation() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function(position) {
                var lat = position.coords.latitude; // 위도
                var lon = position.coords.longitude; // 경도

                var locPosition = new kakao.maps.LatLng(lat, lon); // 마커가 표시될 위치를 생성

                // 현재 위치를 지도의 중심으로 설정
                map.setCenter(locPosition);

                // 마커 생성
                if (currentMarker) {
                    currentMarker.setMap(null);
                }
                currentMarker = new kakao.maps.Marker({
                    map: map,
                    position: locPosition
                });

                // 인포윈도우를 생성
                if (currentInfowindow) {
                    currentInfowindow.close();
                }
                currentInfowindow = new kakao.maps.InfoWindow({
                    content: '<div style="padding:5px; font-size:10px; width: 80px; text-align: center;">현재 위치</div>'
                });

                // 인포윈도우를 마커에 표시
                currentInfowindow.open(map, currentMarker);

            }, function(error) {
                console.error('Error occurred. Error code: ' + error.code);
                setDefaultLocation();
            });
        } else {
            // 브라우저가 Geolocation을 지원하지 않는 경우
            console.error('Geolocation is not supported by this browser');
            setDefaultLocation();
        }
    }

    // 기본 위치 설정 함수
    function setDefaultLocation() {
        var defaultPosition = new kakao.maps.LatLng(37.450701, 126.570667); // 기본 위치
        map.setCenter(defaultPosition);

        if (currentMarker) {
            currentMarker.setMap(null);
        }
        currentMarker = new kakao.maps.Marker({
            map: map,
            position: defaultPosition
        });

        if (currentInfowindow) {
            currentInfowindow.close();
        }
        currentInfowindow = new kakao.maps.InfoWindow({
            content: '<div style="padding:5px; font-size:10px; width: 80px; text-align: center;">기본 위치</div>'
        });

        currentInfowindow.open(map, currentMarker);
    }

    // 주차장 정보를 불러와서 지도에 표시
    function loadParkingLots(page) {
        fetch(`/parking/list?page=${page}&size=5`)
            .then(response => response.json())
            .then(data => {
                if (data.status === 'success') {
                    displayParkingLots(data.data, data.totalPages, data.currentPage);
                } else {
                    displayNoParkingLotsMessage();
                    console.error('Failed to load parking lots:', data.message);
                }
            })
            .catch(error => {
                displayNoParkingLotsMessage();
                console.error('Error fetching parking lots:', error);
            });
    }

    function displayParkingLots(parkingLots, totalPages, currentPage) {
        var infoContainer = document.getElementById('parking-info');
        infoContainer.innerHTML = ''; // 이전 내용 삭제

        if (parkingLots.length === 0) {
            displayNoParkingLotsMessage();
            return;
        }

        var table = document.createElement('table');
        table.className = 'parking-info-table';

        parkingLots.forEach(lot => {
            var row = document.createElement('tr');
            row.className = 'parking-info';
            row.innerHTML = `<td>
                                <strong>주차장 명:</strong> ${lot.prkLotNm}
                                <br>
                                <strong>위치:</strong> ${lot.basAddr}
                                <br>
                                <strong>등록일시:</strong> ${formatDate(lot.regDt)}
                            </td>`;
            row.addEventListener('click', function() {
                showParkingLotDetails(lot);
            });
            table.appendChild(row);
        });

        infoContainer.appendChild(table);

        // 페이지 네비게이션 생성
        var pagination = document.createElement('div');
        pagination.className = 'pagination';
        for (var i = 0; i < totalPages; i++) {
            var pageButton = document.createElement('button');
            pageButton.innerText = i + 1;
            pageButton.onclick = (function(page) {
                return function() {
                    loadParkingLots(page);
                }
            })(i);
            if (i === currentPage) {
                pageButton.style.fontWeight = 'bold';
            }
            pagination.appendChild(pageButton);
        }
        infoContainer.appendChild(pagination);
    }

    function displayNoParkingLotsMessage() {
        var infoContainer = document.getElementById('parking-info');
        infoContainer.innerHTML = '<div>등록된 주차장이 없습니다.</div>';
    }

    function showParkingLotDetails(lot) {
        var detailContainer = document.createElement('div');
        detailContainer.className = 'parking-detail';
        detailContainer.innerHTML = `<div>
                                        <strong>주차장 명:</strong> ${lot.prkLotNm}
                                     </div>
                                     <div>
                                        <strong>위치:</strong> ${lot.basAddr}
                                     </div>
                                     <div>
                                        <strong>등록일시:</strong> ${formatDate(lot.regDt)}
                                     </div>`;
        var infoContainer = document.getElementById('parking-info');
        infoContainer.innerHTML = '';
        infoContainer.appendChild(detailContainer);

        const position = new kakao.maps.LatLng(lot.latitude, lot.longitude);
        map.setCenter(position);
        map.setLevel(2); // 지도를 더 확대

        const marker = new kakao.maps.Marker({
            map: map,
            position: position
        });

        const infowindow = new kakao.maps.InfoWindow({
            content: `<div style="padding:5px; font-size:10px; width: 80px; text-align: center;">${lot.prkLotNm}</div>`
        });
        infowindow.open(map, marker);
    }

    // 페이지 로드 시 현재 위치 표시
    setCurrentLocation();

    // "현황" 버튼을 클릭하면 주차장 정보 로드
    document.querySelector('.map-button.current').addEventListener('click', function() {
        loadParkingLots(0);
    });
});