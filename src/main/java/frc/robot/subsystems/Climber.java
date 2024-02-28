// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkBase.IdleMode;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Climber extends SubsystemBase {
  /** Creates a new Climber. */
  private CANSparkMax leftMotor;
  private CANSparkMax rightMotor;

  
  
  public Climber() {
    leftMotor = new CANSparkMax(Constants.ClimberConstants.leftCANSparkID, MotorType.kBrushless);
    rightMotor = new CANSparkMax(Constants.ClimberConstants.rightCANSparkID, MotorType.kBrushless);

    leftMotor.setIdleMode(IdleMode.kBrake);
    rightMotor.setIdleMode(IdleMode.kBrake);
  }

  public void setSpeed(double speed){

    // rightMotor.setSmartCurrentLimit(15);
    // leftMotor.setSmartCurrentLimit(15);
    
    leftMotor.set(speed);
    rightMotor.set(-speed);


  }

  public void setCoast(){
    leftMotor.setIdleMode(IdleMode.kCoast);
    rightMotor.setIdleMode(IdleMode.kCoast);
  }

  public void setBrake(){
    leftMotor.setIdleMode(IdleMode.kBrake);
    rightMotor.setIdleMode(IdleMode.kBrake);
  }

  public double getLeftMotorVoltage(){
    return leftMotor.getBusVoltage();
  }

  public double getRightMotorVoltage(){
    return rightMotor.getBusVoltage();
  }

  public RelativeEncoder getLeftEncoder() {
    return leftMotor.getEncoder();
  }

  public RelativeEncoder getRightEncoder() {
    return rightMotor.getEncoder();
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("AVERAGE CLIMBER VOLTAGE: ", (getLeftMotorVoltage()+getRightMotorVoltage())/2);
    //This method will be called once per scheduler run
  }
}
