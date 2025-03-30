package com.irankiai.backend.Robot;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RobotController {

    private final RobotService robotService;

    @Autowired
    public RobotController(RobotService robotService) {
        this.robotService = robotService;
    }

    @GetMapping("/robot")
    public ResponseEntity<Robot> getRobot(@RequestParam Integer id) {
        Optional<Robot> robot = robotService.getRobot(id);
        return robot.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/robots")
    public List<Robot> getAllRobots() {
        return robotService.getAllRobots();
    }

    @PostMapping("/robot")
    public ResponseEntity<Robot> addRobot(@RequestBody Robot robot) {
        Robot savedRobot = robotService.addRobot(robot);
        return new ResponseEntity<>(savedRobot, HttpStatus.CREATED);
    }
    
    @PutMapping("/robot")
    public ResponseEntity<Robot> updateRobot(@RequestBody Robot robot) {
        Robot updatedRobot = robotService.updateRobot(robot);
        return ResponseEntity.ok(updatedRobot);
    }
    
    @DeleteMapping("/robot/{id}")
    public ResponseEntity<Void> deleteRobot(@PathVariable Integer id) {
        robotService.deleteRobot(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/robot/{robotId}/pickup/{containerId}")
    public ResponseEntity<Robot> pickupContainer(
            @PathVariable Integer robotId, 
            @PathVariable Integer containerId) {
        Robot robot = robotService.pickupContainer(robotId, containerId);
        if (robot != null) {
            return ResponseEntity.ok(robot);
        }
        return ResponseEntity.badRequest().build();
    }
    
    @PostMapping("/robot/{robotId}/drop")
    public ResponseEntity<Robot> dropContainer(@PathVariable Integer robotId) {
        Robot robot = robotService.dropContainer(robotId);
        if (robot != null) {
            return ResponseEntity.ok(robot);
        }
        return ResponseEntity.badRequest().build();
    }
    
    @PostMapping("/robot/{robotId}/charge")
    public ResponseEntity<Robot> chargeBattery(
            @PathVariable Integer robotId,
            @RequestParam int amount) {
        Robot robot = robotService.chargeBattery(robotId, amount);
        if (robot != null) {
            return ResponseEntity.ok(robot);
        }
        return ResponseEntity.notFound().build();
    }
}