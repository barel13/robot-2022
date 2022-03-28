package frc.robot.auto;

import com.pathplanner.lib.PathPlanner;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.wpilibj2.command.*;
import frc.robot.Constants;
import frc.robot.Robot;
import frc.robot.RobotContainer;
import frc.robot.commandgroups.PickUpCargo;
import frc.robot.commandgroups.QuickReleaseBackAndShootCargo;
import frc.robot.subsystems.conveyor.Conveyor;
import frc.robot.subsystems.conveyor.commands.Convey;
import frc.robot.subsystems.drivetrain.SwerveDrive;
import frc.robot.subsystems.drivetrain.commands.AdjustToTargetOnCommand;
import frc.robot.subsystems.drivetrain.commands.TurnToAngle;
import frc.robot.subsystems.drivetrain.commands.auto.FollowPath;
import frc.robot.subsystems.flap.Flap;
import frc.robot.subsystems.hood.Hood;
import frc.robot.subsystems.hood.commands.HoodCommand;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.commands.IntakeCargo;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.commands.BackAndShootCargo;
import frc.robot.subsystems.shooter.commands.Shoot;
import frc.robot.utils.PhotonVisionModule;
import frc.robot.utils.Utils;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class SaarIsAutonomous extends SequentialCommandGroup {
    protected final SwerveDrive swerveDrive;
    protected final Shooter shooter;
    protected final Conveyor conveyor;
    protected final Intake intake;
    protected final Hood hood;
    protected final Flap flap;
    protected final PhotonVisionModule visionModule;

    public SaarIsAutonomous(SwerveDrive swerveDrive, Shooter shooter, Conveyor conveyor, Intake intake, Hood hood, Flap flap, PhotonVisionModule visionModule, String initialPathPath) {
        this.swerveDrive = swerveDrive;
        this.shooter = shooter;
        this.conveyor = conveyor;
        this.intake = intake;
        this.hood = hood;
        this.flap = flap;
        this.visionModule = visionModule;

        var initialState = PathPlanner.loadPath(initialPathPath, Constants.Autonomous.MAX_VEL, Constants.Autonomous.MAX_ACCEL).getInitialState();
        addCommands(new InstantCommand(() -> {
            swerveDrive.resetOdometry(initialState.poseMeters, initialState.holonomicRotation);
            Robot.resetAngle(initialState.holonomicRotation);
        }));
    }

    protected FollowPath followPath(String path) {
        return new FollowPath(
                PathPlanner.loadPath(path, Constants.Autonomous.MAX_VEL, Constants.Autonomous.MAX_ACCEL),
                swerveDrive::getPose,
                swerveDrive.getKinematics(),
                new PIDController(Constants.Autonomous.KP_X_CONTROLLER, 0, 0),
                new PIDController(Constants.Autonomous.KP_Y_CONTROLLER, 0, 0),
                swerveDrive::setStates,
                swerveDrive);
    }

    protected CommandBase followPathAndPickup(String path, boolean warmup) {
        return new ParallelRaceGroup(
                followPath(path),
                pickup(10),
                new ConditionalCommand(
                        new RunCommand(() -> shooter.setVelocity(3400), shooter),
                        new RunCommand(() -> {
                        }),
                        () -> warmup
                )
        );
    }

    protected CommandBase followPathAndPickup(String path) {
        return followPathAndPickup(path, true);
    }

    protected CommandBase shootAndAdjust(double timeout) {
        Supplier<Pose2d> swervePose = swerveDrive::getPose;
        Supplier<Transform2d> poseRelativeToTarget = () -> Constants.Vision.HUB_POSE.minus(swervePose.get());
        DoubleSupplier distanceFromTarget = visionModule::getDistance;
        DoubleSupplier yaw = () -> visionModule.getYaw().orElse(Robot.getAngle().minus(new Rotation2d(
                        Math.atan2(
                                poseRelativeToTarget.get().getY(),
                                poseRelativeToTarget.get().getX()
                        )
                )
        ).getDegrees());

        return new SequentialCommandGroup(
                new AdjustToTargetOnCommand(swerveDrive, yaw, visionModule::hasTargets).withTimeout(0.3),
                new ParallelRaceGroup(
                        new BackAndShootCargo(
                                shooter,
                                hood,
                                conveyor,
                                flap,
                                distanceFromTarget
                        ).withTimeout(timeout),
                        new IntakeCargo(intake, Constants.Intake.DEFAULT_POWER::get),
                        new AdjustToTargetOnCommand(swerveDrive, yaw, visionModule::hasTargets)
                ));
    }

    protected CommandBase shoot(double timeout) {
        DoubleSupplier distanceFromTarget = visionModule::getDistance;
        DoubleSupplier conveyorPower = Constants.Conveyor.DEFAULT_POWER::get;

        return new SequentialCommandGroup(
                new ParallelRaceGroup(new BackAndShootCargo(
                        shooter,
                        hood,
                        conveyor,
                        flap,
                        distanceFromTarget)
                        .withTimeout(timeout),
                        new IntakeCargo(intake, Constants.Intake.DEFAULT_POWER::get)
                ));
    }


    protected CommandBase quickReleaseBackShootAndAdjust(double timeout) {
        DoubleSupplier distanceFromTarget = visionModule::getDistance;
        DoubleSupplier conveyorPower = Constants.Conveyor.DEFAULT_POWER::get;

        return new SequentialCommandGroup(
                new AdjustToTargetOnCommand(swerveDrive, () -> visionModule.getYaw().orElse(0), visionModule::hasTargets).withTimeout(0.1),
                new ParallelRaceGroup(new QuickReleaseBackAndShootCargo(
                        shooter,
                        hood,
                        conveyor,
                        flap,
                        distanceFromTarget)
                        .withTimeout(timeout),
                        new IntakeCargo(intake, Constants.Intake.DEFAULT_POWER::get),
                        new AdjustToTargetOnCommand(swerveDrive, () -> visionModule.getYaw().orElse(0), visionModule::hasTargets)
                ));
    }


    protected CommandBase shoot3(double timeout) {
        DoubleSupplier distanceFromTarget = visionModule::getDistance;

        return new SequentialCommandGroup(
                new InstantCommand(() -> RobotContainer.cachedSetpoint = RobotContainer.setpointSupplier.getAsDouble()),
                new InstantCommand(() -> RobotContainer.cachedDistance = RobotContainer.distanceSupplier.getAsDouble()),
                new InstantCommand(() -> RobotContainer.odometryCachedSetpoint = RobotContainer.odometrySetpointSupplier.getAsDouble()),
                new InstantCommand(() -> RobotContainer.odometryCachedDistance = RobotContainer.odometryDistanceSupplier.getAsDouble()),
                new InstantCommand(() -> RobotContainer.cachedHasTarget = !RobotContainer.playWithoutVision && RobotContainer.hasTarget.getAsBoolean()),
                new InstantCommand(() -> RobotContainer.shooting = true),
                new InstantCommand(flap::allowShooting),
                new InstantCommand(() -> shooter.setVelocity(Shoot.getSetpointVelocity(distanceFromTarget.getAsDouble()))),
                new WaitUntilCommand(() -> Math.abs(shooter.getVelocity() - (RobotContainer.cachedSetpoint)) <= Constants.Shooter.SHOOTER_VELOCITY_DEADBAND.get()),
                new ParallelRaceGroup(new Shoot(
                        shooter,
                        hood,
                        distanceFromTarget
                )
                        .withTimeout(timeout),
                        new IntakeCargo(intake, Constants.Intake.DEFAULT_POWER::get),
                        new Convey(conveyor, Constants.Conveyor.SHOOT_POWER),
                        new HoodCommand(hood)
                ));
    }


    protected CommandBase pickup(double timeout) {
        return new PickUpCargo(
                conveyor,
                flap,
                intake,
                Constants.Conveyor.DEFAULT_POWER.get(),
                () -> Utils.map(MathUtil.clamp(Math.hypot(swerveDrive.getChassisSpeeds().vxMetersPerSecond, swerveDrive.getChassisSpeeds().vyMetersPerSecond), 0, 4), 0, 4, 0.4, 0.25)
        ).withTimeout(timeout);
    }

    protected CommandBase turnToAngle(Supplier<Rotation2d> target) {
        return new TurnToAngle(
                swerveDrive,
                target
        );
    }
}
