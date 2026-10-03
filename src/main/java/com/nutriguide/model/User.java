package com.nutriguide.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User: Represents an application user corresponding to the 'user_profile' relation.
 * Demonstrates Java Lab Manual Exp 1-2:
 * - Default constructor
 * - Parameterized constructor (for database hydration and new user registration)
 * - Copy constructor (for defensive copying)
 * - Encapsulation (private fields, getters, setters)
 * - Object method overrides (toString, equals, hashCode)
 */
public class User {

    private Integer userId;
    private String name;
    private String email;
    private String passwordHash;
    private Integer age;
    private String gender;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String activityLevel;
    private LocalDateTime createdAt;

    /**
     * Default constructor (Java Lab Exp 1-2)
     */
    public User() {
    }

    /**
     * Parameterized constructor for new user registration (before database assigns userId)
     */
    public User(String name, String email, String passwordHash, Integer age,
                String gender, BigDecimal heightCm, BigDecimal weightKg, String activityLevel) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.age = age;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.activityLevel = activityLevel;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Full parameterized constructor (for database record hydration)
     */
    public User(Integer userId, String name, String email, String passwordHash, Integer age,
                String gender, BigDecimal heightCm, BigDecimal weightKg, String activityLevel,
                LocalDateTime createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.age = age;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.activityLevel = activityLevel;
        this.createdAt = createdAt;
    }

    /**
     * Copy constructor (Java Lab Exp 1-2: creating defensive copies)
     *
     * @param other Existing User instance to duplicate
     */
    public User(User other) {
        if (other != null) {
            this.userId = other.userId;
            this.name = other.name;
            this.email = other.email;
            this.passwordHash = other.passwordHash;
            this.age = other.age;
            this.gender = other.gender;
            this.heightCm = other.heightCm;
            this.weightKg = other.weightKg;
            this.activityLevel = other.activityLevel;
            this.createdAt = other.createdAt;
        }
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Convenience double-conversion getters for calculations
    public double getHeightCmDouble() {
        return heightCm != null ? heightCm.doubleValue() : 0.0;
    }

    public double getWeightKgDouble() {
        return weightKg != null ? weightKg.doubleValue() : 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) &&
               Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", heightCm=" + heightCm +
                ", weightKg=" + weightKg +
                ", activityLevel='" + activityLevel + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
