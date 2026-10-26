package com.dataremo.Data_Remo.repository;

import com.dataremo.Data_Remo.model.MedicaoTelemetria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TelemetriaRepository extends JpaRepository<MedicaoTelemetria, String> {
    List<MedicaoTelemetria> findTop50ByTreinoIdOrderByRecebidoEmDescEventoIdAsc(Long treinoId);
}
