package helloworld;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class DataSource {
    private static final String DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String MIN_PULSE_VALUE = "min_pulse_value";
    private static final String MAX_PULSE_VALUE = "max_pulse_value";

    PreparedStatement statement;

    static {
        String driverClassName = getDriverClassName();
        System.out.printf("driver class name is %s", driverClassName);
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    Connection con;

    public DataSource(String connectionStr, String username, String password) {
        try {
            con = DriverManager.getConnection(connectionStr, username, password);
            statement = con.prepareStatement(
                    String.format(
                            "select %s, %s FROM groups WHERE id = (SELECT group_id FROM patients WHERE patient_id =?)",
                            MIN_PULSE_VALUE, MAX_PULSE_VALUE));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Range getRange(long patientID) {
        try {
            statement.setLong(1, patientID);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int min = rs.getInt(MIN_PULSE_VALUE);
                int max = rs.getInt(MAX_PULSE_VALUE);
                Range range = new Range(min,max);
                return range;
            } else {
                throw new NoSuchElementException(String.format("patient with id %d doesnt exist", patientID));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getDriverClassName() {
        String driverClassName = System.getenv("DRIVER_CLASS_NAME");
        if (driverClassName == null) {
            driverClassName = DEFAULT_DRIVER_CLASS_NAME;
        }
        return driverClassName;
    }

}
