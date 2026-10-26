package com.dataremo.Data_Remo.repository;

import com.dataremo.Data_Remo.model.Treino;
import com.dataremo.Data_Remo.model.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreinoRepository extends JpaRepository<Treino, Long> {

    List<Treino> findByAtleta(Atleta atleta);

    List<Treino> findByAtletaIdOrderByDataDescIdDesc(Long atletaId);
}