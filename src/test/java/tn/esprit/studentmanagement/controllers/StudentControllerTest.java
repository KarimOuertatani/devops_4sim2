package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.services.IStudentService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private IStudentService studentService;

    @InjectMocks
    private StudentController studentController;

    @Test
    void getAllStudents_ShouldReturnAllStudents() {
        // Arrange
        Student student1 = new Student();
        student1.setIdStudent(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");
        student1.setEmail("john.doe@example.com");
        student1.setDateOfBirth(LocalDate.of(2000, 5, 15));

        Student student2 = new Student();
        student2.setIdStudent(2L);
        student2.setFirstName("Jane");
        student2.setLastName("Smith");
        student2.setEmail("jane.smith@example.com");
        student2.setDateOfBirth(LocalDate.of(2001, 3, 20));

        List<Student> expectedStudents = Arrays.asList(student1, student2);
        when(studentService.getAllStudents()).thenReturn(expectedStudents);

        // Act
        List<Student> actualStudents = studentController.getAllStudents();

        // Assert
        assertEquals(2, actualStudents.size());
        assertEquals("John", actualStudents.get(0).getFirstName());
        assertEquals("Doe", actualStudents.get(0).getLastName());
        assertEquals("Jane", actualStudents.get(1).getFirstName());
        assertEquals("Smith", actualStudents.get(1).getLastName());
        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    void getStudent_WhenStudentExists_ShouldReturnStudent() {
        // Arrange
        Long studentId = 1L;
        Student expectedStudent = new Student();
        expectedStudent.setIdStudent(studentId);
        expectedStudent.setFirstName("John");
        expectedStudent.setLastName("Doe");
        expectedStudent.setEmail("john.doe@example.com");
        expectedStudent.setPhone("123-456-7890");
        expectedStudent.setDateOfBirth(LocalDate.of(2000, 5, 15));
        expectedStudent.setAddress("123 Main St");

        when(studentService.getStudentById(studentId)).thenReturn(expectedStudent);

        // Act
        Student actualStudent = studentController.getStudent(studentId);

        // Assert
        assertNotNull(actualStudent);
        assertEquals(studentId, actualStudent.getIdStudent());
        assertEquals("John", actualStudent.getFirstName());
        assertEquals("Doe", actualStudent.getLastName());
        assertEquals("john.doe@example.com", actualStudent.getEmail());
        assertEquals(LocalDate.of(2000, 5, 15), actualStudent.getDateOfBirth());
        verify(studentService, times(1)).getStudentById(studentId);
    }

    @Test
    void getStudent_WhenStudentNotExists_ShouldReturnNull() {
        // Arrange
        Long studentId = 999L;
        when(studentService.getStudentById(studentId)).thenReturn(null);

        // Act
        Student actualStudent = studentController.getStudent(studentId);

        // Assert
        assertNull(actualStudent);
        verify(studentService, times(1)).getStudentById(studentId);
    }

    @Test
    void createStudent_ShouldReturnCreatedStudent() {
        // Arrange
        Student studentToCreate = new Student();
        studentToCreate.setFirstName("Alice");
        studentToCreate.setLastName("Johnson");
        studentToCreate.setEmail("alice.johnson@example.com");
        studentToCreate.setPhone("987-654-3210");
        studentToCreate.setDateOfBirth(LocalDate.of(2002, 7, 30));
        studentToCreate.setAddress("456 Oak Ave");

        Student createdStudent = new Student();
        createdStudent.setIdStudent(1L);
        createdStudent.setFirstName("Alice");
        createdStudent.setLastName("Johnson");
        createdStudent.setEmail("alice.johnson@example.com");
        createdStudent.setPhone("987-654-3210");
        createdStudent.setDateOfBirth(LocalDate.of(2002, 7, 30));
        createdStudent.setAddress("456 Oak Ave");

        when(studentService.saveStudent(studentToCreate)).thenReturn(createdStudent);

        // Act
        Student result = studentController.createStudent(studentToCreate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdStudent());
        assertEquals("Alice", result.getFirstName());
        assertEquals("Johnson", result.getLastName());
        assertEquals("alice.johnson@example.com", result.getEmail());
        assertEquals(LocalDate.of(2002, 7, 30), result.getDateOfBirth());
        verify(studentService, times(1)).saveStudent(studentToCreate);
    }

    @Test
    void updateStudent_ShouldReturnUpdatedStudent() {
        // Arrange
        Student studentToUpdate = new Student();
        studentToUpdate.setIdStudent(1L);
        studentToUpdate.setFirstName("Alice");
        studentToUpdate.setLastName("Johnson-Updated");
        studentToUpdate.setEmail("alice.updated@example.com");
        studentToUpdate.setPhone("111-222-3333");
        studentToUpdate.setDateOfBirth(LocalDate.of(2002, 8, 1));
        studentToUpdate.setAddress("789 Pine St");

        when(studentService.saveStudent(studentToUpdate)).thenReturn(studentToUpdate);

        // Act
        Student result = studentController.updateStudent(studentToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdStudent());
        assertEquals("Alice", result.getFirstName());
        assertEquals("Johnson-Updated", result.getLastName());
        assertEquals("alice.updated@example.com", result.getEmail());
        assertEquals(LocalDate.of(2002, 8, 1), result.getDateOfBirth());
        assertEquals("789 Pine St", result.getAddress());
        verify(studentService, times(1)).saveStudent(studentToUpdate);
    }

    @Test
    void deleteStudent_ShouldCallService() {
        // Arrange
        Long studentId = 1L;
        doNothing().when(studentService).deleteStudent(studentId);

        // Act
        studentController.deleteStudent(studentId);

        // Assert
        verify(studentService, times(1)).deleteStudent(studentId);
    }

    @Test
    void getAllStudents_WhenEmpty_ShouldReturnEmptyList() {
        // Arrange
        when(studentService.getAllStudents()).thenReturn(List.of());

        // Act
        List<Student> result = studentController.getAllStudents();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(studentService, times(1)).getAllStudents();
    }

    @Test
    void createStudent_WithMinimalData_ShouldWork() {
        // Arrange
        Student student = new Student();
        student.setFirstName("Bob");
        student.setLastName("Wilson");

        Student savedStudent = new Student();
        savedStudent.setIdStudent(1L);
        savedStudent.setFirstName("Bob");
        savedStudent.setLastName("Wilson");

        when(studentService.saveStudent(student)).thenReturn(savedStudent);

        // Act
        Student result = studentController.createStudent(student);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdStudent());
        assertEquals("Bob", result.getFirstName());
        assertEquals("Wilson", result.getLastName());
        verify(studentService, times(1)).saveStudent(student);
    }

    @Test
    void updateStudent_WithPartialData_ShouldWork() {
        // Arrange
        Student student = new Student();
        student.setIdStudent(1L);
        student.setFirstName("UpdatedName");

        Student updatedStudent = new Student();
        updatedStudent.setIdStudent(1L);
        updatedStudent.setFirstName("UpdatedName");

        when(studentService.saveStudent(student)).thenReturn(updatedStudent);

        // Act
        Student result = studentController.updateStudent(student);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdStudent());
        assertEquals("UpdatedName", result.getFirstName());
        verify(studentService, times(1)).saveStudent(student);
    }
}
