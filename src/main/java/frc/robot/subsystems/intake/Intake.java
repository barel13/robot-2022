package frc.robot.subsystems.intake;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Ports;

public class Intake extends SubsystemBase {
    private static Intake INSTANCE;
    private final CANSparkMax motor = new CANSparkMax(Ports.Intake.MOTOR, CANSparkMaxLowLevel.MotorType.kBrushless);
    private final Solenoid retractor = new Solenoid(PneumaticsModuleType.CTREPCM, Ports.Intake.SOLENOID);

    private Intake() {
        motor.setInverted(Ports.Intake.IS_MOTOR_INVERTED);
        motor.enableVoltageCompensation(Constants.NOMINAL_VOLTAGE);
    }


    /**
     * creates an instance of the intake.
     *
     * @return the subsystem instance.
     */
    public static Intake getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Intake();
        }
        return INSTANCE;
    }

    /**
     * Gets the power of the intake motor.
     *
     * @return the power of the motor. [%]
     */
    public double getPower() {
        return motor.get();
    }

    /**
     * Sets the power of the intake [%].
     *
     * @param power desired power in percentage.
     */
    public void setPower(double power) {
        motor.set(power);
    }

    /**
     * Open intake.
     */
    public void openRetractor() {
        retractor.set(RetractorState.OPEN.value);
    }

    /**
     * Close intake.
     */
    public void closeRetractor() {
        retractor.set(RetractorState.CLOSED.value);
    }

    /**
     * Toggles intake.
     */
    public void toggleRetractor() {
        retractor.toggle();
    }

    /**
     * Get the retractors current state.
     */
    public boolean getRetractorState() {
        return retractor.get();
    }

    public enum RetractorState {
        OPEN(true),
        CLOSED(false);

        public final boolean value;

        RetractorState(boolean value) {
            this.value = value;
        }
    }
}
