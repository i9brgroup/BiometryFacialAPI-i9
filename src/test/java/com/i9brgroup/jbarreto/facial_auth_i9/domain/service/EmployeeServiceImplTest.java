package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.ObjetoS3;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.employee.Employee;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.face.YuNetFaceDetectorService;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.FaceDetectorService;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.IAuthenticationFacade;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.ObjetoS3Service;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.aws.S3Service;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.PythonServiceErrorException;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.UploadFileS3Exception;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.employee.EmployeeRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.EmployeePayloadPythonRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.EmployeeSearchResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ProcessPayloadResponse;
import jakarta.persistence.EntityNotFoundException;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private IAuthenticationFacade authenticationFacade;

    @Mock
    private YuNetFaceDetectorService faceDetectorService;

    @Mock
    private S3Service s3Service;

    @Mock
    private Employee employee;

    @Mock
    private ObjetoS3Service objetoS3Service;

    @InjectMocks
    @Spy
    private EmployeeServiceImpl employeeService;

    @Test
    @DisplayName("Deve buscar corretamente um funcionário por ID")
    void deveBuscarCorretamenteEmployeePorId(){

        // ARRANGE
        String idBuscado = "1";
        String siteId = "SITE01";
        String s3key = "photo.jpg";
        String s3Url = "https://example.com/photo.jpg";

        UserLoginEntity usuario = new UserLoginEntity("junior.dev", "SITE01");

        given(authenticationFacade.getAuthentication()).willReturn(usuario);

        given(employeeRepository.findEmployeeById(idBuscado, siteId)).willReturn(employee);
        given(employee.getId()).willReturn("UUID-EXTERNO");
        given(objetoS3Service.findById("UUID-EXTERNO")).willReturn(s3key);
        given(s3Service.getPreSignedUrl(s3key)).willReturn(s3Url);

        // ACT
        EmployeeSearchResponse response = employeeService.buscarPorId(idBuscado);

        // ASSERT
        assertNotNull(response);
        assertEquals(employee.getId(), response.id());
        assertEquals(employee.getSiteId(), response.siteId());
        assertEquals(s3Url, response.urlPhoto());

        then(s3Service).should().getPreSignedUrl(s3key);
        then(employeeRepository).should().findEmployeeById(idBuscado, siteId);
    }

    @Test
    @DisplayName("Deve retornar exception quando funcionário não for encontrado")
    void deveRetornarExceptionEmEmployeeNull(){

        // ARRANGE
        String idBuscado = "1";
        String siteId = "SITE01";

        UserLoginEntity usuario = new UserLoginEntity("junior.dev", "SITE01");

        given(authenticationFacade.getAuthentication()).willReturn(usuario);

        given(employeeRepository.findEmployeeById(idBuscado, siteId)).willReturn(null);

        // ASSERT - ACT
        assertThrows(EntityNotFoundException.class, () -> employeeService.buscarPorId(idBuscado));

        then(employeeRepository).should().findEmployeeById(idBuscado, siteId);
    }

    @Test
    @DisplayName("Deve retornar apenas log de warning quando chave S3 for null e retornar funcionário sem URL pré-assinada")
    void deveRetornarApenasLogParaChaveS3Null(){

        // ARRANGE
        String idBuscado = "1";
        String siteId = "SITE01";
        String uuidInterno = "UUID-EXTERNO";

        UserLoginEntity usuario = new UserLoginEntity("junior.dev", "SITE01");

        given(authenticationFacade.getAuthentication()).willReturn(usuario);

        given(employee.getId()).willReturn(uuidInterno);
        given(employee.getSiteId()).willReturn(siteId);

        given(employeeRepository.findEmployeeById(idBuscado, siteId)).willReturn(employee);
        given(objetoS3Service.findById(uuidInterno)).willReturn(null);

        // ACT
        EmployeeSearchResponse response = employeeService.buscarPorId(idBuscado);

        // ASSERT
        assertNotNull(response);
        assertNull(response.urlPhoto());
        assertEquals(uuidInterno, response.id());

        then(s3Service).shouldHaveNoInteractions();
    }


    @Test
    @DisplayName("Deve processar o payload corretamente e retornar a resposta esperada")
    void deveLancarExceptionArquivoVazio(){

        // ARRANGE
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(true);
        EmployeePayloadPythonRequest payload = new EmployeePayloadPythonRequest("1", "John Doe", "teste@hotmail.com", "SITE01", "LOCAL01", "photo.jpg");


        // ACT - ASSERT
        assertThrows(RuntimeException.class, () -> employeeService.processPayload(payload, file));
    }

    @Test
    void deveLancarExceptionSeEmployeeForNullAoProcessarPayload() {
        // ARRANGE
        String idBuscado = "1";
        String siteId = "SITE01";
        EmployeePayloadPythonRequest payload = new EmployeePayloadPythonRequest(idBuscado, "John Doe", "teste@hotmail.com", "SITE01", "LOCAL01", "photo.jpg");

        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        UserLoginEntity usuario = new UserLoginEntity("junior.dev", "SITE01");

        given(authenticationFacade.getAuthentication()).willReturn(usuario);

        given(employeeRepository.findEmployeeById(idBuscado, siteId)).willReturn(null);

        // ACT - ASSERT
        assertThrows(EntityNotFoundException.class, () -> employeeService.processPayload(payload, file));
    }

    @Test
    @DisplayName("Deve processar o payload com sucesso e retornar done")
    void deveProcessarPayloadComSucesso() {
        // ARRANGE
        String idBuscado = "1";
        String siteId = "SITE01";
        String localId = "LOCAL01";
        String nome = "John Doe";
        String email = "teste@hotmail.com";
        String s3Key = "SITE01_LOCAL01_john_doe.jpg";
        String presignedURL = "http://s3.url/photo.jpg";
        Map<Rect, Mat> detectedFaces = Map.of(new Rect(0, 0, 100, 100), new Mat());

        EmployeePayloadPythonRequest payload = new EmployeePayloadPythonRequest(idBuscado, nome, email, siteId, localId, "photo.jpg");
        MultipartFile file = mock(MultipartFile.class);
        UserLoginEntity usuario = new UserLoginEntity("junior.dev", siteId);
        ObjetoS3 objetoS3 = new ObjetoS3("UUID-EXTERNO", s3Key);

        given(file.isEmpty()).willReturn(false);
        given(file.getOriginalFilename()).willReturn("photo.jpg");
        given(authenticationFacade.getAuthentication()).willReturn(usuario);
        given(employeeRepository.findEmployeeById(idBuscado, siteId)).willReturn(employee);
        given(employee.getId()).willReturn("UUID-EXTERNO");
        given(faceDetectorService.detect(any())).willReturn(detectedFaces);
        
        given(s3Service.uploadFile(file, s3Key)).willReturn(true);
        given(objetoS3Service.save(any(ObjetoS3.class))).willReturn(objetoS3);
        given(s3Service.generatedPreSignedUrlForPhotosEmployees(s3Key)).willReturn(presignedURL);
        
        ProcessPayloadResponse pythonResponse = new ProcessPayloadResponse("done");
        doReturn(pythonResponse).when(employeeService).sendPayloadToPythonService(any(EmployeePayloadPythonRequest.class), anyString());

        // ACT
        ProcessPayloadResponse response = employeeService.processPayload(payload, file);

        // ASSERT
        assertNotNull(response);
        assertEquals("done", response.status());
        then(s3Service).should().uploadFile(file, s3Key);
        then(objetoS3Service).should().save(any(ObjetoS3.class));
    }

    @Test
    @DisplayName("Deve lançar exception quando upload para S3 falhar")
    void deveLancarExceptionQuandoUploadS3Falhar() {
        // ARRANGE
        String idBuscado = "1";
        String siteId = "SITE01";
        EmployeePayloadPythonRequest payload = new EmployeePayloadPythonRequest(idBuscado, "John Doe", "teste@hotmail.com", siteId, "LOCAL01", "photo.jpg");
        MultipartFile file = mock(MultipartFile.class);
        UserLoginEntity usuario = new UserLoginEntity("junior.dev", siteId);
        Map<Rect, Mat> detectedFaces = Map.of(new Rect(0, 0, 100, 100), new Mat());


        given(file.isEmpty()).willReturn(false);
        given(faceDetectorService.detect(any())).willReturn(detectedFaces);
        given(file.getOriginalFilename()).willReturn("photo.jpg");
        given(authenticationFacade.getAuthentication()).willReturn(usuario);
        given(employeeRepository.findEmployeeById(idBuscado, siteId)).willReturn(employee);
        
        given(s3Service.uploadFile(any(), anyString())).willReturn(false);

        // ACT - ASSERT
        PythonServiceErrorException exception = assertThrows(PythonServiceErrorException.class, () -> employeeService.processPayload(payload, file));
        assertTrue(exception.getMessage().contains("Erro no processamento do funcionário: Falha ao enviar o payload para o serviço Python."));
    }

    @Test
    @DisplayName("Deve executar rollback do S3 quando ocorrer erro genérico no processamento")
    void deveExecutarRollbackS3QuandoOcorrerErroGenerico() {
        // ARRANGE
        String idBuscado = "1";
        String siteId = "SITE01";
        EmployeePayloadPythonRequest payload = new EmployeePayloadPythonRequest(idBuscado, "John Doe", "teste@hotmail.com", siteId, "LOCAL01", "photo.jpg");
        MultipartFile file = mock(MultipartFile.class);
        UserLoginEntity usuario = new UserLoginEntity("junior.dev", siteId);
        Map<Rect, Mat> detectedFaces = Map.of(new Rect(0, 0, 100, 100), new Mat());


        given(file.isEmpty()).willReturn(false);
        given(faceDetectorService.detect(any())).willReturn(detectedFaces);
        given(file.getOriginalFilename()).willReturn("photo.jpg");
        given(authenticationFacade.getAuthentication()).willReturn(usuario);
        given(employeeRepository.findEmployeeById(idBuscado, siteId)).willReturn(employee);
        
        // Simular erro no upload para disparar o catch
        given(s3Service.uploadFile(any(), anyString())).willReturn(true);
        given(objetoS3Service.save(any())).willThrow(new PythonServiceErrorException("Erro de Banco"));

        // ACT - ASSERT
        assertThrows(PythonServiceErrorException.class, () -> employeeService.processPayload(payload, file));
        then(s3Service).should().executaRollback(anyString());
    }

    @Test
    @DisplayName("Deve lançar exception de falha no payload quando o upload para o S3 retornar false")
    void deveLancarExceptionQuandoUploadS3RetornarFalse() {
        // ARRANGE
        String idBuscado = "1";
        String siteId = "SITE01";
        EmployeePayloadPythonRequest payload = new EmployeePayloadPythonRequest(
                idBuscado, "John Doe", "teste@hotmail.com", siteId, "LOCAL01", null
        );
        Map<Rect, Mat> detectedFaces = Map.of(new Rect(0, 0, 100, 100), new Mat());


        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        given(file.getOriginalFilename()).willReturn("foto.jpg");

        UserLoginEntity usuario = new UserLoginEntity("junior.dev", siteId);
        given(authenticationFacade.getAuthentication()).willReturn(usuario);
        given(employeeRepository.findEmployeeById(idBuscado, siteId)).willReturn(employee);
        given(faceDetectorService.detect(any())).willReturn(detectedFaces);

        // O PONTO CHAVE: O upload não quebra, mas retorna false
        given(s3Service.uploadFile(eq(file), anyString())).willReturn(false);


        // ACT & ASSERT
        PythonServiceErrorException exception = assertThrows(PythonServiceErrorException.class, () ->
                employeeService.processPayload(payload, file)
        );

        // Validamos que a mensagem é a de falha no envio do payload/serviço Python
        assertTrue(exception.getMessage().contains("Erro no processamento do funcionário:"));

        // Verificamos que o banco de dados e o serviço Python NUNCA foram chamados
        then(objetoS3Service).shouldHaveNoInteractions();
        then(s3Service).should(never()).generatedPreSignedUrlForPhotosEmployees(anyString());
    }

    @Test
    @DisplayName("Deve lançar exception quando o serviço Python retornar status diferente de done")
    void deveLancarExceptionQuandoPythonServiceNaoRetornarDone() {
        // ARRANGE
        String idBuscado = "1";
        String siteId = "SITE01";
        String s3Key = "SITE01_LOCAL01_john_doe.jpg";
        EmployeePayloadPythonRequest payload = new EmployeePayloadPythonRequest(idBuscado, "John Doe", "teste@hotmail.com", siteId, "LOCAL01", "photo.jpg");
        MultipartFile file = mock(MultipartFile.class);
        UserLoginEntity usuario = new UserLoginEntity("junior.dev", siteId);
        ObjetoS3 objetoS3 = new ObjetoS3("UUID-EXTERNO", s3Key);
        Map<Rect, Mat> detectedFaces = Map.of(new Rect(0, 0, 100, 100), new Mat());


        given(file.isEmpty()).willReturn(false);
        given(file.getOriginalFilename()).willReturn("photo.jpg");
        given(authenticationFacade.getAuthentication()).willReturn(usuario);
        given(employeeRepository.findEmployeeById(idBuscado, siteId)).willReturn(employee);
        given(employee.getId()).willReturn("UUID-EXTERNO");
        given(faceDetectorService.detect(any())).willReturn(detectedFaces);
        
        given(s3Service.uploadFile(file, s3Key)).willReturn(true);
        given(objetoS3Service.save(any(ObjetoS3.class))).willReturn(objetoS3);
        
        ProcessPayloadResponse pythonResponse = new ProcessPayloadResponse("fail");
        doReturn(pythonResponse).when(employeeService).sendPayloadToPythonService(any(EmployeePayloadPythonRequest.class), anyString());

        // ACT - ASSERT
        PythonServiceErrorException exception = assertThrows(PythonServiceErrorException.class, () -> employeeService.processPayload(payload, file));
        assertTrue(exception.getMessage().contains("Erro no processamento do funcionário: "));
    }
}