package de.ait.restaurantapp.controller;

import de.ait.restaurantapp.model.FileEntity;
import de.ait.restaurantapp.services.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // метод для показа формы загрузки
    @GetMapping("upload")
    public String showUploadForm(Model model){
        return "upload-form";
    }


    //список всех файлов
    @GetMapping("/list")
    public String listFiles(Model model) {
        List<FileEntity> files = fileService.getAllFiles();
        model.addAttribute("files", files);
        return "file-list";
    }

    // загрузка файла
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model){
        if (file.isEmpty()){
            model.addAttribute("message", "Файл для загрузки не выбран");
            return "upload-form";
        }
        FileEntity savedFile = fileService.saveFile(file);
        if (savedFile == null){
            model.addAttribute("message", "Ошибка при загрузке файла");
            return "upload-form";
        }
        else {
            model.addAttribute("message", "Файл успешно загружен");
            model.addAttribute("fileId", savedFile.getId());
            return "upload-form";
        }
    }

    // скачивание файла
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id){
        return fileService.getFile(id)
                .map(fileEntity -> {
                    return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + fileEntity.getName() + "\"").body(fileEntity.getData());
                })
                .orElseGet(()-> {
                    return ResponseEntity.notFound().build();
                });
    }

    //удаление файла
    @PostMapping("/delete-file/{id}")
    public String deleteById(@PathVariable Long id, RedirectAttributes redirectAttributes){
        Optional<FileEntity> fileEntityOptional= fileService.getFile(id);
        if (fileEntityOptional.isEmpty()){
            redirectAttributes.addAttribute("message", "файл не найден");
        }
        else {
            fileService.deleteById(id);
            redirectAttributes.addAttribute("message", "файл успешно удален");
        }
        return "redirect:/file/list";
    }

}
