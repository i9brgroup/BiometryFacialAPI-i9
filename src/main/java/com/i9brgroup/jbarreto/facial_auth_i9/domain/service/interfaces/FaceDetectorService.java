package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces;

import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FaceDetectorService {
        /**
        * Detecta faces em um frame e retorna um mapa de retângulos (áreas detectadas) e os respectivos Mats (imagens das faces).
        *
        * @param frame O frame de entrada para detecção.
        * @return Um mapa onde a chave é o retângulo da face detectada e o valor é o Mat correspondente àquela face.
        */
        Map<Rect, Mat> detect(org.bytedeco.javacv.Frame frame);
        Frame convertToFrame(MultipartFile file);
}
