package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TuitionRoomDatabaseTest {

    private lateinit var db: TuitionDeskDatabase
    private lateinit var studentDao: StudentDao
    private lateinit var feeDao: FeeDao
    private lateinit var attendanceDao: AttendanceDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TuitionDeskDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        studentDao = db.studentDao()
        feeDao = db.feeDao()
        attendanceDao = db.attendanceDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testStudentDaoOperations() = runBlocking {
        val student = StudentEntity(
            id = "S101",
            name = "Rohan Sharma",
            rollNo = "10A-99",
            batchId = "B1",
            batchName = "Class 10 - Maths",
            parentName = "Mr. Sharma",
            parentPhone = "+91 9876543210",
            attendancePct = 90,
            pendingFee = 2500
        )

        studentDao.insertStudent(student)

        val count = studentDao.getStudentCount()
        assertEquals(1, count)

        val retrieved = studentDao.getStudentById("S101").first()
        assertNotNull(retrieved)
        assertEquals("Rohan Sharma", retrieved?.name)
        assertEquals("10A-99", retrieved?.rollNo)
    }

    @Test
    fun testFeeDaoOperationsAndStatusUpdate() = runBlocking {
        val fee = FeeEntity(
            id = "F101",
            studentId = "S101",
            studentName = "Rohan Sharma",
            rollNo = "10A-99",
            batchName = "Class 10 - Maths",
            amountDue = 2500,
            dueDate = "10 Oct 2026",
            daysLate = 12,
            isPaid = false,
            parentPhone = "+91 9876543210"
        )

        feeDao.insertFee(fee)

        val initialList = feeDao.getAllFees().first()
        assertEquals(1, initialList.size)
        assertEquals(false, initialList[0].isPaid)

        // Mark fee as paid via UPI
        feeDao.updateFeePaymentStatus("F101", true, "22 Oct 2026", "UPI")

        val updated = feeDao.getFeeById("F101").first()
        assertNotNull(updated)
        assertTrue(updated!!.isPaid)
        assertEquals("22 Oct 2026", updated.paidDate)
        assertEquals("UPI", updated.paymentMode)
    }

    @Test
    fun testAttendanceDaoOperationsAndBatchMarkAllPresent() = runBlocking {
        val date = "22 Oct 2026"
        val batch = "Class 10 - Maths"

        val logs = listOf(
            AttendanceLogEntity(
                studentId = "S1",
                studentName = "Student 1",
                rollNo = "01",
                batchId = "B1",
                batchName = batch,
                parentPhone = "+91 9876543211",
                parentName = "Parent 1",
                isPresent = true,
                date = date
            ),
            AttendanceLogEntity(
                studentId = "S2",
                studentName = "Student 2",
                rollNo = "02",
                batchId = "B1",
                batchName = batch,
                parentPhone = "+91 9876543212",
                parentName = "Parent 2",
                isPresent = false,
                date = date
            )
        )

        attendanceDao.insertAttendanceLogs(logs)

        val initialLogs = attendanceDao.getAttendanceForBatchAndDate(batch, date).first()
        assertEquals(2, initialLogs.size)

        // Toggle student 2 to Present
        attendanceDao.updateStudentAttendance("S2", date, true)

        val updatedLogs = attendanceDao.getAttendanceForBatchAndDate(batch, date).first()
        assertTrue(updatedLogs.all { it.isPresent })
    }
}
