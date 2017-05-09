/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
/* Written by T0xicFail                                                       */
/* (c) 2014                                                                   */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.*;
// import Java.Swag.*;
// import Java.FRC.Champs.St.Louis.*;

public class Voltage2014 extends IterativeRobot {

    private RobotDrive Drive;
    private Solenoid LowGear, HighGear, IntakeUp, IntakeDown, shooterLatch, shooterUnlatch, winchLock, winchUnlock;
    private Joystick Controller, Controller2;
    private Compressor Compressor;
    private SpeedController FrontLeft, FrontRight, BackLeft, BackRight, pivotArm, Shooter1, Shooter2, Intake;
    private DigitalInput Pivot, BtmShooterSwitch;
    private AnalogChannel potVal;
    private final DriverStationLCD lcd = DriverStationLCD.getInstance();
    private boolean hasShifted;
    private boolean isBottom;
    private boolean isReset;
    private boolean isFired;
    private boolean isPressure;
    private boolean isIntakeUp;
    private boolean isLatched;
    private boolean stopShooter;
    private boolean stopPreset;
    private boolean Adeg;
    private boolean Bdeg;
    private boolean Xdeg;
    private boolean Ydeg;
    private boolean bottomDeg;
    private double autoTimeStart;
    private double startDelay;
    private double endDelay;
    private double ShooterSpeed;
    private double PivotArmSpeed;
    private double IntakeSpeed;
    private double angleThreshold;
    private double currentAngle;
    private int ShortShot;

    public void robotInit() {
        ControllerInit();
        TalonInit();
        RobotDriveInit();
        CompressorInit();
        autonomousInit();
        IntakeInit();
        SensorInit();
        ShooterStuffInit();
        hasShifted = false;
        isIntakeUp = false;
        isLatched = false;
        isFired = false;
        isBottom = false;
        isReset = true;
        isPressure = false;
        stopShooter = false;
        Adeg = false;
        Bdeg = false;
        Xdeg = false;
        Ydeg = false;
        bottomDeg = false;
        currentAngle = 0;
        angleThreshold = .20;
        autoTimeStart = 0;
        ShooterSpeed = .625;
        PivotArmSpeed = 1;
        IntakeSpeed = .75;
        ShortShot = 10;

    }

    public void RobotDriveInit() {
        Drive = new RobotDrive(FrontLeft, FrontRight, BackLeft, BackRight);
    }

    public void ShooterStuffInit() {
        winchLock = new Solenoid(1);
        winchUnlock = new Solenoid(2);
        shooterLatch = new Solenoid(3);
        shooterUnlatch = new Solenoid(4);

    }

    public void SensorInit() {
        Pivot = new DigitalInput(2);// Digital I/O
        BtmShooterSwitch = new DigitalInput(3);
        potVal = new AnalogChannel(4);
    }

    public void CompressorInit() {
        Compressor = new Compressor(1, 1);// Digital I/O, Relay
        LowGear = new Solenoid(8);
        HighGear = new Solenoid(7);
    }

    public void IntakeInit() {
        IntakeUp = new Solenoid(6);
        IntakeDown = new Solenoid(5);

    }

    public void ControllerInit() {
        Controller = new Joystick(1);
        Controller2 = new Joystick(2);
    }

    public void TalonInit() {
        FrontLeft = new Talon(1);
        FrontRight = new Talon(2);
        BackLeft = new Talon(3);
        BackRight = new Talon(4);
        pivotArm = new Talon(5);
        Intake = new Talon(6);
        Shooter1 = new Talon(7);
        Shooter2 = new Talon(8);

    }

    public void autonomousInit() {
        autoTimeStart = Timer.getFPGATimestamp();
        startDelay = 0;
        endDelay = 0;

    }

    public void autonomousPeriodic() {
        double autoTimeCurr = Timer.getFPGATimestamp() - autoTimeStart;

        if (autoTimeCurr <= 0.3) {
            Intake.set(-1);
            pivotArm.set(-0.5);
        } else if (autoTimeCurr <= 0.6) {
            pivotArm.set(0);
        } else if (autoTimeCurr <= 0.8) {
            Intake.set(0);
        } else if (autoTimeCurr <= 2.8) {
            IntakeDown.set(false);
            IntakeUp.set(true);
            isIntakeUp = true;
        } else if (autoTimeCurr <= 2.84) {
            shooterLatch.set(false);
            shooterUnlatch.set(true);
        } else if (autoTimeCurr <= 3.1) {
        } else if (autoTimeCurr <= 6.0) {
            Drive.setSafetyEnabled(false);
            FrontLeft.set(.5);
            BackRight.set(-.5);
        } else if (autoTimeCurr <= 6.1) {
            Drive.setSafetyEnabled(true);
            FrontRight.set(0);
            BackRight.set(0);
        }
    }

    public void disabledInit() {
    }

    public void disabledPeriodic() {
    }

    public void Fire() {
        IntakeDown.set(false);
        IntakeUp.set(true);
        isIntakeUp = true;
        Timer.delay(0.25);
        shooterLatch.set(false);
        shooterUnlatch.set(true);
        Timer.delay(1.0);
        isLatched = false;
        isBottom = false;
        isReset = false;
        isFired = true;

    }

    public void BottomReset() {
        if (!isReset) {
            if (!isBottom && BtmShooterSwitch.get()) {
                IntakeUp.set(false);
                IntakeDown.set(true);
                winchLock.set(true);
                winchUnlock.set(false);
                Shooter1.set(-ShooterSpeed);
                Shooter2.set(-ShooterSpeed);
            } else if (!BtmShooterSwitch.get()) {

                shooterLatch.set(true);
                shooterUnlatch.set(false);
                Shooter1.set(0);
                Shooter2.set(0);
                Timer.delay(0.5);
                winchUnlock.set(true);
                winchLock.set(false);
                isBottom = true;
                isReset = true;
                isLatched = true;
                isFired = false;
                IntakeUp.set(false);
                IntakeDown.set(true);
            }
        }
    }

    public void PresetAngle() {
        if (potVal.getVoltage() < (currentAngle - angleThreshold)) {
            pivotArm.set(-1);
        } else if (potVal.getVoltage() > (currentAngle + angleThreshold)) {
            pivotArm.set(1);
        } else {
            pivotArm.set(0);
            Adeg = false;
            Bdeg = false;
            Xdeg = false;
            Ydeg = false;
            bottomDeg = false;
        }
    }

    public void teleopInit() {
    }

    public void teleopPeriodic() {

        lcd.println(DriverStationLCD.Line.kUser2, 1, "Bottom" + BtmShooterSwitch.get());
        lcd.updateLCD();
        lcd.println(DriverStationLCD.Line.kUser3, 1, "LinearPot: " + potVal.getAverageVoltage());
        lcd.updateLCD();

        //Drive
        Drive.arcadeDrive(getDeadZone(-Controller.getY(), 0.25),
                getDeadZone(-Controller.getThrottle(), 0.25), true);

        // Compressor
        if (!Compressor.getPressureSwitchValue()) {
            Compressor.start();
            lcd.println(DriverStationLCD.Line.kUser1, 1, "Compressor: ON ");
        } else if (Compressor.getPressureSwitchValue()) {
            Compressor.stop();
            lcd.println(DriverStationLCD.Line.kUser1, 1, "Compressor: OFF");
        }

        // Shifter
        if (Controller.getRawButton(6) && !hasShifted) {
            HighGear.set(true);
            Timer.delay(0.1);
            LowGear.set(false);
            hasShifted = true;
            lcd.println(DriverStationLCD.Line.kUser4, 1, "Speed: High");
            lcd.updateLCD();
        } else if (Controller.getRawButton(5) && hasShifted) {
            LowGear.set(true);
            Timer.delay(0.1);
            HighGear.set(false);
            hasShifted = false;
            lcd.println(DriverStationLCD.Line.kUser4, 1, "Speed: Low ");
            lcd.updateLCD();
        }

        //Intake
        if (Controller2.getRawButton(1)) {
            Intake.set(-1);
        } else if (Controller2.getRawButton(4)) {
            Intake.set(1);
        } else if (Controller2.getRawButton(2)) {
            Intake.set(0);
        }
        //IntakePiston

        if (Controller2.getRawButton(6)) {
            IntakeUp.set(false);
            IntakeDown.set(true);
        }
        if (Controller2.getRawButton(5)) {
            IntakeDown.set(false);
            IntakeUp.set(true);
        }
        //pivotArm
        if (Controller2.getRawAxis(3) > 0.9) {
            Adeg = false;
            pivotArm.set(PivotArmSpeed);
        } else if (Controller2.getRawAxis(3) < -0.9) {
            Adeg = false;
            pivotArm.set(-PivotArmSpeed);

        } else {
            pivotArm.set(0);
        }

        //AutoShot
        if (Controller.getRawAxis(3) < -0.9) {
            Fire();
            Timer.delay(0.5);
            BottomReset();
        } else {

            if (!isReset) {
                BottomReset();

            }
        }

        // presetAngles
        if (Controller.getRawButton(4) || Ydeg) {
            currentAngle = 1.89;
            Ydeg = true;
            potVal.getVoltage();
            PresetAngle();
        }
        if (Controller.getRawButton(2) || Bdeg) {
            currentAngle = 2.337;
            Bdeg = true;
            potVal.getVoltage();
            PresetAngle();
        }
        if (Controller.getRawButton(1) || Adeg) {
            currentAngle = 2.52;
            Adeg = true;
            potVal.getVoltage();
            PresetAngle();
        }
        if (Controller.getRawButton(3) || Xdeg) {
            currentAngle = 2.71;
            Xdeg = true;
            potVal.getVoltage();
            PresetAngle();
        }
        if (Controller2.getRawButton(3) || bottomDeg) {
            currentAngle = 3.900;
            bottomDeg = true;
            potVal.getVoltage();
            PresetAngle();
        }
    }

    private double getDeadZone(double num, double dead) {
        if (num < 0) {
            if (num < dead) {
                return num;
            } else {
                return 0;
            }
        } else {
            if (num > dead) {
                return num;
            } else {
                return 0;
            }
        }
    }

    public void testPeriodic() {

    }
}
