package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    @Mock
    private IDepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    @Test
    void getAllDepartment_ShouldReturnAllDepartments() {
        // Arrange
        Department dept1 = new Department();
        dept1.setIdDepartment(1L);
        dept1.setName("Computer Science");
        dept1.setLocation("Building A");

        Department dept2 = new Department();
        dept2.setIdDepartment(2L);
        dept2.setName("Mathematics");
        dept2.setLocation("Building B");

        List<Department> expectedDepartments = Arrays.asList(dept1, dept2);
        when(departmentService.getAllDepartments()).thenReturn(expectedDepartments);

        // Act
        List<Department> actualDepartments = departmentController.getAllDepartment();

        // Assert
        assertEquals(2, actualDepartments.size());
        assertEquals("Computer Science", actualDepartments.get(0).getName());
        assertEquals("Mathematics", actualDepartments.get(1).getName());
        verify(departmentService, times(1)).getAllDepartments();
    }

    @Test
    void getDepartment_WhenDepartmentExists_ShouldReturnDepartment() {
        // Arrange
        Long departmentId = 1L;
        Department expectedDepartment = new Department();
        expectedDepartment.setIdDepartment(departmentId);
        expectedDepartment.setName("Computer Science");
        expectedDepartment.setLocation("Building A");
        expectedDepartment.setPhone("123-456-7890");
        expectedDepartment.setHead("Dr. Smith");

        when(departmentService.getDepartmentById(departmentId)).thenReturn(expectedDepartment);

        // Act
        Department actualDepartment = departmentController.getDepartment(departmentId);

        // Assert
        assertNotNull(actualDepartment);
        assertEquals(departmentId, actualDepartment.getIdDepartment());
        assertEquals("Computer Science", actualDepartment.getName());
        assertEquals("Building A", actualDepartment.getLocation());
        verify(departmentService, times(1)).getDepartmentById(departmentId);
    }

    @Test
    void getDepartment_WhenDepartmentNotExists_ShouldReturnNull() {
        // Arrange
        Long departmentId = 999L;
        when(departmentService.getDepartmentById(departmentId)).thenReturn(null);

        // Act
        Department actualDepartment = departmentController.getDepartment(departmentId);

        // Assert
        assertNull(actualDepartment);
        verify(departmentService, times(1)).getDepartmentById(departmentId);
    }

    @Test
    void createDepartment_ShouldReturnCreatedDepartment() {
        // Arrange
        Department departmentToCreate = new Department();
        departmentToCreate.setName("Physics");
        departmentToCreate.setLocation("Building C");
        departmentToCreate.setPhone("987-654-3210");
        departmentToCreate.setHead("Dr. Johnson");

        Department createdDepartment = new Department();
        createdDepartment.setIdDepartment(1L);
        createdDepartment.setName("Physics");
        createdDepartment.setLocation("Building C");
        createdDepartment.setPhone("987-654-3210");
        createdDepartment.setHead("Dr. Johnson");

        when(departmentService.saveDepartment(departmentToCreate)).thenReturn(createdDepartment);

        // Act
        Department result = departmentController.createDepartment(departmentToCreate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdDepartment());
        assertEquals("Physics", result.getName());
        assertEquals("Building C", result.getLocation());
        verify(departmentService, times(1)).saveDepartment(departmentToCreate);
    }

    @Test
    void updateDepartment_ShouldReturnUpdatedDepartment() {
        // Arrange
        Department departmentToUpdate = new Department();
        departmentToUpdate.setIdDepartment(1L);
        departmentToUpdate.setName("Physics Updated");
        departmentToUpdate.setLocation("Building D");
        departmentToUpdate.setPhone("111-222-3333");
        departmentToUpdate.setHead("Dr. Brown");

        when(departmentService.saveDepartment(departmentToUpdate)).thenReturn(departmentToUpdate);

        // Act
        Department result = departmentController.updateDepartment(departmentToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdDepartment());
        assertEquals("Physics Updated", result.getName());
        assertEquals("Building D", result.getLocation());
        verify(departmentService, times(1)).saveDepartment(departmentToUpdate);
    }

    @Test
    void deleteDepartment_ShouldCallService() {
        // Arrange
        Long departmentId = 1L;
        doNothing().when(departmentService).deleteDepartment(departmentId);

        // Act
        departmentController.deleteDepartment(departmentId);

        // Assert
        verify(departmentService, times(1)).deleteDepartment(departmentId);
    }

    @Test
    void createDepartment_WithMinimalData_ShouldWork() {
        // Arrange
        Department department = new Department();
        department.setName("Test Department");

        Department savedDepartment = new Department();
        savedDepartment.setIdDepartment(1L);
        savedDepartment.setName("Test Department");

        when(departmentService.saveDepartment(department)).thenReturn(savedDepartment);

        // Act
        Department result = departmentController.createDepartment(department);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdDepartment());
        assertEquals("Test Department", result.getName());
        verify(departmentService, times(1)).saveDepartment(department);
    }

    @Test
    void getAllDepartment_WhenEmpty_ShouldReturnEmptyList() {
        // Arrange
        when(departmentService.getAllDepartments()).thenReturn(List.of());

        // Act
        List<Department> result = departmentController.getAllDepartment();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(departmentService, times(1)).getAllDepartments();
    }
}