package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.climber.Climber;
import frc.robot.subsystems.climber.commands.JoystickClimb;
import frc.robot.subsystems.example.ExampleSubsystem;
import frc.robot.valuetuner.ValueTuner;
import webapp.Webserver;

public class RobotContainer {
//    private final XboxController xbox = new XboxController(Ports.Controls.JOYSTICK);
//    private final JoystickButton a = new JoystickButton(xbox, XboxController.Button.kA.value);
    private final Climber climber = Climber.getInstance();
    private final Joystick joystick = new Joystick(Ports.Controls.JOYSTICK);
    // The robot's subsystems and commands are defined here...
    public ExampleSubsystem exampleSubsystem = ExampleSubsystem.getInstance();


    /**
     * The container for the robot.  Contains subsystems, OI devices, and commands.
     */
    public RobotContainer() {
        // Configure the button bindings and default commands
        configureDefaultCommands();

        if (Robot.debug) {
            startValueTuner();
            startFireLog();
        }

        configureButtonBindings();
    }

    private void configureDefaultCommands() {
        climber.setDefaultCommand(new JoystickClimb(climber, () -> false, () -> joystick.getRawAxis(0)));
    }

    private void configureButtonBindings() {

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
     */
    private void startValueTuner() {
        new ValueTuner().start();
    }

    /**
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
