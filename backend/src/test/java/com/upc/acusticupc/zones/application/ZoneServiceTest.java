package com.upc.acusticupc.zones.application;

import com.upc.acusticupc.shared.exception.ResourceNotFoundException;
import com.upc.acusticupc.sonometry.domain.repository.MeasurementBatchRepository;
import com.upc.acusticupc.zones.domain.model.Sector;
import com.upc.acusticupc.zones.domain.model.Subsector;
import com.upc.acusticupc.zones.domain.model.Zone;
import com.upc.acusticupc.zones.domain.repository.ZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock ZoneRepository zoneRepository;
    @Mock MeasurementBatchRepository batchRepository;
    @InjectMocks ZoneService service;

    private Zone zone(UUID id) {
        return Zone.builder().id(id).name("Bloque A")
                .sector(Sector.B_TRANQUILIDAD_RUIDO_MODERADO)
                .subsector(Subsector.UNIVERSIDADES_COLEGIOS).active(true).build();
    }

    @Test
    void delete_softPorDefecto_desactivaSinBorrar() {
        UUID id = UUID.randomUUID();
        Zone z = zone(id);
        when(zoneRepository.findById(id)).thenReturn(Optional.of(z));

        service.delete(id, false);

        assertThat(z.isActive()).isFalse();
        verify(zoneRepository).save(z);
        verify(zoneRepository, never()).delete(any());
    }

    @Test
    void delete_hardConBatches_lanzaZoneInUse() {
        UUID id = UUID.randomUUID();
        when(zoneRepository.findById(id)).thenReturn(Optional.of(zone(id)));
        when(batchRepository.existsByZoneId(id)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(id, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ZONE_IN_USE");

        verify(zoneRepository, never()).delete(any());
    }

    @Test
    void delete_hardSinBatches_borra() {
        UUID id = UUID.randomUUID();
        Zone z = zone(id);
        when(zoneRepository.findById(id)).thenReturn(Optional.of(z));
        when(batchRepository.existsByZoneId(id)).thenReturn(false);

        service.delete(id, true);

        verify(zoneRepository).delete(z);
    }

    @Test
    void delete_inexistente_lanzaResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(zoneRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id, false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Zone");
    }
}
