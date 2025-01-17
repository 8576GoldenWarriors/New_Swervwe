// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;


import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color8Bit;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.Command.InterruptionBehavior;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.commands.Climb;
import frc.robot.commands.ClimbDown;
import frc.robot.commands.FFShooterAngle;
import frc.robot.commands.IntakeDown;
import frc.robot.commands.IntakeIn;
import frc.robot.commands.IntakeOut;
import frc.robot.commands.IntakeUp;
import frc.robot.commands.OverrideIntakeDown;
import frc.robot.commands.OverrideIntakeUp;
import frc.robot.commands.ReverseShoot;
import frc.robot.commands.SetShooterAmp;
import frc.robot.commands.SetShooterAngle;
import frc.robot.commands.Shoot;
import frc.robot.commands.ShooterDown;
import frc.robot.commands.ShooterUp;
import frc.robot.commands.SwerveDrive;
import frc.robot.subsystems.Climber;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.IntakeRoller;
import frc.robot.subsystems.LEDStrip;
import frc.robot.subsystems.PhasingLEDPattern;
import frc.robot.subsystems.PhyscialLEDStrip;
import frc.robot.subsystems.RainbowLEDPattern;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.ShooterRoller;


public class RobotContainer {

  public static final Drivetrain drivetrain = Drivetrain.getInstance();


  public static final Intake m_Intake = new Intake();
  public static final IntakeRoller m_IntakeRoller = new IntakeRoller();

  public static final Shooter m_Shooter = new Shooter();
  public static final ShooterRoller m_ShooterRoller = new ShooterRoller();
  public static final Climber m_Climber = new Climber();

  public final LEDStrip ledStrip;
 // public static final LED m_led = new LED(Constants.LEDConstants.LED_PORT1, Constants.LEDConstants.LedLength1);

 

  public static final CommandXboxController driverController = new CommandXboxController(Constants.ControllerConstants.kDriverControllerPort);
  public static final CommandXboxController operatorController = new CommandXboxController(Constants.ControllerConstants.kOperatorControllerPort);

  private final JoystickButton resetHeading_Start = new JoystickButton(driverController.getHID(), XboxController.Button.kStart.value);

  private final SendableChooser<Command> autoChooser;

  public RobotContainer() {

    ledStrip = new PhyscialLEDStrip(9, 58); //58

    registerNamedCommands();

    configureBindings();

    drivetrain.setDefaultCommand(new SwerveDrive());
   //Add all the choise of Autonomous modes to the Smart Dashboard
    autoChooser = AutoBuilder.buildAutoChooser();


  
    SmartDashboard.putData("AutoChooser", autoChooser);

    

  }


  private void configureBindings() {
    
    //Driver controller
    resetHeading_Start.onTrue(
      new InstantCommand(drivetrain::zeroHeading, drivetrain));

    ledStrip.setDefaultCommand(new RunCommand(() -> {
       
      if (!m_IntakeRoller.getDigitalInput().get()){//note is loaded
        if(m_ShooterRoller.isRevved()){ //shooter is activated -> cyan
          ledStrip.usePattern(new PhasingLEDPattern(new Color8Bit(57, 190, 165), 1.0));
        }
        else if(m_ShooterRoller.isAmping()){// shooter in amp state -> purple
          ledStrip.usePattern(new PhasingLEDPattern(new Color8Bit(255, 0, 200), 1.0));
        }
        else { //shooter is not running -> green
          ledStrip.usePattern(new PhasingLEDPattern(new Color8Bit(44, 255,10), 0.5));
        }
      }
      else if (m_Climber.getRainbowBoolean()){ //if climbers are extending -> rainbow
          ledStrip.usePattern(new RainbowLEDPattern(5, 7));
        }
       
      else if(RobotState.isDisabled()){ //when robot is disabled -> yellow
          ledStrip.usePattern(new PhasingLEDPattern(new Color8Bit(255, 255, 0), 0.5));
      }
      else{
        // default -> red
        ledStrip.usePattern(new PhasingLEDPattern(new Color8Bit(255, 0, 0
        ), 0.5));
      }
      



    }, ledStrip)); 
      
    //Operator Controller

     // Intake
    //operatorController.a().whileTrue(new IntakeUp(m_Intake));
    // operatorController.a().whileTrue(new IntakeDown(m_Intake));//new SequentialCommandGroup(new IntakeIn(m_Intake), new IntakeUp(m_Intake), new Shoot(m_Shooter)));
    // operatorController.x().whileTrue(new IntakeUp(m_Intake));


    //default
    operatorController.a().onTrue(new SequentialCommandGroup(
      new IntakeDown(m_Intake),
      new IntakeIn(m_IntakeRoller).until(() -> !m_IntakeRoller.getDigitalInput().get()), 
      new IntakeIn(m_IntakeRoller).withTimeout(0.8), 
      new ParallelCommandGroup(
        new InstantCommand(() -> m_ShooterRoller.setSpeed(0.525)),
        new InstantCommand(() -> m_ShooterRoller.setRevved(true)), 
        new IntakeUp(m_Intake), 
        new SetShooterAngle(m_Shooter, 0.045)))); //0.015


     //High angle, same speed - Source to mid launch - assembly line pt1
    operatorController.x().onTrue(new SequentialCommandGroup(
      new IntakeDown(m_Intake),
      new IntakeIn(m_IntakeRoller).until(() -> !m_IntakeRoller.getDigitalInput().get()), 
      new IntakeIn(m_IntakeRoller).withTimeout(0.8), 
      new ParallelCommandGroup(
        new InstantCommand(() -> m_ShooterRoller.setSpeed(0.525)),
        new InstantCommand(() -> m_ShooterRoller.setRevved(true)), 
        new IntakeUp(m_Intake), 
        new SetShooterAngle(m_Shooter, 0.058))));

    //assembly line pt2
    operatorController.y().onTrue(new SequentialCommandGroup(
      new IntakeDown(m_Intake),
      new IntakeIn(m_IntakeRoller).until(() -> !m_IntakeRoller.getDigitalInput().get()), 
      new IntakeIn(m_IntakeRoller).withTimeout(0.8), 
      new ParallelCommandGroup(
        new InstantCommand(() -> m_ShooterRoller.setSpeed(0.4)),
        new InstantCommand(() -> m_ShooterRoller.setRevved(true)), 
        new IntakeUp(m_Intake), 
        new SetShooterAngle(m_Shooter, 0.015))));



    //operatorController.a().whileTrue(new IntakeIn(m_IntakeRoller));
    //operatorController.x().whileTrue(new IntakeIn(m_IntakeRoller));

    operatorController.y().and(operatorController.leftBumper()).whileTrue(new ShooterUp(m_Shooter));
    operatorController.b().and(operatorController.leftBumper()).whileTrue(new ShooterDown(m_Shooter));

    operatorController.b().onTrue(new InstantCommand(() -> m_ShooterRoller.setSpeed(0)));


    driverController.povDown().whileTrue(new OverrideIntakeUp(m_Intake));
    driverController.povUp().whileTrue(new OverrideIntakeDown(m_Intake));

    //Shooter
  
    //operatorController.y().onTrue(new Shoot(m_ShooterRoller)); //b button ends shoot command, defined in shoot command
    //Shintake
    // operatorController.povDown().whileTrue(new Shintake(m_Shooter));
    
    //Climber
    //Window button is button #7. Retracts the climber.
    // operatorController.back().whileTrue(new Climb(m_Climber));
    driverController.b().whileTrue(new Climb(m_Climber));
    // //Three line button is button #8. Extends the climber.
    driverController.a().whileTrue((new ClimbDown(m_Climber)));

    driverController.rightBumper().whileTrue(new IntakeOut(m_IntakeRoller));
    

    driverController.button(7).onTrue(new InstantCommand(() -> m_Shooter.zeroEncoder()));
    // operatorController.start().whileTrue(new ClimbDown(m_Climber));

    //driverController.y().onTrue(getAutonomousCommand())
    
    //Arm
    //Commented bindings match the documented bindings
    // operatorController.leftBumper().and(operatorController.a()).whileTrue( new  OverrideIntakeDown(m_Intake));
    // operatorController.leftBumper().and(operatorController.x()).whileTrue( new OverrideIntakeUp(m_Intake));
    operatorController.a().and(operatorController.leftBumper()).onTrue(new IntakeUp(m_Intake));
    operatorController.x().and(operatorController.leftBumper()).onTrue(new IntakeDown(m_Intake));

    //test code for shooter pivot

    operatorController.rightBumper().whileTrue(new IntakeIn(m_IntakeRoller));
    
    

    operatorController.povLeft().onTrue(new SetShooterAngle(m_Shooter, 0.015)); //0.015
    operatorController.povUp().onTrue(new SetShooterAngle(m_Shooter, 0.0520)); //0.058
    operatorController.povRight().onTrue(new SetShooterAmp(m_Shooter, 0.097, m_ShooterRoller));

    
    
    //operatorController.povLeft().onTrue(new FFShooterAngle(m_Shooter, 0.015));
    operatorController.povDown().onTrue(new FFShooterAngle(m_Shooter, 0.055));

    //************************ */

    driverController.leftBumper().whileTrue(new ParallelCommandGroup(new IntakeIn(m_IntakeRoller), new ReverseShoot(m_ShooterRoller)));
  }


  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return autoChooser.getSelected();
  }

  //Autonomous Commands:
  public void registerNamedCommands(){
    //Drivetrain Commands:
    NamedCommands.registerCommand("ResetHeading", (new InstantCommand(() -> drivetrain.zeroHeading())).deadlineWith(new InstantCommand(() ->  new WaitCommand(1))));

    NamedCommands.registerCommand("StopModules", (new InstantCommand(() -> drivetrain.stopModules())).deadlineWith(new InstantCommand(() ->  new WaitCommand(1))));
    //Shooter Commands:
    NamedCommands.registerCommand("RunShooter", (new InstantCommand(() -> m_ShooterRoller.setSpeed(Constants.ShooterConstants.kShooterSpeed))));
    NamedCommands.registerCommand("StopShooter", (new InstantCommand(() -> m_ShooterRoller.stopShooter())).deadlineWith(new InstantCommand(() ->  new WaitCommand(0.2))));
    NamedCommands.registerCommand("ShooterMiddle", (new SetShooterAngle(m_Shooter, .018)));//new InstantCommand(() -> m_Shooter.setPivotSpeed(Constants.ShooterConstants.kPivotDownSpeed))).deadlineWith(new InstantCommand(() ->  new WaitCommand(0.5))));
    NamedCommands.registerCommand("ShooterUp", (new SetShooterAngle(m_Shooter, 0.055)).deadlineWith(new WaitCommand(2)));//new InstantCommand(() -> m_Shooter.setPivotSpeed(Constants.ShooterConstants.kPivotUpSpeed))).deadlineWith(new InstantCommand(() ->  new WaitCommand(0.5))));
    NamedCommands.registerCommand("ShooterDown", (new SetShooterAngle(m_Shooter, 0)).deadlineWith(new WaitCommand(2)));//new SetShooterAngle(m_Shooter, 0.07)));//new InstantCommand(() -> m_Shooter.setPivotSpeed(Constants.ShooterConstants.kPivotUpSpeed))).deadlineWith(new InstantCommand(() ->  new WaitCommand(0.5))));
    NamedCommands.registerCommand("StopShooterPivot", (new InstantCommand(() -> m_Shooter.setPivotSpeed(0))).deadlineWith(new InstantCommand(() ->  new WaitCommand(0.5))));
    NamedCommands.registerCommand("ResetEncoders", (new InstantCommand(() -> m_Shooter.zeroEncoder()).deadlineWith(new InstantCommand(() -> new WaitCommand(0.5)))));
    NamedCommands.registerCommand("ShooterManualDown", (new InstantCommand(() -> m_Shooter.setPivotSpeed(-0.4))).deadlineWith(new InstantCommand(() ->  new WaitCommand(0.5))));
    NamedCommands.registerCommand("ShooterManualUp", (new InstantCommand(() -> m_Shooter.setPivotSpeed(0.4))).deadlineWith(new InstantCommand(() ->  new WaitCommand(0.5))));


    //Intake Commands:
    NamedCommands.registerCommand("IntakeUp", (new IntakeUp(m_Intake)));
    NamedCommands.registerCommand("ManualIntakeUp", (new InstantCommand(() -> m_Intake.setArmSpeed(Constants.IntakeConstants.kArmUpSpeed))).deadlineWith(new InstantCommand(() ->  new WaitCommand(2))));
    NamedCommands.registerCommand("IntakeDown", (new IntakeDown(m_Intake)));
    NamedCommands.registerCommand("ManualIntakeDown", (new InstantCommand(() -> m_Intake.setArmSpeed(-0.8))).deadlineWith(new InstantCommand(() ->  new WaitCommand(2))));
    NamedCommands.registerCommand("IntakeOut", (new InstantCommand(() -> m_IntakeRoller.setRollerSpeed(-0.95))).deadlineWith(new InstantCommand(() ->  new WaitCommand(3))));
    NamedCommands.registerCommand("IntakeIn", (new InstantCommand(() -> m_IntakeRoller.setRollerSpeed(0.95))).deadlineWith(new InstantCommand(() ->  new WaitCommand(1.5))));
    NamedCommands.registerCommand("StopIntake", (new InstantCommand(() -> m_IntakeRoller.stopRollerSpeed())).deadlineWith(new InstantCommand(() ->  new WaitCommand(0.2))));
    NamedCommands.registerCommand("IntakePIDReset", (new InstantCommand(() -> m_Intake.zeroEncoder())));


    //Macros
    NamedCommands.registerCommand("MacroCommand", (new SequentialCommandGroup(
      new IntakeDown(m_Intake),
      new IntakeIn(m_IntakeRoller).until(() -> !m_IntakeRoller.getDigitalInput().get()), 
      new IntakeIn(m_IntakeRoller).withTimeout(0.4),
      new ParallelCommandGroup(
        //new Shoot(m_ShooterRoller).until(() -> m_IntakeRoller.getDigitalInput().get()), 
        new IntakeUp(m_Intake), 
        new SetShooterAngle(m_Shooter, 0.016)))));
    
    NamedCommands.registerCommand("MacroCommandStage", (new SequentialCommandGroup(
      new IntakeDown(m_Intake),
      new IntakeIn(m_IntakeRoller).until(() -> !m_IntakeRoller.getDigitalInput().get()),
      new IntakeIn(m_IntakeRoller).withTimeout(0.4), 
      new ParallelCommandGroup(
        //new Shoot(m_ShooterRoller).until(() -> m_IntakeRoller.getDigitalInput().get()), 
        new IntakeUp(m_Intake), 
        new SetShooterAngle(m_Shooter, 0.007)))));

    NamedCommands.registerCommand("checkNote", new InstantCommand(() -> m_IntakeRoller.checkNote()));
  }
}