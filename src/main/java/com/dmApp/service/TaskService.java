package com.dmApp.service;

import com.dmApp.dto.CreateTaskRequestDto;
import com.dmApp.dto.TaskResponseDto;
import com.dmApp.dto.UpdateTaskRequestDto;
import com.dmApp.entity.Task;
import com.dmApp.entity.User;
import com.dmApp.enums.TaskStatus;
import com.dmApp.repository.TaskRepository;
import com.dmApp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void checkOwnership(Task task, User currentUser) {
        if (task.getUser().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this task");
        }
    }

    private TaskResponseDto mapToResponseDto(Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getUser().getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getFinishedAt(),
                task.getStatus()
        );
    }

    @Transactional
    public ResponseEntity<TaskResponseDto> createNewTask(CreateTaskRequestDto request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User currentUser = getCurrentUser();
        if (user.getId() != (currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only create tasks for yourself");
        }

        Task task = new Task();
        task.setUser(user);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setCompleted(false);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setFinishedAt(null);
        task.setStatus(TaskStatus.CREATED);

        Task savedTask = taskRepository.save(task);

        return ResponseEntity.ok(mapToResponseDto(savedTask));
    }

    public ResponseEntity<TaskResponseDto> getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        User currentUser = getCurrentUser();
        checkOwnership(task, currentUser);

        return ResponseEntity.ok(mapToResponseDto(task));
    }

    public ResponseEntity<List<TaskResponseDto>> getAllUserTasks() {
        User currentUser = getCurrentUser();

        List<Task> userTasks = taskRepository.findByUserId(currentUser.getId());

        List<TaskResponseDto> taskDtos = userTasks.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDtos);
    }

    @Transactional
    public ResponseEntity<TaskResponseDto> updateTask(Long id, UpdateTaskRequestDto request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        User currentUser = getCurrentUser();
        checkOwnership(task, currentUser);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setCompleted(request.completed());
        task.setFinishedAt(request.finishedAt());
        task.setStatus(request.status());
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);

        return ResponseEntity.ok(mapToResponseDto(updatedTask));
    }


    @Transactional
    public ResponseEntity<TaskResponseDto> updateTaskStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        User currentUser = getCurrentUser();
        checkOwnership(task, currentUser);

        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());

        if (status == TaskStatus.COMPLETED || status == TaskStatus.FINISHED) {
            task.setCompleted(true);
            task.setFinishedAt(LocalDateTime.now());
        }

        Task updatedTask = taskRepository.save(task);

        return ResponseEntity.ok(mapToResponseDto(updatedTask));
    }

    @Transactional
    public ResponseEntity<Void> deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        User currentUser = getCurrentUser();
        checkOwnership(task, currentUser);

        taskRepository.delete(task);

        return ResponseEntity.ok().build();
    }
}
