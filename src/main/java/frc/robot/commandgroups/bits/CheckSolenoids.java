package frc.robot.commandgroups.bits;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.subsystems.flap.Flap;
import frc.robot.subsystems.flap.commands.bits.OscillateFlap;
import frc.robot.subsystems.helicopter.Helicopter;
import frc.robot.subsystems.helicopter.commands.OscillateStopper;
import frc.robot.subsystems.hood.Hood;
import frc.robot.subsystems.hood.commands.bits.CheckHoodPressure;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.commands.bits.RetractorTest;

public class CheckSolenoids extends SequentialCommandGroup {

    public CheckSolenoids(Hood hood, Flap flap, Intake intake, Helicopter helicopter) {
        addCommands(
                new OscillateStopper(helicopter).withTimeout(5),
                new CheckHoodPressure(hood).withTimeout(5),
                new OscillateFlap(flap).withTimeout(5),
                new WaitCommand(3),
                new RetractorTest(intake, 3).withTimeout(5)
        );
    }
}
