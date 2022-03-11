package frc.robot.commandgroups;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.conveyor.Conveyor;
import frc.robot.subsystems.conveyor.commands.Convey;
import frc.robot.subsystems.flap.Flap;
import frc.robot.subsystems.hood.Hood;
import frc.robot.subsystems.shooter.Shooter;

import java.util.function.DoubleSupplier;

public class BackAndShootCargo2 extends SequentialCommandGroup {
    public BackAndShootCargo2(Shooter shooter,
                              Hood hood,
                              Conveyor conveyor,
                              Flap flap,
                              DoubleSupplier conveyorPower,
                              DoubleSupplier distanceFromTarget) {
        addCommands(new Convey(conveyor, -0.25).withTimeout(0.075).withInterrupt(conveyor::isPreFlapBeamConnected),
                new ShootCargo2(shooter, hood, conveyor, flap, conveyorPower, distanceFromTarget));
    }
}