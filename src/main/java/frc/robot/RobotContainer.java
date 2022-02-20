package frc.robot;

import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.commandgroups.ShootCargo;
import frc.robot.commandgroups.bits.TestBallFlow;
import frc.robot.subsystems.conveyor.Conveyor;
import frc.robot.subsystems.conveyor.commands.Convey;
import frc.robot.subsystems.drivetrain.SwerveDrive;
import frc.robot.subsystems.drivetrain.commands.OverpoweredDrive;
import frc.robot.subsystems.flap.Flap;
import frc.robot.subsystems.hood.Hood;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.commands.IntakeCargo;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.commands.Shoot;
import frc.robot.utils.PhotonVisionModule;
import webapp.Webserver;

public class RobotContainer {
    private static final Joystick joystick = new Joystick(Ports.Controls.JOYSTICK);
    private static final Joystick joystick2 = new Joystick(Ports.Controls.JOYSTICK2);
    // The robot's subsystems and commands are defined here...
    private final XboxController xbox = new XboxController(Ports.Controls.XBOX);
    private final JoystickButton a = new JoystickButton(xbox, XboxController.Button.kA.value);
    private final JoystickButton b = new JoystickButton(xbox, XboxController.Button.kB.value);
    private final JoystickButton x = new JoystickButton(xbox, XboxController.Button.kX.value);
    private final JoystickButton y = new JoystickButton(xbox, XboxController.Button.kY.value);
    private final JoystickButton ten = new JoystickButton(joystick, 10);
    private final JoystickButton twelve = new JoystickButton(joystick, 12);
    private final JoystickButton eleven = new JoystickButton(joystick, 11);
    private final JoystickButton nine = new JoystickButton(joystick, 9);
    private final JoystickButton leftTrigger = new JoystickButton(joystick, Joystick.ButtonType.kTrigger.value);
    private final JoystickButton rightTrigger = new JoystickButton(joystick2, Joystick.ButtonType.kTrigger.value);
    private final JoystickButton rightButton = new JoystickButton(joystick2, 6);
    private final Trigger trigger = new Trigger(() -> xbox.getRightTriggerAxis() > Constants.Control.RIGHT_TRIGGER_DEADBAND);
    // The robot's subsystems and commands are defined here...
    private final SwerveDrive swerve = SwerveDrive.getFieldOrientedInstance();
    private final Intake intake = Intake.getInstance();
    private final Conveyor conveyor = Conveyor.getInstance();
    private final Flap flap = Flap.getInstance();
    private final Shooter shooter = Shooter.getInstance();
    //    private final Climber climber = Climber.getInstance();
    private final Hood hood = Hood.getInstance();
    public static final PhotonVisionModule photonVisionModule = new PhotonVisionModule("photonvision", null);

    /**
     * The container for the robot.  Contains subsystems, OI devices, and commands.
     */
    public RobotContainer() {
        // Configure the button bindings and default commands
        configureDefaultCommands();

        if (Robot.debug) {
            startFireLog();
        }

        configureButtonBindings();
    }

    private void configureDefaultCommands() {
        swerve.setDefaultCommand(new OverpoweredDrive(swerve, () -> -joystick.getY(), () -> -joystick.getX(), () -> -joystick2.getX()));

    }

    private void configureButtonBindings() {
        b.whenPressed(hood::toggle);
        x.whenPressed(flap::toggleFlap);
//        y.whileHeld(new Convey(conveyor, -Constants.Conveyor.DEFAULT_POWER.get()));
        ten.whileHeld(new Shoot(shooter, () -> SmartDashboard.getNumber("set_velocity", 0)));
        leftTrigger.whenPressed(() -> Robot.resetAngle());
//        nine.whileHeld(new ShootCargo(shooter, hood, conveyor, Constants.Conveyor.DEFAULT_POWER::get, () -> SmartDashboard.getNumber("set_velocity", 0)));
        nine.whileHeld(new IntakeCargo(intake, Constants.Intake.DEFAULT_POWER));
        twelve.whileHeld(new Convey(conveyor, Constants.Conveyor.DEFAULT_POWER.get()));
//        trigger.whileActiveContinuous(new ShootCargo(
//                shooter, hood, conveyor, Constants.Conveyor.DEFAULT_POWER::get, () -> SmartDashboard.getNumber("set_velocity", 0)));
//        twelve.whileHeld(new TestBallFlow(hood, flap, conveyor, intake, shooter,
//                () -> false, () -> false));
    }


    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand() {
        return null;
    }

    /**
     * Initiates the value tuner.
     * <p>
     * Initiates the port of team 225s Fire-Logger.
     */
    private void startFireLog() {
        try {
            new Webserver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
