package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.repository.DinosaurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DinosaurServiceTest {

    @Mock
    private DinosaurRepository dinosaurRepository;

    @InjectMocks
    private DinosaurService dinosaurService;

    private Dinosaur dinosaur;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dinosaur = Dinosaur.builder()
                .id("1")
                .name("T-Rex")
                .damage(100)
                .build();
    }

    @Test
    void testCreateDinosaur() {
        when(dinosaurRepository.save(any(Dinosaur.class))).thenReturn(dinosaur);
        Dinosaur createdDinosaur = dinosaurService.createDinosaur(dinosaur);
        assertNotNull(createdDinosaur);
        assertEquals("T-Rex", createdDinosaur.getName());
        verify(dinosaurRepository, times(1)).save(dinosaur);
    }

    @Test
    void testGetAllDinosaurs() {
        when(dinosaurRepository.findAll()).thenReturn(Collections.singletonList(dinosaur));
        List<Dinosaur> dinosaurs = dinosaurService.getAllDinosaurs();
        assertFalse(dinosaurs.isEmpty());
        assertEquals(1, dinosaurs.size());
        verify(dinosaurRepository, times(1)).findAll();
    }

    @Test
    void testGetDinosaurById() {
        when(dinosaurRepository.findById("1")).thenReturn(Optional.of(dinosaur));
        Optional<Dinosaur> foundDinosaur = dinosaurService.getDinosaurById("1");
        assertTrue(foundDinosaur.isPresent());
        assertEquals("1", foundDinosaur.get().getId());
        verify(dinosaurRepository, times(1)).findById("1");
    }

    @Test
    void testGetDinosaurById_NotFound() {
        when(dinosaurRepository.findById("2")).thenReturn(Optional.empty());
        Optional<Dinosaur> foundDinosaur = dinosaurService.getDinosaurById("2");
        assertFalse(foundDinosaur.isPresent());
        verify(dinosaurRepository, times(1)).findById("2");
    }

    @Test
    void testUpdateDinosaur() {
        Dinosaur updatedDetails = Dinosaur.builder().name("Raptor").damage(80).build();
        when(dinosaurRepository.findById("1")).thenReturn(Optional.of(dinosaur));
        when(dinosaurRepository.save(any(Dinosaur.class))).thenReturn(dinosaur);

        Optional<Dinosaur> updatedDinosaur = dinosaurService.updateDinosaur("1", updatedDetails);

        assertTrue(updatedDinosaur.isPresent());
        verify(dinosaurRepository, times(1)).findById("1");
        verify(dinosaurRepository, times(1)).save(dinosaur);
        assertEquals("Raptor", dinosaur.getName());
        assertEquals(80, dinosaur.getDamage());
    }

    @Test
    void testUpdateDinosaur_NotFound() {
        Dinosaur updatedDetails = Dinosaur.builder().build();
        when(dinosaurRepository.findById("2")).thenReturn(Optional.empty());
        Optional<Dinosaur> updatedDinosaur = dinosaurService.updateDinosaur("2", updatedDetails);
        assertFalse(updatedDinosaur.isPresent());
        verify(dinosaurRepository, times(1)).findById("2");
        verify(dinosaurRepository, never()).save(any(Dinosaur.class));
    }

    @Test
    void testDeleteDinosaur() {
        when(dinosaurRepository.existsById("1")).thenReturn(true);
        doNothing().when(dinosaurRepository).deleteById("1");
        boolean deleted = dinosaurService.deleteDinosaur("1");
        assertTrue(deleted);
        verify(dinosaurRepository, times(1)).existsById("1");
        verify(dinosaurRepository, times(1)).deleteById("1");
    }

    @Test
    void testDeleteDinosaur_NotFound() {
        when(dinosaurRepository.existsById("2")).thenReturn(false);
        boolean deleted = dinosaurService.deleteDinosaur("2");
        assertFalse(deleted);
        verify(dinosaurRepository, times(1)).existsById("2");
        verify(dinosaurRepository, never()).deleteById(anyString());
    }
}

