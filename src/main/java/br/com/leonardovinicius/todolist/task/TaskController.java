package br.com.leonardovinicius.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.leonardovinicius.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;




@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")  
    public ResponseEntity create (@RequestBody TaskModel taskModel, HttpServletRequest request ){
        System.out.println("chegou no controller : " + request.getAttribute("idUser"));
        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve ser maior do que a data atual");
        } else if (currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de fim deve ser maior do que a data atual");
        }

        else if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de fim deve ser maior do que a data de início");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }
    
    
    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    
    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var tasks = this.taskRepository.findByIdUser((UUID) idUser);
        return tasks;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
        try {
            var task = this.taskRepository.findById(id).orElse(null);

            if (task == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Tarefa Não encontrada");
            }

            var idUser = request.getAttribute("idUser");
            System.out.println("id user: " + taskModel);
            if ( !task.getIdUser().equals(idUser)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Usuário não tem permissão para alterar essa tarefa");
            }

            Utils.copyNonNullProperties(taskModel, task);

            var taskUpdated = this.taskRepository.save(task);
            return ResponseEntity.status(HttpStatus.OK).body(taskUpdated);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno: " + e.getMessage());
        }
    }



}
