package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Student operations.
 */
@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentById(id: String): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE batchId = :batchId ORDER BY rollNo ASC")
    fun getStudentsByBatch(batchId: String): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: String)

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()
}

/**
 * Data Access Object for Monthly Fee tracking and status updates.
 */
@Dao
interface FeeDao {

    @Query("SELECT * FROM fee_records ORDER BY daysLate DESC, studentName ASC")
    fun getAllFees(): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fee_records WHERE studentId = :studentId ORDER BY dueDate DESC")
    fun getFeesByStudent(studentId: String): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fee_records WHERE isPaid = 0 ORDER BY daysLate DESC")
    fun getPendingFees(): Flow<List<FeeEntity>>

    @Query("SELECT * FROM fee_records WHERE id = :id LIMIT 1")
    fun getFeeById(id: String): Flow<FeeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFees(fees: List<FeeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: FeeEntity)

    @Query("UPDATE fee_records SET isPaid = :isPaid, paidDate = :paidDate, paymentMode = :paymentMode WHERE id = :feeId")
    suspend fun updateFeePaymentStatus(feeId: String, isPaid: Boolean, paidDate: String?, paymentMode: String?)

    @Query("DELETE FROM fee_records WHERE id = :id")
    suspend fun deleteFeeById(id: String)

    @Query("SELECT COUNT(*) FROM fee_records")
    suspend fun getFeeCount(): Int

    @Query("DELETE FROM fee_records")
    suspend fun deleteAllFees()
}

/**
 * Data Access Object for Daily Attendance records and alerts.
 */
@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance_logs WHERE date = :date ORDER BY rollNo ASC")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceLogEntity>>

    @Query("SELECT * FROM attendance_logs WHERE batchName = :batchName AND date = :date ORDER BY rollNo ASC")
    fun getAttendanceForBatchAndDate(batchName: String, date: String): Flow<List<AttendanceLogEntity>>

    @Query("SELECT * FROM attendance_logs ORDER BY recordedTimestamp DESC")
    fun getAllAttendanceLogs(): Flow<List<AttendanceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceLogs(logs: List<AttendanceLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceLog(log: AttendanceLogEntity)

    @Query("UPDATE attendance_logs SET isPresent = :isPresent, recordedTimestamp = :timestamp WHERE studentId = :studentId AND date = :date")
    suspend fun updateStudentAttendance(studentId: String, date: String, isPresent: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE attendance_logs SET isPresent = 1, recordedTimestamp = :timestamp WHERE batchName = :batchName AND date = :date")
    suspend fun markAllPresentInBatch(batchName: String, date: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM attendance_logs WHERE date = :date")
    suspend fun getAttendanceCountForDate(date: String): Int

    @Query("DELETE FROM attendance_logs")
    suspend fun deleteAllAttendance()
}

/**
 * Data Access Object for Institute and Teacher Profile.
 */
@Dao
interface CentreProfileDao {

    @Query("SELECT * FROM institute_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<CentreProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: CentreProfileEntity)

    @Query("SELECT COUNT(*) FROM institute_profile")
    suspend fun getProfileCount(): Int
}

