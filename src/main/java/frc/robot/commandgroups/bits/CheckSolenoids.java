package frc.robot.commandgroups.bits;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.subsystems.flap.Flap;
import frc.robot.subsystems.flap.commands.bits.OscillateFlap;
import frc.robot.subsystems.hood.Hood;
import frc.robot.subsystems.hood.commands.bits.CheckHoodPressure;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.intake.commands.bits.RetractorTest;

public class CheckSolenoids extends SequentialCommandGroup {

    public CheckSolenoids(Hood hood, Flap flap, Intake intake) {
        addCommands(
                new CheckHoodPressure(hood).withTimeout(3),
                new OscillateFlap(flap).withTimeout(3),
                new WaitCommand(3),
                new RetractorTest(intake, 3).withTimeout(5.1)
        );
    }
}
