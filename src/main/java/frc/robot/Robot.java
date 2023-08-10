// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

/** Changed red max-min
 * camrea display
 * Motors
    * Check CAN Numbers ^
    * Do I Stop all motors when disabled^
    * invert^
    * motor safety
 * Joysticks
    * Check Joystick USB Ports^
    * Joystick axis correct^
    * Change to desired deadzones
 * Test buttons?
 * Pneumatics^
    * test shifter^
    * test claw^
 * Arm
    * Test overide
    * test zero^
    * test limits
 * Auto balance etc
 * limelight
 * 
 * 
 *   NetworkTableEntry ledMode = table.getEntry("ledMode");
  NetworkTableValue l_mode = ledMode.getInteger(0);
  l_mode = ledMode.putValue(1);
 * 
 * 
Solenoid exampleSolenoidPCM = new Solenoid(PneumaticsModuleType.CTREPCM, 1);
Solenoid exampleSolenoidPH = new Solenoid(PneumaticsModuleType.REVPH, 1);
exampleSolenoidPCM.set(true);
 */
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;

//What pneumatics stuff do I need?

import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.fasterxml.jackson.core.io.UTF32Reader;
import com.kauailabs.navx.AHRSProtocol.AHRSPosTSUpdate;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;

// Limelight
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableValue;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the
 * name of this class or
 * the package after creating this project, you must also update the
 * build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  MovementBlocks myBlocks = new MovementBlocks();

  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private static final String kVision = "Vision";
  private static final String kBalance = "Balance";
  private static final String kTopCubeBalance = "TopCubeBalance";
  private static final String kTopCubeMobilityBalance = "TopCubeMobilityBalance";
  private static final String kTopCubeMobility = "TopCubeMobility";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  // Left Drive
  final CANSparkMax m_frontLeft = new CANSparkMax(1, MotorType.kBrushless);
  final CANSparkMax m_rearLeft = new CANSparkMax(2, MotorType.kBrushless);
  MotorControllerGroup m_leftDrive = new MotorControllerGroup(m_frontLeft, m_rearLeft);
  RelativeEncoder l_driveEncoder;
  double leftEncoder;

  // Right Drive
  CANSparkMax m_frontRight = new CANSparkMax(3, MotorType.kBrushless);
  CANSparkMax m_rearRight = new CANSparkMax(4, MotorType.kBrushless);
  MotorControllerGroup m_rightDrive = new MotorControllerGroup(m_frontRight, m_rearRight);
  RelativeEncoder r_driveEncoder;
  double rightEncoder;

  // Drive
  DifferentialDrive m_robotDrive = new DifferentialDrive(m_leftDrive, m_rightDrive);

  BlueArm blueArm = new BlueArm();

  // Red Arm
  final CANSparkMax m_redArm = new CANSparkMax(7, MotorType.kBrushless);
  RelativeEncoder red_armEncoder;
  double redEncoder;
  double redMax = 0;
  double redMin = -157;

  // Joysticks
  private final Joystick driveJoystick = new Joystick(0);
  private final Joystick wheelJoystick = new Joystick(1);
  private final Joystick redJoystick = new Joystick(5);

  final double driveDeadzone = 0.10;
  final double wheelDeadzone = 0.10;
  final double redDeadzone = 0.10;

  double desiredThrottle;
  double desiredWheel;
  double desiredRed;

  // Pneumatics
  Solenoid shifter = new Solenoid(PneumaticsModuleType.CTREPCM, 1);
  Solenoid claw = new Solenoid(PneumaticsModuleType.CTREPCM, 2);

  private final Timer m_timer = new Timer();

  // Gyro
  AHRS ahrs = new AHRS();

  // Limelight
  NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
  NetworkTableEntry tx = table.getEntry("tx");
  NetworkTableEntry ty = table.getEntry("ty");
  NetworkTableEntry ta = table.getEntry("ta");
  NetworkTableEntry ledMode = table.getEntry("ledMode");

  double l_x = tx.getDouble(0.0);
  double l_y = ty.getDouble(0.0);
  double l_area = ta.getDouble(0.0);

  boolean done;

  /**
   * This function is run when the robot is first started up and should be used
   * for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    m_chooser.addOption("Vision", kVision);
    m_chooser.addOption("Balance", kBalance);
    m_chooser.addOption("TopCubeBalance", kTopCubeBalance);
    m_chooser.addOption("TopCubeMobilityBalance", kTopCubeMobilityBalance);
    m_chooser.addOption("TopCubeMobility", kTopCubeMobility);

    SmartDashboard.putData("Auto choices", m_chooser);

    // invert right drive
    m_rightDrive.setInverted(true);


    // Encoder objects are created
    l_driveEncoder = m_frontLeft.getEncoder();
    r_driveEncoder = m_frontRight.getEncoder();
    red_armEncoder = m_redArm.getEncoder();

    blueArm.robotInit();

    // AHRS gyro = new AHRS();
    // ahrs new AHRS();
    ahrs.zeroYaw();

    myBlocks.printMessage("Salutations");

  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items
   * like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    // Reads Gyro Angles
    ahrs.getYaw(); // in degrees, from -180 to 180 z axis
    ahrs.getAngle(); // angle is continuous, z axis
    ahrs.getPitch(); // (in degrees, from -180 to 180) x axis
    ahrs.getRoll(); // in degrees, from -180 to 180 x axis

    // Displays gyro values on SmartDashboared
    SmartDashboard.putNumber("Yaw", ahrs.getYaw());
    SmartDashboard.putNumber("Total Yaw", ahrs.getAngle());
    SmartDashboard.putNumber("Pitch", ahrs.getPitch());
    SmartDashboard.putNumber("Roll", ahrs.getRoll());

    SmartDashboard.putNumber("desiredRed", desiredRed);

    // Puts incoder values in variables
    leftEncoder = l_driveEncoder.getPosition();
    rightEncoder = r_driveEncoder.getPosition();
    redEncoder = red_armEncoder.getPosition();

    //red_armEncoder.setPosition(kDefaultPeriod);

    // Displays encoder variable values on SmartDashboard
    SmartDashboard.putNumber("Left Drive Encoder", leftEncoder);
    SmartDashboard.putNumber("Right Drive Encoder", rightEncoder);
    SmartDashboard.putNumber("Red Arm Encoder", redEncoder);
    SmartDashboard.updateValues();

    // post limelight to smart dashboard
    l_x = tx.getDouble(0.0);
    l_y = ty.getDouble(0.0);
    // ta.setBoolean(true);
    l_area = ta.getDouble(0.0);
    SmartDashboard.putNumber("LimelightX", l_x);
    SmartDashboard.putNumber("LimelightY", l_y);
    SmartDashboard.putNumber("LimelightArea", l_area);

  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different
   * autonomous modes using the dashboard. The sendable chooser code works with
   * the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the
   * chooser code and
   * uncomment the getString line to get the auto name from the text box below the
   * Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure
   * below with additional strings. If using the SendableChooser make sure to add
   * them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    ahrs.zeroYaw();
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        if (done == false) {
          if (leftEncoder + rightEncoder / 2 >= 500) {
            while (leftEncoder + rightEncoder / 2 >= 500) {
              m_robotDrive.arcadeDrive(1, 0, false);
            }
          } else {
            done = true;
          }
        }
        break;

      case kVision:
        // Put custom auto code here
        break;

      case kBalance:
        // Put custom auto code here
        break;

      case kTopCubeBalance:
        // Put custom auto code here
        break;

      case kTopCubeMobilityBalance:
        // Put custom auto code here
        break;

      case kTopCubeMobility:
        // Put custom auto code here
        break;

      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    // Computes desired throttle speed
    desiredThrottle = Threshold.getValue(driveJoystick.getRawAxis(1), driveDeadzone);
    // Computes desired spin speed
    desiredWheel = Threshold.getValue(wheelJoystick.getRawAxis(0), driveDeadzone);

    // update blueArm for teleopPeriodic.
    blueArm.teleopPeriodic();

    /*
     * Computes desired red arm speed
     * 
     * desiredRed equals the joystick value only if:
     * it won't move the arm out of bounds and
     * the value is bigger than the deadzone.
     */

    if (redEncoder >= redMax && redJoystick.getRawButton(3) == false) {
      if (redJoystick.getRawAxis(1) > 0 || redJoystick.getRawAxis(1) < redDeadzone) {
        desiredRed = 0.0;
      } else {
        desiredRed = redJoystick.getRawAxis(1);
      }
    } else if (redEncoder <= redMin && redJoystick.getRawButton(3) == false) {
      if (redJoystick.getRawAxis(1) < 0 || redJoystick.getRawAxis(1) < redDeadzone) {
        desiredRed = 0.0;
      } else {
        desiredRed = redJoystick.getRawAxis(1);
      }
    } else {
      if (redJoystick.getRawAxis(1) < redDeadzone) {
        desiredRed = 0.0;
      } else {
        desiredRed = redJoystick.getRawAxis(1);
      }
    }

    // Sets all motor speeds
    m_robotDrive.arcadeDrive(desiredThrottle, desiredWheel);
    m_redArm.set(desiredRed);

    // Controls shifter solenoid with trigger
    shifter.set(driveJoystick.getRawButton(1));

    // Controls claw solenoid with trigger
    claw.set(redJoystick.getRawButton(1));

    // Resets incoders when button 4 is pressed on the redJoystick
    if (redJoystick.getRawButtonPressed(5) == true) {
      red_armEncoder.setPosition(0);
      blueArm.resetEncoder();
    }

    // Reset Motor Inversion
    if (wheelJoystick.getRawButton(1) == true) {
      m_rightDrive.setInverted(true);
      m_leftDrive.setInverted(false);
      m_redArm.setInverted(false);
      blueArm.setInverted(true);
    }
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {
    m_frontLeft.stopMotor();
    m_rearLeft.stopMotor();
    m_leftDrive.stopMotor();

    m_frontRight.stopMotor();
    m_rearRight.stopMotor();
    m_rightDrive.stopMotor();

    m_redArm.stopMotor();
    blueArm.stopMotor();
  }

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
  }

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {
  }

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {

  }
}
