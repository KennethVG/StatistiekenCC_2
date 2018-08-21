package be.somedi.statistiekencc.javafx;

import be.somedi.statistiekencc.dao.StatsDao;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
public class StatistiekenController {

    @FXML
    private DatePicker dpStartDate;
    @FXML
    private DatePicker dpEndDate;
    @FXML
    private DatePicker dpFromUsers;
    @FXML
    private DatePicker dpFromDoctors;
    @FXML
    private Label lblSecretaryStats;
    @FXML
    private Label lblCountSecretaryStats;
    @FXML
    private Label lblLetterCount;
    @FXML
    private Label lblOldestLetter;
    @FXML
    private Label lblActiveUsers;
    @FXML
    private Label lblActiveDocters;
    @FXML
    private TextArea txtaFirstLetterTyped;
    @FXML
    private TextArea txtaLastLetterTyped;
    @FXML
    private TextField newTypiste;
    @FXML
    private Text secretaryAdded;

    private final StatsDao dao;

    @Autowired
    public StatistiekenController(StatsDao dao) {
        this.dao = dao;
    }

    @FXML
    private void initialize() {
        lblSecretaryStats.setVisible(false);
        lblLetterCount.setVisible(false);
        lblOldestLetter.setVisible(false);
        lblCountSecretaryStats.setVisible(false);
        lblActiveDocters.setVisible(false);
        lblActiveUsers.setVisible(false);
        txtaFirstLetterTyped.setVisible(false);
        txtaLastLetterTyped.setVisible(false);
    }

    @FXML
    private void generateStatistics() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();
        dao.getStatsForMedSec(startDate, endDate);
    }

    @FXML
    private void generateFastStast() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        String datum = dao.getStatsForOldestLetterTyped(startDate, endDate);
        String dates[] = datum.split(" ");
        lblOldestLetter.setVisible(true);
        lblOldestLetter.setText(dates[0] + " " + dates[1].split(":")[0] + ":" + dates[1].split(":")[1]);

        String result = dao.getStatsForTotalLettersFinishedPerSecretary(startDate, endDate);
        lblSecretaryStats.setVisible(true);
        lblSecretaryStats.setText(result);

        String resultTotal = dao.getStatsForTotalLetters(startDate, endDate);
        lblLetterCount.setVisible(true);
        lblLetterCount.setText(resultTotal);

        String resultCount = dao.getStatsForTotalLettersFinishedPerSecretary2(startDate, endDate);
        lblCountSecretaryStats.setVisible(true);
        lblCountSecretaryStats.setText(resultCount);

        String resultFirstLetter = dao.getStatsForFirstLetterFinishedBySecretary(startDate, endDate);
        txtaFirstLetterTyped.setVisible(true);
        txtaFirstLetterTyped.setText(resultFirstLetter);
        String resultLastLetter = dao.getStatsForLastLetterFinishedBySecretary(startDate, endDate);
        txtaLastLetterTyped.setVisible(true);
        txtaLastLetterTyped.setText(resultLastLetter);

    }

    @FXML
    private void showActiveUsers() {
        LocalDate from = dpFromUsers.getValue();
        String result = dao.getStatsForActiveUsers(from);
        lblActiveUsers.setVisible(true);
        lblActiveUsers.setText(result);
    }

    @FXML
    private void showActiveDocters() {
        LocalDate from = dpFromDoctors.getValue();
        String result = dao.getStatsForActiveDoctors(from);
        lblActiveDocters.setVisible(true);
        lblActiveDocters.setText(result);
    }

    @FXML
    private void showActiveDoctersWithName() {
        LocalDate from = dpFromDoctors.getValue();
        String result = dao.getStatsForActiveDoctorsWithName(from);
        if (result != null) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("Iedereen die in Cliniconnect DOKTER rechten heeft");
            alert.setTitle("Actieve dokters:");
            alert.setContentText(result);
            alert.showAndWait();
        }
    }

    @FXML
    private void showActiveUsersWithName() {
        LocalDate from = dpFromUsers.getValue();
        String result = dao.getStatsForActiveUsersWithName(from);
        if (result != null) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("Iedereen die in Cliniconnect VERPLEGING of SECRETARIAAT rechten heeft");
            alert.setTitle("Actieve users:");
            alert.setContentText(result);
            alert.showAndWait();
        }
    }

    @FXML
    private void addSecretary(){
        String username = newTypiste.getText();
        String nameTypiste = dao.getNameSecretary(username);
        int rowsAffected = dao.addSecretary(username);
        if(rowsAffected > 0){
            secretaryAdded.setText("De volgende typiste is in Cliniconnect succesvol toegevoegd :\n" + nameTypiste);
            secretaryAdded.setFont(Font.font("System Bold", 12));
        }
    }
}
