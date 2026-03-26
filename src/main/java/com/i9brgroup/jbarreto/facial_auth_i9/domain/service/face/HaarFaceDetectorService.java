package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.face;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.FaceDetectorService;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.HaarCascadeException;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.NoFacesDetectedOnImageException;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.equalizeHist;

@Service("haar")
public class HaarFaceDetectorService implements FaceDetectorService {
    private final Logger logger = LoggerFactory.getLogger(HaarFaceDetectorService.class);

    private CascadeClassifier faceCascade;
    // Mantenha os conversores como atributos para evitar que o GC limpe a memória nativa prematuramente
    private final OpenCVFrameConverter.ToMat toMatConverter = new OpenCVFrameConverter.ToMat();
    private final Java2DFrameConverter java2dConverter = new Java2DFrameConverter();

    public HaarFaceDetectorService() {
        try {
            // Use o Loader para garantir um caminho real no Windows
            URL url = getClass().getResource("/haarcascades/haarcascade_frontalface_alt2.xml");
            File haarCascadeFile = org.bytedeco.javacpp.Loader.cacheResource(url);

            faceCascade = new CascadeClassifier(haarCascadeFile.getAbsolutePath());
            if (faceCascade.empty()) throw new HaarCascadeException("Cascade vazio!");

        } catch (Exception e) {
            throw new HaarCascadeException("Erro ao carregar XML: " + e.getMessage());
        }
    }

    @Override
    public Map<Rect, Mat> detect(Frame frame) {
        if (frame == null || frame.image == null) return Collections.emptyMap();

        // Sincronize para evitar que múltiplas requisições batam no mesmo Mat nativo
        synchronized (this) {

                Mat matImg = toMatConverter.convert(frame);

                if (matImg == null || matImg.empty()) return Collections.emptyMap();

                Mat grayImg = new Mat();
                opencv_imgproc.cvtColor(matImg, grayImg, opencv_imgproc.COLOR_BGR2GRAY);
                opencv_imgproc.equalizeHist(grayImg, grayImg);

                RectVector detectObjects = new RectVector();

            try {
                faceCascade.detectMultiScale(
                        matImg,
                        detectObjects,
                        1.1,
                        3,
                        0,
                        new Size(30, 30),
                        new Size()
                );

                Map<Rect, Mat> detectedFaces = new HashMap<>();
                for (int i = 0; i < detectObjects.size(); i++) {
                    Rect rect = detectObjects.get(i);
                    // IMPORTANTE: .clone() cria uma nova memória que o Java gerencia com segurança
                    Mat faceMat = new Mat(matImg, rect).clone();
                    detectedFaces.put(rect, faceMat);
                }

               return detectedFaces;

            } finally {
                grayImg.release();
            }
        }
    }

    @Override
    public Frame convertToFrame(MultipartFile file) {
        try (var inputStream = file.getInputStream()) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            if (bufferedImage == null) {
                throw new HaarCascadeException("Arquivo de imagem inválido ou corrompido");
            }
            // Não fechar o conversor aqui para não perder a referência da memória nativa
            return java2dConverter.convert(bufferedImage);
        } catch (IOException e) {
            logger.error("Erro ao converter MultipartFile para Frame: {}", e.getMessage());
            throw new HaarCascadeException("Erro ao processar imagem: " + e.getMessage());
        }
    }
}