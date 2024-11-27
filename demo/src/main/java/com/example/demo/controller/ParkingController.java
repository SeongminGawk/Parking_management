package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.ParkingLot;
import com.example.demo.service.ParkingService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/parking") //기본 경로 설정
@Slf4j
public class ParkingController {

    @Autowired
    private ParkingService parkingService;

    @Value("${file.uploadDir}")
    private String uploadDir;

    @GetMapping("/registration")
    public String parkingRegistrationForm() {
        return "parking_registration";
    }

    @PostMapping("/register")
    @ResponseBody
    @Transactional // 트랜잭션 설정
    public ResponseEntity<Map<String, Object>> registerParking(
            @RequestParam("prk_lot_nm") String prkLotNm,
            @RequestParam("bas_addr") String basAddr,
            @RequestParam("dtl_addr") String dtlAddr,
            @RequestParam("prk_lot_id_a") String prkLotIdA,
            @RequestParam("prk_lot_id_b") String prkLotIdB,
            @RequestParam("prk_lot_id_c") String prkLotIdC,
            @RequestParam("inst_dt") String instDt,
            @RequestParam("latitude") String latitude,
            @RequestParam("longitude") String longitude,
            @RequestParam("prk_cmprt_co") int prkCmprtCo,
            @RequestParam(value = "opertn_day", required = false) String opertnDay,
            @RequestParam("prk_chrge_mthd") String prkChrgeMthd,
            @RequestParam("prk_image") MultipartFile prkImage,
            HttpSession session) throws IOException {

        Map<String, Object> response = new HashMap<>();
        log.info("registerParking called with prk_lot_nm: {}, bas_addr: {}, dtl_addr: {}", prkLotNm, basAddr, dtlAddr);

        try {
            // 세션에서 인증된 사용자 아이디 가져오기
            String regId = (String) session.getAttribute("userid");

            // 주차장 관리번호 조립
            String prkLotId = String.format("%s-%s-%s", prkLotIdA, prkLotIdB, prkLotIdC);

            // 이미지 파일 저장
            String imagePath = saveImage(prkImage);

            ParkingLot parkingLot = new ParkingLot();
            parkingLot.setPrkLotNm(prkLotNm);
            parkingLot.setZipcd(prkLotIdA);
            parkingLot.setBasAddr(basAddr);
            parkingLot.setDtlAddr(dtlAddr);
            parkingLot.setPrkLotId(prkLotId); // 주차장 관리번호 설정

            // instDt 필드가 not null 속성이므로 null로 설정하지 않음
            parkingLot.setInstDt(LocalDate.parse(instDt));

            parkingLot.setLatitude(latitude);
            parkingLot.setLongitude(longitude);
            parkingLot.setPrkCmprtCo(prkCmprtCo);
            
            if (opertnDay != null) {
                // 중복 제거
                Set<String> uniqueOpertnDays = new HashSet<>(Arrays.asList(opertnDay.split(",")));
                String opertnDaysStr = uniqueOpertnDays.stream().collect(Collectors.joining(","));
                parkingLot.setOpertnDay(opertnDaysStr);
            }
            
            parkingLot.setPrkChrgMthd(prkChrgeMthd);
            parkingLot.setImagePath(imagePath);
            parkingLot.setRgtrId(regId); // 인증된 사용자 ID 설정
            parkingLot.setRegDt(LocalDateTime.now());

            parkingService.save(parkingLot);

            response.put("status", "success");
            response.put("parkingLotId", parkingLot.getId()); // 등록된 주차장 ID 반환
            return ResponseEntity.ok(response);
        } catch (DataIntegrityViolationException e) {
            log.error("DataIntegrityViolationException: ", e);
            response.put("status", "error");
            response.put("message", "중복된 데이터가 있습니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (IOException e) {
            log.error("IOException: ", e);
            response.put("status", "error");
            response.put("message", "파일 저장 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            log.error("Exception: ", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getParkingLots(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Page<ParkingLot> parkingLots = parkingService.findAll(page, size);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", parkingLots.getContent());
        response.put("totalPages", parkingLots.getTotalPages());
        response.put("currentPage", parkingLots.getNumber());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getNextPrkLotSeq")
    @ResponseBody // GET 요청에 대해 /getNextPrkLotSeq 경로를 매핑합니다. 다음 주차장 순번을 JSON 형식으로 반환
    public Map<String, Integer> getNextPrkLotSeq() {
        Integer nextSeq = parkingService.getNextParkingLotSeq();
        Map<String, Integer> response = new HashMap<>();
        response.put("nextSeq", nextSeq);
        return response;
    }

    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody //이미지 파일을 읽고 없으면 404 또는 500 에러 반환
    public ResponseEntity<org.springframework.core.io.Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename);
            org.springframework.core.io.Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity
                        .ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    // MultipartFile 객체를 받아 이미지를 저장하는 메서드. 파일 이름을 현재 날짜와 시간으로 설정하여 중복을 피한다.
    private String saveImage(MultipartFile image) throws IOException {
        // 폴더 없으면 생성
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_" + image.getOriginalFilename();
        File file = new File(uploadDirFile, fileName);
        image.transferTo(file);
        return file.getName(); // 절대 경로가 아닌 파일 이름만 반환
    }

    @GetMapping("/details/{id}")
    public String getParkingLotDetails(@PathVariable Integer id, Model model) {
        //log.info("Fetching details for parking lot with ID: {}", id);
        Optional<ParkingLot> parkingLotOpt = parkingService.findById(id);
        if (parkingLotOpt.isPresent()) {
            ParkingLot parkingLot = parkingLotOpt.get();
            //log.info("Parking lot details: {}", parkingLot);
            model.addAttribute("parkingLot", parkingLot);
            model.addAttribute("imagePath", parkingLot.getImagePath());
            return "parking-details";
        } else {
            log.warn("Parking lot with ID {} not found", id);
            return "error/404"; // 적절한 에러 페이지로 리다이렉트
        }
    }

    //예외처리 메서드
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unhandled exception: ", e);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

