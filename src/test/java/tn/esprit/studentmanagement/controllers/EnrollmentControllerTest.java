package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentControllerTest {

    @Mock
    private IEnrollment enrollmentService;

    @InjectMocks
    private EnrollmentController enrollmentController;

    @Test
    void getAllEnrollment_ShouldReturnAllEnrollments() {
        // Arrange
        Enrollment enrollment1 = new Enrollment();
        enrollment1.setIdEnrollment(1L);
        enrollment1.setEnrollmentDate(LocalDate.of(2024, 1, 15));
        enrollment1.setGrade(85.5);
        enrollment1.setStatus(Status.ACTIVE);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setIdEnrollment(2L);
        enrollment2.setEnrollmentDate(LocalDate.of(2024, 2, 20));
        enrollment2.setGrade(90.0);
        enrollment2.setStatus(Status.COMPLETED);

        List<Enrollment> expectedEnrollments = Arrays.asList(enrollment1, enrollment2);
        when(enrollmentService.getAllEnrollments()).thenReturn(expectedEnrollments);

        // Act
        List<Enrollment> actualEnrollments = enrollmentController.getAllEnrollment();

        // Assert
        assertEquals(2, actualEnrollments.size());
        assertEquals(1L, actualEnrollments.get(0).getIdEnrollment());
        assertEquals(2L, actualEnrollments.get(1).getIdEnrollment());
        assertEquals(Status.ACTIVE, actualEnrollments.get(0).getStatus());
        assertEquals(Status.COMPLETED, actualEnrollments.get(1).getStatus());
        verify(enrollmentService, times(1)).getAllEnrollments();
    }

    @Test
    void getEnrollment_WhenEnrollmentExists_ShouldReturnEnrollment() {
        // Arrange
        Long enrollmentId = 1L;
        Enrollment expectedEnrollment = new Enrollment();
        expectedEnrollment.setIdEnrollment(enrollmentId);
        expectedEnrollment.setEnrollmentDate(LocalDate.of(2024, 1, 15));
        expectedEnrollment.setGrade(85.5);
        expectedEnrollment.setStatus(Status.ACTIVE);

        when(enrollmentService.getEnrollmentById(enrollmentId)).thenReturn(expectedEnrollment);

        // Act
        Enrollment actualEnrollment = enrollmentController.getEnrollment(enrollmentId);

        // Assert
        assertNotNull(actualEnrollment);
        assertEquals(enrollmentId, actualEnrollment.getIdEnrollment());
        assertEquals(LocalDate.of(2024, 1, 15), actualEnrollment.getEnrollmentDate());
        assertEquals(85.5, actualEnrollment.getGrade());
        assertEquals(Status.ACTIVE, actualEnrollment.getStatus());
        verify(enrollmentService, times(1)).getEnrollmentById(enrollmentId);
    }

    @Test
    void getEnrollment_WhenEnrollmentNotExists_ShouldReturnNull() {
        // Arrange
        Long enrollmentId = 999L;
        when(enrollmentService.getEnrollmentById(enrollmentId)).thenReturn(null);

        // Act
        Enrollment actualEnrollment = enrollmentController.getEnrollment(enrollmentId);

        // Assert
        assertNull(actualEnrollment);
        verify(enrollmentService, times(1)).getEnrollmentById(enrollmentId);
    }

    @Test
    void createEnrollment_ShouldReturnCreatedEnrollment() {
        // Arrange
        Enrollment enrollmentToCreate = new Enrollment();
        enrollmentToCreate.setEnrollmentDate(LocalDate.of(2024, 3, 10));
        enrollmentToCreate.setGrade(75.0);
        enrollmentToCreate.setStatus(Status.ACTIVE);

        Enrollment createdEnrollment = new Enrollment();
        createdEnrollment.setIdEnrollment(1L);
        createdEnrollment.setEnrollmentDate(LocalDate.of(2024, 3, 10));
        createdEnrollment.setGrade(75.0);
        createdEnrollment.setStatus(Status.ACTIVE);

        when(enrollmentService.saveEnrollment(enrollmentToCreate)).thenReturn(createdEnrollment);

        // Act
        Enrollment result = enrollmentController.createEnrollment(enrollmentToCreate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdEnrollment());
        assertEquals(LocalDate.of(2024, 3, 10), result.getEnrollmentDate());
        assertEquals(75.0, result.getGrade());
        assertEquals(Status.ACTIVE, result.getStatus());
        verify(enrollmentService, times(1)).saveEnrollment(enrollmentToCreate);
    }

    @Test
    void updateEnrollment_ShouldReturnUpdatedEnrollment() {
        // Arrange
        Enrollment enrollmentToUpdate = new Enrollment();
        enrollmentToUpdate.setIdEnrollment(1L);
        enrollmentToUpdate.setEnrollmentDate(LocalDate.of(2024, 4, 1));
        enrollmentToUpdate.setGrade(88.0);
        enrollmentToUpdate.setStatus(Status.COMPLETED);

        when(enrollmentService.saveEnrollment(enrollmentToUpdate)).thenReturn(enrollmentToUpdate);

        // Act
        Enrollment result = enrollmentController.updateEnrollment(enrollmentToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdEnrollment());
        assertEquals(LocalDate.of(2024, 4, 1), result.getEnrollmentDate());
        assertEquals(88.0, result.getGrade());
        assertEquals(Status.COMPLETED, result.getStatus());
        verify(enrollmentService, times(1)).saveEnrollment(enrollmentToUpdate);
    }

    @Test
    void deleteEnrollment_ShouldCallService() {
        // Arrange
        Long enrollmentId = 1L;
        doNothing().when(enrollmentService).deleteEnrollment(enrollmentId);

        // Act
        enrollmentController.deleteEnrollment(enrollmentId);

        // Assert
        verify(enrollmentService, times(1)).deleteEnrollment(enrollmentId);
    }

    @Test
    void getAllEnrollment_WhenEmpty_ShouldReturnEmptyList() {
        // Arrange
        when(enrollmentService.getAllEnrollments()).thenReturn(List.of());

        // Act
        List<Enrollment> result = enrollmentController.getAllEnrollment();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(enrollmentService, times(1)).getAllEnrollments();
    }

    @Test
    void createEnrollment_WithDifferentStatus_ShouldWork() {
        // Arrange
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setGrade(95.5);
        enrollment.setStatus(Status.COMPLETED);

        Enrollment savedEnrollment = new Enrollment();
        savedEnrollment.setIdEnrollment(1L);
        savedEnrollment.setEnrollmentDate(LocalDate.now());
        savedEnrollment.setGrade(95.5);
        savedEnrollment.setStatus(Status.COMPLETED);

        when(enrollmentService.saveEnrollment(enrollment)).thenReturn(savedEnrollment);

        // Act
        Enrollment result = enrollmentController.createEnrollment(enrollment);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdEnrollment());
        assertEquals(95.5, result.getGrade());
        assertEquals(Status.COMPLETED, result.getStatus());
        verify(enrollmentService, times(1)).saveEnrollment(enrollment);
    }

    @Test
    void getEnrollment_WithFailedStatus_ShouldReturnCorrectEnrollment() {
        // Arrange
        Long enrollmentId = 3L;
        Enrollment enrollment = new Enrollment();
        enrollment.setIdEnrollment(enrollmentId);
        enrollment.setEnrollmentDate(LocalDate.of(2024, 5, 1));
        enrollment.setGrade(45.0);
        enrollment.setStatus(Status.FAILED);

        when(enrollmentService.getEnrollmentById(enrollmentId)).thenReturn(enrollment);

        // Act
        Enrollment result = enrollmentController.getEnrollment(enrollmentId);

        // Assert
        assertNotNull(result);
        assertEquals(enrollmentId, result.getIdEnrollment());
        assertEquals(45.0, result.getGrade());
        assertEquals(Status.FAILED, result.getStatus());
        verify(enrollmentService, times(1)).getEnrollmentById(enrollmentId);
    }
}