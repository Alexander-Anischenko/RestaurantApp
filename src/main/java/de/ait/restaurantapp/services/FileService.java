package de.ait.restaurantapp.services;

import de.ait.restaurantapp.model.FileEntity;
import de.ait.restaurantapp.repositories.FileRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    private final FileRepo fileRepo;

    public FileService(FileRepo fileRepo) {
        this.fileRepo = fileRepo;
    }


    // сохранение файла в бд
    public FileEntity saveFile(MultipartFile file){
        String fileName = file.getOriginalFilename();

        String fileType = file.getContentType();
        try {
            byte[] data = file.getBytes();
            FileEntity fileEntity = new FileEntity(fileName, fileType, data);
            return fileRepo.save(fileEntity);
        } catch (IOException exception){
            return null;
        }
    }


    //список всез файлов
    public List<FileEntity> getAllFiles(){
        return fileRepo.findAll();
    }


    //метод для удаления файла
    public void deleteById(Long id){
        fileRepo.deleteById(id);
    }

    //Нужен ли нам метод для поиска файла? В данном случае у нас будет только один файл(меню)
    public Optional<FileEntity> getFile(Long id){
        return fileRepo.findById(id);
    }


}
