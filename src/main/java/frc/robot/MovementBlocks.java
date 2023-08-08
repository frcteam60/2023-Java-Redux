package frc.robot;

public class MovementBlocks extends Robot {
  //
  public void printMessage(String message) {
    System.out.println(message);
  }

  public void turn2Angle(double desiredAngle, double closeEnough) {
    double error = ahrs.getYaw() - desiredAngle;
    double speed;
    // Runs while
    while (error >= closeEnough || error <= (closeEnough * -1)) {
      error = ahrs.getYaw() - desiredAngle;
      speed = error * 1 + 0.2;
      m_robotDrive.arcadeDrive(0, speed);
      m_robotDrive.arcadeDrive(0, 0);
    }
  }

  public void driveStraight(double desiredAngle, double speed, double inches) {
    double error = ahrs.getYaw() - desiredAngle;
    double ratio = 1;
    double desiredDistance = inches * ratio;
    double rotation;
    /*while () {
      error = ahrs.getYaw() - desiredAngle;
      rotation = error * 1 + 0.2;
      m_robotDrive.arcadeDrive(speed, rotation);
      m_robotDrive.tankDrive(speed, speed, false);
      */
    


  }

}
