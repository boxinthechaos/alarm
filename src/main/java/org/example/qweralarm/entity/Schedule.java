package org.example.qweralarm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate targetDate;
    private String targetTime;
    private String content;
    private boolean isSent = false;
    private boolean isCompleted = false;
    private LocalDateTime completedAt;

    @Column(length = 20)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void markSend(){
        this.isSent = true;
    }

    public void successSchedule(){
        this.isCompleted = true;
        this.completedAt = java.time.LocalDateTime.now();
    }
    public void cancelSuccess(){
        this.isCompleted = false;
        this.completedAt = null;
    }
}
