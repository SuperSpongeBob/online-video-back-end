package com.example.onlinevideo.Entity;

import com.example.onlinevideo.DTO.Page;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@Entity
@Table(name = "video_album")
@AllArgsConstructor
@NoArgsConstructor
public class VideoAlbum extends Page implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer videoAlbumId;
    private String videoAlbumName;
    private String videoPostPath;
    private String videoReleaseDate;
    private String videoSummary;
    private String videoChannel;
    private String videoDirector;
    private String videoArea;
    private Integer videoFavoriteNumber;
    private String videoUpdateTime;
    private String videoLastUpdate;
    private String videoActor;
    private Integer userId;
}
