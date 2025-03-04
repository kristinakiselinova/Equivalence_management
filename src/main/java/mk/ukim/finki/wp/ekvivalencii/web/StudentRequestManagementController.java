package mk.ukim.finki.wp.ekvivalencii.web;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Join;
import mk.ukim.finki.wp.ekvivalencii.model.EquivalenceStatus;
import mk.ukim.finki.wp.ekvivalencii.model.Student;
import mk.ukim.finki.wp.ekvivalencii.model.StudentEquivalenceRequest;
import mk.ukim.finki.wp.ekvivalencii.model.StudyProgram;
import mk.ukim.finki.wp.ekvivalencii.service.interfaces.StudentRequestManagementService;
import mk.ukim.finki.wp.ekvivalencii.service.interfaces.StudentService;
import mk.ukim.finki.wp.ekvivalencii.service.interfaces.StudyProgramService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static mk.ukim.finki.wp.ekvivalencii.service.interfaces.specifications.FieldFilterSpecification.filterContainsText;
import static mk.ukim.finki.wp.ekvivalencii.service.interfaces.specifications.FieldFilterSpecification.filterEqualsV;
import static org.springframework.data.jpa.domain.Specification.where;


@Controller

public class StudentRequestManagementController {
    private final StudentRequestManagementService service;
    private final StudyProgramService studyProgramService;
    private final StudentService studentService;

    public StudentRequestManagementController(StudentRequestManagementService service,
                                              StudyProgramService studyProgramService,
                                              StudentService studentService) {
        this.service = service;
        this.studyProgramService = studyProgramService;
        this.studentService = studentService;
    }

@GetMapping("/ekvivalencii")
public String getStudentRequestManagement(Model model,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer results,
                                          @RequestParam(required = false) String index,
                                          @RequestParam(required = false) String oldStudyProgram,
                                          @RequestParam(required = false) String newStudyProgram,
                                          @RequestParam(required = false) String status) {
    Page<StudentEquivalenceRequest> studentEquivalenceRequestPage;

    Specification<StudentEquivalenceRequest> spec = Specification.where(filterEquals(StudentEquivalenceRequest.class, "student.index", index));


    if (oldStudyProgram != null && !oldStudyProgram.isEmpty()) {
        spec = spec.and(filterEqualsV(StudentEquivalenceRequest.class, "oldStudyProgram.code", oldStudyProgram));
    }
    if (newStudyProgram != null && !newStudyProgram.isEmpty()) {
        spec = spec.and(filterEqualsV(StudentEquivalenceRequest.class, "newStudyProgram.code", newStudyProgram));
    }
    if (status != null && !status.isEmpty()) {
        spec = spec.and(filterEqualsV(StudentEquivalenceRequest.class, "status", status));
    }

    studentEquivalenceRequestPage = service.list(spec, pageNum, results);
    model.addAttribute("page", studentEquivalenceRequestPage);
    model.addAttribute("requests", studentEquivalenceRequestPage.getContent());
    model.addAttribute("oldStudyPrograms", this.studyProgramService.findAll());
    model.addAttribute("newStudyPrograms", this.studyProgramService.findAll());
    return "listStudentRequestManagement";
}
    private <T> Specification<T> filterEquals(Class<T> entityClass, String attributeName, String attributeValue) {
        if (attributeValue != null && !attributeValue.isEmpty()) {
            return (root, query, criteriaBuilder) -> {
                Join<T, Student> studentJoin = root.join("student");
                return criteriaBuilder.equal(studentJoin.get("index"), attributeValue);
            };
        } else {
            return null;
        }
    }

    @GetMapping("/ekvivalencii/add")
public String addRequest(Model model) {
    List<Student> students=this.studentService.getAllStudents();
    List<StudyProgram> oldStudyPrograms=this.studyProgramService.findAll();
    List<StudyProgram> newStudyPrograms=this.studyProgramService.findAll();
    model.addAttribute("students", students);
    model.addAttribute("oldStudyPrograms", oldStudyPrograms);
    model.addAttribute("newStudyPrograms", newStudyPrograms);
    model.addAttribute("equivalenceStatusValues",EquivalenceStatus.values());
    return "formStudenRequestManagement";
}
    @PostMapping("/ekvivalencii/save")
    public String save(@RequestParam (required = false)  String id,
                       @RequestParam (required = false) Student student,
                       @RequestParam (required = false)  StudyProgram oldStudyProgram,
                       @RequestParam (required = false)  StudyProgram newStudyProgram,
                       @RequestParam (defaultValue = "REQUESTED")  EquivalenceStatus status) {
        StudyProgram oldSP=student.getStudyProgram();
        this.service.save(student, oldSP, newStudyProgram, status);
        return "redirect:/ekvivalencii";
    }

    @PostMapping("/ekvivalencii/{id}/delete")
public String delete(@PathVariable String id) {
    this.service.deleteById(id);
    return "redirect:/ekvivalencii";
}
@GetMapping("/ekvivalencii/{id}/edit")
public String edit(@PathVariable String id, Model model){
    if(this.service.findByIndex(id).isPresent()) {
        StudentEquivalenceRequest request=this.service.findByIndex(id).get();
        List<Student> students=this.studentService.getAllStudents();
        List<StudyProgram> oldStudyPrograms=this.studyProgramService.findAll();
        List<StudyProgram> newStudyPrograms=this.studyProgramService.findAll();
        model.addAttribute("request",request);
        model.addAttribute("students", students);
        model.addAttribute("oldStudyPrograms",oldStudyPrograms);
        model.addAttribute("newStudyPrograms", newStudyPrograms);
        model.addAttribute("status",EquivalenceStatus.values());
        return "editRequest";
    }
    return "/ekvivalencii";
}

@PostMapping("/ekvivalencii/{id}/edit")
public String editStudentGrades (@PathVariable String id,
                                 @RequestParam Student student,
                                 @RequestParam StudyProgram oldStudyProgram,
                                 @RequestParam StudyProgram newStudyProgram,
                                 @RequestParam EquivalenceStatus status) {
    this.service.edit(id, student, oldStudyProgram, newStudyProgram,status);
    return "redirect:/ekvivalencii";
}

}
