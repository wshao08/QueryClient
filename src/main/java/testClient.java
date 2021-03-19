
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.Resource;
import ty.querytest.utils.JdbcConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class testClient
{
    public static long totalline = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(testClient.class);
    private static long startTime;
    private static long endTime;
    private static List<String> devicelist = new ArrayList<>();
    private static Random random = new Random();

    public static String[] vehicles = {
        "accelerationFB", "accelerationLR", "speed_TypeA", "steeringAngle_TypeA", "EngineRPM_TypeA",
        "TirePressureFL_kpa", "FuelGageIndication", "latitude", "longitude", "AccelPedalAngle_TypeA",
        "TirePressureFR_kpa", "TirePressureRL_kpa", "TirePressureRR_kpa", "AmbientTemperature",
        "TemperatureD",
        "turnLampSwitchStatus", "ATShiftPosition", "BrakePedal", "DoorOpenD", "ParkingBrake",
        "EcoModeIndicator",
        "PowerModeSelect_TypeA", "SportModeSelect", "WindowPositionD", "AirConIndicator",
        "Odometer_km", "HeadLamp_TypeB"
    };

    public static int query_count = 0;

    static void towardsquerytest(int devices, int round, Connection connection) throws SQLException {
        //LOGGER.info("Now : {}, Test device {} timeseries {} Start", System.currentTimeMillis(), devices, timeseries);
        long totalcost = 0;
        long totalquery = 0;
        while (round > 0) {
            round--;

            int deviceNum = random.nextInt(devicelist.size());
            try {
                //List<Pair<Long, List<List<String>>>> ret = getRange(usernames, vclIds, tmnlIds, metrics, starttime, endtime);
                StringBuilder qry = new StringBuilder("");
                qry.append("select ").append(vehicles[0]);
                for (int i = 1; i < vehicles.length; i++) {
                    qry.append(",").append(vehicles[i]);
                }
                qry.append(" from ").append(devicelist.get(deviceNum)).append(" where time >= ").append(startTime).append(" and time <= ").append(endTime);
                long st = System.currentTimeMillis();
                Statement statement = connection.createStatement();
                statement.execute(qry.toString());
                ResultSet resultSet = statement.getResultSet();
                int rownum = 0;
                while (resultSet.next()) {
                    rownum++;
                }

                // TODO: increase fetch size

                long ed = System.currentTimeMillis();
                //totaldatapoints += v;
                totalquery++;
                totalcost += ed - st;
                //totalline += ret.size();
                LOGGER.info("Query, {}, cost, {}, {}, rows", qry.toString(), totalcost, rownum);
            } catch (Exception e) {
                LOGGER.error("query failed: {}", e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        TestConfig conf = loadConfig();
        try {
            String ip = conf.getUrl();
            Connection connection = JdbcConnectionUtils.getConnection(ip);
            if (connection == null) {
                LOGGER.error("get connection failed");
            }
            Statement statement = connection.createStatement();
            statement.execute("show devices");
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                String tmp = resultSet.getString(1);
                try {
                    Long.parseLong(tmp.split("\\.")[2]);
                    devicelist.add(tmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            startTime = conf.getStart();
            endTime = conf.getEnd();
            int round = conf.getLoop();
            long st = System.currentTimeMillis();
            towardsquerytest(1, round, connection);
            long ed = System.currentTimeMillis();
            LOGGER.info("Total time cost:{}", ed - st);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /*try (Statement statement = connection.createStatement()) {
            statement.execute("clear cache");
        }
        Thread.sleep(1800000);*/
    }

    public static TestConfig loadConfig() {
        TestConfig conf = new TestConfig();
        try {
            String fileName = "properties.config";
            InputStream is = new FileInputStream(fileName);
            Properties properties = new Properties();
            properties.load(is);

            conf.setUrl(properties.getProperty("server_ip"));
            conf.setLoop(Integer.parseInt(properties.getProperty("loop", "1")));
            conf.setStart(Integer.parseInt(properties.getProperty("start_time", "0")));
            conf.setEnd(Integer.parseInt(properties.getProperty("end_time", "0")));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return conf;
    }
}
