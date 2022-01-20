package frc.robot.subsystems.climber;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Ports;
import frc.robot.subsystems.UnitModel;

public class Climber extends SubsystemBase {
    private static Climber INSTANCE = null;
    private final WPI_TalonFX leftMotor = new WPI_TalonFX(Ports.Climber.LEFT);
    private final WPI_TalonFX rightMotor = new WPI_TalonFX(Ports.Climber.RIGHT);
    private final UnitModel unitModel = new UnitModel(Constants.Climber.TICKS_PER_RAD);

    public Climber() {

        /*
         Set the left motor on Brake mode.
         */
        leftMotor.setNeutralMode(NeutralMode.Brake);

        /*
         sets the phase of the sensor
         */
        leftMotor.setSensorPhase(Ports.Climber.LEFT_SENSOR_PHASE);

        /*
         checking is motor inverted.
         */
        leftMotor.setInverted(Ports.Climber.IS_LEFT_INVERTED);

        /*
         config PID velocity for left motor.
         */
        leftMotor.config_kP(0, Constants.Climber.P_LEFT_VELOCITY);
        leftMotor.config_kI(0, Constants.Climber.I_LEFT_VELOCITY);
        leftMotor.config_kD(0, Constants.Climber.D_LEFT_VELOCITY);


        /*
         config PID position for left motor.
         */
        leftMotor.config_kP(1, Constants.Climber.P_LEFT_POSITION);
        leftMotor.config_kI(1, Constants.Climber.I_LEFT_POSITION);
        leftMotor.config_kD(1, Constants.Climber.D_LEFT_POSITION);

        /*
         set the right motor on Brake mode.
         */
        rightMotor.setNeutralMode(NeutralMode.Brake);

        /*
        sets the phase of the sensor.
         */
        rightMotor.setSensorPhase(Ports.Climber.RIGHT_SENSOR_PHASE);

        /*
         checking is motor inverted.
         */
        rightMotor.setInverted(Ports.Climber.IS_RIGHT_INVERTED);

        /*
         config PID velocity for right motor.
         */
        rightMotor.config_kP(0, Constants.Climber.P_RIGHT_VELOCITY);
        rightMotor.config_kI(0, Constants.Climber.I_RIGHT_VELOCITY);
        rightMotor.config_kD(0, Constants.Climber.D_RIGHT_VELOCITY);

        /*
         config PID position for right motor.
         */
        rightMotor.config_kP(1, Constants.Climber.P_RIGHT_POSITION);
        rightMotor.config_kI(1, Constants.Climber.I_RIGHT_POSITION);
        rightMotor.config_kD(1, Constants.Climber.D_RIGHT_POSITION);
    }

    /**
     * @return the object Climber.
     */
    public static Climber getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Climber();
        }
        return INSTANCE;
    }

    /**
     * @return get motors velocity.
     */
    public double getVelocity() {
        return unitModel.toVelocity(rightMotor.getSelectedSensorVelocity());
    }

    /**
     * @param velocity the velocity of the right & left.
     */

    public void setVelocity(double velocity) {
        int ticks100ms = unitModel.toTicks100ms(velocity);
        rightMotor.set(ControlMode.Velocity, ticks100ms);
        leftMotor.set(ControlMode.Velocity, ticks100ms);
    }

    /**
     * @return get motors position.
     */
    public double getPosition() {
        return unitModel.toUnits(rightMotor.getSelectedSensorPosition());
    }

    /**
     * @param position the position of the motors.
     */
    public void setPosition(double position) {
        rightMotor.set(ControlMode.Position, unitModel.toTicks(position));
    }

    /**
     * set the velocity 0.
     * stop both motors in the place they were.
     */
    public void stop() {
        leftMotor.stopMotor();
        rightMotor.stopMotor();
    }
}