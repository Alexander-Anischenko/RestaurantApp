package de.ait.restaurantapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "files") //создаем сущность для всех файлов, для гибкости (если захотим добавить еще файлы)
@Data
@NoArgsConstructor
public class FileEntity {

    //прописываем генерацию ID для файлов
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // имя файла
    @Column(nullable = false)
    private String name;

    // тип файла
    @Column(nullable = false)
    private String fileType;

    // указываем формат хранения данных
    @Lob
    @Column(nullable = false)
    private byte[] data;

    // прописываем конструктор с аргументами
    public FileEntity(String name, String fileType, byte[] data) {
        this.name = name;
        this.fileType = fileType;
        this.data = data;
    }
}
