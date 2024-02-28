// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.math.controller.BangBangController;
import frc.robot.Constants;
import frc.robot.subsystems.Climber;

public class ClimbDown extends Command {
  public Climber climber;
  /** Creates a new ClimbDown. */

  public BangBangController controller = new BangBangController();

  public ClimbDown(Climber climber) {
    this.climber = climber;

    addRequirements(climber);
    // Use addRequirements() here to declare subsystem dependencies.
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    //climber.setSpeed(-controller.calculate(Constants.ClimberConstants.kClimberSpeed));
    climber.setSpeed(-Constants.ClimberConstants.kClimberSpeed);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    climber.setSpeed(0.0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {

    // if(climber.getLeftMotorVoltage()>11.5 || climber.getRightMotorVoltage()>11.5){
    //   return true;
    // }
    return false;
  }
}
