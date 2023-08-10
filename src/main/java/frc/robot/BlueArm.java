package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class BlueArm {

    // Blue Arm
    final CANSparkMax m_blue1 = new CANSparkMax(5, MotorType.kBrushless);
    final CANSparkMax m_blue2 = new CANSparkMax(6, MotorType.kBrushless);
    MotorControllerGroup m_blueArm = new MotorControllerGroup(m_blue1, m_blue2);
    RelativeEncoder blue_armEncoder;
    double blueEncoder;
    double blueMax = 78;
    double blueMin = 0;

    private final Joystick blueJoystick = new Joystick(4);
    final double blueDeadzone = 0.10;
    double desiredBlue;

    public void robotInit() {
        // invert blue arm motors
        m_blueArm.setInverted(true);
        blue_armEncoder = m_blue1.getEncoder();
    }

    public void robotPeriodic() {
        blueEncoder = blue_armEncoder.getPosition();
        SmartDashboard.putNumber("Blue Arm Encoder", blueEncoder);
        SmartDashboard.putNumber("desiredBlue", desiredBlue);
    }

    public void teleopPeriodic() {
        /**
         * Computes desired blue arm speed
         * 
         * desiredBlue equals the joystick value only if:
         * it won't move the arm out of bounds and
         * the value is bigger than the deadzone.
         */

        double desiredBlue = Threshold.getValue(blueJoystick.getRawAxis(1), blueDeadzone);

        if (blueEncoder >= blueMax) {
            // Arm extended too far, so don't allow motion in the positive direction.
            // Clamp to less than zero.
            Math.min(desiredBlue, 0);
        } else if (blueEncoder <= blueMin) {
            // Arm retracted too far, so don't allow motion in the negative direction.
            // Clamp to greater than zero
            Math.max(desiredBlue, 0);
        }
        m_blueArm.set(desiredBlue);
    }

    public void resetEncoder() {
        blue_armEncoder.setPosition(0);
    }

    public void setInverted(boolean isInverted) {
        m_blueArm.setInverted(true);
    }

    public void stopMotor() {
        m_blue1.stopMotor();
        m_blue2.stopMotor();
        m_blueArm.stopMotor();
    }

}
