package frc.robot.subsystems.conveyor.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.conveyor.Conveyor;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import static frc.robot.Constants.Shooter.SHOOTER_VELOCITY_DEADBAND;

public class Convey3 extends CommandBase {
    private final Conveyor conveyor;
    private final BooleanSupplier preFlapSupplier;
    private final Timer timer = new Timer();
    private final DoubleSupplier setpointSuppier;
    private final DoubleSupplier velocitySupplier;
    private final boolean first = true;
    private final Timer delayTimer = new Timer();
    boolean go = false;
    private boolean last = false;
    private boolean getBallToPreFlap = true;
    private double setpoint = 0;
    private boolean wait = true;

    public Convey3(Conveyor conveyor, BooleanSupplier preFlapSupplier, DoubleSupplier distanceSupplier, DoubleSupplier velocitySupplier) {
        this.conveyor = conveyor;
        this.preFlapSupplier = preFlapSupplier;
        this.setpointSuppier = distanceSupplier;
        this.velocitySupplier = velocitySupplier;
        addRequirements(conveyor);
    }

    @Override
    public void initialize() {
        timer.stop();
        delayTimer.stop();
        if (preFlapSupplier.getAsBoolean()) {
            getBallToPreFlap = false;
        }
        setpoint = setpointSuppier.getAsDouble();
//        conveyor.setPower(Constants.Conveyor.DEFAULT_POWER.get());
        wait = true;
        last = false;
    }

    @Override
    public void execute() {
//        SmartDashboard.putBoolean("getBallToPreFlap", getBallToPreFlap);
//        SmartDashboard.putBoolean("last", last);
//        SmartDashboard.putNumber("timer", timer.get());
//        SmartDashboard.putBoolean("setpoint_reached", Math.abs(setpoint - velocitySupplier.getAsDouble()) < SHOOTER_VELOCITY_DEADBAND.get());
//        SmartDashboard.putBoolean("wait", wait);

        if (wait) {
            if (Math.abs(setpoint - velocitySupplier.getAsDouble()) < SHOOTER_VELOCITY_DEADBAND.get()) {
                wait = false;
//                last = false;
            }
            SmartDashboard.putString("Saar", "Mama");
        } else {
            SmartDashboard.putString("Saar", "Joe");
            SmartDashboard.putNumber("Saar2", timer.get());

            if (getBallToPreFlap) {
                conveyor.setPower(Constants.Conveyor.DEFAULT_POWER.get());
            } else {
//            if (delayTimer.hasElapsed(0.1))
                conveyor.setPower(0);
            }

            if (preFlapSupplier.getAsBoolean()) {
                if (!last) {
                    last = true;
                    getBallToPreFlap = false;
                    timer.start();
                    timer.reset();
                    delayTimer.start();
                    delayTimer.reset();
                }
            } else {
                last = false;
                getBallToPreFlap = true;
            }

//            if (Math.abs(setpoint - velocitySupplier.getAsDouble()) < SHOOTER_VELOCITY_DEADBAND.get()) {
            if (timer.hasElapsed(0.3)) {
                getBallToPreFlap = true;
                SmartDashboard.putNumber("time", timer.get());
                timer.reset();
                timer.stop();
            }
//            }
        }
    }

    @Override
    public void end(boolean interrupted) {
        timer.stop();
        delayTimer.stop();
        conveyor.setPower(0);
    }

    @Override
    public boolean isFinished() {
        return super.isFinished();
    }
}
