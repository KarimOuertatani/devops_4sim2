package tn.esprit.studentmanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Student;
import tn.esprit.studentmanagement.repositories.StudentRepository;
import tn.esprit.studentmanagement.services.StudentService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void getAllStudents_ShouldReturnAllStudents() {
        // Arrange
        Student student1 = new Student();
        student1.setIdStudent(1L);
        student1.setFirstName("John");

        Student student2 = new Student();
        student2.setIdStudent(2L);
        student2.setFirstName("Jane");

        List<Student> expectedStudents = Arrays.asList(student1, student2);
        when(studentRepository.findAll()).thenReturn(expectedStudents);

        // Act
        List<Student> actualStudents = studentService.getAllStudents();

        // Assert
        assertEquals(2, actualStudents.size());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void getStudentById_WhenStudentExists_ShouldReturnStudent() {
        // Arrange
        Long studentId = 1L;
        Student expectedStudent = new Student();
        expectedStudent.setIdStudent(studentId);
        expectedStudent.setFirstName("John");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(expectedStudent));

        // Act
        Student actualStudent = studentService.getStudentById(studentId);

        // Assert
        assertNotNull(actualStudent);
        assertEquals(studentId, actualStudent.getIdStudent());
        verify(studentRepository, times(1)).findById(studentId);
    }

    @Test
    void getStudentById_WhenStudentNotExists_ShouldReturnNull() {
        // Arrange
        Long studentId = 999L;
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act
        Student actualStudent = studentService.getStudentById(studentId);

        // Assert
        assertNull(actualStudent);
        verify(studentRepository, times(1)).findById(studentId);
    }

    @Test
    void saveStudent_ShouldReturnSavedStudent() {
        // Arrange
        Student studentToSave = new Student();
        studentToSave.setFirstName("John");

        Student savedStudent = new Student();
        savedStudent.setIdStudent(1L);
        savedStudent.setFirstName("John");

        when(studentRepository.save(studentToSave)).thenReturn(savedStudent);

        // Act
        Student result = studentService.saveStudent(studentToSave);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdStudent());
        verify(studentRepository, times(1)).save(studentToSave);
    }

    @Test
    void deleteStudent_ShouldCallRepository() {
        // Arrange
        Long studentId = 1L;

        // Act
        studentService.deleteStudent(studentId);

        // Assert
        verify(studentRepository, times(1)).deleteById(studentId);
    }
}