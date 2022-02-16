package frc.robot.autoPaths;

import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.commands.PPSwerveControllerCommand;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.Constants;
import frc.robot.commandgroups.ShootCargo;
import frc.robot.subsystems.conveyor.Conveyor;
import frc.robot.subsystems.drivetrain.SwerveDrive;
import frc.robot.subsystems.flap.Flap;
import frc.robot.subsystems.hood.Hood;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.shooter.Shooter;

import java.util.function.DoubleSupplier;


public class TaxiFromUpLowAndShoot extends SequentialCommandGroup {
    private DoubleSupplier distanceFromTarget;
    private DoubleSupplier conveyorPower;

    //Taxi from up low tarmac, shoot pre-loaded cargo, park near up tarmac.

    public TaxiFromUpLowAndShoot(Shooter shooter, SwerveDrive swerveDrive, Conveyor conveyor, Intake intake, Hood hood, Flap flap) {
        var rotationPID = new ProfiledPIDController(Constants.Autonomous.KP_THETA_CONTROLLER, 0, 0, new TrapezoidProfile.Constraints(Constants.Autonomous.MAX_VEL, Constants.Autonomous.MAX_ACCEL));
        rotationPID.enableContinuousInput(-Math.PI, Math.PI);

        var taxiFromUpLowTarmac = PathPlanner.loadPath("p0 - Taxi from up low tarmac(1.1)", Constants.Autonomous.MAX_VEL, Constants.Autonomous.MAX_ACCEL);
        new PPSwerveControllerCommand(
                taxiFromUpLowTarmac,
                swerveDrive::getPose,
                swerveDrive.getKinematics(),
                new PIDController(Constants.Autonomous.KP_X_CONTROLLER, 0, 0),
                new PIDController(Constants.Autonomous.KP_Y_CONTROLLER, 0, 0),
                rotationPID,
                swerveDrive::setStates,
                swerveDrive);

        addCommands(new ShootCargo(
                shooter,
                hood,
                conveyor,
                flap,
                distanceFromTarget,
                () -> Constants.Conveyor.DEFAULT_POWER));

        var goToUpTarmac = PathPlanner.loadPath("p0 - Go to up tarmac(1.2.2)", Constants.Autonomous.MAX_VEL, Constants.Autonomous.MAX_ACCEL);


        new PPSwerveControllerCommand(
                goToUpTarmac,
                swerveDrive::getPose,
                swerveDrive.getKinematics(),
                new PIDController(Constants.Autonomous.KP_X_CONTROLLER, 0, 0),
                new PIDController(Constants.Autonomous.KP_Y_CONTROLLER, 0, 0),
                rotationPID,
                swerveDrive::setStates,
                swerveDrive);
    }
}



