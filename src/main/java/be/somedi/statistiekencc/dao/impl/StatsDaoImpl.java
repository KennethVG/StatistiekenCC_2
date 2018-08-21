package be.somedi.statistiekencc.dao.impl;

import be.somedi.statistiekencc.dao.StatsDao;
import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Repository
public class StatsDaoImpl implements StatsDao {

    private final JdbcTemplate template;

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    private Logger logger = LoggerFactory.getLogger(StatsDaoImpl.class);

    @Autowired
    public StatsDaoImpl(JdbcTemplate template) {
        this.template = template;
    }

    private void createWorkbook() {
        workbook = new XSSFWorkbook();

        sheet = workbook.createSheet("Statistieken MedSec");
        sheet.setDefaultColumnWidth(30);

        // Style
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        // Create header row:
        Row header = sheet.createRow(0);
        Cell c = header.createCell(0);
        c.setCellValue("Naam typiste:");
        c.setCellStyle(style);
        Cell c1 = header.createCell(1);
        c1.setCellStyle(style);
        c1.setCellValue("Datum raadpleging:");
        Cell c2 = header.createCell(2);
        c2.setCellStyle(style);
        c2.setCellValue("Raadpleging:");
        Cell c3 = header.createCell(3);
        c3.setCellStyle(style);
        c3.setCellValue("Aanmaakdatum brief:");
    }

    private void showAlertDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Datum niet ingevuld:");
        alert.setContentText("Het SQL statement kan niet worden uitgevoerd als de datum niet is ingevuld.");
        alert.showAndWait();
    }

    private String statsFromDate(LocalDate from, String sql) {
        final String[] result = {""};
        if (from == null) {
            showAlertDialog();
        } else {
            template.query(sql, new Object[]{from.minusDays(1).toString()}, rs -> {
                result[0] = rs.getString(1);
            });
        }
        return result[0];
    }

    private List<Integer> getTypisten() {
        String sql = "SELECT id from Global_User where usertype=1";
        return template.query(sql, (rs, i) -> rs.getInt("id"));
    }

    private String statsWithStartAndEndForSecretaries(LocalDate startDate, LocalDate endDate, String sql) {
        StringBuilder result = new StringBuilder();

        if (startDate == null || endDate == null) {
            showAlertDialog();
        } else {
            List<Integer> typisten = getTypisten();
            logger.info(typisten.toString());
            for (int t : typisten) {
                String s = "";
                try {
                    s = template.queryForObject(sql, new Object[]{startDate.toString(), endDate.plusDays(1).toString(), t}, (rs, i) -> {
                        String typiste = rs.getString(1);
                        String date = rs.getString(2);
                        String dates[] = date.split(" ");
                        return typiste + ": " + dates[0] + " " + dates[1].split(":")[0] + ":" + dates[1].split(":")[1] + "\n";
                    });
                } catch (EmptyResultDataAccessException ignored) {

                }
                if (s.length() > 1) {
                    result.append(s);
                }
            }
        }
        return result.toString();
    }

    private String statsForUsersFromDate(LocalDate from, String sql) {
        StringBuilder result = new StringBuilder();
        if (from == null) {
            showAlertDialog();
        } else {
            template.query(sql, new Object[]{from.minusDays(1).toString()}, rs -> {
                String name = rs.getString(1);
                result.append(name).append("\n");
            });
        }
        return result.toString();
    }

    private String statsWithStartAndEndDate(LocalDate startDate, LocalDate endDate, String sql) {
        StringBuilder result = new StringBuilder();
        if (startDate == null || endDate == null) {
            showAlertDialog();
        } else {
            template.query(sql, new Object[]{startDate.toString(), endDate.plusDays(1).toString()}, rs -> {
                String typiste = rs.getString(1);
                result.append(typiste).append("\n");
            });
        }
        return result.toString();
    }

    private String getFastStatsWithOneResultBetweenTwoDates(String sql, LocalDate startDate, LocalDate endDate) {
        StringBuilder result = new StringBuilder();
        if (startDate == null || endDate == null) {
            showAlertDialog();
        } else {
            template.query(sql, new Object[]{startDate.toString(), endDate.plusDays(1).toString()}, rs -> {
                String name = rs.getString(1);
                result.append(name);

            });
        }
        return result.toString();
    }

    @Override
    public void getStatsForMedSec(LocalDate startDate, LocalDate endDate) {
        String selectSQLExcel = "SELECT dbo.displayName(gus.person_id) 'Typiste'"
                + ", ltr.lastModificationDate 'Datum'" + ", ltr.freeTextContent 'Raadpleging' " + ", ltr.creationDate 'Aanmaakdatum'" + "FROM medical_letter ltr "
                + "JOIN global_user gus ON gus.id = ltr.assistant_id "
                + "WHERE ltr.status= 'FINAL' AND ltr.lastModificationDate > ? and ltr.lastModificationDate < ? "
                + "ORDER BY ltr.lastModificationDate;";

        if (startDate == null || endDate == null) {
            showAlertDialog();
        } else {

            template.query(selectSQLExcel, new Object[]{startDate.toString(), endDate.plusDays(1).toString()}, rs -> {
                createWorkbook();
                int count = 1; // 0= header of Excel file
                while (rs.next()) {
                    // Get data from database:
                    String typiste = rs.getString(1);
                    String datum = rs.getString(2);
                    String raadpleging = rs.getString(3);
                    String aanmaakdatum = rs.getString(4);

                    // Create new row per record:
                    Row r = sheet.createRow(count++);
                    Cell cell = r.createCell(0);
                    cell.setCellValue(typiste);
                    Cell cell1 = r.createCell(1);
                    cell1.setCellValue(datum);
                    Cell cell2 = r.createCell(2);
                    cell2.setCellValue(raadpleging);
                    Cell cell3 = r.createCell(3);
                    cell3.setCellValue(aanmaakdatum);
                }
                // Write it to File:
                BufferedOutputStream output;
                try {
                    output = new BufferedOutputStream(new FileOutputStream(new File("\\\\ares\\data\\Toepassingen\\Cliniconnect\\Statistieken\\" + "Stats -"
                            + LocalDate.now().toString() + " - " + new Random().nextInt(100) + ".xlsx")));
                    workbook.write(output);
                    output.flush();
                    output.close();

                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public Integer addSecretary(String username) {
        String sql = "UPDATE global_user set usertype=1 WHERE username=?";
        return template.update(sql, username);
    }

    @Override
    public String getNameSecretary(String username) {
        String sqlId = "SELECT person_id FROM global_user where username=?";
        int person_id = template.queryForObject(sqlId, new Object[]{username}, (rs, i) -> rs.getInt("person_id"));
        String sqlName = "SELECT firstName, lastName FROM personalinfo_person WHERE id=?";

        return template.queryForObject(sqlName, new Object[]{person_id}, (rs, i) -> rs.getString("firstName") + " " + rs.getString("lastName"));
    }

    @Override
    public String getStatsForTotalLetters(LocalDate startDate, LocalDate endDate) {
        String selectSQLTotal = " SELECT COUNT(*) 'Aantal' FROM medical_letter ltr JOIN global_user gus ON gus.id = ltr.assistant_id WHERE ltr.status= 'FINAL' AND ltr.lastModificationDate > ? and ltr.lastModificationDate < ?";
        return getFastStatsWithOneResultBetweenTwoDates(selectSQLTotal, startDate, endDate);
    }

    @Override
    public String getStatsForOldestLetterTyped(LocalDate startDate, LocalDate endDate) {
        String selectSQLOldestLetter = "SELECT TOP(1) ltr.creationDate 'Aanmaakdatum'" + "FROM medical_letter ltr "
                + "JOIN global_user gus ON gus.id = ltr.assistant_id "
                + "WHERE ltr.status= 'FINAL' AND ltr.lastModificationDate > ? and ltr.lastModificationDate < ? "
                + "ORDER BY ltr.creationDate;";
        return getFastStatsWithOneResultBetweenTwoDates(selectSQLOldestLetter, startDate, endDate);
    }

    @Override
    public String getStatsForTotalLettersFinishedPerSecretary(LocalDate startDate, LocalDate endDate) {
        String selectSQLBySecretary = "SELECT dbo.displayName(gus.person_id) FROM medical_letter mlt JOIN global_user gus on gus.id = mlt.assistant_id WHERE mlt.status = 'FINAL' AND mlt.outdateddate IS NULL AND mlt.lastModificationDate > ? AND mlt.lastModificationDate < ? GROUP BY dbo.displayName(gus.person_id) ORDER BY 1;";
        return statsWithStartAndEndDate(startDate, endDate, selectSQLBySecretary);
    }

    @Override
    public String getStatsForTotalLettersFinishedPerSecretary2(LocalDate startDate, LocalDate endDate) {
        String selectSQLBySecretary2 = "SELECT count(*) FROM medical_letter mlt JOIN global_user gus on gus.id = mlt.assistant_id WHERE mlt.status = 'FINAL' AND mlt.outdateddate IS NULL AND mlt.lastModificationDate > ? AND mlt.lastModificationDate < ? GROUP BY dbo.displayName(gus.person_id)";
        return statsWithStartAndEndDate(startDate, endDate, selectSQLBySecretary2);
    }

    @Override
    public String getStatsForLastLetterFinishedBySecretary(LocalDate startDate, LocalDate endDate) {
        String selectSQLBySecretaryLastLetter = "SELECT TOP(1) dbo.displayName(gus.person_id), mlt.lastModificationDate FROM medical_letter mlt JOIN global_user gus on gus.id = mlt.assistant_id WHERE mlt.status = 'FINAL' AND mlt.outdateddate IS NULL AND mlt.lastModificationDate > ? AND mlt.lastModificationDate < ? and mlt.assistant_id=? ORDER BY mlt.lastModificationDate DESC;";
        return statsWithStartAndEndForSecretaries(startDate, endDate, selectSQLBySecretaryLastLetter);
    }

    @Override
    public String getStatsForFirstLetterFinishedBySecretary(LocalDate startDate, LocalDate endDate) {
        String selectSQLBySecretaryFirstLetter = "SELECT TOP(1) dbo.displayName(gus.person_id), mlt.lastModificationDate FROM medical_letter mlt JOIN global_user gus on gus.id = mlt.assistant_id WHERE mlt.status = 'FINAL' AND mlt.outdateddate IS NULL AND mlt.lastModificationDate > ? AND mlt.lastModificationDate < ? and mlt.assistant_id=? ORDER BY mlt.lastModificationDate;";
        return statsWithStartAndEndForSecretaries(startDate, endDate, selectSQLBySecretaryFirstLetter);
    }

    @Override
    public String getStatsForActiveUsers(LocalDate from) {
        String selectSQLActiveUsers = "SELECT COUNT (distinct username) FROM [EMRServer].[dbo].[Global_User] INNER JOIN [EMRServer].[dbo].[Statistic_RecordOpened] ON [EMRServer].[dbo].[Statistic_RecordOpened].user_fk = [EMRServer].[dbo].[Global_User].id where actionDate>? AND speciality_fk IS NULL";
        return statsFromDate(from, selectSQLActiveUsers);
    }

    @Override
    public String getStatsForActiveDoctors(LocalDate from) {
        String selectSQLActiveDoctors = "SELECT COUNT(distinct username) FROM [EMRServer].[dbo].[Global_User] INNER JOIN [EMRServer].[dbo].[Statistic_RecordOpened] ON [EMRServer].[dbo].[Statistic_RecordOpened].user_fk = [EMRServer].[dbo].[Global_User].id where actionDate> ? AND speciality_fk IS NOT NULL";
        return statsFromDate(from, selectSQLActiveDoctors);
    }

    @Override
    public String getStatsForActiveUsersWithName(LocalDate from) {
        String selectSQLActiveUsersWithName = "SELECT distinct(dbo.displayName(gus.person_id)) FROM [EMRServer].[dbo].[Global_User] gus INNER JOIN [EMRServer].[dbo].[Statistic_RecordOpened] sr ON sr.user_fk = gus.id where actionDate> ? AND speciality_fk IS NULL";
        return statsForUsersFromDate(from, selectSQLActiveUsersWithName);
    }

    @Override
    public String getStatsForActiveDoctorsWithName(LocalDate from) {
        String selectSQLActiveDoctorsWithName = "SELECT distinct(dbo.displayName(gus.person_id)) FROM [EMRServer].[dbo].[Global_User] gus INNER JOIN [EMRServer].[dbo].[Statistic_RecordOpened] sr ON sr.user_fk = gus.id where actionDate> ? AND speciality_fk IS NOT NULL";
        return statsForUsersFromDate(from, selectSQLActiveDoctorsWithName);
    }
}
