package be.somedi.statistiekencc.dao;

import java.time.LocalDate;

public interface StatsDao {

    void getStatsForMedSec(LocalDate startDate, LocalDate endDate);

    String getStatsForTotalLetters(LocalDate startDate, LocalDate endDate);

    String getStatsForOldestLetterTyped(LocalDate startDate, LocalDate endDate);

    String getStatsForTotalLettersFinishedPerSecretary(LocalDate startDate, LocalDate endDate);

    String getStatsForTotalLettersFinishedPerSecretary2(LocalDate startDate, LocalDate endDate);

    String getStatsForLastLetterFinishedBySecretary(LocalDate startDate, LocalDate endDate);

    String getStatsForFirstLetterFinishedBySecretary(LocalDate startDate, LocalDate endDate);

    String getStatsForActiveUsers(LocalDate from);

    String getStatsForActiveDoctors(LocalDate from);

    String getStatsForActiveUsersWithName(LocalDate from);

    String getStatsForActiveDoctorsWithName(LocalDate from);

    Integer addSecretary(String username);

    String getNameSecretary(String username);
}