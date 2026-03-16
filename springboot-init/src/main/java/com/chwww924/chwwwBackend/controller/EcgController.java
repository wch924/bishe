//package com.chwww924.chwwwBackend.controller;
//
//import com.chwww924.chwwwBackend.model.dto.ecg.EcgPoint;
//import com.chwww924.chwwwBackend.service.EcgService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/ecg")
//@Slf4j
//public class EcgController {
//    private final EcgService ecgService;
//
//    public EcgController(EcgService ecgService) {
//        this.ecgService = ecgService;
//    }
//
//    @PostMapping("/{sessionId}")
//    public ResponseEntity<?> saveEcgData(
//            @PathVariable Long sessionId,
//            @RequestBody List<EcgPoint> dataPoints) {
//
//        if (dataPoints.size() > 5000) {
//            return ResponseEntity.badRequest()
//                    .body("Exceed maximum batch size of 5000 records");
//        }
//
//        ecgService.saveEcgDataBatch(sessionId, dataPoints);
//        return ResponseEntity.ok().build();
//    }
//}
