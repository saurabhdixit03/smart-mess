package com.smartmess.backend.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.smartmess.backend.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "mess_settings")
public class MessSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settingsId;

    /*
     * Tenant Ownership
     *
     * Each mess has its own settings record.
     * The unique mess_id prevents multiple settings records
     * from being created for the same mess.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mess_id", nullable = false, unique = true)
    private Mess mess;

    /*
     * Payment Settings
     *
     * Payment details are optional until the owner
     * configures UPI payment settings.
     */
    @Column(unique = true)
    private String upiId;

    @Column
    private String receiverName;

    /*
     * Customer Response Cutoff Settings
     *
     * The response window starts automatically
     * when the menu is published.
     */
    @Column
    private LocalTime lunchResponseCutoff;

    @Column
    private LocalTime dinnerResponseCutoff;

    /*
     * Weekly Off Settings
     *
     * Example:
     * weeklyClosedDay = SUNDAY
     * weeklyLunchClosed = false
     * weeklyDinnerClosed = true
     *
     * This represents Sunday dinner as the weekly off.
     *
     * The owner can change the configured day and
     * sessions whenever the weekly schedule changes.
     */
    @Enumerated(EnumType.STRING)
    @Column
    private DayOfWeek weeklyClosedDay;

    @Column(nullable = false)
    private boolean weeklyLunchClosed = false;

    @Column(nullable = false)
    private boolean weeklyDinnerClosed = false;

    public Long getSettingsId() {
        return settingsId;
    }

    public void setSettingsId(Long settingsId) {
        this.settingsId = settingsId;
    }

    public Mess getMess() {
        return mess;
    }

    public void setMess(Mess mess) {
        this.mess = mess;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public LocalTime getLunchResponseCutoff() {
        return lunchResponseCutoff;
    }

    public void setLunchResponseCutoff(
            LocalTime lunchResponseCutoff) {

        this.lunchResponseCutoff = lunchResponseCutoff;
    }

    public LocalTime getDinnerResponseCutoff() {
        return dinnerResponseCutoff;
    }

    public void setDinnerResponseCutoff(
            LocalTime dinnerResponseCutoff) {

        this.dinnerResponseCutoff = dinnerResponseCutoff;
    }

    public DayOfWeek getWeeklyClosedDay() {
        return weeklyClosedDay;
    }

    public void setWeeklyClosedDay(
            DayOfWeek weeklyClosedDay) {

        this.weeklyClosedDay = weeklyClosedDay;
    }

    public boolean isWeeklyLunchClosed() {
        return weeklyLunchClosed;
    }

    public void setWeeklyLunchClosed(
            boolean weeklyLunchClosed) {

        this.weeklyLunchClosed = weeklyLunchClosed;
    }

    public boolean isWeeklyDinnerClosed() {
        return weeklyDinnerClosed;
    }

    public void setWeeklyDinnerClosed(
            boolean weeklyDinnerClosed) {

        this.weeklyDinnerClosed = weeklyDinnerClosed;
    }
}