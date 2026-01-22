package com.jacolp.task_system.controller;

import com.jacolp.task_system.entity.PlayerStatus;
import com.jacolp.task_system.service.PlayerStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@RequestMapping("/player")
public class PlayerStatusController {
    @Autowired private PlayerStatusService playerStatusService;

    @GetMapping("/status")
    public ResponseEntity<PlayerStatus> getPlayerStatus() {
        return ResponseEntity.ok(playerStatusService.getCurrentPlayerStatus());
    }
}
