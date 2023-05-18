package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.util.PropertiesUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static final Properties PROPERTIES = PropertiesUtil.getProperties("rabbit.properties");
    public static void main(String[] args) {
        int interval = Integer.parseInt(PROPERTIES.getProperty("rabbit.interval"));
        try (Connection connection = DriverManager.getConnection(PROPERTIES.getProperty("db.url"),
                PROPERTIES.getProperty("db.username"),
                PROPERTIES.getProperty("db.password"))) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            String sql = "insert into rabbit(created_date) values(?)";
            try (PreparedStatement statement = cn.prepareStatement(sql)) {
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}