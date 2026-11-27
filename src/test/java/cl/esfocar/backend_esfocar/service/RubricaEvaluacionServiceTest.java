package cl.esfocar.backend_esfocar.service;

import cl.esfocar.backend_esfocar.model.EvaluacionAlumno;
import cl.esfocar.backend_esfocar.model.PlantillaRubrica;
import cl.esfocar.backend_esfocar.repository.EvaluacionAlumnoRepository;
import cl.esfocar.backend_esfocar.repository.PlantillaRubricaRepository;
import cl.esfocar.backend_esfocar.util.RutUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RubricaEvaluacionServiceTest {

    @Mock
    private PlantillaRubricaRepository plantillaRepo;

    @Mock
    private EvaluacionAlumnoRepository evaluacionRepo;

    @InjectMocks
    private RubricaEvaluacionService service;

    private PlantillaRubrica plantillaMock;

    @BeforeEach
    void setUp() {
        plantillaMock = new PlantillaRubrica();
        plantillaMock.setId(10L);
        plantillaMock.setTitulo("Evaluación de Tiro Policial");
        plantillaMock.setTipo("Individual");
        plantillaMock.setPuntajeMaximo(20.0);
    }

    @Test
    @DisplayName("Normalización de RUT: Limpia puntos, espacios y convierte k a K")
    void testRutUtilsNormalizar() {
        assertEquals("12345678-K", RutUtils.normalizarRut(" 12.345.678-k "));
        assertEquals("12345678-9", RutUtils.normalizarRut("12.345.678-9"));
        assertEquals("12345678-K", RutUtils.normalizarRut("12345678k"));
        assertEquals("9876543-K", RutUtils.normalizarRut("9.876.543-k"));
        assertEquals("9876543-2", RutUtils.normalizarRut("98765432"));
    }

    @Test
    @DisplayName("evaluarAlumno: resuelve plantilla por defecto cuando id es nulo y BD está vacía")
    void testEvaluarAlumnoSinPlantillaCreaPorDefecto() {
        when(plantillaRepo.findAll()).thenReturn(Collections.emptyList());
        when(plantillaRepo.save(any(PlantillaRubrica.class))).thenAnswer(invocation -> {
            PlantillaRubrica p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(evaluacionRepo.save(any(EvaluacionAlumno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvaluacionAlumno req = new EvaluacionAlumno();
        req.setRutAlumno("12.345.678-k");
        req.setNombreAlumno("Juan");
        req.setApellidoAlumno("Perez");
        req.setPuntaje1(5.0);
        req.setPuntaje2(5.0);
        req.setPuntaje3(5.0);
        req.setPuntaje4(5.0); // 20 / 20 = Nota 7.0

        EvaluacionAlumno guardada = service.evaluarAlumno(req);

        assertNotNull(guardada.getPlantillaRubricaId());
        assertEquals("12345678-K", guardada.getRutAlumno());
        assertEquals(7.00, guardada.getNotaFinal());
        assertEquals("APROBADO", guardada.getEstado());
        assertNotNull(guardada.getFeedbackDocente());
        verify(plantillaRepo, times(1)).save(any(PlantillaRubrica.class));
        verify(evaluacionRepo, times(1)).save(any(EvaluacionAlumno.class));
    }

    @Test
    @DisplayName("evaluarAlumno: acepta modelo Rubrica de Android con solo 'nota' y deduce puntajes")
    void testEvaluarAlumnoConSoloNotaDesdeAndroid() {
        when(plantillaRepo.findAll()).thenReturn(List.of(plantillaMock));
        when(evaluacionRepo.save(any(EvaluacionAlumno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvaluacionAlumno req = new EvaluacionAlumno();
        req.setRutAlumno("19.876.543-2");
        req.setTitulo("Evaluación de Tiro Policial");
        req.setNota(5.5); // Enviado como "nota" desde Android
        req.setFeedback("Buen tiro");

        EvaluacionAlumno guardada = service.evaluarAlumno(req);

        assertEquals("19876543-2", guardada.getRutAlumno());
        assertEquals(10L, guardada.getPlantillaRubricaId());
        assertEquals(5.50, guardada.getNotaFinal());
        assertEquals(5.50, guardada.getNota());
        assertEquals("APROBADO", guardada.getEstado());
        assertEquals("Buen tiro", guardada.getFeedbackDocente());
        assertNotNull(guardada.getPuntaje1());
        assertTrue(guardada.getPuntaje1() > 0.0);
    }

    @Test
    @DisplayName("listarPorRutAlumno: normaliza RUT de búsqueda y consulta fallback si es necesario")
    void testListarPorRutAlumnoNormalizacionYFallback() {
        when(evaluacionRepo.findByRutAlumno("12345678-K")).thenReturn(Collections.emptyList());
        when(evaluacionRepo.findByRutAlumnoIgnoreCase("12345678-K")).thenReturn(Collections.emptyList());

        EvaluacionAlumno legacy = new EvaluacionAlumno();
        legacy.setId(99L);
        legacy.setRutAlumno("12.345.678-k");
        legacy.setNotaFinal(6.0);

        when(evaluacionRepo.buscarPorRutFlexible("12345678-K", "12345678K", "12.345.678-k"))
                .thenReturn(List.of(legacy));

        List<EvaluacionAlumno> res = service.listarPorRutAlumno("12.345.678-k");

        assertEquals(1, res.size());
        assertEquals(6.0, res.get(0).getNotaFinal());
    }
}
