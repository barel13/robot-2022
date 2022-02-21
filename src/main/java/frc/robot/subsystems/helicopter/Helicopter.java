package frc.robot.subsystems.helicopter;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Ports;
import frc.robot.Robot;
import frc.robot.subsystems.UnitModel;


public class Helicopter extends SubsystemBase {

    private static Helicopter INSTANCE = null;

    private final WPI_TalonFX mainMotor = new WPI_TalonFX(Ports.Helicopter.MAIN);
    private final WPI_TalonFX auxMotor = new WPI_TalonFX(Ports.Helicopter.AUX);
    private final Solenoid stopper = new Solenoid(PneumaticsModuleType.CTREPCM, Ports.Helicopter.STOPPER);
    private final UnitModel unitModelPosition = new UnitModel(Constants.Helicopter.TICKS_PER_RAD);
    private final DutyCycleEncoder dutyCycleEncoder = new DutyCycleEncoder(Ports.Helicopter.ENCODER);

    private final PIDController controller;
    private final SingleJointedArmSim armSim;
    private final ArmFeedforward feedforward;
    private final MechanismLigament2d arm;


    private Helicopter() {
        if (Robot.isSimulation()) {

            controller = new PIDController(Constants.Helicopter.KP.get(), Constants.Helicopter.KI.get(), Constants.Helicopter.KD.get());
            feedforward = new ArmFeedforward(Constants.Helicopter.F_FORWARD_S, Constants.Helicopter.F_FORWARD_COS, Constants.Helicopter.F_FORWARD_V, Constants.Helicopter.F_FORWARD_A);
            DCMotor armGearbox = DCMotor.getFalcon500(2);


            armSim =
                    new SingleJointedArmSim(
                            armGearbox,
                            Constants.Helicopter.GEAR_RATIO,
                            SingleJointedArmSim.estimateMOI(Constants.Helicopter.ARM_LENGTH, Constants.Helicopter.ARM_MASS),
                            Constants.Helicopter.ARM_LENGTH,
                            Constants.Helicopter.MIN_ANGLE,
                            Constants.Helicopter.MAX_ANGLE,
                            Constants.Helicopter.ARM_MASS,
                            true,
                            VecBuilder.fill(Constants.Helicopter.ARM_ENCODER_DIST_PER_PULSE)
                    );


            Mechanism2d mechanism2d = new Mechanism2d(60, 60);
            MechanismRoot2d armPivot = mechanism2d.getRoot("ArmPivot", 30, 30);
            MechanismLigament2d armTower =
                    armPivot.append(new MechanismLigament2d("ArmTower", 30, -90));

            arm =
                    armPivot.append(
                            new MechanismLigament2d(
                                    "Arm",
                                    30,
                                    Units.radiansToDegrees(armSim.getAngleRads()),
                                    6,
                                    new Color8Bit(Color.kYellow)));

            SmartDashboard.putData("Arm sim", mechanism2d);
            armTower.setColor(new Color8Bit(Color.kBlue));
        } else {
            controller = null;
            armSim = null;
            feedforward = null;
            arm = null;
        }

        /*
         Set sensor phase.
         */
        mainMotor.setSensorPhase(Ports.Helicopter.SENSOR_PHASE);
        /*
         Set the right motor on Brake mode.
         */
        mainMotor.setNeutralMode(NeutralMode.Brake);

        /*
         Setting the motor to go clockwise.
         */
        mainMotor.setInverted(Ports.Helicopter.IS_MAIN_INVERTED);

        /*
         config PID velocity for main motor.
         */
        mainMotor.configMotionCruiseVelocity(Constants.Helicopter.CRUISE_VELOCITY);
        mainMotor.configMotionAcceleration(Constants.Helicopter.MAXIMAL_ACCELERATION);
        mainMotor.config_kP(0, Constants.Helicopter.KP.get(), Constants.TALON_TIMEOUT);
        mainMotor.config_kI(0, Constants.Helicopter.KI.get(), Constants.TALON_TIMEOUT);
        mainMotor.config_kD(0, Constants.Helicopter.KD.get(), Constants.TALON_TIMEOUT);


        auxMotor.follow(mainMotor);

        mainMotor.enableVoltageCompensation(Constants.Helicopter.VOLTAGE_COMPENSATION);

        mainMotor.configVoltageCompSaturation(Constants.NOMINAL_VOLTAGE);

        auxMotor.enableVoltageCompensation(Constants.Helicopter.VOLTAGE_COMPENSATION);

        auxMotor.configVoltageCompSaturation(Constants.NOMINAL_VOLTAGE);

        /*
         Set the aux motor on Brake mode.
         */
        auxMotor.setNeutralMode(NeutralMode.Brake);

        /*
         Setting the motor to go clockwise.
         */
        auxMotor.setInverted(Ports.Helicopter.IS_AUX_INVERTED);
    }

    /**
     * @return the object Helicopter.
     */
    public static Helicopter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Helicopter();
        }
        return INSTANCE;
    }

    /**
     * @return get motor velocity. [rad/s]
     */
    public double getVelocity() {
        return unitModelPosition.toVelocity(mainMotor.getSelectedSensorVelocity(0));
    }

    /**
     * @param velocity the velocity of the motors. [rad/s]
     */
    public void setVelocity(double velocity) {
        if (Robot.isSimulation()) {
            double volts = controller.calculate(getVelocity(), velocity);
            mainMotor.setVoltage(volts + feedforward.calculate(0, 0));
        } else {
            mainMotor.set(ControlMode.Velocity, unitModelPosition.toTicks100ms(velocity));
        }
    }

    public void vroomVroom(double power) {
        mainMotor.set(ControlMode.PercentOutput, power);
    }

    /**
     * Set Helicopter position to zero.
     * Zero is the balanced position of the arms.
     */
    public void setAngleZero() {
        double angle = getAbsolutePosition();
        setPosition(getPosition().minus(new Rotation2d(angle)));
    }

    /**
     * @return the absolute position of the Helicopter.
     */
    public double getAbsolutePosition() {
        return unitModelPosition.toUnits(dutyCycleEncoder.getDistance() - Constants.Helicopter.ZERO_POSITION);
    }


    /**
     * @return get motors position. [rad]
     */
    public Rotation2d getPosition() {
        return new Rotation2d(Math.IEEEremainder(unitModelPosition.toUnits(mainMotor.getSelectedSensorPosition(0)), Math.PI*2));
    }

    /**
     * @param position the position of the motors. [rad]
     */
    public void setPosition(Rotation2d position) {
        var currentPosition = getPosition();
        var error = position.minus(currentPosition);
        Rotation2d minMove = new Rotation2d(Math.IEEEremainder(unitModelPosition.toTicks(error), Math.PI *2));
        mainMotor.set(ControlMode.MotionMagic, unitModelPosition.toTicks(minMove),
              DemandType.ArbitraryFeedForward, feedforward.calculate(getPosition().getRadians(), getVelocity()));
}

    /**
     * Get the stopper's mode.
     *
     * @return the stopper's mode.
     */
    public boolean isStopperEngaged() {
        return stopper.get();
    }

    /**
     * Set the stopper's mode.
     *
     * @param engaged whether the stopper is engaged.
     */
    public void setStopperMode(boolean engaged) {
        stopper.set(!engaged);
    }

    /**
     * Toggle the value of the stopper.
     */
    public void toggleStopper() {
        stopper.toggle();
    }

    /**
     * Stop both motors in the place they were.
     */
    public void stop() {
        mainMotor.stopMotor();
    }

    @Override
    public void periodic() {
        mainMotor.config_kP(0, Constants.Helicopter.KP.get(), Constants.TALON_TIMEOUT);
        mainMotor.config_kI(0, Constants.Helicopter.KI.get(), Constants.TALON_TIMEOUT);
        mainMotor.config_kD(0, Constants.Helicopter.KD.get(), Constants.TALON_TIMEOUT);
    }

    /**
     * Add periodic for the simulation.
     */
    @Override
    public void simulationPeriodic() {
        armSim.setInput(mainMotor.get() * RobotController.getBatteryVoltage());

        armSim.update(Constants.SIMULATION_LOOP_PERIOD);

        RoboRioSim.setVInVoltage(
                BatterySim.calculateDefaultBatteryLoadedVoltage(armSim.getCurrentDrawAmps()));
        mainMotor.getSimCollection().setIntegratedSensorRawPosition(unitModelPosition.toTicks(new Rotation2d(armSim.getAngleRads())));
        arm.setAngle(Units.radiansToDegrees(armSim.getAngleRads()));
    }
}