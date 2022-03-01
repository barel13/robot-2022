package frc.robot.autoPaths;

import com.pathplanner.lib.PathPlanner;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.*;
import frc.robot.Constants;
import frc.robot.commandgroups.PickUpCargo;
import frc.robot.commandgroups.ShootCargo;
import frc.robot.subsystems.conveyor.Conveyor;
import frc.robot.subsystems.drivetrain.SwerveDrive;
import frc.robot.subsystems.drivetrain.commands.auto.FollowPath;
import frc.robot.subsystems.drivetrain.commands.testing.SimpleAdjustWithVision;
import frc.robot.subsystems.flap.Flap;
import frc.robot.subsystems.hood.Hood;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.commands.Shoot;
import frc.robot.utils.PhotonVisionModule;

import java.util.function.DoubleSupplier;
import java.util.function.Function;

public class TaxiFromLowLeftPickShoot extends SequentialCommandGroup {
    private DoubleSupplier distanceFromTarget;
    private DoubleSupplier conveyorPower;

    // Taxi from low left, pick up middle cargo, shoot, park between tarmacs.(3)
    public TaxiFromLowLeftPickShoot(Shooter shooter, SwerveDrive swerveDrive, Conveyor conveyor, Intake intake, Hood hood, Flap flap, PhotonVisionModule module) {
        distanceFromTarget = module::getDistance;
        conveyorPower = Constants.Conveyor.DEFAULT_POWER::get;
        var rotationPID = new ProfiledPIDController(Constants.Autonomous.KP_THETA_CONTROLLER, 0, 0, new TrapezoidProfile.Constraints(Constants.Autonomous.MAX_VEL, Constants.Autonomous.MAX_ACCEL));
        rotationPID.enableContinuousInput(-Math.PI, Math.PI);

        Function<String, FollowPath> createCommand = path -> new FollowPath(
                PathPlanner.loadPath(path, Constants.Autonomous.MAX_VEL, Constants.Autonomous.MAX_ACCEL),
                swerveDrive::getPose,
                swerveDrive.getKinematics(),
                new PIDController(Constants.Autonomous.KP_X_CONTROLLER, 0, 0),
                new PIDController(Constants.Autonomous.KP_Y_CONTROLLER, 0, 0),
                swerveDrive::setStates,
                swerveDrive);

        addCommands(new InstantCommand(() -> module.setLeds(false)));

        addCommands(new ParallelCommandGroup((createCommand.apply("p1 - Taxi from low left and pickup middle cargo(3.1)")),
                new PickUpCargo(
                        conveyor,
                        flap,
                        intake,
                        Constants.Conveyor.DEFAULT_POWER.get(),
                        Constants.Intake.DEFAULT_POWER::get
                ).withTimeout(3)));


        addCommands(new ParallelRaceGroup(new ShootCargo(
                shooter,
                hood,
                conveyor,
                flap,
                conveyorPower,
                distanceFromTarget)
                .withTimeout(3),
                new RunCommand(() -> {
                    SmartDashboard.putNumber("Mama", Shoot.getSetpointVelocity(distanceFromTarget.getAsDouble(), hood.isOpen()));
                    SmartDashboard.putNumber("Joe", distanceFromTarget.getAsDouble());
                }),
                new SimpleAdjustWithVision(swerveDrive, () -> 0, () -> true, () -> module.getYaw().orElse(0), distanceFromTarget)));

        addCommands(createCommand.apply("p1 - Going to middle tarmac(3.2.2)"));
    }
}