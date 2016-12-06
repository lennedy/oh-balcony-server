package io.github.ohbalcony;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.github.ohbalcony.model.HardwareController;
import io.github.ohbalcony.model.HardwareReference;
import io.github.ohbalcony.model.Instructions;
import io.github.ohbalcony.model.MoistureSensor;
import io.github.ohbalcony.model.Pump;
import io.github.ohbalcony.model.SensorData;
import io.github.ohbalcony.model.SystemState;
import io.github.ohbalcony.model.Tank;
import io.github.ohbalcony.model.Valve;
import io.github.ohbalcony.model.Zone;

@RestController
@CrossOrigin // TODO security: don't allow for any origin for production
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    
    private final Store store;

    @Autowired
    public Controller(Store store) {
        this.store = store;
    }
    
    @GetMapping(value = "/")
    public SystemState getSystemState() {
        
        SystemState systemState = new SystemState();
        
        HardwareController hardwareController = new HardwareController();
        hardwareController.id = "controller1";
        hardwareController.name = "Raspberry Pi";
        systemState.controllers.add(hardwareController);
        
        Pump pump = new Pump();
        pump.id = "pump1";
        pump.name = "Pump 1";
        hardwareController.pumps.add(pump);
        
        MoistureSensor moistureSensor1 = new MoistureSensor();
        moistureSensor1.id = "moisture1";
        moistureSensor1.name = "Strawberries";
        hardwareController.moistureSensors.add(moistureSensor1);
        
        MoistureSensor moistureSensor2 = new MoistureSensor();
        moistureSensor2.id = "moisture2";
        moistureSensor2.name = "Wine";
        hardwareController.moistureSensors.add(moistureSensor2);
        
        Valve valve = new Valve();
        valve.id = "valve1";
        valve.name = "Valve 1";
        hardwareController.valves.add(valve);
        
        Tank tank = new Tank();
        tank.id = "tank1";
        tank.name = "Water tank";
        hardwareController.tanks.add(tank);
        
        Zone zone = new Zone();
        HardwareReference hardwareReference = new HardwareReference();
        hardwareReference.controllerId = hardwareController.id;
        hardwareReference.componentId = pump.id;
        zone.hardwareComponents.add(hardwareReference);
        systemState.zones.add(zone);
        
        return systemState;
    }

    @RequestMapping(value = "/store", method = RequestMethod.POST)
    public Instructions store(@RequestBody SensorData sensorData) {
        store.save(sensorData);
        
        boolean shouldWater = shouldWater();
        
        // TODO hardcoded pump and valve names
        Instructions instructions = new Instructions();
        instructions.pumps.put("pump1", shouldWater);
        instructions.valves.put("valve1", shouldWater);
        return instructions;
    }

    private boolean shouldWater() {
        boolean shouldWater = false;
        
        // TODO hardcoded times
        LocalTime startTime1 = LocalTime.of(8, 0);
        LocalTime endTime1 = LocalTime.of(8, 2);
        
        LocalTime startTime2 = LocalTime.of(15, 0);
        LocalTime endTime2 = LocalTime.of(15, 1);
        
        LocalDateTime now = LocalDateTime.now();
        LocalTime nowTime = now.toLocalTime();
        if ( (nowTime.isAfter(startTime1) && nowTime.isBefore(endTime1)) ||
             (nowTime.isAfter(startTime2) && nowTime.isBefore(endTime2)) ) {
            // TODO hardcoded tank name
            double tankWaterLevel = store.getCurrentTankLevel("tank1");
            
            shouldWater = tankWaterLevel > 0.0;
            
            if(shouldWater)
                log.info("Watering now");
            else
                log.info("NOT watering, because the tank is empty.");
        }
        return shouldWater;
    }

}
