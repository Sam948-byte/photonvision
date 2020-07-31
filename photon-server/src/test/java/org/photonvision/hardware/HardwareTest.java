/*
 * Copyright (C) 2020 Photon Vision.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.photonvision.hardware;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.photonvision.common.hardware.GPIO.CustomGPIO;
import org.photonvision.common.hardware.GPIO.GPIOBase;
import org.photonvision.common.hardware.GPIO.PiGPIO;
import org.photonvision.common.hardware.Platform;
import org.photonvision.common.hardware.metrics.CPU;
import org.photonvision.common.hardware.metrics.GPU;
import org.photonvision.common.hardware.metrics.RAM;

public class HardwareTest {

    @Test
    public void testHardware() {
        CPU cpu = CPU.getInstance();
        RAM ram = RAM.getInstance();
        GPU gpu = GPU.getInstance();

        if (!Platform.isRaspberryPi()) return;

        System.out.println("Testing on platform: " + Platform.CurrentPlatform);

        System.out.println("Printing CPU Info:");
        System.out.println("Memory: " + cpu.getMemory() + "MB");
        System.out.println("Temperature: " + cpu.getTemp() + "C");
        System.out.println("Utilization: : " + cpu.getUtilization() + "%");

        System.out.println("Printing GPU Info:");
        System.out.println("Memory: " + gpu.getMemory() + "MB");
        System.out.println("Temperature: " + gpu.getTemp() + "C");

        System.out.println("Printing RAM Info: ");
        System.out.println("Used RAM: : " + ram.getUsedRam() + "MB");
    }

    @Test
    public void testGPIO() {
        GPIOBase gpio;
        if (Platform.isRaspberryPi()) {
            gpio = new PiGPIO(18, 0, 100);
        } else {
            gpio = new CustomGPIO(18);
        }

        gpio.setHigh(); // HIGH
        assertTrue(gpio.getState());

        gpio.setLow(); // LOW
        assertFalse(gpio.getState());

        gpio.togglePin(); // HIGH
        assertTrue(gpio.getState());

        gpio.togglePin(); // LOW
        assertFalse(gpio.getState());

        gpio.setState(true); // HIGH
        assertTrue(gpio.getState());

        gpio.setState(false); // LOW
        assertFalse(gpio.getState());

        var success = gpio.shutdown();
        assertTrue(success);
    }

    @Test
    public void testBlink() {
        if (!Platform.isRaspberryPi()) return;
        GPIOBase pwm = new PiGPIO(18, 0, 100);
        pwm.blink(125, 3);
        var startms = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startms > 4500) break;
        }
    }
}
