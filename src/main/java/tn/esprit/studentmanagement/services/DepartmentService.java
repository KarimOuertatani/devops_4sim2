package tn.esprit.studentmanagement.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class DepartmentService implements IDepartmentService {
    private DepartmentRepository departmentRepository;

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Department getDepartmentById(Long idDepartment) {
        return departmentRepository.findById(idDepartment).orElse(null);
    }

    @Override
    public Department saveDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public void deleteDepartment(Long idDepartment) {
        departmentRepository.deleteById(idDepartment);
    }
}
