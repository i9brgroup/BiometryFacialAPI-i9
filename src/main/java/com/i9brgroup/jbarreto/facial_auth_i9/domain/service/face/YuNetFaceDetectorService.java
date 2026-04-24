package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.face;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.FaceDetectorService;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.YuNetException;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.FaceDetectorYN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service("yunet")
public class YuNetFaceDetectorService implements FaceDetectorService {

    private final Logger logger = LoggerFactory.getLogger(YuNetFaceDetectorService.class);

    private FaceDetectorYN faceDetector;
    private final OpenCVFrameConverter.ToMat toMatConverter = new OpenCVFrameConverter.ToMat();
    private final Java2DFrameConverter java2dConverter = new Java2DFrameConverter();

    public YuNetFaceDetectorService() {
        try {
            // Extrai o modelo ONNX para um arquivo real
            URL url = getClass().getResource("/models/face_detection_yunet_2023mar.onnx");
            File modelFile = Loader.cacheResource(url);

            // Inicializa o YuNet
            // Parâmetros: modelo, config, tamanho_entrada, score_threshold, nms_threshold, top_k
            this.faceDetector = FaceDetectorYN.create(
                    modelFile.getAbsolutePath(), // O caminho do .onnx
                    "",                          // Config (vazio para YuNet)
                    new Size(320, 320),          // input_size
                    0.6f,                        // score_threshold
                    0.3f,                        // nms_threshold
                    5000,                        // top_k
                    0,                           // backend_id (0 = default/opencv)
                    0                            // target_id (0 = default/cpu)
            );

            logger.info("IA YuNet carregada com sucesso!");

        } catch (Exception e) {
            logger.error("Erro ao carregar modelo YuNet: {}", e.getMessage());
            throw new YuNetException("Falha no carregamento da IA " + e.getMessage());
        }
    }

    @Override
    public Map<Rect, Mat> detect(Frame frame) {
        if (frame == null || frame.image == null) return new HashMap<>();

        synchronized (this) {
            Mat matImg = toMatConverter.convert(frame);

            if (matImg == null || matImg.address() == 0 || matImg.empty()) {
                return new HashMap<>();
            }

            // Garante que a imagem tenha 3 canais (RGB) para o YuNet
            if (matImg.channels() == 4) {
                Mat rgbMat = new Mat();
                opencv_imgproc.cvtColor(matImg, rgbMat, opencv_imgproc.COLOR_RGBA2RGB);
                matImg = rgbMat;
            }

            try {
                // Define o tamanho da entrada baseado na imagem atual
                faceDetector.setInputSize(new Size(matImg.cols(), matImg.rows()));

                Mat faces = new Mat();
                faceDetector.detect(matImg, faces);

                Map<Rect, Mat> detectedFaces = new HashMap<>();
                for (int i = 0; i < faces.rows(); i++) {
                    // Pegando as coordenadas do rosto
                    float x = faces.ptr(i, 0).getFloat();
                    float y = faces.ptr(i, 1).getFloat();
                    float w = faces.ptr(i, 2).getFloat();
                    float h = faces.ptr(i, 3).getFloat();

                    Rect rect = new Rect((int) x, (int) y, (int) w, (int) h);

                    // .clone() é OBRIGATÓRIO aqui para criar uma cópia da memória
                    // que não depende do matImg original
                    detectedFaces.put(rect, new Mat(matImg, rect).clone());
                }

                return detectedFaces;
            } catch (Exception e) {
                logger.error("Erro OpenCV durante detecção YuNet: {}", e.getMessage());
                throw new YuNetException("Erro ao processar imagem para detecção facial: formato ou canais inválidos.");
            }
        }
    }

    @Override
    public Frame convertToFrame(MultipartFile file) {
        try (var inputStream = file.getInputStream()) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            return java2dConverter.convert(bufferedImage);
        } catch (IOException e) {
            throw new YuNetException("Erro ao converter MultipartFile para Frame: " + e.getMessage());
        }
    }
}